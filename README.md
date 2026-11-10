# Pixelmon NeoForge community ports

Unofficial community builds for **Minecraft 1.21.1**, **NeoForge 21.1.x**, and **Pixelmon 9.4.2**.

| Project | Build | What it is |
| --- | --- | --- |
| [Atlantis Development API](atlantis/) | `9.0.0-local.2` | NeoForge port of the open-source Atlantis Development library by Waterdude Studios Ltd. |
| [SharkScope Radar](gameshark/) | `2.1.1` | Independent Pixelmon radar with server opt-in, inspired by the older GameShark. |

These are **not official releases** from Pixelmon or the original GameShark and Atlantis Development authors. The radar does not contain code or graphics from the original all-rights-reserved GameShark. Atlantis retains its original copyright and dual MIT/LGPL-3.0 license; see [its license](atlantis/LICENSE-Atlantis.txt). The new radar code is MIT-licensed; see [its license](gameshark/LICENSE).

## Install

1. Install Minecraft 1.21.1, NeoForge 21.1.x (tested with 21.1.265), and Pixelmon 9.4.2.
2. Download both JARs from CurseForge: [Atlantis Development Community Port](https://www.curseforge.com/minecraft/mc-mods/atlantis-development-community-port) and [SharkScope Radar](https://www.curseforge.com/minecraft/mc-mods/sharkscope-radar). Use `sharkscope-2.1.1.jar` instead of an older radar JAR.
3. Put both in the instance's `mods` folder. Remove older builds of either mod before launching.
4. The radar's key bindings appear under **SharkScope** in Minecraft's Controls menu. Open its settings in-game to choose categories, tracked Pokémon, range, HUD position, and HUD size.

The radar currently declares Atlantis as a required dependency. Pixelmon is required but is **not redistributed** here.

## Scope and limits

- Single-player is allowed. On multiplayer, the radar requires SharkScope on the server; servers without it leave the client radar disabled. Server owners can deny permission with `config/sharkscope-server.properties` containing `enabled=false`. Multiplayer behavior still needs an in-game validation pass.
- The radar scans entities and block entities in **loaded chunks** within its configured radius. Distant Horizons' distant terrain does not extend that scan.
- It tracks 18 categories, including legendary, shiny, boss, Ultra Beast, NPC, quest giver, loot, chest, trainer, and custom Pokémon targets. Custom quest-giver NPCs remain best effort.
- The HUD has a compact category strip, nearest-target details, a direction indicator, and colored corner markers for all enabled targets. Markers draw through terrain.
- The in-game Pokémon picker supports English and currently translated names, with up to five suggestions.
- This is an independent rewrite, **not feature parity** with the original GameShark. The server opt-in is an honest-client restriction, not an anti-cheat.

## Build from source

Use Java 21. Download Pixelmon 9.4.2 yourself and put `Pixelmon-1.21.1-9.4.2-universal.jar` in both `atlantis/libs/` and `gameshark/libs/`. The JAR is intentionally excluded from Git.

```text
cd atlantis
./gradlew build shadowJar

cd ../gameshark
./gradlew build
```

On Windows, use `gradlew.bat`. The distributable Atlantis file is the `-all.jar` build.

## Community maintenance

Issues and pull requests are welcome. Please include the Pixelmon, NeoForge, and mod versions plus logs and reproduction steps for bugs. Maintenance is **best effort**: there is no promise of ongoing updates, support response times, or compatibility with future Pixelmon versions. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Credits

- [Atlantis Development](https://gitlab.com/atlantis-development/atlantis) and Waterdude Studios Ltd for the original Atlantis library.
- [Original GameShark project](https://www.curseforge.com/minecraft/mc-mods/pixelmon-gameshark) by Waterdude for the concept.
- Pixelmon and Minecraft remain the property of their respective owners. Neither is included in this repository or its releases.
