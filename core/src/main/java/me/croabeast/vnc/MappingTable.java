package me.croabeast.vnc;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable table of exact classic/drop correspondences.
 *
 * <p>The table is intentionally explicit instead of arithmetic. That keeps conversions exact
 * for release lines where Mojang skipped patch numbers or reused the same classic minor line
 * for several independent drops. Callers can populate their own table and pass it to
 * {@link VersionScheme#mapped(MappingTable)} when they want a custom exact scheme.</p>
 */
@NoArgsConstructor
public final class MappingTable {

    static final MappingTable MOJANG_MAPPINGS = createMojangMappings();
    static final MappingTable CROA_CUSTOM_MAPPINGS = createCroaCustomMappings();
    static final Map<String, String> CROA_CUSTOM_ALIASES = createCroaCustomAliases();
    static final Map<String, Integer> CLASSIC_PROTOCOLS = createClassicProtocols();
    static final Map<String, Integer> DROP_PROTOCOLS = createDropProtocols();
    static final Map<String, Integer> SNAPSHOT_PROTOCOLS = createSnapshotProtocols();
    static final Map<String, Integer> PRE_RELEASE_PROTOCOLS = createPreReleaseProtocols();

    private final Map<String, String> classicToDrop = new LinkedHashMap<>();
    private final Map<String, String> dropToClassic = new LinkedHashMap<>();

    // Newest drop line registered through registerLine; projections start after it.
    private int latestYear, latestRelease, latestMinor;

    /**
     * Registers a release line where one drop version maps to a sequence of classic versions.
     *
     * <p>The first classic version in the array becomes {@code year.release}. Each following
     * entry becomes a hotfix within the same drop line using contiguous hotfix numbering
     * ({@code year.release.1}, {@code year.release.2}, and so on).</p>
     *
     * @param year            drop year segment
     * @param release         drop release segment inside the year
     * @param classicVersions classic versions belonging to that release line
     * @return this table for chaining
     */
    @NotNull
    public MappingTable registerLine(int year, int release, @NotNull String... classicVersions) {
        for (int i = 0; i < classicVersions.length; i++) {
            String classic = normalizeClassic(MinecraftVersion.parse(classicVersions[i]));
            String drop = year + "." + release + (i == 0 ? "" : "." + i);

            registerMapping(classic, drop);
        }

        if (classicVersions.length > 0 && (year > latestYear || (year == latestYear && release > latestRelease))) {
            latestYear = year;
            latestRelease = release;
            latestMinor = MinecraftVersion.parse(classicVersions[0]).getMinor();
        }

        return this;
    }

    /**
     * Registers one exact classic/drop correspondence.
     *
     * <p>If the drop version was already associated with another classic version, the new
     * classic version becomes the canonical reverse lookup returned by
     * the built-in conversion schemes. Existing classic-to-drop aliases remain valid.</p>
     *
     * @param classicVersion classic version text
     * @param dropVersion    drop version text
     * @return this table for chaining
     */
    @NotNull
    public MappingTable registerMapping(@NotNull String classicVersion, @NotNull String dropVersion) {
        String normalizedClassic = normalizeClassic(MinecraftVersion.parse(classicVersion));
        String normalizedDrop = normalizeDrop(MinecraftVersion.parse(dropVersion));

        registerClassicMapping(normalizedClassic, normalizedDrop);
        dropToClassic.put(normalizedDrop, normalizedClassic);
        return this;
    }

    /**
     * Creates a shallow copy of this table.
     *
     * @return new table containing the same exact mappings
     */
    @NotNull
    public MappingTable copy() {
        MappingTable copy = new MappingTable();
        copy.classicToDrop.putAll(classicToDrop);
        copy.dropToClassic.putAll(dropToClassic);
        copy.latestYear = latestYear;
        copy.latestRelease = latestRelease;
        copy.latestMinor = latestMinor;
        return copy;
    }

    String findClassic(@NotNull MinecraftVersion version) {
        return dropToClassic.get(normalizeDrop(version));
    }

    String findDrop(@NotNull MinecraftVersion version) {
        return classicToDrop.get(normalizeClassic(version));
    }

    @Nullable
    static Integer findProtocol(@NotNull MinecraftVersion version) {
        // The static tables below are still null while this class initializes its own mappings,
        // because building them parses versions, which lands back here.
        if (version.getPhase().isPreRelease())
            return PRE_RELEASE_PROTOCOLS == null ? null : PRE_RELEASE_PROTOCOLS.get(version.getVersion());

        boolean classic = version.getFamily() == VersionFamily.CLASSIC;

        Map<String, Integer> protocols = classic ? CLASSIC_PROTOCOLS : DROP_PROTOCOLS;
        if (protocols == null)
            return null;

        return protocols.get(classic ? normalizeClassic(version) : normalizeDrop(version));
    }

    @Nullable
    static Integer findSnapshotProtocol(@NotNull String versionId) {
        return SNAPSHOT_PROTOCOLS.get(versionId.trim().toLowerCase());
    }

    private void registerClassicMapping(String classicVersion, String dropVersion) {
        String previous = classicToDrop.putIfAbsent(classicVersion, dropVersion);
        if (previous != null)
            throw new IllegalStateException(
                    "Duplicate classic mapping for " + classicVersion + ": " + previous + " and " + dropVersion
            );
    }

    static MappingTable createMojangMappings() {
        return createSharedMappings()
                .registerLine(26, 1, "1.22", "1.22.1", "1.22.2")
                .registerLine(26, 2, "1.23")
                .registerLine(26, 3, "1.24");
    }

    static MappingTable createCroaCustomMappings() {
        return createSharedMappings()
                .registerMapping("1.22", "25.4")
                .registerLine(26, 1, "1.23", "1.23.1", "1.23.2")
                .registerLine(26, 2, "1.24")
                .registerLine(26, 3, "1.25");
    }

    // Lines up to 25.4, identical in every scheme. Each year drop from 26.1 onward is its own
    // classic minor, and the schemes disagree on which minor that is, so they add those lines.
    private static MappingTable createSharedMappings() {
        return new MappingTable()
                // Alpha and Beta take slot 0 of their year, indexed chronologically. Slot 0 was
                // never used by a release, so the release lines below stay untouched.
                .registerLine(10, 0,
                        "a1.0.4", "a1.0.5_01", "a1.0.11", "a1.0.14", "a1.0.15",
                        "a1.0.16", "a1.0.17_02", "a1.0.17_04", "a1.1.0", "a1.1.2",
                        "a1.1.2_01", "a1.2.0", "a1.2.0_01", "a1.2.0_02", "a1.2.1",
                        "a1.2.1_01", "a1.2.2a", "a1.2.2b", "a1.2.3", "a1.2.3_01",
                        "a1.2.3_02", "a1.2.3_04", "a1.2.4_01", "a1.2.5", "a1.2.6",
                        "b1.0", "b1.0_01", "b1.0.2", "b1.1_01", "b1.1_02"
                )
                .registerLine(11, 0,
                        "b1.2", "b1.2_01", "b1.2_02", "b1.3b", "b1.3_01",
                        "b1.4", "b1.4_01", "b1.5", "b1.5_01", "b1.6",
                        "b1.6.1", "b1.6.2", "b1.6.3", "b1.6.4", "b1.6.5",
                        "b1.6.6", "b1.7", "b1.7.2", "b1.7.3", "b1.8",
                        "b1.8.1"
                )
                .registerLine(11, 1, "1.0.0", "1.0.1")
                .registerLine(12, 1, "1.1")
                .registerLine(12, 2, "1.2.1", "1.2.2", "1.2.3", "1.2.4", "1.2.5")
                .registerLine(12, 3, "1.3.1", "1.3.2")
                .registerLine(12, 4, "1.4.2", "1.4.4", "1.4.5", "1.4.6", "1.4.7")
                .registerLine(13, 1, "1.5", "1.5.1", "1.5.2")
                .registerLine(13, 2, "1.6.1", "1.6.2", "1.6.4")
                .registerLine(13, 3, "1.7.2", "1.7.4", "1.7.5", "1.7.6", "1.7.7", "1.7.8", "1.7.9", "1.7.10")
                .registerLine(14, 1, "1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9")
                .registerLine(16, 1, "1.9", "1.9.1", "1.9.2", "1.9.3", "1.9.4")
                .registerLine(16, 2, "1.10", "1.10.1", "1.10.2")
                .registerLine(16, 3, "1.11", "1.11.1", "1.11.2")
                .registerLine(17, 1, "1.12", "1.12.1", "1.12.2")
                .registerLine(18, 1, "1.13", "1.13.1", "1.13.2")
                .registerLine(19, 1, "1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4")
                .registerLine(19, 2, "1.15", "1.15.1", "1.15.2")
                .registerLine(20, 1, "1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5")
                .registerLine(21, 1, "1.17", "1.17.1")
                .registerLine(21, 2, "1.18", "1.18.1", "1.18.2")
                .registerLine(22, 1, "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4")
                .registerLine(23, 1, "1.20", "1.20.1", "1.20.2")
                .registerLine(23, 2, "1.20.3", "1.20.4")
                .registerLine(24, 1, "1.20.5", "1.20.6")
                .registerLine(24, 2, "1.21", "1.21.1")
                .registerLine(24, 3, "1.21.2", "1.21.3")
                .registerLine(24, 4, "1.21.4")
                .registerLine(25, 1, "1.21.5")
                .registerLine(25, 2, "1.21.6", "1.21.7", "1.21.8")
                .registerLine(25, 3, "1.21.9", "1.21.10")
                .registerLine(25, 4, "1.21.11");
    }

    static Map<String, String> createCroaCustomAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("1.21.11", "1.22");
        return Collections.unmodifiableMap(aliases);
    }

    static Map<String, Integer> createClassicProtocols() {
        Map<String, Integer> protocols = new LinkedHashMap<>();

        putClassicProtocol(protocols, "1.0.0", 22);
        putClassicProtocol(protocols, "1.0.1", 22);
        putClassicProtocol(protocols, "1.1", 23);
        putClassicProtocol(protocols, "1.2.1", 28);
        putClassicProtocol(protocols, "1.2.2", 28);
        putClassicProtocol(protocols, "1.2.3", 28);
        putClassicProtocol(protocols, "1.2.4", 29);
        putClassicProtocol(protocols, "1.2.5", 29);
        putClassicProtocol(protocols, "1.3.1", 39);
        putClassicProtocol(protocols, "1.3.2", 39);
        putClassicProtocol(protocols, "1.4.2", 47);
        putClassicProtocol(protocols, "1.4.4", 49);
        putClassicProtocol(protocols, "1.4.5", 49);
        putClassicProtocol(protocols, "1.4.6", 51);
        putClassicProtocol(protocols, "1.4.7", 51);
        putClassicProtocol(protocols, "1.5", 60);
        putClassicProtocol(protocols, "1.5.1", 60);
        putClassicProtocol(protocols, "1.5.2", 61);
        putClassicProtocol(protocols, "1.6.1", 73);
        putClassicProtocol(protocols, "1.6.2", 74);
        putClassicProtocol(protocols, "1.6.4", 78);
        putClassicProtocol(protocols, "1.7.2", 4);
        putClassicProtocol(protocols, "1.7.4", 4);
        putClassicProtocol(protocols, "1.7.5", 4);
        putClassicProtocol(protocols, "1.7.6", 5);
        putClassicProtocol(protocols, "1.7.7", 5);
        putClassicProtocol(protocols, "1.7.8", 5);
        putClassicProtocol(protocols, "1.7.9", 5);
        putClassicProtocol(protocols, "1.7.10", 5);
        putClassicProtocol(protocols, "1.8", 47);
        putClassicProtocol(protocols, "1.8.1", 47);
        putClassicProtocol(protocols, "1.8.2", 47);
        putClassicProtocol(protocols, "1.8.3", 47);
        putClassicProtocol(protocols, "1.8.4", 47);
        putClassicProtocol(protocols, "1.8.5", 47);
        putClassicProtocol(protocols, "1.8.6", 47);
        putClassicProtocol(protocols, "1.8.7", 47);
        putClassicProtocol(protocols, "1.8.8", 47);
        putClassicProtocol(protocols, "1.8.9", 47);
        putClassicProtocol(protocols, "1.9", 107);
        putClassicProtocol(protocols, "1.9.1", 108);
        putClassicProtocol(protocols, "1.9.2", 109);
        putClassicProtocol(protocols, "1.9.3", 110);
        putClassicProtocol(protocols, "1.9.4", 110);
        putClassicProtocol(protocols, "1.10", 210);
        putClassicProtocol(protocols, "1.10.1", 210);
        putClassicProtocol(protocols, "1.10.2", 210);
        putClassicProtocol(protocols, "1.11", 315);
        putClassicProtocol(protocols, "1.11.1", 316);
        putClassicProtocol(protocols, "1.11.2", 316);
        putClassicProtocol(protocols, "1.12", 335);
        putClassicProtocol(protocols, "1.12.1", 338);
        putClassicProtocol(protocols, "1.12.2", 340);
        putClassicProtocol(protocols, "1.13", 393);
        putClassicProtocol(protocols, "1.13.1", 401);
        putClassicProtocol(protocols, "1.13.2", 404);
        putClassicProtocol(protocols, "1.14", 477);
        putClassicProtocol(protocols, "1.14.1", 480);
        putClassicProtocol(protocols, "1.14.2", 485);
        putClassicProtocol(protocols, "1.14.3", 490);
        putClassicProtocol(protocols, "1.14.4", 498);
        putClassicProtocol(protocols, "1.15", 573);
        putClassicProtocol(protocols, "1.15.1", 575);
        putClassicProtocol(protocols, "1.15.2", 578);
        putClassicProtocol(protocols, "1.16", 735);
        putClassicProtocol(protocols, "1.16.1", 736);
        putClassicProtocol(protocols, "1.16.2", 751);
        putClassicProtocol(protocols, "1.16.3", 753);
        putClassicProtocol(protocols, "1.16.4", 754);
        putClassicProtocol(protocols, "1.16.5", 754);
        putClassicProtocol(protocols, "1.17", 755);
        putClassicProtocol(protocols, "1.17.1", 756);
        putClassicProtocol(protocols, "1.18", 757);
        putClassicProtocol(protocols, "1.18.1", 757);
        putClassicProtocol(protocols, "1.18.2", 758);
        putClassicProtocol(protocols, "1.19", 759);
        putClassicProtocol(protocols, "1.19.1", 760);
        putClassicProtocol(protocols, "1.19.2", 760);
        putClassicProtocol(protocols, "1.19.3", 761);
        putClassicProtocol(protocols, "1.19.4", 762);
        putClassicProtocol(protocols, "1.20", 763);
        putClassicProtocol(protocols, "1.20.1", 763);
        putClassicProtocol(protocols, "1.20.2", 764);
        putClassicProtocol(protocols, "1.20.3", 765);
        putClassicProtocol(protocols, "1.20.4", 765);
        putClassicProtocol(protocols, "1.20.5", 766);
        putClassicProtocol(protocols, "1.20.6", 766);
        putClassicProtocol(protocols, "1.21", 767);
        putClassicProtocol(protocols, "1.21.1", 767);
        putClassicProtocol(protocols, "1.21.2", 768);
        putClassicProtocol(protocols, "1.21.3", 768);
        putClassicProtocol(protocols, "1.21.4", 769);
        putClassicProtocol(protocols, "1.21.5", 770);
        putClassicProtocol(protocols, "1.21.6", 771);
        putClassicProtocol(protocols, "1.21.7", 772);
        putClassicProtocol(protocols, "1.21.8", 772);
        putClassicProtocol(protocols, "1.21.9", 773);
        putClassicProtocol(protocols, "1.21.10", 773);
        putClassicProtocol(protocols, "1.21.11", 774);

        return Collections.unmodifiableMap(protocols);
    }

    static Map<String, Integer> createDropProtocols() {
        Map<String, Integer> protocols = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : MOJANG_MAPPINGS.classicToDrop.entrySet()) {
            Integer protocol = CLASSIC_PROTOCOLS.get(entry.getKey());
            if (protocol != null)
                putDropProtocol(protocols, entry.getValue(), protocol);
        }

        putDropProtocol(protocols, "26.1", 775);
        putDropProtocol(protocols, "26.1.1", 775);
        putDropProtocol(protocols, "26.1.2", 775);
        putDropProtocol(protocols, "26.2", 776);
        putDropProtocol(protocols, "26.3", 777);
        return Collections.unmodifiableMap(protocols);
    }

    static Map<String, Integer> createPreReleaseProtocols() {
        Map<String, Integer> protocols = new LinkedHashMap<>();

        protocols.put("a1.2.5", 6);
        protocols.put("a1.2.6", 6);

        protocols.put("b1.0", 7);
        protocols.put("b1.0_01", 7);
        protocols.put("b1.0.2", 7);
        protocols.put("b1.1_01", 7);
        protocols.put("b1.1_02", 7);
        protocols.put("b1.2", 8);
        protocols.put("b1.2_01", 8);
        protocols.put("b1.2_02", 8);
        protocols.put("b1.3b", 9);
        protocols.put("b1.3_01", 9);
        protocols.put("b1.4", 10);
        protocols.put("b1.4_01", 10);
        protocols.put("b1.5", 11);
        protocols.put("b1.5_01", 11);
        protocols.put("b1.6", 12);
        protocols.put("b1.6.1", 13);
        protocols.put("b1.6.2", 13);
        protocols.put("b1.6.3", 13);
        protocols.put("b1.6.4", 13);
        protocols.put("b1.6.5", 13);
        protocols.put("b1.6.6", 13);
        protocols.put("b1.7", 14);
        protocols.put("b1.7.2", 14);
        protocols.put("b1.7.3", 14);
        protocols.put("b1.8", 17);
        protocols.put("b1.8.1", 17);

        return Collections.unmodifiableMap(protocols);
    }

    static Map<String, Integer> createSnapshotProtocols() {
        Map<String, Integer> protocols = new LinkedHashMap<>();
        protocols.put("26.2-snapshot-1", 1073742130);
        protocols.put("26.2-snapshot-2", 1073742132);
        protocols.put("26.2-snapshot-3", 1073742133);
        protocols.put("26.2-snapshot-4", 1073742134);
        protocols.put("26.2-snapshot-5", 1073742135);
        protocols.put("26.2-snapshot-6", 1073742136);
        protocols.put("26.2-snapshot-7", 1073742137);
        protocols.put("26.2-snapshot-8", 1073742138);
        protocols.put("26.2-pre-1", 1073742139);
        protocols.put("26.2-pre-2", 1073742140);
        protocols.put("26.2-pre-3", 1073742141);
        protocols.put("26.2-pre-4", 1073742142);
        protocols.put("26.2-pre-5", 1073742143);
        protocols.put("26.2-pre-6", 1073742144);
        protocols.put("26.2-rc-1", 1073742145);
        protocols.put("26.2-rc-2", 1073742146);
        protocols.put("26.3-snapshot-1", 1073742147);
        protocols.put("26.3-snapshot-2", 1073742148);
        protocols.put("26.3-snapshot-3", 1073742149);
        protocols.put("26.3-snapshot-4", 1073742150);
        protocols.put("26.3-snapshot-5", 1073742151);
        protocols.put("26.3-snapshot-6", 1073742152);
        protocols.put("26.3-snapshot-7", 1073742153);
        protocols.put("26.3-snapshot-8", 1073742154);
        protocols.put("26.3-snapshot-9", 1073742155);
        protocols.put("26.3-snapshot-10", 1073742156);
        protocols.put("26.3-pre-1", 1073742157);
        protocols.put("26.3-pre-2", 1073742158);
        protocols.put("26.3-pre-3", 1073742159);
        protocols.put("26.3-rc-1", 1073742160);
        protocols.put("26.3-rc-2", 1073742161);
        protocols.put("26.3-rc-3", 1073742162);
        protocols.put("26.4-snapshot-1", 1073742163);
        return Collections.unmodifiableMap(protocols);
    }

    private static void putClassicProtocol(Map<String, Integer> protocols, String version, int protocol) {
        protocols.put(version, protocol);
    }

    private static void putDropProtocol(Map<String, Integer> protocols, String version, int protocol) {
        protocols.put(version, protocol);
    }

    @NotNull
    static String projectOfficialClassic(@NotNull MinecraftVersion version) {
        return MOJANG_MAPPINGS.projectClassic(version, "Mojang-style");
    }

    @NotNull
    static String projectOfficialDrop(@NotNull MinecraftVersion version) {
        return MOJANG_MAPPINGS.projectDrop(version, "Mojang-style");
    }

    @NotNull
    static String projectCustomClassic(@NotNull MinecraftVersion version) {
        if (version.getMajor() == 25 && version.getMinor() == 4 && version.getPatch() > 0)
            return "1.22." + version.getPatch();

        return CROA_CUSTOM_MAPPINGS.projectClassic(version, "custom");
    }

    @NotNull
    static String projectCustomDrop(@NotNull MinecraftVersion version) {
        if (version.getMajor() == 1 && version.getMinor() == 22 && version.getPatch() > 0)
            return "25.4." + version.getPatch();

        return CROA_CUSTOM_MAPPINGS.projectDrop(version, "custom");
    }

    // Drops newer than the latest table line are approximated as the following classic minors,
    // one per drop, so version gates keep ordering them correctly. The exact minor of a new year
    // is unknown until its lines are added to the table, so 27.1 may later map elsewhere.
    @NotNull
    private String projectClassic(@NotNull MinecraftVersion version, String scheme) {
        int year = version.getMajor();
        int release = version.getMinor();
        String hotfix = version.getPatch() > 0 ? "." + version.getPatch() : "";

        if (year == latestYear && release == latestRelease)
            return "1." + latestMinor + hotfix;

        if (year > latestYear || (year == latestYear && release > latestRelease))
            return "1." + (latestMinor + (year == latestYear ? release - latestRelease : release)) + hotfix;

        throw new IllegalArgumentException(
                "No exact " + scheme + " classic alias is defined for drop version " +
                        normalizeDrop(version) + ". Add the release line to the mapping table."
        );
    }

    @NotNull
    private String projectDrop(@NotNull MinecraftVersion version, String scheme) {
        if (version.getPhase().isPreRelease())
            throw new IllegalArgumentException(
                    "No drop mapping is defined for pre-release version " + version.getVersion() +
                            ". Only the Alpha and Beta identifiers published by Mojang are mapped."
            );

        if (version.getMajor() == 1 && version.getMinor() >= latestMinor)
            return latestYear + "." + (latestRelease + version.getMinor() - latestMinor) +
                    (version.getPatch() > 0 ? "." + version.getPatch() : "");

        throw new IllegalArgumentException(
                "No exact " + scheme + " drop mapping is defined for classic version " +
                        normalizeClassic(version) + ". Add the release line to the mapping table."
        );
    }

    @NotNull
    static String normalizeClassic(@NotNull MinecraftVersion version) {
        // Alpha and Beta keys need the phase, build, and qualifier parts to stay unique:
        // a1.2.2a and a1.2.2b share the same numeric segments.
        if (version.getPhase().isPreRelease())
            return version.getVersion();

        return normalizeClassic(version.getMajor(), version.getMinor(), version.getPatch());
    }

    @NotNull
    static String normalizeDrop(@NotNull MinecraftVersion version) {
        return normalizeDrop(version.getMajor(), version.getMinor(), version.getPatch());
    }

    @NotNull
    private static String normalizeClassic(int major, int minor, int patch) {
        return major + "." + minor + (patch > 0 || (major == 1 && minor == 0) ? "." + patch : "");
    }

    @NotNull
    private static String normalizeDrop(int major, int minor, int patch) {
        return major + "." + minor + (patch > 0 ? "." + patch : "");
    }
}
