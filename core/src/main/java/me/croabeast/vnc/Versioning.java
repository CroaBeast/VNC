package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General Java entry point for VNC version parsing, conversion, and comparison helpers.
 *
 * <p>This class intentionally has no Bukkit, proxy, or mod-loader dependency. Runtime adapters
 * live in their own modules and delegate back to these helpers.</p>
 */
@UtilityClass
public class Versioning {

    private static final Pattern VERSION_TOKEN = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)?)");

    /**
     * Default conversion scheme used by platform modules when normalizing runtime versions.
     */
    public static final VersionScheme DEFAULT_SCHEME = VersionScheme.MOJANG;

    /**
     * Parses the first Minecraft-looking version token from arbitrary runtime text.
     *
     * <p>Values such as {@code 1.20.6-R0.1-SNAPSHOT}, {@code git-Paper-123 (MC: 1.21.4)}, and
     * plain values such as {@code 26.1} are supported.</p>
     *
     * @param value runtime version text
     * @return parsed version, or {@code null} when no supported token is present
     */
    @Nullable
    public static MinecraftVersion parseMinecraftVersion(@Nullable String value) {
        if (value == null) return null;

        String token = value.trim();

        int hyphen = token.indexOf('-');
        if (hyphen >= 0)
            token = token.substring(0, hyphen);

        try {
            return MinecraftVersion.parse(token);
        } catch (IllegalArgumentException ignored) {}

        Matcher matcher = VERSION_TOKEN.matcher(value);
        if (!matcher.find()) return null;

        try {
            return MinecraftVersion.parse(matcher.group(1));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Parses a runtime version token and fails with a clear error if it cannot be resolved.
     *
     * @param value runtime version text
     * @return parsed Minecraft version
     */
    @NotNull
    public static MinecraftVersion requireMinecraftVersion(@Nullable String value) {
        MinecraftVersion version = parseMinecraftVersion(value);
        if (version != null) return version;

        throw new IllegalArgumentException("Could not resolve a Minecraft version from: " + value);
    }

    /**
     * Compares a parsed version against a textual version identifier.
     *
     * @param left parsed left-hand version
     * @param right textual right-hand version
     * @return negative when {@code left < right}, zero when equal, positive when {@code left > right}
     */
    public static int compare(@NotNull MinecraftVersion left, @NotNull String right) {
        return compare(left, parseComparableVersion(right));
    }

    /**
     * Compares two parsed versions after normalizing both to classic numbering.
     *
     * @param left left-hand version
     * @param right right-hand version
     * @return negative when {@code left < right}, zero when equal, positive when {@code left > right}
     */
    public static int compare(@NotNull MinecraftVersion left, @NotNull MinecraftVersion right) {
        MinecraftVersion leftClassic = comparableClassic(Objects.requireNonNull(left, "left"));
        MinecraftVersion rightClassic = comparableClassic(Objects.requireNonNull(right, "right"));

        int result = Integer.compare(leftClassic.getMajor(), rightClassic.getMajor());
        if (result != 0) return result;

        result = Integer.compare(leftClassic.getMinor(), rightClassic.getMinor());
        if (result != 0) return result;

        return Integer.compare(leftClassic.getPatch(), rightClassic.getPatch());
    }

    public static boolean isAtLeast(@NotNull MinecraftVersion version, @NotNull String minimum) {
        return compare(version, minimum) >= 0;
    }

    public static boolean isBefore(@NotNull MinecraftVersion version, @NotNull String maximum) {
        return compare(version, maximum) < 0;
    }

    public static boolean isBetween(
            @NotNull MinecraftVersion version,
            @NotNull String minInclusive,
            @NotNull String maxInclusive
    ) {
        return compare(version, minInclusive) >= 0 && compare(version, maxInclusive) <= 0;
    }

    /**
     * Parses the current JVM major version.
     *
     * @return Java major version, for example {@code 8}, {@code 17}, or {@code 21}
     */
    public static int javaVersion() {
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

    /**
     * Converts a classic {@code 1.x.y} version to the legacy double shape used by old plugins.
     *
     * @param classicVersion classic version text
     * @return legacy double representation
     */
    public static double toLegacyServerVersion(@NotNull String classicVersion) {
        if (!classicVersion.startsWith("1."))
            throw new IllegalArgumentException("Classic version must start with 1.: " + classicVersion);

        return Double.parseDouble(classicVersion.substring(2));
    }

    @NotNull
    private static MinecraftVersion parseComparableVersion(@NotNull String version) {
        return comparableClassic(MinecraftVersion.parse(Objects.requireNonNull(version, "version").trim()));
    }

    @NotNull
    private static MinecraftVersion comparableClassic(@NotNull MinecraftVersion version) {
        return version.isClassic() ? version : MinecraftVersion.parse(DEFAULT_SCHEME.toClassic(version));
    }
}
