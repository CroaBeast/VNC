package me.croabeast.vnc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers comparison and conversion, the part consumers use to gate features by version.
 */
class VersioningTest {

    private static MinecraftVersion at(String value) {
        return MinecraftVersion.parse(value);
    }

    @Test
    void comparesWithinTheClassicFamily() {
        assertTrue(Versioning.compare(at("1.16.5"), "1.16.4") > 0);
        assertTrue(Versioning.compare(at("1.16.4"), "1.16.5") < 0);
        assertEquals(0, Versioning.compare(at("1.16.5"), "1.16.5"));
    }

    @Test
    void treatsAMissingPatchAsZero() {
        assertEquals(0, Versioning.compare(at("1.16"), "1.16.0"));
        assertTrue(Versioning.compare(at("1.16.1"), "1.16") > 0);
    }

    @Test
    void ordersPreReleasesBeforeTheReleaseLine() {
        assertTrue(Versioning.compare(at("b1.7.3"), "1.0") < 0);
        assertTrue(Versioning.compare(at("a1.2.6"), "b1.7.3") < 0);
    }

    @Test
    void comparesAcrossFamilies() {
        // 1.16 and 20.1 are the same release in the two numbering schemes.
        assertEquals(0, Versioning.compare(at("1.16"), "20.1"));
        assertTrue(Versioning.compare(at("25.2"), "1.16") > 0);
    }

    @Test
    void boundChecksFollowComparison() {
        MinecraftVersion current = at("1.20.4");

        assertTrue(Versioning.isAtLeast(current, "1.16"));
        assertTrue(Versioning.isAtLeast(current, "1.20.4"));
        assertFalse(Versioning.isAtLeast(current, "1.21"));

        assertTrue(Versioning.isBefore(current, "1.21"));
        assertFalse(Versioning.isBefore(current, "1.20.4"));

        assertTrue(Versioning.isBetween(current, "1.16", "1.21"));
        assertTrue(Versioning.isBetween(current, "1.20.4", "1.20.4"));
        assertFalse(Versioning.isBetween(current, "1.8", "1.16"));
    }

    @Test
    void parseReturnsNullOnBadInputAndRequireThrows() {
        assertNull(Versioning.parseMinecraftVersion("potato"));
        assertNull(Versioning.parseMinecraftVersion(null));

        assertEquals("1.16.5", Versioning.requireMinecraftVersion("1.16.5").getVersion());
        assertThrows(IllegalArgumentException.class, () -> Versioning.requireMinecraftVersion("potato"));
    }

    @Test
    void convertsClassicIdentifiersToTheLegacyDouble() {
        assertEquals(16.5D, Versioning.toLegacyServerVersion("1.16.5"), 0.0001D);
        assertEquals(8.0D, Versioning.toLegacyServerVersion("1.8"), 0.0001D);
    }

    @Test
    void reportsTheRunningJavaVersion() {
        assertTrue(Versioning.javaVersion() >= 8, "unexpected java version: " + Versioning.javaVersion());
    }

    @Test
    void mojangSchemeRoundTripsKnownReleases() {
        assertEquals("20.1", VersionScheme.MOJANG.toDrop("1.16"));
        assertEquals("1.16", VersionScheme.MOJANG.toClassic("20.1"));
    }

    @Test
    void mojangSchemeGivesEachDropItsOwnMinor() {
        assertEquals("1.22", VersionScheme.MOJANG.toClassic("26.1"));
        assertEquals("1.22.2", VersionScheme.MOJANG.toClassic("26.1.2"));
        assertEquals("1.23", VersionScheme.MOJANG.toClassic("26.2"));
        assertEquals("1.24", VersionScheme.MOJANG.toClassic("26.3"));

        assertEquals("26.2", VersionScheme.MOJANG.toDrop("1.23"));
        assertEquals("26.3", VersionScheme.MOJANG.toDrop("1.24"));
        assertEquals(24.0D, Versioning.toLegacyServerVersion(VersionScheme.MOJANG.toClassic("26.3")), 0.0001D);
    }

    @Test
    void customSchemeShiftsEveryDropByOneMinor() {
        assertEquals("1.22.1", VersionScheme.CROA_CUSTOM.toClassic("25.4.1"));
        assertEquals("1.23", VersionScheme.CROA_CUSTOM.toClassic("26.1"));
        assertEquals("1.24", VersionScheme.CROA_CUSTOM.toClassic("26.2"));
        assertEquals("1.25", VersionScheme.CROA_CUSTOM.toClassic("26.3"));

        assertEquals("26.3", VersionScheme.CROA_CUSTOM.toDrop("1.25"));
    }

    @Test
    void projectsDropsNewerThanTheTable() {
        assertEquals("1.24.1", VersionScheme.MOJANG.toClassic("26.3.1"));
        assertEquals("1.25", VersionScheme.MOJANG.toClassic("26.4"));
        assertEquals("1.25", VersionScheme.MOJANG.toClassic("27.1"));
        assertEquals("26.4", VersionScheme.MOJANG.toDrop("1.25"));

        assertTrue(Versioning.compare(at("27.1"), "26.3") > 0);
        assertThrows(IllegalArgumentException.class, () -> VersionScheme.MOJANG.toClassic("26.2.1"));
    }
}
