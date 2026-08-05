# VNC (Version Numbering Converter)

Version Numbering Converter (VNC) is a Java 8 library for working with Minecraft versions across both numbering families:

- Classic Java-style versions such as `1.20.6` and `1.21.11`
- Year-based drop versions such as `24.1`, `25.4`, and `26.1.1`

Alpha and Beta identifiers such as `a1.2.6` and `b1.7.3` are supported as a separate `VersionPhase`, so `b1.2` and `1.2.1` never collide. The pre-classic, Classic, Indev, and Infdev eras are not modelled: their identifiers are date or build based and do not fit the numeric model.

The project is split so the common API can be used from any Java project without Bukkit on the classpath. Platform-specific runtime helpers live in separate modules and are selected through the bootstrap provider.

## Modules

| Module | Artifact | Purpose |
| --- | --- | --- |
| `bootstrap` | `me.croabeast.vnc:VNC` | Final aggregate jar with `VNC`, `core`, and the provider implementations. |
| `core` | `me.croabeast.vnc:core` | Common parser, provider contract, mapping tables, conversion schemes, protocol lookup, and generic runtime snapshots. |
| `bukkit` | `me.croabeast.vnc:bukkit` | Internal Bukkit/Paper provider and ViaVersion-backed player protocol lookup. |
| `mod:fabric` | `me.croabeast.vnc:fabric` | Fabric loader runtime version bridge, including the Legacy Fabric, Ornithe, and Babric distributions. |
| `mod:quilt` | `me.croabeast.vnc:quilt` | Quilt loader runtime version bridge, including Ornithe on Quilt. |
| `mod:forge` | `me.croabeast.vnc:forge` | Forge runtime version bridge. |
| `mod:neoforge` | `me.croabeast.vnc:neoforge` | NeoForge runtime version bridge compiled as Java 8 bytecode. |
| `mod:sponge` | `me.croabeast.vnc:sponge` | Sponge runtime version bridge. |
| `mod:liteloader` | `me.croabeast.vnc:liteloader` | Legacy LiteLoader bridge. |
| `mod:rift` | `me.croabeast.vnc:rift` | Rift bridge for the `1.13` line. |
| `mod:modloader` | `me.croabeast.vnc:modloader` | Risugami's ModLoader bridge. |
| `agent:nilloader` | `me.croabeast.vnc:nilloader` | NilLoader bridge. |
| `agent:java` | `me.croabeast.vnc:javaagent` | Generic Java agent bridge used as the last detection fallback. |
| `proxy:bungee` | `me.croabeast.vnc:bungee` | BungeeCord proxy version bridge. |
| `proxy:velocity` | `me.croabeast.vnc:velocity` | Velocity proxy bridge for a supplied backend/support Minecraft version. |

## Core API

```java
MinecraftVersion classic = MinecraftVersion.parse("1.21.11");
MinecraftVersion drop = MinecraftVersion.parse("26.1");

classic.getVersion();         // "1.21.11"
classic.getProtocol();        // 774
classic.supportsHex();        // true

drop.getVersion();            // "26.1"
drop.getProtocol();           // 775
```

```java
MinecraftVersion beta = MinecraftVersion.parse("b1.7.3");

beta.getVersion();            // "b1.7.3"
beta.getPhase();              // VersionPhase.BETA
beta.getFamily();             // VersionFamily.CLASSIC
beta.getProtocol();           // 14
beta.isPreRelease();          // true
```

The phase may be written as a prefix letter (`b1.7.3`), a prefix word (`Beta 1.7.3`), or a suffix word (`1.7.3-beta`). The `_NN` build suffix (`a1.0.17_02`) and the trailing letter qualifier (`a1.2.2a`) are preserved, so `getVersion()` reproduces the original identifier.

Alpha and Beta take slot `0` of their release year in drop numbering, indexed chronologically. That slot was never used by a release, so every existing release mapping stays exactly as it was:

```java
VersionScheme.MOJANG.toDrop("a1.2.6");    // "10.0.24"
VersionScheme.MOJANG.toDrop("b1.7.3");    // "11.0.18"
VersionScheme.MOJANG.toDrop("1.0.0");     // "11.1"
```

The phase is the primary comparison key, so every Alpha precedes every Beta and both precede any release. Alpha and Beta have no legacy `double` shape, so `toLegacyServerVersion` returns `-1` for them. They are also absent from the reverse protocol index: their protocol numbers are small integers that overlap the early release line, which would make `fromProtocol` ambiguous.

```java
String dropName = VersionScheme.MOJANG.toDrop("1.20.3");       // "23.2"
String classicName = VersionScheme.MOJANG.toClassic("25.2.2"); // "1.21.8"

boolean modernRegistry = Versioning.isAtLeast(MinecraftVersion.parse("1.20.6"), "1.20.5");
int comparison = VNC.compare(MinecraftVersion.parse("26.1"), "1.21.11");
```

## Platform Runtime API

Use `bootstrap` when you want `VNC` to detect the runtime provider:

```java
VNCProvider provider = VNC.getProvider();

String platform = provider.getPlatform();
String classic = provider.getClassicVersion();
String drop = provider.getDropVersion();
int protocol = provider.getProtocol();
boolean modernRegistry = provider.isAtLeast("1.20.5");
```

`VNC` still exposes convenience methods for the detected provider:

```java
MinecraftVersion server = VNC.SERVER_MINECRAFT_VERSION;
String rawVersion = VNC.SERVER_RAW_VERSION;
double legacy = VNC.SERVER_VERSION;
boolean modernRegistry = VNC.isAtLeast("1.20.5");
```

When the runtime may not have a supported provider, use the nullable accessor:

```java
VNCProvider provider = VNC.getProviderOrNull();
if (provider != null && provider.isAtLeast("1.20.5")) {
    // use modern behavior
}
```

Platform modules contain package-private provider implementations. Consumers should depend on `bootstrap` for runtime detection instead of calling loader-specific classes directly.

Legacy Fabric, Ornithe, and Babric all expose the same `FabricLoader` API, so they are reported as flavors of the Fabric provider rather than as separate modules. `getPlatform()` returns `Fabric`, `Legacy Fabric`, `Ornithe`, `Babric`, or `Babric (BTA)`; on the Quilt provider it returns `Quilt` or `Ornithe`. The flavor is resolved from the loaded mod set (`osl`, `legacy-fabric-api-base`) and, for Babric, from the shape of the reported game version.

BTA reports its own build number, which is not a Minecraft version: `1.7.6.2` and `1.7.7.0` under its old scheme, `7.2_01` and `7.3_04` under the current one. Every BTA build runs on Beta 1.7.3, so the provider resolves them all to `b1.7.3`. Babric reporting the bare `1.7.3` is read the same way, since `1.7.3` was never a release id.

Loaders under `agent`, plus Rift and Risugami's ModLoader, do not expose the Minecraft version themselves. Their providers read it through `GameVersionLookup`, which checks the `minecraft.version` and `fml.mcVersion` system properties, the `version.json` descriptor bundled since the `1.14` line, and the `--version` launch argument.

Velocity does not expose one global backend Minecraft version. Its provider is still detected from the Velocity API, but version methods need a `minecraft.version` system property.

## Requirements

- Java 8 bytecode for every module
- Gradle 9.5.0 wrapper for local builds
- Optional platform APIs are `compileOnly`; platform modules use direct APIs and guard runtime detection with `Throwable`/linkage protection
- The NeoForge module targets NeoForge `21.1.230`, compiles Maven-style with `source/target 1.8`, and only resolves at runtime when Java `21+` and NeoForge are present

## Build

```bash
./gradlew clean build
```

The final consumer jar is generated by the `bootstrap` module:

```text
bootstrap/build/libs/VNC-${version}.jar
```

Maven repository publishing is handled by `.github/workflows/publish.yml`; GitHub Releases are published by `.github/workflows/release.yml`.
