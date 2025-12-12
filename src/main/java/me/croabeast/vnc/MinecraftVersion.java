package me.croabeast.vnc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable description of a Minecraft version understood by the converter.
 * <p>
 * Instances know whether they originate from the classic {@code 1.x.y} stream or from the
 * modern {@code year.drop[.hotfix]} naming used by Mojang. This context lets
 * {@link VersionScheme} strategies format and translate versions without heuristics or hidden
 * state. The class performs only structural validation; it does not attempt to decide whether a
 * particular combination is historically real.
 */
@RequiredArgsConstructor
@Getter
public final class MinecraftVersion {

    private static final Pattern DOT_VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

    /**
     * Signals if the version comes from the classic numbering stream (as opposed to the drop
     * scheme). The flag influences how the version is formatted and which alias rules a
     * {@link VersionScheme} can safely apply.
     */
    private final boolean classic;

    /**
     * First numeric component of the version. Classic releases always use {@code 1} while drop
     * releases store the calendar year of the drop (for example, {@code 24} for releases named
     * "24.x").
     */
    private final int major;

    /**
     * Second numeric component. For classic versions this is the familiar minor segment
     * (e.g., the {@code 20} in {@code 1.20.2}); for drop versions it represents the drop index
     * within a year (e.g., the {@code 1} in {@code 24.1}).
     */
    private final int minor;

    /**
     * Optional tertiary component used for patches and hotfixes. Zero indicates that no explicit
     * patch segment should be rendered when turning the version back into text.
     */
    private final int patch;

    /**
     * Returns a plain dotted string assembled directly from the numeric fields with no
     * translation or aliasing applied.
     *
     * @return plain version number
     */
    @NotNull
    public String getVersion() {
        return major + "." + minor + (patch != 0 ? "." + patch : "");
    }

    /**
     * Provides a descriptive string that labels the version as either "classic" or "drop".
     *
     * @return human-readable description
     */
    @Override
    public String toString() {
        return (classic ? "classic " : "drop ") + getVersion();
    }

    /**
     * Parses a textual representation into a {@link MinecraftVersion}. Inputs are restricted to
     * well-formed classic {@code 1.x[.y]} or drop {@code year.drop[.hotfix]} shapes; everything
     * else results in an {@link IllegalArgumentException}. This method never infers missing
     * numbers or rewrites aliases—it simply captures the intent expressed by the caller.
     *
     * @param text text to parse
     * @return parsed immutable version
     * @throws IllegalArgumentException when the input cannot be interpreted as a Minecraft version
     */
    public static MinecraftVersion parse(String text) {
        String s = text.trim();

        Matcher m = DOT_VERSION.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("Unsupported version format: " + text);

        int first = Integer.parseInt(m.group(1));
        int second = Integer.parseInt(m.group(2));
        int third = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;

        if (first == (byte) 1)
            return new MinecraftVersion(true, first, second, third);

        if (first >= 11 && first <= 99)
            return new MinecraftVersion(false, first, second, third);

        throw new IllegalArgumentException("Unsupported version number: " + text);
    }
}
