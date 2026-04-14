package me.croabeast.vnc;

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
public final class MappingTable {

    static final MappingTable MOJANG_MAPPINGS = createMojangMappings();
    static final MappingTable CROA_CUSTOM_MAPPINGS = createCroaCustomMappings();
    static final Map<String, String> CROA_CUSTOM_ALIASES = createCroaCustomAliases();
    static final Map<String, Integer> CLASSIC_PROTOCOLS = createClassicProtocols();
    static final Map<String, Integer> DROP_PROTOCOLS = createDropProtocols();
    static final Map<String, Integer> SNAPSHOT_PROTOCOLS = createSnapshotProtocols();

    private final Map<String, String> classicToDrop = new LinkedHashMap<>();
    private final Map<String, String> dropToClassic = new LinkedHashMap<>();

    /**
     * Creates an empty mapping table.
     */
    public MappingTable() {}

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
        return copy;
    }

    String findClassic(@NotNull MinecraftVersion version) {
        return dropToClassic.get(normalizeDrop(version));
    }

    String findDrop(@NotNull MinecraftVersion version) {
        return classicToDrop.get(normalizeClassic(version));
    }

    @Nullable
    static Integer findProtocol(boolean classic, int major, int minor, int patch) {
        Map<String, Integer> protocols = classic ? CLASSIC_PROTOCOLS : DROP_PROTOCOLS;
        if (protocols == null)
            return null;

        return protocols.get(classic ? normalizeClassic(major, minor, patch) : normalizeDrop(major, minor, patch));
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
        return new MappingTable()
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

    static MappingTable createCroaCustomMappings() {
        return createMojangMappings()
                .registerMapping("1.22", "25.4");
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
        return Collections.unmodifiableMap(protocols);
    }

    static Map<String, Integer> createSnapshotProtocols() {
        Map<String, Integer> protocols = new LinkedHashMap<>();
        protocols.put("26.2-snapshot-1", 1073742130);
        protocols.put("26.2-snapshot-2", 1073742132);
        protocols.put("26.2-snapshot-3", 1073742133);
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
        int year = version.getMajor();
        int release = version.getMinor();
        int hotfix = version.getPatch();

        if (year >= 26 && release == 1)
            return "1." + (year - 4) + (hotfix > 0 ? "." + hotfix : "");

        if (year == 26 && release == 2)
            return "1.22." + (hotfix + 3);

        throw new IllegalArgumentException(
                "No exact Mojang-style classic alias is defined for drop version " +
                        normalizeDrop(version) +
                        ". Future drops beyond the first release of a year need an explicit table entry."
        );
    }

    @NotNull
    static String projectOfficialDrop(@NotNull MinecraftVersion version) {
        if (version.getMajor() == 1 && version.getMinor() == 22 && version.getPatch() >= 3)
            return "26.2" + (version.getPatch() > 3 ? "." + (version.getPatch() - 3) : "");

        if (version.getMajor() == 1 && version.getMinor() >= 22)
            return (version.getMinor() + 4) + ".1" + (version.getPatch() > 0 ? "." + version.getPatch() : "");

        throw new IllegalArgumentException(
                "No exact Mojang-style drop mapping is defined for classic version " +
                        normalizeClassic(version) +
                        ". Add the release line explicitly instead of guessing."
        );
    }

    @NotNull
    static String projectCustomClassic(@NotNull MinecraftVersion version) {
        int year = version.getMajor();
        int release = version.getMinor();
        int hotfix = version.getPatch();

        if (year == 25 && release == 4 && hotfix > 0)
            return "1.22." + hotfix;

        if (year >= 26 && release == 1)
            return "1." + (year - 3) + (hotfix > 0 ? "." + hotfix : "");

        if (year == 26 && release == 2)
            return "1.23." + (hotfix + 3);

        return projectOfficialClassic(version);
    }

    @NotNull
    static String projectCustomDrop(@NotNull MinecraftVersion version) {
        if (version.getMajor() == 1 && version.getMinor() == 22 && version.getPatch() > 0)
            return "25.4." + version.getPatch();

        if (version.getMajor() == 1 && version.getMinor() == 23 && version.getPatch() >= 3)
            return "26.2" + (version.getPatch() > 3 ? "." + (version.getPatch() - 3) : "");

        if (version.getMajor() == 1 && version.getMinor() >= 23)
            return (version.getMinor() + 3) + ".1" + (version.getPatch() > 0 ? "." + version.getPatch() : "");

        return projectOfficialDrop(version);
    }

    @NotNull
    static String normalizeClassic(@NotNull MinecraftVersion version) {
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
