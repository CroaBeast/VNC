package me.croabeast.vnc;

import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bukkit/Paper implementation of the common VNC provider contract.
 */
final class BukkitProvider implements VNCProvider {

    private final Pattern CRAFT_BUKKIT_VERSION = Pattern.compile("(?:^|\\.)(v\\d+_\\d+_R\\d+)$");
    @Nullable
    private final BukkitServerVersionInfo SERVER = resolveServerInfoOrNull();

    @Nullable
    MinecraftVersion serverMinecraftVersion() {
        return SERVER != null ? SERVER.getVersion() : null;
    }

    String craftBukkitPackage() {
        return SERVER != null ? SERVER.getCraftBukkitPackage() : "";
    }

    String bukkitApiVersion() {
        return SERVER != null ? SERVER.getBukkitApiVersion() : "";
    }

    boolean isPaper() {
        return SERVER != null && SERVER.isPaper();
    }

    @Override
    public boolean isAvailable() {
        try {
            return Bukkit.getServer() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    @NotNull
    public VNCProvider.VersionInfo resolve() {
        BukkitServerVersionInfo server = resolveServerInfo();
        return new VersionInfo("Bukkit", server.getServerFork(), server.getVersion());
    }

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

        return version != null ? version : resolveServerInfo().getVersion();
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
        return resolveServerInfo().isAtLeast(version);
    }

    /**
     * Convenience overload for classic minor-line comparisons such as {@code 1.16} or
     * {@code 1.21}.
     *
     * @param minor classic minor number to compare against
     * @return {@code true} when the current server is at least {@code 1.minor}
     */
    public boolean isAtLeast(int minor) {
        return resolveServerInfo().isAtLeast(minor);
    }

    /**
     * Convenience overload for classic patch-level comparisons.
     *
     * @param minor classic minor number
     * @param patch classic patch number
     * @return {@code true} when the current server is at least {@code 1.minor.patch}
     */
    public boolean isAtLeast(int minor, int patch) {
        return resolveServerInfo().isAtLeast(minor, patch);
    }

    /**
     * Checks whether the current server is older than the supplied version.
     *
     * @param version exclusive upper bound to compare against
     * @return {@code true} when the current server is strictly older than the supplied version
     */
    public boolean isBefore(@NotNull String version) {
        return resolveServerInfo().isBefore(version);
    }

    /**
     * Checks whether the current server falls inside an inclusive version range.
     *
     * @param minInclusive lower inclusive bound
     * @param maxInclusive upper inclusive bound
     * @return {@code true} when the current server is between both supplied versions
     */
    public boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
        return resolveServerInfo().isBetween(minInclusive, maxInclusive);
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
        return Versioning.compare(left, right);
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
        return Versioning.compare(left, right);
    }

    @NotNull
    private BukkitServerVersionInfo resolveServerInfo() {
        MinecraftVersion version = resolveServerVersion();
        String classicVersion = Versioning.DEFAULT_SCHEME.toClassic(version);
        String dropVersion = Versioning.DEFAULT_SCHEME.toDrop(version);
        int protocol = version.getProtocol() != null ? version.getProtocol() : -1;
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitApiVersion = resolveBukkitApiVersion(craftBukkitPackage);

        return new BukkitServerVersionInfo(
                version,
                classicVersion,
                dropVersion,
                protocol,
                craftBukkitPackage,
                bukkitApiVersion,
                Bukkit.getName() + " " + classicVersion,
                isPaperRuntime(),
                Versioning.javaVersion(),
                Versioning.toLegacyServerVersion(classicVersion)
        );
    }

    @Nullable
    private BukkitServerVersionInfo resolveServerInfoOrNull() {
        try {
            return isAvailable() ? resolveServerInfo() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @NotNull
    private MinecraftVersion resolveServerVersion() {
        MinecraftVersion parsed = Versioning.parseMinecraftVersion(Bukkit.getBukkitVersion());
        if (parsed != null) return parsed;

        parsed = Versioning.parseMinecraftVersion(Bukkit.getVersion());
        if (parsed != null) return parsed;

        throw new IllegalStateException("Could not resolve the server Minecraft version from Bukkit runtime.");
    }

    @NotNull
    private String resolveBukkitApiVersion(@NotNull String craftBukkitPackage) {
        Matcher matcher = CRAFT_BUKKIT_VERSION.matcher(craftBukkitPackage);
        return matcher.find() ? matcher.group(1) : "";
    }

    private boolean isPaperRuntime() {
        String name = Bukkit.getName().toLowerCase();
        String version = Bukkit.getVersion().toLowerCase();
        return name.contains("paper") || version.contains("paper");
    }

    @Nullable
    private MinecraftVersion resolveProtocolVersion(int protocol) {
        return protocol < 0 ? null : MinecraftVersion.fromProtocol(protocol);
    }

    private int resolvePlayerProtocol(@NotNull Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion"))
            return resolveServerInfo().getProtocol();

        try {
            return Via.getAPI().getPlayerVersion(player.getUniqueId());
        } catch (Throwable ignored) {}

        return resolveServerInfo().getProtocol();
    }
}
