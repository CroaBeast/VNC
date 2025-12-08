package me.croabeast.vnc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
public final class MinecraftVersion {

    private static final Pattern DOT_VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

    final boolean classic;
    final int major, minor, patch;

    @NotNull
    public String getVersion() {
        return major + "." + minor + (patch != 0 ? "." + patch : "");
    }

    @Override
    public String toString() {
        return (classic ? "classic " : "drop ") + getVersion();
    }

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
