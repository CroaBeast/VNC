package me.croabeast.vnc;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Development phase a version identifier belongs to.
 *
 * <p>Alpha and Beta reused the same {@code 1.x} numbers that the release line later used, so the
 * phase is what separates {@code b1.2} from {@code 1.2.1}. Declaration order is chronological,
 * which makes the enum ordinal usable as the primary comparison key.</p>
 *
 * <p>The pre-classic, Classic, Indev, and Infdev eras are intentionally not modelled. Their
 * identifiers are date or build based ({@code rd-132211}, {@code c0.0.13a}, {@code inf-20100618})
 * and do not fit the numeric shape this library is built on.</p>
 */
@Getter
public enum VersionPhase {

    /**
     * Alpha versions such as {@code a1.2.6}, released during 2010.
     */
    ALPHA("a", "Alpha"),

    /**
     * Beta versions such as {@code b1.7.3}, released during 2010 and 2011.
     */
    BETA("b", "Beta"),

    /**
     * Full releases such as {@code 1.20.6}, and every year-based drop identifier.
     */
    RELEASE("", "Release");

    /**
     * Single-letter prefix used by the identifier, or an empty string for {@link #RELEASE}.
     */
    private final String prefix;

    /**
     * Human readable name of the phase.
     */
    private final String displayName;

    VersionPhase(String prefix, String displayName) {
        this.prefix = prefix;
        this.displayName = displayName;
    }

    /**
     * Returns whether this phase precedes the full release line.
     *
     * @return {@code true} for {@link #ALPHA} and {@link #BETA}
     */
    public boolean isPreRelease() {
        return this != RELEASE;
    }

    /**
     * Resolves a phase from any of the supported textual forms.
     *
     * <p>Accepts the single-letter prefix ({@code a}, {@code b}) and the full name
     * ({@code alpha}, {@code beta}), case-insensitively.</p>
     *
     * @param name textual phase marker
     * @return matching phase, or {@code null} when the text is not a phase marker
     */
    @Nullable
    public static VersionPhase byName(@Nullable String name) {
        if (name == null) return null;

        String value = name.trim().toLowerCase();

        if (value.equals("a") || value.equals("alpha")) return ALPHA;
        if (value.equals("b") || value.equals("beta")) return BETA;

        return null;
    }

    /**
     * Returns the phase declared by a textual marker, defaulting to {@link #RELEASE}.
     *
     * @param name textual phase marker, or {@code null}
     * @return matching phase, or {@link #RELEASE} when none is declared
     */
    @NotNull
    public static VersionPhase byNameOrRelease(@Nullable String name) {
        VersionPhase phase = byName(name);
        return phase != null ? phase : RELEASE;
    }
}
