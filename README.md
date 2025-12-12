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

## Conversion map

Every classic release from `1.0` through `1.21.11` is listed below alongside its Mojang drop name and the CROA custom aliases. For classics earlier than `1.21`, any patch segment simply carries over to the drop name (for example, `1.19.4` maps to `22.1.4`). Starting with `1.21`, the mapping follows Mojang's multi-drop rollout, and CROA_CUSTOM begins treating `1.21.11` as the birth of the `1.22.x` line.

| Classic version | Drop (MOJANG) | Drop (CROA_CUSTOM) | Classic (CROA_CUSTOM) |
| --- | --- | --- | --- |
| 1.0.0 | 11.1 | 11.1 | 1.0.0 |
| 1.1 | 12.1 | 12.1 | 1.1 |
| 1.2.1 | 12.2.1 | 12.2.1 | 1.2.1 |
| 1.2.2 | 12.2.2 | 12.2.2 | 1.2.2 |
| 1.2.3 | 12.2.3 | 12.2.3 | 1.2.3 |
| 1.2.4 | 12.2.4 | 12.2.4 | 1.2.4 |
| 1.2.5 | 12.2.5 | 12.2.5 | 1.2.5 |
| 1.3.1 | 12.3.1 | 12.3.1 | 1.3.1 |
| 1.3.2 | 12.3.2 | 12.3.2 | 1.3.2 |
| 1.4.2 | 12.4.2 | 12.4.2 | 1.4.2 |
| 1.4.4 | 12.4.4 | 12.4.4 | 1.4.4 |
| 1.4.5 | 12.4.5 | 12.4.5 | 1.4.5 |
| 1.4.6 | 12.4.6 | 12.4.6 | 1.4.6 |
| 1.4.7 | 12.4.7 | 12.4.7 | 1.4.7 |
| 1.5 | 13.1 | 13.1 | 1.5 |
| 1.5.1 | 13.1.1 | 13.1.1 | 1.5.1 |
| 1.5.2 | 13.1.2 | 13.1.2 | 1.5.2 |
| 1.6.1 | 13.2.1 | 13.2.1 | 1.6.1 |
| 1.6.2 | 13.2.2 | 13.2.2 | 1.6.2 |
| 1.6.4 | 13.2.4 | 13.2.4 | 1.6.4 |
| 1.7.2 | 13.3.2 | 13.3.2 | 1.7.2 |
| 1.7.4 | 13.3.4 | 13.3.4 | 1.7.4 |
| 1.7.5 | 13.3.5 | 13.3.5 | 1.7.5 |
| 1.7.6 | 13.3.6 | 13.3.6 | 1.7.6 |
| 1.7.7 | 13.3.7 | 13.3.7 | 1.7.7 |
| 1.7.8 | 13.3.8 | 13.3.8 | 1.7.8 |
| 1.7.9 | 13.3.9 | 13.3.9 | 1.7.9 |
| 1.7.10 | 13.3.10 | 13.3.10 | 1.7.10 |
| 1.8 | 14.1 | 14.1 | 1.8 |
| 1.8.1 | 14.1.1 | 14.1.1 | 1.8.1 |
| 1.8.2 | 14.1.2 | 14.1.2 | 1.8.2 |
| 1.8.3 | 14.1.3 | 14.1.3 | 1.8.3 |
| 1.8.4 | 14.1.4 | 14.1.4 | 1.8.4 |
| 1.8.5 | 14.1.5 | 14.1.5 | 1.8.5 |
| 1.8.6 | 14.1.6 | 14.1.6 | 1.8.6 |
| 1.8.7 | 14.1.7 | 14.1.7 | 1.8.7 |
| 1.8.8 | 14.1.8 | 14.1.8 | 1.8.8 |
| 1.8.9 | 14.1.9 | 14.1.9 | 1.8.9 |
| 1.9 | 16.1 | 16.1 | 1.9 |
| 1.9.1 | 16.1.1 | 16.1.1 | 1.9.1 |
| 1.9.2 | 16.1.2 | 16.1.2 | 1.9.2 |
| 1.9.3 | 16.1.3 | 16.1.3 | 1.9.3 |
| 1.9.4 | 16.1.4 | 16.1.4 | 1.9.4 |
| 1.10 | 16.2 | 16.2 | 1.10 |
| 1.10.1 | 16.2.1 | 16.2.1 | 1.10.1 |
| 1.10.2 | 16.2.2 | 16.2.2 | 1.10.2 |
| 1.11 | 16.3 | 16.3 | 1.11 |
| 1.11.1 | 16.3.1 | 16.3.1 | 1.11.1 |
| 1.11.2 | 16.3.2 | 16.3.2 | 1.11.2 |
| 1.12 | 17.1 | 17.1 | 1.12 |
| 1.12.1 | 17.1.1 | 17.1.1 | 1.12.1 |
| 1.12.2 | 17.1.2 | 17.1.2 | 1.12.2 |
| 1.13 | 18.1 | 18.1 | 1.13 |
| 1.13.1 | 18.1.1 | 18.1.1 | 1.13.1 |
| 1.13.2 | 18.1.2 | 18.1.2 | 1.13.2 |
| 1.14 | 19.1 | 19.1 | 1.14 |
| 1.14.1 | 19.1.1 | 19.1.1 | 1.14.1 |
| 1.14.2 | 19.1.2 | 19.1.2 | 1.14.2 |
| 1.14.3 | 19.1.3 | 19.1.3 | 1.14.3 |
| 1.14.4 | 19.1.4 | 19.1.4 | 1.14.4 |
| 1.15 | 19.2 | 19.2 | 1.15 |
| 1.15.1 | 19.2.1 | 19.2.1 | 1.15.1 |
| 1.15.2 | 19.2.2 | 19.2.2 | 1.15.2 |
| 1.16 | 20.1 | 20.1 | 1.16 |
| 1.16.1 | 20.1.1 | 20.1.1 | 1.16.1 |
| 1.16.2 | 20.1.2 | 20.1.2 | 1.16.2 |
| 1.16.3 | 20.1.3 | 20.1.3 | 1.16.3 |
| 1.16.4 | 20.1.4 | 20.1.4 | 1.16.4 |
| 1.16.5 | 20.1.5 | 20.1.5 | 1.16.5 |
| 1.17 | 21.1 | 21.1 | 1.17 |
| 1.17.1 | 21.1.1 | 21.1.1 | 1.17.1 |
| 1.18 | 21.2 | 21.2 | 1.18 |
| 1.18.1 | 21.2.1 | 21.2.1 | 1.18.1 |
| 1.18.2 | 21.2.2 | 21.2.2 | 1.18.2 |
| 1.19 | 22.1 | 22.1 | 1.19 |
| 1.19.1 | 22.1.1 | 22.1.1 | 1.19.1 |
| 1.19.2 | 22.1.2 | 22.1.2 | 1.19.2 |
| 1.19.3 | 22.1.3 | 22.1.3 | 1.19.3 |
| 1.19.4 | 22.1.4 | 22.1.4 | 1.19.4 |
| 1.20 | 23.1 | 23.1 | 1.20 |
| 1.20.1 | 23.1.1 | 23.1.1 | 1.20.1 |
| 1.20.2 | 23.1.2 | 23.1.2 | 1.20.2 |
| 1.20.3 | 23.1.3 | 23.1.3 | 1.20.3 |
| 1.20.4 | 23.1.4 | 23.1.4 | 1.20.4 |
| 1.20.5 | 23.1.5 | 23.1.5 | 1.20.5 |
| 1.20.6 | 23.1.6 | 23.1.6 | 1.20.6 |
| 1.21 | 24.1 | 24.1 | 1.21 |
| 1.21.1 | 24.1.1 | 24.1.1 | 1.21.1 |
| 1.21.2 | 24.1.2 | 24.1.2 | 1.21.2 |
| 1.21.3 | 24.1.3 | 24.1.3 | 1.21.3 |
| 1.21.4 | 24.1.4 | 24.1.4 | 1.21.4 |
| 1.21.5 | 25.1 | 25.1 | 1.21.5 |
| 1.21.6 | 25.2 | 25.2 | 1.21.6 |
| 1.21.7 | 25.2.1 | 25.2.1 | 1.21.7 |
| 1.21.8 | 25.2.2 | 25.2.2 | 1.21.8 |
| 1.21.9 | 25.3 | 25.3 | 1.21.9 |
| 1.21.10 | 25.3.1 | 25.3.1 | 1.21.10 |
| 1.21.11 | 25.4 | 25.4 | 1.22 |

## Getting started

This repository uses Gradle. To build the project, run:

```bash
./gradlew build
```

You can then reference the compiled artifacts in your own projects, or copy the classes directly for lightweight use.

## Contributing

Issues and pull requests are welcome! If you spot an unmapped version or have a better story to tell with the custom scheme, open a discussion so we can keep the version lore accurate and fun.
