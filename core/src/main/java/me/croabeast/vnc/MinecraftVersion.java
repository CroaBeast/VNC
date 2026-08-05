package me.croabeast.vnc;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable representation of a Minecraft version identifier.
 *
 * <p>The library works with two numbering families, described by {@link VersionFamily}:</p>
 * <ul>
 *     <li>Classic Java release numbers such as {@code 1.20.6} or {@code 1.21.11}.</li>
 *     <li>Year-based drop numbers such as {@code 25.2} or {@code 26.1.1}.</li>
 * </ul>
 *
 * <p>Orthogonally to the family, {@link VersionPhase} tells whether the identifier belongs to the
 * Alpha, Beta, or release line. Alpha and Beta reused the same {@code 1.x} numbers the release
 * line later used, so {@code b1.2} and {@code 1.2.1} are only distinguishable through the
 * phase.</p>
 *
 * <p>Alpha and Beta identifiers also carry two extra parts that the release line never used: a
 * {@code _NN} build suffix ({@code a1.0.17_02}) and a trailing letter qualifier
 * ({@code a1.2.2a}). Both are stored so {@link #getVersion()} reproduces the original identifier
 * exactly.</p>
 *
 * <p>A {@code MinecraftVersion} stores only the parsed structure. It also exposes the published
 * network protocol number when the parsed identifier corresponds to an exact release covered by
 * the library's built-in tables.</p>
 *
 * <p>Projected aliases that depend on a conversion scheme, such as {@code 1.22} under
 * {@link VersionScheme#MOJANG} or {@code 1.23} under {@link VersionScheme#CROA_CUSTOM},
 * intentionally do not invent a protocol number here. Conversion rules remain delegated to
 * {@link VersionScheme} implementations so this value object can stay scheme-agnostic.</p>
 */
@Getter
public final class MinecraftVersion {

    private static final Pattern IDENTIFIER = Pattern.compile(
            "^(?:(alpha|beta|a|b)[ _-]?)?" +
                    "(\\d+)\\.(\\d+)(?:\\.(\\d+))?" +
                    "(?:_(\\d+))?" +
                    "([a-z])?" +
                    "(?:[ _-](alpha|beta))?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Map<Integer, List<MinecraftVersion>> PROTOCOL_INDEX = createProtocolIndex();

    /**
     * The numbering family this identifier is written in.
     */
    private final VersionFamily family;
    /**
     * The development phase this identifier belongs to.
     */
    private final VersionPhase phase;
    /**
     * The first numeric segment of the parsed version.
     */
    private final int major;
    /**
     * The second numeric segment of the parsed version.
     */
    private final int minor;
    /**
     * The optional third numeric segment of the parsed version.
     */
    private final int patch;
    /**
     * The {@code _NN} build suffix used by several Alpha and Beta identifiers, or {@code 0} when
     * the identifier has none.
     */
    private final int build;
    /**
     * The trailing letter used by {@code a1.2.2a}, {@code a1.2.2b}, and {@code b1.3b}, or
     * {@code 0} when the identifier has none.
     */
    private final char qualifier;
    /**
     * The published network protocol number for this exact identifier, or {@code null} when the
     * identifier is only a projected alias and not a known exact release in the built-in tables.
     *
     * <p>The value is available for exact historic mappings and for official drop identifiers
     * that appear in the built-in protocol table. It is absent for scheme-dependent projected
     * aliases such as {@code 1.22} or {@code 1.23}.</p>
     */
    private final Integer protocol;

    /**
     * Creates an immutable version descriptor from already parsed parts.
     *
     * <p>The constructor does not infer the family or the phase. Callers are responsible for
     * supplying the correct values for the segments they provide.</p>
     *
     * <p>When the supplied identifier matches an exact release known by the built-in tables,
     * the corresponding protocol number is resolved automatically. Otherwise,
     * {@code getProtocol()} returns {@code null}.</p>
     *
     * @param family    numbering family of the identifier
     * @param phase     development phase of the identifier
     * @param major     first numeric segment
     * @param minor     second numeric segment
     * @param patch     third numeric segment, or {@code 0} when omitted
     * @param build     {@code _NN} build suffix, or {@code 0} when absent
     * @param qualifier trailing letter, or {@code 0} when absent
     */
    public MinecraftVersion(
            @NotNull VersionFamily family,
            @NotNull VersionPhase phase,
            int major,
            int minor,
            int patch,
            int build,
            char qualifier
    ) {
        this.family = Objects.requireNonNull(family, "family");
        this.phase = Objects.requireNonNull(phase, "phase");
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
        this.qualifier = qualifier;
        this.protocol = MappingTable.findProtocol(this);
    }

    /**
     * Creates a release-line version descriptor without build or qualifier parts.
     *
     * @param family numbering family of the identifier
     * @param major  first numeric segment
     * @param minor  second numeric segment
     * @param patch  third numeric segment, or {@code 0} when omitted
     */
    public MinecraftVersion(@NotNull VersionFamily family, int major, int minor, int patch) {
        this(family, VersionPhase.RELEASE, major, minor, patch, 0, (char) 0);
    }

    /**
     * Creates a release-line version descriptor from the legacy boolean family flag.
     *
     * @param classic whether this identifier belongs to the classic Java release family
     * @param major   first numeric segment
     * @param minor   second numeric segment
     * @param patch   third numeric segment, or {@code 0} when omitted
     * @deprecated use {@link #MinecraftVersion(VersionFamily, int, int, int)} instead; the boolean
     * flag cannot express the Alpha and Beta phases.
     */
    @Deprecated
    public MinecraftVersion(boolean classic, int major, int minor, int patch) {
        this(classic ? VersionFamily.CLASSIC : VersionFamily.DROP, major, minor, patch);
    }

    /**
     * Returns the identifier text, including the phase prefix, build suffix, and qualifier.
     *
     * <p>The result reproduces the original identifier: {@code b1.7.3}, {@code a1.0.17_02},
     * {@code a1.2.2a}, {@code 1.21.11}, {@code 26.1}.</p>
     *
     * @return normalized identifier text
     */
    @NotNull
    public String getVersion() {
        StringBuilder builder = new StringBuilder(phase.getPrefix())
                .append(major).append('.').append(minor);

        // Alpha identifiers always carry three segments, even when the patch is zero (a1.1.0).
        if (patch != 0 || phase == VersionPhase.ALPHA)
            builder.append('.').append(patch);

        if (build != 0)
            builder.append(build < 10 ? "_0" : "_").append(build);

        if (qualifier != 0)
            builder.append(qualifier);

        return builder.toString();
    }

    /**
     * Whether this identifier is written in classic {@code 1.x} numbering.
     *
     * @return {@code true} when the family is {@link VersionFamily#CLASSIC}
     * @deprecated use {@code getFamily()} and {@link VersionFamily} instead. The name collides
     * with Minecraft's own Classic era, which this library does not model.
     */
    @Deprecated
    public boolean isClassic() {
        return family == VersionFamily.CLASSIC;
    }

    /**
     * Whether this identifier belongs to the full release line.
     *
     * @return {@code true} when the phase is {@link VersionPhase#RELEASE}
     */
    public boolean isRelease() {
        return phase == VersionPhase.RELEASE;
    }

    /**
     * Whether this identifier belongs to the Alpha or Beta line.
     *
     * @return {@code true} when the phase precedes the release line
     */
    public boolean isPreRelease() {
        return phase.isPreRelease();
    }

    /**
     * Whether this version supports RGB / hex chat colors.
     *
     * <p>Hex colors were introduced in the classic {@code 1.16} line, which corresponds to the
     * year-based drop line {@code 20.1}. Any later version in either family keeps that support.</p>
     *
     * @return {@code true} when hex colors are supported
     */
    public boolean supportsHex() {
        if (phase.isPreRelease()) return false;

        if (family == VersionFamily.CLASSIC)
            return major > 1 || (major == 1 && minor >= 16);

        return major > 20 || (major == 20 && minor >= 1);
    }

    /**
     * Returns a readable description that includes the family, the phase, and the identifier.
     *
     * <p>Examples: {@code "classic 1.21.6"}, {@code "classic beta b1.7.3"}, {@code "drop 25.2.1"}.</p>
     *
     * @return descriptive representation of this instance
     */
    @Override
    public String toString() {
        String familyName = family == VersionFamily.CLASSIC ? "classic " : "drop ";
        String phaseName = phase.isPreRelease() ? phase.getDisplayName().toLowerCase() + " " : "";

        return familyName + phaseName + getVersion();
    }

    /**
     * Parses a textual version into a {@code MinecraftVersion}.
     *
     * <p>The parser accepts dotted numeric values with two or three segments, optionally carrying
     * a phase marker, a {@code _NN} build suffix, and a trailing letter qualifier. The phase may
     * be written as a prefix letter ({@code b1.7.3}), a prefix word ({@code Beta 1.7.3}), or a
     * suffix word ({@code 1.7.3-beta}), case-insensitively.</p>
     *
     * <p>The numeric part is then classified into a family:</p>
     * <ul>
     *     <li>{@code 1.x} or {@code 1.x.y} is treated as classic numbering.</li>
     *     <li>{@code 10.x} through {@code 99.x} is treated as year-based drop numbering. The
     *     {@code 10.x} line only holds the 2010 Alpha and Beta slot.</li>
     * </ul>
     *
     * <p>Whitespace around the input is ignored. Any other shape is rejected so the conversion
     * layer can operate on a well-defined model. In particular, identifiers with four numeric
     * segments are rejected outright instead of being truncated to the first three.</p>
     *
     * @param text version text to parse; must not be {@code null}
     * @return parsed immutable representation of the supplied version
     * @throws NullPointerException     if {@code text} is {@code null}
     * @throws IllegalArgumentException if the text does not match a supported version format
     */
    @NotNull
    public static MinecraftVersion parse(@NotNull String text) {
        String value = Objects.requireNonNull(text, "text").trim();

        Matcher m = IDENTIFIER.matcher(value);
        if (!m.matches())
            throw new IllegalArgumentException("Unsupported version format: " + text);

        VersionPhase phase = VersionPhase.byNameOrRelease(m.group(1) != null ? m.group(1) : m.group(7));

        int first = Integer.parseInt(m.group(2));
        int second = Integer.parseInt(m.group(3));
        int third = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
        int build = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
        char qualifier = m.group(6) != null ? Character.toLowerCase(m.group(6).charAt(0)) : 0;

        // Alpha and Beta only ever used the 1.x shape, so they are always classic numbering.
        if (phase.isPreRelease()) {
            if (first != 1)
                throw new IllegalArgumentException("Unsupported pre-release version number: " + text);

            return new MinecraftVersion(VersionFamily.CLASSIC, phase, first, second, third, build, qualifier);
        }

        if (first == 1)
            return new MinecraftVersion(VersionFamily.CLASSIC, phase, first, second, third, build, qualifier);

        if (first >= 10 && first <= 99)
            return new MinecraftVersion(VersionFamily.DROP, phase, first, second, third, build, qualifier);

        throw new IllegalArgumentException("Unsupported version number: " + text);
    }

    /**
     * Returns whether the supplied text is a complete, well-formed version identifier.
     *
     * @param text version text to test
     * @return {@code true} when {@link #parse(String)} would succeed
     */
    public static boolean isIdentifier(@Nullable String text) {
        return text != null && IDENTIFIER.matcher(text.trim()).matches();
    }

    /**
     * Resolves the published protocol for an arbitrary Mojang version identifier.
     *
     * <p>Dotted classic/drop identifiers are resolved through {@link #parse(String)} and the
     * built-in release tables. Snapshot identifiers such as {@code 26.2-snapshot-3} are looked
     * up through the separate published snapshot protocol table.</p>
     *
     * <p>This method deliberately returns {@code null} for projected future releases like
     * {@code 26.2} or {@code 1.22.3} until Mojang publishes an exact release protocol.</p>
     *
     * @param identifier dotted release/drop version or Mojang snapshot id
     * @return published protocol when known, otherwise {@code null}
     */
    @Nullable
    public static Integer protocolForIdentifier(@NotNull String identifier) {
        String normalized = Objects.requireNonNull(identifier, "identifier").trim();

        if (isIdentifier(normalized)) {
            try {
                return parse(normalized).getProtocol();
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return MappingTable.findSnapshotProtocol(normalized);
    }

    /**
     * Returns every known version that uses the supplied protocol number.
     *
     * <p>The returned list is ordered from the earliest known matching release to the latest one.
     * When the protocol only exists in Mojang's year-based drop numbering, the list contains the
     * corresponding projected classic aliases produced by {@link VersionScheme#MOJANG}.</p>
     *
     * <p>Alpha and Beta versions are deliberately absent from this index. Their protocol numbers
     * are small integers that overlap the early release line, so including them would make the
     * reverse lookup ambiguous.</p>
     *
     * @param protocol protocol number to inspect
     * @return immutable list of known versions for that protocol; empty when none are known
     */
    @NotNull
    public static List<MinecraftVersion> versionsForProtocol(int protocol) {
        List<MinecraftVersion> versions = PROTOCOL_INDEX.get(protocol);
        return versions != null ? versions : Collections.emptyList();
    }

    /**
     * Resolves a canonical version for the supplied protocol number.
     *
     * <p>Because Mojang often reused the same protocol across multiple hotfix releases, the
     * resolver returns the newest known matching version. For example, protocol {@code 754}
     * resolves to {@code 1.16.5}.</p>
     *
     * @param protocol protocol number to resolve
     * @return newest known matching version, or {@code null} when the protocol is unknown
     */
    @Nullable
    public static MinecraftVersion fromProtocol(int protocol) {
        List<MinecraftVersion> versions = PROTOCOL_INDEX.get(protocol);
        return versions == null || versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }

    @NotNull
    private static Map<Integer, List<MinecraftVersion>> createProtocolIndex() {
        Map<Integer, Map<String, MinecraftVersion>> versions = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : MappingTable.CLASSIC_PROTOCOLS.entrySet()) {
            versions.computeIfAbsent(entry.getValue(), key -> new LinkedHashMap<>())
                    .put(entry.getKey(), MinecraftVersion.parse(entry.getKey()));
        }

        for (Map.Entry<String, Integer> entry : MappingTable.DROP_PROTOCOLS.entrySet()) {
            try {
                MinecraftVersion classicVersion = MinecraftVersion.parse(VersionScheme.MOJANG.toClassic(entry.getKey()));
                versions.computeIfAbsent(entry.getValue(), key -> new LinkedHashMap<>())
                        .put(classicVersion.getVersion(), classicVersion);
            } catch (IllegalArgumentException ignored) {}
        }

        Map<Integer, List<MinecraftVersion>> index = new LinkedHashMap<>();
        for (Map.Entry<Integer, Map<String, MinecraftVersion>> entry : versions.entrySet()) {
            index.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue().values())));
        }

        return Collections.unmodifiableMap(index);
    }
}
