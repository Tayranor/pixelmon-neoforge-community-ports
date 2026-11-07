# GameShark-inspired local radar — independent community rewrite

This is an unofficial, newly written Pixelmon radar for Minecraft 1.21.1 / NeoForge 21.1.x / Pixelmon 9.4.2. It is inspired by the search workflow of [Waterdude's original GameShark](https://www.curseforge.com/minecraft/mc-mods/pixelmon-gameshark), but includes none of that project's source code or graphics and is not endorsed by its author.

The radar finds targets in loaded chunks up to the configured radius and shows all enabled categories as colored corner markers through terrain. Its HUD can be positioned and scaled. The Pokémon picker searches English and translated names and shows up to five suggestions. The existing client-side settings are saved to `config/gameshark-local.properties`.

This implementation is intended for single-player use. Server permissions, administrator commands, and full feature parity with the original GameShark are not implemented. Quest giver detection is heuristic. Maintenance and support are best effort, without guaranteed updates.

Install `gameshark-local-2.0.0-local.8.jar` alongside Pixelmon 9.4.2 and the Atlantis Development community port. See the [repository README](../README.md) for install and build steps.
