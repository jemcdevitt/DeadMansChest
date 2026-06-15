<p align="center">
	<img src="images/DeadMansChest.png" width="150">
</p>

# Dead Man's Chest

> Drifting barrels, pirate maps, and cursed treasure for PaperMC servers.

Dead Man's Chest is a PaperMC plugin that turns the open ocean into the start of a pirate treasure hunt.

Find a strange barrel bobbing in the water, claim the soggy chart inside, follow a custom parchment-style map, and discover what waits at the skull-marked location. Treasure is never truly free, and a dead pirate is not likely to give up his loot without a fight.

---

## Features

* Floating treasure barrels that appear in the world
* Custom parchment-style treasure maps
* Prefilled pirate map rendering with land, water, and terrain hints
* Skull-and-crossbones treasure marker drawn directly onto the map
* Player tracking on the map using the standard Minecraft map cursor
* Treasure locations generated away from the original barrel
* Survival-friendly treasure hunt gameplay
* No resource pack required
* No NMS or internal server classes

Dead Man's Chest is designed to feel like something that belongs naturally in a survival world: rare, atmospheric, dangerous, and rewarding.

---

## Philosophy

Dead Man's Chest is not trying to replace vanilla maps or become a full quest system.

The goal is simple:

> Find a barrel.
> Read the map.
> Follow the skull.
> Face whatever guards the treasure.

The plugin favors atmosphere over complexity. The map style is intentionally simplified, using an old parchment look rather than a full Minecraft terrain map. The result is readable, distinctive, and immediately recognizable as a pirate treasure chart.

---

## Requirements

* PaperMC 1.21.11+
* Java 21+

### Tested Versions

* Paper 1.21.11
* Paper 26.2.1

Dead Man's Chest uses only supported Paper APIs only and does not rely on NMS or internal server classes.

---

## Installation

1. Download the latest release
2. Drop the jar into your server's `plugins/` directory
3. Start or restart the server
4. Watch the waters
5. Find a floating barrel, claim the chart, and be warned.

---

## Configuration

Configuration options are still being developed during the beta phase.

Future configuration may include options for:

* Barrel spawn frequency
* Barrel spawn limits
* Treasure distance
* Treasure rewards
* Encounter difficulty
* Cleanup and recovery behavior

For now, the plugin is tuned around real survival gameplay and active testing.

---
## Commands

| Command                | Description                                            |
|------------------------|--------------------------------------------------------|
| `/deadmanschest:flush` | Removes all currently spawned Dead Man's Chest barrels |
| `/deadmanschest:info`  | Prints the location of all currently spawned barrels   |

### Beta Note

At the moment, commands are intentionally unrestricted during the beta phase.

This means any player with access to commands can currently execute either command.

These commands exist primarily as recovery and debugging tools while the plugin continues to mature. They may become permission-restricted in a future release.

---
## Notes

Dead Man's Chest is built around temporary world encounters and persistent player-held treasure maps.

Barrels are intended to be transient. They may be cleaned up during restart or recovery operations.

Treasure maps are intended to be part of the player experience. They are custom-rendered maps and may require repair/recovery logic across server restarts as the plugin continues to develop.

The plugin is currently focused on the core gameplay loop:

1. Spawn a floating barrel.
2. Let the player discover it.
3. Give the player a custom treasure map.
4. Guide the player to the marked location.
5. Trigger the treasure encounter.


---

## Current Status

Dead Man's Chest is currently in beta.

The plugin is fully playable and is actively used in survival gameplay, but there may still be occasional edge cases around chunk loading, map recovery, server restarts, and encounter behavior.

The visual style of the treasure maps is considered stable. Current development is focused on treasure discovery, encounter mechanics, loot behavior, and cleanup/recovery polish.

Feedback, bug reports, and suggestions are welcome.

---

## Screenshots and Videos

Screenshots and videos will be added as the plugin moves closer to release.

Planned media includes:

* Finding a floating barrel
* Receiving a soggy pirate map
* Following the skull-marked chart
* Reaching the treasure location
* Triggering the treasure encounter


---

## Inspiration

Dead Man's Chest is inspired by pirate legends, cursed treasure, shipwreck stories, and the simple joy of finding something strange floating in the water.

A pirate, even a dead one, is not going to give up his loot willingly.


---

## License

DeadMansChest is licensed under the MIT License.

See the [LICENSE](LICENSE) file for full details.

---

## Final Thoughts

Dead Man's Chest is meant to add a little mystery to the ocean.

Not every barrel should be ignored.
Not every map should be trusted.
Not every treasure should be easy to claim.


