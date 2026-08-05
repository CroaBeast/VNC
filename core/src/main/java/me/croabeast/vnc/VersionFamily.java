package me.croabeast.vnc;

/**
 * Numbering family a version identifier belongs to.
 *
 * <p>This is orthogonal to {@link VersionPhase}: the family describes how the number is written,
 * while the phase describes where the version sits in Minecraft's development history.</p>
 */
public enum VersionFamily {

    /**
     * Classic Java numbering such as {@code 1.20.6}, and the pre-release lines {@code a1.2.6} and
     * {@code b1.7.3} that share the same {@code 1.x} shape.
     */
    CLASSIC,

    /**
     * Year-based drop numbering such as {@code 25.4} or {@code 26.1.1}.
     */
    DROP
}
