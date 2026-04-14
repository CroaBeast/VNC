package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Strategy interface for converting between classic Java release numbers and Minecraft's
 * year-based drop numbering.
 *
 * <p>The project ships with three entry points:</p>
 * <ul>
 *     <li>{@link #MOJANG}, which keeps the historically grounded mappings and stays conservative
 *     when no official classic alias exists.</li>
 *     <li>{@link #CROA_CUSTOM}, which keeps the same historical mappings but treats
 *     {@code 1.21.11} as a custom {@code 1.22} milestone, continues custom hotfixes on the
 *     {@code 25.4.x} line, and shifts the projected future classic aliases forward by one minor
 *     line.</li>
 *     <li>{@link #mapped(MappingTable)}, which lets callers create their own exact scheme from
 *     user-defined mappings.</li>
 * </ul>
 *
 * <p>Both built-in schemes are exact for the release lines that are explicitly registered in
 * {@link MappingTable}. Those mappings are based on the real Java release groupings, so hotfix
 * numbers stay contiguous even when Mojang skipped a classic patch number such as
 * {@code 1.4.3} or {@code 1.7.3}.</p>
 */
public interface VersionScheme {

    /**
     * Creates a scheme backed only by the supplied explicit mapping table.
     *
     * <p>The returned scheme normalizes values that are already in the requested family and uses
     * the table only for cross-family conversions. If a conversion is not present in the table,
     * the scheme throws an {@link IllegalArgumentException} instead of guessing a fallback.</p>
     *
     * @param mappings mapping table to use for exact drop/classic conversions
     * @return scheme backed by the supplied explicit mappings
     */
    @NotNull
    static VersionScheme mapped(@NotNull MappingTable mappings) {
        Objects.requireNonNull(mappings, "mappings");

        return new VersionScheme() {
            @NotNull
            public String toClassic(@NotNull MinecraftVersion version) {
                if (version.isClassic())
                    return MappingTable.normalizeClassic(version);

                String classic = mappings.findClassic(version);
                if (classic != null)
                    return classic;

                throw new IllegalArgumentException(
                        "No classic mapping is defined for drop version " + MappingTable.normalizeDrop(version) + "."
                );
            }

            @NotNull
            public String toDrop(@NotNull MinecraftVersion version) {
                if (!version.isClassic())
                    return MappingTable.normalizeDrop(version);

                String drop = mappings.findDrop(version);
                if (drop != null)
                    return drop;

                throw new IllegalArgumentException(
                        "No drop mapping is defined for classic version " + MappingTable.normalizeClassic(version) + "."
                );
            }
        };
    }

    /**
     * Canonical scheme that models Mojang's published numbering direction as closely as possible.
     *
     * <p>For historical Java releases, this scheme uses an explicit mapping table grouped by the
     * actual feature update or game drop each version belongs to. That means examples such as
     * {@code 1.20.3 -> 23.2} and {@code 1.21.4 -> 24.4} are treated as separate drops instead of
     * being folded into the same release line.</p>
     *
     * <p>For post-2025 releases, Mojang no longer publishes a classic {@code 1.x.y} alias. To
     * avoid inventing more structure than the official numbering guarantees, this scheme only
     * projects the first drop of a future year back into classic form. Additional future drops
     * without an explicit table entry are rejected rather than guessed.</p>
     */
    VersionScheme MOJANG = new VersionScheme() {
        @NotNull
        public String toClassic(@NotNull MinecraftVersion version) {
            if (version.isClassic())
                return MappingTable.normalizeClassic(version);

            String exact = MappingTable.MOJANG_MAPPINGS.findClassic(version);
            if (exact != null)
                return exact;

            return MappingTable.projectOfficialClassic(version);
        }

        @NotNull
        public String toDrop(@NotNull MinecraftVersion version) {
            if (!version.isClassic())
                return MappingTable.normalizeDrop(version);

            String exact = MappingTable.MOJANG_MAPPINGS.findDrop(version);
            if (exact != null)
                return exact;

            return MappingTable.projectOfficialDrop(version);
        }
    };

    /**
     * Custom scheme that keeps the same exact historical mappings as {@link #MOJANG}, but makes
     * one project-specific reinterpretation.
     *
     * <p>Under this scheme, the release line represented officially by {@code 1.21.11} /
     * {@code 25.4} is treated as a custom {@code 1.22}. Hotfixes on that custom line continue
     * naturally as {@code 1.22.1 -> 25.4.1}, {@code 1.22.2 -> 25.4.2}, and so on. Because the
     * base {@code 1.22} identifier is already consumed by that alias, projected future aliases
     * move forward by one classic minor line: {@code 26.1 -> 1.23},
     * {@code 26.1.1 -> 1.23.1}, {@code 27.1 -> 1.24}, and so on.</p>
     *
     * <p>Outside that reinterpretation, this scheme follows {@link #MOJANG} as closely as
     * possible.</p>
     */
    VersionScheme CROA_CUSTOM = new VersionScheme() {
        @NotNull
        public String toClassic(@NotNull MinecraftVersion version) {
            if (version.isClassic()) {
                String normalizedClassic = MappingTable.normalizeClassic(version);
                String alias = MappingTable.CROA_CUSTOM_ALIASES.get(normalizedClassic);
                return alias != null ? alias : normalizedClassic;
            }

            String exact = MappingTable.CROA_CUSTOM_MAPPINGS.findClassic(version);
            if (exact != null) return exact;

            return MappingTable.projectCustomClassic(version);
        }

        @NotNull
        public String toDrop(@NotNull MinecraftVersion version) {
            if (!version.isClassic())
                return MappingTable.normalizeDrop(version);

            String exact = MappingTable.CROA_CUSTOM_MAPPINGS.findDrop(version);
            if (exact != null)
                return exact;

            return MappingTable.projectCustomDrop(version);
        }
    };

    /**
     * Converts the supplied parsed version into classic numbering.
     *
     * <p>If the input is already classic, implementations return a normalized classic string.
     * Otherwise, they translate the version according to the scheme's own mapping rules.</p>
     *
     * @param version parsed version to convert
     * @return classic representation of the supplied version
     * @throws IllegalArgumentException when the scheme cannot produce a reliable classic mapping
     */
    @NotNull
    String toClassic(@NotNull MinecraftVersion version);

    /**
     * Parses the supplied text and delegates to {@link #toClassic(MinecraftVersion)}.
     *
     * <p>This is the convenient entry point when the caller does not need to keep a
     * {@link MinecraftVersion} instance around.</p>
     *
     * @param version textual version to convert
     * @return classic representation of the supplied version
     * @throws IllegalArgumentException when the text cannot be parsed or the scheme cannot map it
     */
    @NotNull
    default String toClassic(@NotNull String version) {
        return toClassic(MinecraftVersion.parse(version));
    }

    /**
     * Converts the supplied parsed version into drop numbering.
     *
     * <p>If the input is already a drop version, implementations return a normalized drop string.
     * Otherwise, they translate the version according to the scheme's own mapping rules.</p>
     *
     * @param version parsed version to convert
     * @return drop representation of the supplied version
     * @throws IllegalArgumentException when the scheme cannot produce a reliable drop mapping
     */
    @NotNull
    String toDrop(@NotNull MinecraftVersion version);

    /**
     * Parses the supplied text and delegates to {@link #toDrop(MinecraftVersion)}.
     *
     * <p>This overload is useful when callers want the convenience of string input while still
     * benefiting from the same validation and mapping rules as the object-based API.</p>
     *
     * @param version textual version to convert
     * @return drop representation of the supplied version
     * @throws IllegalArgumentException when the text cannot be parsed or the scheme cannot map it
     */
    @NotNull
    default String toDrop(@NotNull String version) {
        return toDrop(MinecraftVersion.parse(version));
    }
}
