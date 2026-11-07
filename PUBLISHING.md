# CurseForge project copy

Use two separate Minecraft **Mods** projects, both with a unique, original icon. Screenshots are optional for a mod and will be supplied later. Select **Minecraft 1.21.1** and **NeoForge** for both files. Mark initial files **Release** so the default CurseForge app install can find them; the version and description still identify this as a community beta.

## 1. Atlantis Development API — Unofficial NeoForge Port

**Suggested slug:** `atlantis-development-api-neoforge-community-port`  
**Summary:** Unofficial NeoForge 1.21.1 port of the Atlantis Development API for Pixelmon sidemods.  
**Category:** API and Library  
**License:** MIT (the original also permits LGPL-3.0; the full original notice is included)  
**Source / issues:** https://github.com/Tayranor/pixelmon-neoforge-community-ports  
**File:** `atlantis-9.0.0-local.2-all.jar`  

### Description

This is an **unofficial community port** of [Atlantis Development API](https://www.curseforge.com/minecraft/mc-mods/atlantis-api) by Waterdude Studios Ltd for Minecraft 1.21.1 and NeoForge 21.1.x. It provides the Atlantis utility library needed by the companion GameShark-inspired radar. It is not the unrelated Atlantis ocean-dimension mod.

Install the `-all.jar` file. The plain JAR is not a distributable build. This port was built with NeoForge 21.1.265 and Pixelmon 9.4.2. Pixelmon itself is not bundled. Source and original license notices are available on [GitHub](https://github.com/Tayranor/pixelmon-neoforge-community-ports).

The original Atlantis project is copyright Waterdude Studios Ltd and dual licensed under MIT/LGPL-3.0. This port is not endorsed by the original authors or Pixelmon. Community issues and pull requests are welcome. Maintenance and future-version support are best effort, with no promised response time.

### File changelog

Initial unofficial NeoForge 1.21.1 build. Adapted Atlantis build and APIs for NeoForge; preserved original notices. Tested as a dependency of the companion radar in a Pixelmon 9.4.2 single-player instance.

## 2. GameShark-inspired Pixelmon Radar

**Suggested slug:** `gameshark-inspired-pixelmon-radar`  
**Summary:** Independent local Pixelmon radar for NeoForge 1.21.1 with category HUD and through-wall target markers.  
**Category:** Addons / Map and Information (choose the closest available)  
**License:** MIT  
**Source / issues:** https://github.com/Tayranor/pixelmon-neoforge-community-ports  
**File:** `gameshark-local-2.0.0-local.8.jar`  
**Required dependency:** Atlantis Development API — Unofficial NeoForge Port  

### Description

An **independently written, unofficial** Pixelmon radar inspired by the older [GameShark](https://www.curseforge.com/minecraft/mc-mods/pixelmon-gameshark). This is not a release from the original GameShark developer, does not use its code or graphics, and does not promise full feature parity.

For Minecraft 1.21.1, NeoForge 21.1.x, and Pixelmon 9.4.2. Install the companion Atlantis Development API port alongside this JAR. Pixelmon is required and not included.

The configurable HUD shows enabled target categories, nearby target details and direction. Colored corner markers highlight detected targets, including through terrain. The radar tracks Pokémon categories, custom Pokémon selections, loot, chests, trainers, and other local targets. Search for a Pokémon by English or current game-language name in the in-game settings. Controls are under GameShark in Minecraft's key bindings; HUD position, size and scan range are configurable.

The radar scans only entities and block entities in **loaded chunks**. It was designed and tested for single-player use. Multiplayer permissions, original server-side configuration and commands are not implemented. Some detection categories are best effort. See [GitHub](https://github.com/Tayranor/pixelmon-neoforge-community-ports) for source, install notes, known limits, and issues.

Community fixes are welcome. Maintenance and future-version support are best effort, with no promised response time.

### File changelog

Initial public beta of the independent NeoForge radar. Includes category HUD, Pokémon picker, configurable HUD size and position, key bindings, and through-wall target corner markers.

## Release note / community post

### Reddit draft — r/PixelmonMod

**Title:** Unofficial Atlantis API port and an independent GameShark-inspired radar for Pixelmon 9.4.2 / NeoForge 1.21.1

I ported Atlantis Development API to NeoForge 1.21.1 and built a separate, independently written Pixelmon radar inspired by the old GameShark. The radar has a compact category HUD, custom Pokémon search, adjustable size and position, and colored markers for detected targets through terrain.

These are unofficial community builds for Pixelmon 9.4.2. They are built and tested for single-player; multiplayer support is not validated. The radar scans loaded chunks only and does not reproduce every feature of the original GameShark. Original authors are credited and Atlantis's license notices are retained.

Source and downloads: https://github.com/Tayranor/pixelmon-neoforge-community-ports

Issues and PRs are welcome. I do not plan a fixed support schedule, so future compatibility will depend on community contributions. I'd be glad to hear what needs fixing first.

### Discord draft — only after server staff approves a suitable channel

I made an unofficial Atlantis Development API port and an independent GameShark-inspired Pixelmon radar for 1.21.1 NeoForge / Pixelmon 9.4.2. Both are open source and currently aimed at single-player. Is there an appropriate place to share the GitHub source and builds? I read the server's advertising and #addon-downloads rules and don't want to post in the wrong channel.
