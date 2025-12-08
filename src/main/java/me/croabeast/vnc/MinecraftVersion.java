package me.croabeast.vnc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable holder for a Minecraft version number. The class knows whether it is telling the
 * story of the classic 1.x.y era or the year.drop chronicles, so conversion strategies can
 * embellish that lore accordingly.
 */
@RequiredArgsConstructor
@Getter
public final class MinecraftVersion {

    private static final Pattern DOT_VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

    /**
     * Chronicles whether this version belongs to the classic lineage, a flag the getters use
     * to retell the era-appropriate story in their outputs.
     */
    private final boolean classic;

    /**
     * Anchors the primary numeric milestone, starring as the hero in both the raw and descriptive
     * getters.
     */
    private final int major;

    /**
     * Marks the secondary beat of the version narrative, ensuring getters have the rhythm they
     * need to compose the version string.
     */
    private final int minor;

    /**
     * Optional tertiary segment representing patches or hotfix counts, ready for the getter to
     * showcase when the plot requires extra detail.
     */
    private final int patch;

    /**
     * Returns the raw semantic version string without any lore prefixes.
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
     * Parses a textual representation into a {@link MinecraftVersion}. The method deliberately
     * keeps a narrow scope, accepting only classic 1.x.y or year.drop formats, and throws an
     * exception when it encounters time-travelers from unsupported universes.
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
