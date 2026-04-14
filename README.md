# Version Numbering Converter

Version Numbering Converter (VNC) is a small Java library for translating between Minecraft's classic Java release numbers and the newer year-based drop notation.

The project aims to keep those conversions historically accurate by grouping versions by their real release line instead of assuming that every `1.x.*` branch belongs to the same drop. That matters for releases such as `1.20.3`, `1.21.2`, and `1.21.4`, which are separate drops even though they share nearby classic version numbers.

## Features

- Exact historic mappings for Java full releases from `1.0.0` through `1.21.11`
- Mojang-aligned drop numbering via `VersionScheme.MOJANG`
- Custom aliasing via `VersionScheme.CROA_CUSTOM`, where `1.21.11` is treated as `1.22`
- User-defined schemes via `MappingTable` and `VersionScheme.mapped(...)`
- Parsing support for both text input and pre-built `MinecraftVersion` objects

## Examples

```java
import me.croabeast.vnc.MappingTable;
import me.croabeast.vnc.MinecraftVersion;
import me.croabeast.vnc.VersionScheme;

// Historic mappings are grouped by the actual release line.
String batsAndPots = VersionScheme.MOJANG.toDrop("1.20.3");          // -> "23.2"
String bundles = VersionScheme.MOJANG.toDrop("1.21.2");              // -> "24.3"

// Reverse conversion preserves the exact classic release.
String classic = VersionScheme.MOJANG.toClassic("25.2.2");           // -> "1.21.8"

// The custom scheme treats 1.21.11 as a custom 1.22 milestone.
String customClassic = VersionScheme.CROA_CUSTOM.toClassic("25.4");  // -> "1.22"
String customDrop = VersionScheme.CROA_CUSTOM.toDrop("1.22");        // -> "25.4"
String customHotfixDrop = VersionScheme.CROA_CUSTOM.toDrop("1.22.1");// -> "25.4.1"

// Because 1.22 is already used, the projected 26.1 line becomes 1.23.
String projectedClassic = VersionScheme.CROA_CUSTOM.toClassic("26.1"); // -> "1.23"

// You can also define your own exact scheme.
VersionScheme customScheme = VersionScheme.mapped(
        new MappingTable().registerLine(30, 1, "1.50", "1.50.1")
);

// You can also work with parsed objects.
MinecraftVersion parsed = MinecraftVersion.parse("1.19.4");
String dropName = VersionScheme.MOJANG.toDrop(parsed);               // -> "22.1.4"
Integer protocol = parsed.getProtocol();                             // -> 762

// Projected aliases stay scheme-agnostic, so they do not guess protocol numbers.
Integer unknownProtocol = MinecraftVersion.parse("1.22").getProtocol(); // -> null
```

## Build

```bash
./gradlew build
```
