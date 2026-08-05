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

    // The lookarounds keep the fallback from truncating a longer number: "1.7.7.0" must not
    // resolve to the real release 1.7.7.
    private static final Pattern VERSION_TOKEN =
            Pattern.compile("(?<![\\d.])((?:[ab])?\\d+\\.\\d+(?:\\.\\d+)?(?:_\\d+)?[a-z]?)(?![\\d.])");

    /**
     * Default conversion scheme used by platform modules when normalizing runtime versions.
     */
    public static final VersionScheme DEFAULT_SCHEME = VersionScheme.MOJANG;

    /**
     * Parses the first Minecraft-looking version token from arbitrary runtime text.
     *
     * <p>Values such as {@code 1.20.6-R0.1-SNAPSHOT}, {@code git-Paper-123 (MC: 1.21.4)},
     * {@code b1.7.3}, and plain values such as {@code 26.1} are supported.</p>
     *
     * <p>The extraction fallback deliberately gives up instead of guessing when the text holds a
     * version-shaped token it cannot fully parse. Values such as {@code 1.7.7.0} return
     * {@code null} rather than the unrelated release {@code 1.7.7}.</p>
     *
     * @param value runtime version text
     * @return parsed version, or {@code null} when no supported token is present
     */
    @Nullable
    public static MinecraftVersion parseMinecraftVersion(@Nullable String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        if (MinecraftVersion.isIdentifier(trimmed))
            return parseOrNull(trimmed);

        int hyphen = trimmed.indexOf('-');
        if (hyphen >= 0) {
            String token = trimmed.substring(0, hyphen);
            if (MinecraftVersion.isIdentifier(token))
                return parseOrNull(token);
        }

        Matcher matcher = VERSION_TOKEN.matcher(trimmed);
        while (matcher.find()) {
            MinecraftVersion version = parseOrNull(matcher.group(1));
            if (version != null) return version;
        }

        return null;
    }

    @Nullable
    private static MinecraftVersion parseOrNull(@NotNull String text) {
        try {
            return MinecraftVersion.parse(text);
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

        // Alpha and Beta reused the release line's numbers, so the phase decides first.
        int result = Integer.compare(leftClassic.getPhase().ordinal(), rightClassic.getPhase().ordinal());
        if (result != 0) return result;

        result = Integer.compare(leftClassic.getMajor(), rightClassic.getMajor());
        if (result != 0) return result;

        result = Integer.compare(leftClassic.getMinor(), rightClassic.getMinor());
        if (result != 0) return result;

        result = Integer.compare(leftClassic.getPatch(), rightClassic.getPatch());
        if (result != 0) return result;

        result = Integer.compare(leftClassic.getBuild(), rightClassic.getBuild());
        if (result != 0) return result;

        return Character.compare(leftClassic.getQualifier(), rightClassic.getQualifier());
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
        // Alpha and Beta have no legacy double shape: it was only ever used for 1.x releases.
        if (!classicVersion.startsWith("1.")) return -1D;

        return Double.parseDouble(classicVersion.substring(2));
    }

    @NotNull
    private static MinecraftVersion parseComparableVersion(@NotNull String version) {
        return comparableClassic(MinecraftVersion.parse(Objects.requireNonNull(version, "version").trim()));
    }

    @NotNull
    private static MinecraftVersion comparableClassic(@NotNull MinecraftVersion version) {
        return version.getFamily() == VersionFamily.CLASSIC
                ? version
                : MinecraftVersion.parse(DEFAULT_SCHEME.toClassic(version));
    }
}
