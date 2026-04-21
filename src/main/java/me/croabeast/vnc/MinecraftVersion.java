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
 * <p>The library works with two families of version numbers:</p>
 * <ul>
 *     <li>Classic Java release numbers such as {@code 1.20.6} or {@code 1.21.11}.</li>
 *     <li>Year-based drop numbers such as {@code 25.2} or {@code 26.1.1}.</li>
 * </ul>
 *
 * <p>A {@code MinecraftVersion} stores only the parsed numeric structure and a flag that
 * tells callers which family the original value belongs to. It also exposes the published
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

    private static final Pattern DOT_VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");
    private static final Map<Integer, List<MinecraftVersion>> PROTOCOL_INDEX = createProtocolIndex();

    /**
     * Whether this instance represents a classic Java release number.
     */
    private final boolean classic;
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
     * The published network protocol number for this exact identifier, or {@code null} when the
     * identifier is only a projected alias and not a known exact release in the built-in tables.
     *
     * <p>The value is available for exact historic mappings and for official drop identifiers
     * that appear in the built-in protocol table. It is absent for scheme-dependent projected
     * aliases such as {@code 1.22} or {@code 1.23}.</p>
     */
    private final Integer protocol;

    /**
     * Creates an immutable version descriptor from already parsed numeric parts.
     *
     * <p>The constructor does not infer the numbering family. Callers are responsible for
     * supplying the correct {@code classic} flag for the numeric segments they provide.</p>
     *
     * <p>When the supplied identifier matches an exact release known by the built-in tables,
     * the corresponding protocol number is resolved automatically. Otherwise,
     * {@link #getProtocol()} returns {@code null}.</p>
     *
     * @param classic whether this identifier belongs to the classic Java release family
     * @param major   first numeric segment
     * @param minor   second numeric segment
     * @param patch   third numeric segment, or {@code 0} when omitted
     */
    public MinecraftVersion(boolean classic, int major, int minor, int patch) {
        this.classic = classic;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.protocol = MappingTable.findProtocol(classic, major, minor, patch);
    }

    /**
     * Returns the numeric version string without adding any descriptive prefix.
     *
     * <p>The returned text preserves the same three-segment model used by this value object:
     * the patch segment is omitted when it is {@code 0}.</p>
     *
     * @return normalized numeric version text
     */
    @NotNull
    public String getVersion() {
        return major + "." + minor + (patch != 0 ? "." + patch : "");
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
        if (classic)
            return major > 1 || (major == 1 && minor >= 16);

        return major > 20 || (major == 20 && minor >= 1);
    }

    /**
     * Returns a readable description that includes the numbering family and the normalized
     * numeric value.
     *
     * <p>Examples: {@code "classic 1.21.6"} or {@code "drop 25.2.1"}.</p>
     *
     * @return descriptive representation of this instance
     */
    @Override
    public String toString() {
        return (classic ? "classic " : "drop ") + getVersion();
    }

    /**
     * Parses a textual version into a {@code MinecraftVersion}.
     *
     * <p>The parser accepts only dotted numeric values with two or three segments. It then
     * classifies the number using the library's supported families:</p>
     * <ul>
     *     <li>{@code 1.x} or {@code 1.x.y} is treated as a classic Java release number.</li>
     *     <li>{@code 11.x} through {@code 99.x} is treated as a year-based drop number.</li>
     * </ul>
     *
     * <p>Whitespace around the input is ignored. Any other shape is rejected so the conversion
     * layer can operate on a well-defined model.</p>
     *
     * @param text version text to parse; must not be {@code null}
     * @return parsed immutable representation of the supplied version
     * @throws NullPointerException     if {@code text} is {@code null}
     * @throws IllegalArgumentException if the text does not match a supported version format
     */
    @NotNull
    public static MinecraftVersion parse(@NotNull String text) {
        String s = Objects.requireNonNull(text, "text").trim();

        Matcher m = DOT_VERSION.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("Unsupported version format: " + text);

        int first = Integer.parseInt(m.group(1));
        int second = Integer.parseInt(m.group(2));
        int third = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;

        if (first == 1)
            return new MinecraftVersion(true, first, second, third);

        if (first >= 11 && first <= 99)
            return new MinecraftVersion(false, first, second, third);

        throw new IllegalArgumentException("Unsupported version number: " + text);
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

        if (DOT_VERSION.matcher(normalized).matches())
            return parse(normalized).getProtocol();

        return MappingTable.findSnapshotProtocol(normalized);
    }

    /**
     * Returns every known version that uses the supplied protocol number.
     *
     * <p>The returned list is ordered from the earliest known matching release to the latest one.
     * When the protocol only exists in Mojang's year-based drop numbering, the list contains the
     * corresponding projected classic aliases produced by {@link VersionScheme#MOJANG}.</p>
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
