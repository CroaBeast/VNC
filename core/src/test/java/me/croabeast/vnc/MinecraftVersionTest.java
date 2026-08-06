package me.croabeast.vnc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the identifier parser and the protocol tables, which is what every consumer of VNC ends
 * up depending on.
 */
class MinecraftVersionTest {

    @Test
    void parsesClassicReleases() {
        MinecraftVersion version = MinecraftVersion.parse("1.16.5");

        assertSame(VersionFamily.CLASSIC, version.getFamily());
        assertSame(VersionPhase.RELEASE, version.getPhase());
        assertEquals(1, version.getMajor());
        assertEquals(16, version.getMinor());
        assertEquals(5, version.getPatch());
        assertEquals("1.16.5", version.getVersion());
        assertTrue(version.isRelease());
        assertFalse(version.isPreRelease());
    }

    @Test
    void parsesDropReleases() {
        MinecraftVersion version = MinecraftVersion.parse("25.2.1");

        assertSame(VersionFamily.DROP, version.getFamily());
        assertEquals("25.2.1", version.getVersion());
        assertEquals("drop 25.2.1", version.toString());
    }

    @Test
    void omitsAZeroPatchOnReleasesButKeepsItOnAlpha() {
        assertEquals("1.16", MinecraftVersion.parse("1.16.0").getVersion());
        assertEquals("a1.1.0", MinecraftVersion.parse("a1.1.0").getVersion());
    }

    @Test
    void parsesBuildSuffixesAndQualifiers() {
        assertEquals("a1.0.17_02", MinecraftVersion.parse("a1.0.17_02").getVersion());
        assertEquals("a1.2.2a", MinecraftVersion.parse("a1.2.2a").getVersion());
    }

    @Test
    void acceptsEveryDocumentedPhaseSpelling() {
        MinecraftVersion prefixLetter = MinecraftVersion.parse("b1.7.3");
        MinecraftVersion prefixWord = MinecraftVersion.parse("Beta 1.7.3");
        MinecraftVersion suffixWord = MinecraftVersion.parse("1.7.3-beta");

        assertSame(VersionPhase.BETA, prefixLetter.getPhase());
        assertSame(VersionPhase.BETA, prefixWord.getPhase());
        assertSame(VersionPhase.BETA, suffixWord.getPhase());

        assertEquals("classic beta b1.7.3", prefixLetter.toString());
        assertTrue(prefixLetter.isPreRelease());
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertEquals("1.21.4", MinecraftVersion.parse("  1.21.4  ").getVersion());
    }

    @Test
    void rejectsMalformedIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> MinecraftVersion.parse("1.2.3.4"));
        assertThrows(IllegalArgumentException.class, () -> MinecraftVersion.parse("potato"));
        assertThrows(IllegalArgumentException.class, () -> MinecraftVersion.parse("2.0"));
        assertThrows(NullPointerException.class, () -> MinecraftVersion.parse(null));
    }

    @Test
    void rejectsPreReleasesOutsideTheOneDotXShape() {
        assertThrows(IllegalArgumentException.class, () -> MinecraftVersion.parse("b25.1"));
    }

    @Test
    void identifierCheckMatchesTheParser() {
        assertTrue(MinecraftVersion.isIdentifier("1.16.5"));
        assertTrue(MinecraftVersion.isIdentifier(" b1.7.3 "));
        assertFalse(MinecraftVersion.isIdentifier("1.2.3.4"));
        assertFalse(MinecraftVersion.isIdentifier(null));
        assertFalse(MinecraftVersion.isIdentifier("26.2-snapshot-3"));
    }

    @Test
    void hexSupportStartsAtOneDotSixteenAndTwentyDotOne() {
        assertFalse(MinecraftVersion.parse("1.15.2").supportsHex());
        assertTrue(MinecraftVersion.parse("1.16").supportsHex());
        assertTrue(MinecraftVersion.parse("1.21.4").supportsHex());

        assertFalse(MinecraftVersion.parse("19.4").supportsHex());
        assertTrue(MinecraftVersion.parse("20.1").supportsHex());
        assertTrue(MinecraftVersion.parse("25.2").supportsHex());
    }

    @Test
    void preReleasesNeverSupportHex() {
        assertFalse(MinecraftVersion.parse("b1.7.3").supportsHex());
        assertFalse(MinecraftVersion.parse("a1.2.6").supportsHex());
    }

    @Test
    void resolvesProtocolsForKnownReleases() {
        assertEquals(754, MinecraftVersion.parse("1.16.5").getProtocol());
        assertEquals(754, MinecraftVersion.protocolForIdentifier("1.16.5"));
    }

    @Test
    void leavesUnpublishedReleasesWithoutAProtocol() {
        assertNull(MinecraftVersion.protocolForIdentifier("1.99.9"));
    }

    @Test
    void resolvesTheNewestVersionSharingAProtocol() {
        MinecraftVersion resolved = MinecraftVersion.fromProtocol(754);

        assertNotNull(resolved);
        assertEquals("1.16.5", resolved.getVersion());
    }

    @Test
    void listsEveryVersionSharingAProtocolInOrder() {
        List<MinecraftVersion> versions = MinecraftVersion.versionsForProtocol(754);

        assertFalse(versions.isEmpty());
        assertEquals("1.16.5", versions.get(versions.size() - 1).getVersion());
        assertTrue(versions.stream().noneMatch(MinecraftVersion::isPreRelease));
    }

    @Test
    void returnsAnEmptyListForUnknownProtocols() {
        assertTrue(MinecraftVersion.versionsForProtocol(-42).isEmpty());
        assertNull(MinecraftVersion.fromProtocol(-42));
    }
}
