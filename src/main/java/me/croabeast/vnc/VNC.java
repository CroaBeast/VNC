package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime bridge between Bukkit/Paper and the version model provided by this library.
 *
 * <p>{@code VNC} keeps the public surface intentionally simple: it resolves the current server's
 * runtime metadata once, exposes legacy-compatible constants such as {@link #SERVER_VERSION}, and
 * provides comparison helpers that work with both classic and drop numbering.</p>
 *
 * <p>The class is designed to replace older ad-hoc server version utilities. For new code, prefer
 * {@link #SERVER_MINECRAFT_VERSION} and the string-based comparison helpers over raw
 * {@code double} checks whenever the boundary includes patch-level changes such as
 * {@code 1.20.5}.</p>
 */
@UtilityClass
public class VNC {

    private final Pattern BUKKIT_VERSION_TOKEN = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)?)");
    private final Pattern CRAFT_BUKKIT_VERSION = Pattern.compile("(?:^|\\.)(v\\d+_\\d+_R\\d+)$");
    private final VersionScheme DEFAULT_SCHEME = VersionScheme.MOJANG;

    private final ServerVersionInfo SERVER = resolveServerInfo();

    /**
     * Parsed server version as a structured value object.
     *
     * <p>This is the preferred source of truth for feature checks, protocol-aware behavior,
     * and version family conversions.</p>
     */
    public final MinecraftVersion SERVER_MINECRAFT_VERSION = SERVER.getVersion();
    /**
     * Server version normalized into classic numbering.
     *
     * <p>Examples include values such as {@code 1.20.6}, {@code 1.21.11}, or projected aliases
     * such as {@code 1.22} when the runtime only exposes a drop identifier.</p>
     */
    public final String SERVER_CLASSIC_VERSION = SERVER.getClassicVersion();
    /**
     * Server version normalized into year-based drop numbering.
     *
     * <p>Examples include values such as {@code 24.1}, {@code 25.4}, or {@code 26.1.1}.</p>
     */
    public final String SERVER_DROP_VERSION = SERVER.getDropVersion();
    /**
     * Published protocol of the current server version.
     *
     * <p>When the exact runtime version does not have a known protocol mapping, this field is
     * {@code -1}.</p>
     */
    public final int SERVER_PROTOCOL = SERVER.getProtocol();
    /**
     * Raw CraftBukkit package name detected from the running server implementation.
     */
    public final String CRAFT_BUKKIT_PACKAGE = SERVER.getCraftBukkitPackage();
    /**
     * Legacy CraftBukkit package suffix such as {@code v1_20_R3}.
     *
     * <p>Versionless servers expose an empty string here.</p>
     */
    public final String BUKKIT_API_VERSION = SERVER.getBukkitApiVersion();
    /**
     * Human-readable server fork description built from the current Bukkit runtime.
     */
    public final String SERVER_FORK = SERVER.getServerFork();
    /**
     * Whether the current runtime appears to be Paper or a Paper-derived fork.
     */
    public final boolean PAPER_ENABLED = SERVER.isPaper();
    /**
     * Parsed major Java runtime version, for example {@code 17}, {@code 21}, or {@code 25}.
     */
    public final int JAVA_VERSION = SERVER.getJavaVersion();
    /**
     * Legacy double representation of {@link #SERVER_CLASSIC_VERSION}.
     *
     * <p>This field exists for compatibility with older plugins that compare server versions as
     * {@code 16.0}, {@code 20.5}, and similar values. New code should prefer
     * {@link #SERVER_MINECRAFT_VERSION} or the string-based helper methods in this class.</p>
     */
    public final double SERVER_VERSION = SERVER.getServerVersion();

    /**
     * Resolves the effective Minecraft version for a specific player.
     *
     * <p>When ViaVersion is installed, the player's negotiated protocol is translated back into a
     * canonical {@link MinecraftVersion}. Without ViaVersion, this falls back to the current
     * server version.</p>
     *
     * @param player online player to inspect
     * @return best-known Minecraft version for that player
     */
    @NotNull
    public MinecraftVersion player(@NotNull Player player) {
        Objects.requireNonNull(player, "player");

        int protocol = resolvePlayerProtocol(player);
        MinecraftVersion version = resolveProtocolVersion(protocol);

        return version != null ? version : SERVER_MINECRAFT_VERSION;
    }

    /**
     * Checks whether the current server is at least the supplied version.
     *
     * <p>The comparison accepts both classic identifiers such as {@code 1.21.9} and drop
     * identifiers such as {@code 25.3}. Cross-family comparisons are normalized through
     * {@link VersionScheme#MOJANG} before comparing numeric segments.</p>
     *
     * @param version minimum version to compare against
     * @return {@code true} when the current server is equal to or newer than the supplied version
     */
    public boolean isAtLeast(@NotNull String version) {
        return SERVER.isAtLeast(version);
    }

    /**
     * Convenience overload for classic minor-line comparisons such as {@code 1.16} or
     * {@code 1.21}.
     *
     * @param minor classic minor number to compare against
     * @return {@code true} when the current server is at least {@code 1.minor}
     */
    public boolean isAtLeast(int minor) {
        return SERVER.isAtLeast(minor);
    }

    /**
     * Convenience overload for classic patch-level comparisons.
     *
     * @param minor classic minor number
     * @param patch classic patch number
     * @return {@code true} when the current server is at least {@code 1.minor.patch}
     */
    public boolean isAtLeast(int minor, int patch) {
        return SERVER.isAtLeast(minor, patch);
    }

    /**
     * Checks whether the current server is older than the supplied version.
     *
     * @param version exclusive upper bound to compare against
     * @return {@code true} when the current server is strictly older than the supplied version
     */
    public boolean isBefore(@NotNull String version) {
        return SERVER.isBefore(version);
    }

    /**
     * Checks whether the current server falls inside an inclusive version range.
     *
     * @param minInclusive lower inclusive bound
     * @param maxInclusive upper inclusive bound
     * @return {@code true} when the current server is between both supplied versions
     */
    public boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
        return SERVER.isBetween(minInclusive, maxInclusive);
    }

    /**
     * Compares a parsed version against a textual version identifier.
     *
     * <p>The textual identifier may use classic or drop numbering. Both operands are normalized
     * to classic numbering before the numeric comparison is performed.</p>
     *
     * @param left parsed left-hand version
     * @param right textual right-hand version
     * @return negative when {@code left < right}, zero when equal, positive when {@code left > right}
     */
    public int compare(@NotNull MinecraftVersion left, @NotNull String right) {
        return compare(left, parseComparableVersion(right));
    }

    /**
     * Compares two parsed versions after normalizing both to classic numbering.
     *
     * <p>This is the lowest-level comparison helper used by the string-based overloads and by the
     * runtime bridge methods in this class.</p>
     *
     * @param left left-hand version
     * @param right right-hand version
     * @return negative when {@code left < right}, zero when equal, positive when {@code left > right}
     */
    public int compare(@NotNull MinecraftVersion left, @NotNull MinecraftVersion right) {
        MinecraftVersion leftClassic = comparableClassic(Objects.requireNonNull(left, "left"));
        MinecraftVersion rightClassic = comparableClassic(Objects.requireNonNull(right, "right"));

        int result = Integer.compare(leftClassic.getMajor(), rightClassic.getMajor());
        if (result != 0) return result;

        result = Integer.compare(leftClassic.getMinor(), rightClassic.getMinor());
        if (result != 0) return result;

        return Integer.compare(leftClassic.getPatch(), rightClassic.getPatch());
    }

    @NotNull
    private ServerVersionInfo resolveServerInfo() {
        MinecraftVersion version = resolveServerVersion();
        String classicVersion = DEFAULT_SCHEME.toClassic(version);
        String dropVersion = DEFAULT_SCHEME.toDrop(version);
        int protocol = version.getProtocol() != null ? version.getProtocol() : -1;
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitApiVersion = resolveBukkitApiVersion(craftBukkitPackage);

        return new ServerVersionInfo(
                version,
                classicVersion,
                dropVersion,
                protocol,
                craftBukkitPackage,
                bukkitApiVersion,
                Bukkit.getName() + " " + classicVersion,
                isPaperRuntime(),
                resolveJavaVersion(),
                toLegacyServerVersion(classicVersion)
        );
    }

    @NotNull
    private MinecraftVersion resolveServerVersion() {
        MinecraftVersion parsed = parseServerVersion(Bukkit.getBukkitVersion());
        if (parsed != null) return parsed;

        parsed = parseServerVersion(Bukkit.getVersion());
        if (parsed != null) return parsed;

        throw new IllegalStateException("Could not resolve the server Minecraft version from Bukkit runtime.");
    }

    @Nullable
    private MinecraftVersion parseServerVersion(@Nullable String value) {
        if (value == null) return null;

        String token = value.trim();

        int hyphen = token.indexOf('-');
        if (hyphen >= 0)
            token = token.substring(0, hyphen);

        try {
            return MinecraftVersion.parse(token);
        } catch (IllegalArgumentException ignored) {}

        Matcher matcher = BUKKIT_VERSION_TOKEN.matcher(value);
        if (!matcher.find()) return null;

        try {
            return MinecraftVersion.parse(matcher.group(1));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @NotNull
    private String resolveBukkitApiVersion(@NotNull String craftBukkitPackage) {
        Matcher matcher = CRAFT_BUKKIT_VERSION.matcher(craftBukkitPackage);
        return matcher.find() ? matcher.group(1) : "";
    }

    private boolean isPaperRuntime() {
        return hasClass("io.papermc.paper.adventure.PaperAdventure")
                || hasClass("io.papermc.paper.threadedregions.RegionizedServer")
                || hasClass("com.destroystokyo.paper.ParticleBuilder");
    }

    private boolean hasClass(@NotNull String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private int resolveJavaVersion() {
        String version = System.getProperty("java.version", "");

        if (version.startsWith("1.")) {
            int dot = version.indexOf('.', 2);
            String value = dot == -1 ? version.substring(2) : version.substring(2, dot);
            return Integer.parseInt(value);
        }

        int dot = version.indexOf('.');
        String value = dot == -1 ? version : version.substring(0, dot);
        return Integer.parseInt(value);
    }

    private double toLegacyServerVersion(@NotNull String classicVersion) {
        if (!classicVersion.startsWith("1."))
            throw new IllegalArgumentException("Classic version must start with 1.: " + classicVersion);

        return Double.parseDouble(classicVersion.substring(2));
    }

    @NotNull
    private MinecraftVersion parseComparableVersion(@NotNull String version) {
        return comparableClassic(MinecraftVersion.parse(Objects.requireNonNull(version, "version").trim()));
    }

    @NotNull
    private MinecraftVersion comparableClassic(@NotNull MinecraftVersion version) {
        return version.isClassic() ? version : MinecraftVersion.parse(DEFAULT_SCHEME.toClassic(version));
    }

    @Nullable
    private MinecraftVersion resolveProtocolVersion(int protocol) {
        return protocol < 0 ? null : MinecraftVersion.fromProtocol(protocol);
    }

    private int resolvePlayerProtocol(@NotNull Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion"))
            return SERVER_PROTOCOL;

        try {
            Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
            Object api = viaClass.getMethod("getAPI").invoke(null);
            Method getPlayerVersion = api.getClass().getMethod("getPlayerVersion", UUID.class);
            Object value = getPlayerVersion.invoke(api, player.getUniqueId());

            if (value instanceof Number)
                return ((Number) value).intValue();
        } catch (Throwable ignored) {}

        return SERVER_PROTOCOL;
    }
}
