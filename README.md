# Version Numbering Converter

Version Numbering Converter (VNC) is a tiny Java helper for retelling Minecraft's version history in two languages: the familiar classic `1.x.y` numbering and the newer year-based "drop" notation. It exists so tooling, plugins, and curious players can effortlessly jump between both dialects without manually tracking Mojang's ever-growing mapping table.

## Why this project exists

Minecraft's release story is split between eras. Early versions use the classic three-part semantic sequence, while modern releases are announced as yearly drops (for example, `24.1`). Keeping those eras in sync usually means hunting through wikis or hard-coding brittle tables. VNC captures that lore in reusable code so you can:

- Convert classic versions to their official drop names (and back) with one call.
- Experiment with custom aliasing rules for hypothetical future releases.
- Validate incoming version strings before your tooling relies on them.

## Features at a glance

- **Drop ↔ Classic conversion:** Swap between the two naming schemes using `VersionScheme.MOJANG` for official mappings or `VersionScheme.CROA_CUSTOM` for playful aliases.
- **Immutable version model:** `MinecraftVersion` stores the parsed components and makes it clear whether a version belongs to the classic or drop timeline.
- **String and object APIs:** Convert plain strings or pre-parsed `MinecraftVersion` instances—whichever fits your workflow.

## Examples

```java
import me.croabeast.vnc.MinecraftVersion;
import me.croabeast.vnc.VersionScheme;

// Translate a classic release into its official drop name
String trailsTalesDrop = VersionScheme.MOJANG.toDrop("1.20.1");      // -> "23.1"

// Bring a drop release back to classic notation
String cavesCliffsClassic = VersionScheme.MOJANG.toClassic("21.2.0"); // -> "1.18"

// Dream up a future alias with the custom scheme
String speculativeClassic = VersionScheme.CROA_CUSTOM.toClassic("26.1"); // -> "1.22"

// Work with parsed objects when you need stricter control
MinecraftVersion parsed = MinecraftVersion.parse("1.19.4");
String dropName = VersionScheme.MOJANG.toDrop(parsed); // "22.1.4"
```

## Getting started

This repository uses Gradle. To build the project, run:

```bash
./gradlew build
```

You can then reference the compiled artifacts in your own projects, or copy the classes directly for lightweight use.

## Contributing

Issues and pull requests are welcome! If you spot an unmapped version or have a better story to tell with the custom scheme, open a discussion so we can keep the version lore accurate and fun.
