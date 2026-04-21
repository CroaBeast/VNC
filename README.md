# VNC (Version Numbering Converter)

Version Numbering Converter (VNC) is a Java library for working with Minecraft versions across both numbering families:

- Classic Java-style versions such as `1.20.6` and `1.21.11`
- Year-based drop versions such as `24.1`, `25.4`, and `26.1.1`

It covers two use cases:

- Pure version modeling and conversion through `MinecraftVersion`, `VersionScheme`, and `MappingTable`
- Bukkit/Paper runtime detection through `VNC`, including server constants, player version resolution, protocol lookup, and version comparisons

## Highlights

- Exact historic mappings from `1.0.0` through `1.21.11`
- Drop support for current lines such as `25.4`, `26.1`, and `26.1.1`
- Protocol lookup for exact releases and published snapshots
- A runtime bridge for Bukkit/Paper with legacy-compatible constants like `SERVER_VERSION`
- String-based comparisons that correctly handle patch-sensitive boundaries like `1.20.5`

## Core API

### 1. Parse and inspect versions

```java
MinecraftVersion classic = MinecraftVersion.parse("1.21.11");
MinecraftVersion drop = MinecraftVersion.parse("26.1");

classic.isClassic();          // true
classic.getVersion();         // "1.21.11"
classic.getProtocol();        // 774
classic.supportsHex();        // true

drop.isClassic();             // false
drop.getVersion();            // "26.1"
drop.getProtocol();           // 775
```

### 2. Convert between numbering schemes

```java
String dropName = VersionScheme.MOJANG.toDrop("1.20.3");      // "23.2"
String classicName = VersionScheme.MOJANG.toClassic("25.2.2"); // "1.21.8"

String customClassic = VersionScheme.CROA_CUSTOM.toClassic("25.4"); // "1.22"
String customDrop = VersionScheme.CROA_CUSTOM.toDrop("1.22.1");     // "25.4.1"
```

### 3. Create your own mapping scheme

```java
MappingTable table = new MappingTable()
        .registerLine(30, 1, "1.50", "1.50.1")
        .registerMapping("1.51", "30.2");

VersionScheme scheme = VersionScheme.mapped(table);

scheme.toDrop("1.50.1");   // "30.1.1"
scheme.toClassic("30.2");  // "1.51"
```

### 4. Resolve protocols

```java
Integer protocol = MinecraftVersion.protocolForIdentifier("1.20.6");      // 766
Integer snapshot = MinecraftVersion.protocolForIdentifier("26.2-snapshot-3");

MinecraftVersion newest = MinecraftVersion.fromProtocol(754);              // 1.16.5
List<MinecraftVersion> all = MinecraftVersion.versionsForProtocol(767);    // 1.21, 1.21.1
```

## Bukkit / Paper runtime API

`VNC` exposes a runtime snapshot of the current server:

```java
MinecraftVersion server = VNC.SERVER_MINECRAFT_VERSION;
String classic = VNC.SERVER_CLASSIC_VERSION;
String drop = VNC.SERVER_DROP_VERSION;
int protocol = VNC.SERVER_PROTOCOL;
double legacy = VNC.SERVER_VERSION;

boolean modernRegistry = VNC.isAtLeast("1.20.5");
boolean legacyCommands = VNC.isBefore("1.13");
boolean inRange = VNC.isBetween("1.19", "1.21.11");
```

Player version resolution uses ViaVersion when present and falls back to the server version otherwise:

```java
MinecraftVersion playerVersion = VNC.player(player);

if (playerVersion.supportsHex()) {
    // Safe to send RGB formatting to this player
}
```

## Why use the string helpers?

`SERVER_VERSION` is still available for compatibility with older plugins, but patch-sensitive checks should prefer:

```java
VNC.isAtLeast("1.20.5");
VNC.isBefore("1.21.9");
VNC.compare(VNC.SERVER_MINECRAFT_VERSION, "26.1");
```

That avoids the common limitations of comparing versions only as `double`.

## Requirements

- Java 8+
- For the Bukkit runtime helpers: a Bukkit/Paper-compatible runtime on the classpath
- Optional: ViaVersion, if you want `VNC.player(...)` to resolve the effective client version instead of the server version

## Coordinates

If you publish the artifact to your own repository or consume it from a local build, the coordinates are:

```text
groupId:    me.croabeast
artifactId: VNC
version:    1.1.0
```

## Build

```bash
./gradlew jar
```
