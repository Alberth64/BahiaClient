# ZeyaMoon

Client-side mod for Minecraft 26.2 with selected features organized by category.
In the menu, drag category headers with the left mouse button, right-click
headers to collapse or expand them, and left-click features to toggle them.
Right-click a feature to open a compact settings panel. Open the menu with
**`/z`**, **`/zeyafabric`**, or press **K**.

## Features

### HUD

- **FPS, ping, and TPS** display with editable position.
- **Enemy Info** shows the name and estimated health of the mob under your crosshair
  when that information is available to the client.
- Customize text and bar colors, choose the background color, or disable it.

### Visual

- **Player Size** changes the player's visual scale.
- **Player Glow** with a color picker.
- **Mob Hitboxes** draws hitboxes without changing reach or collisions.
- **Hide Armor** independently hides the helmet/head, chestplate, leggings, or boots.
- **Fullbright**.

### Macros

- **AutoClick** clicks the left mouse button while held, with configurable CPS.
- **Auto Fishing** with configurable cast/reel delays, minimum fish wait,
  pause-while-screen-open, and Golden Fish cue handling, aiming, and action delay.
  It starts after you cast once manually, pauses if you stop holding the rod,
  and resumes after your next manual cast.
- **Oxygen Recovery** when swimming to the surface.
- **Blaze Dagger Swap**: while targeting a mob with a nearby shield armor stand,
  selects the matching dagger from the hotbar and switches its attunement. The
  preferred hotbar slot and swap delay are configurable.
- **Loadout Keybinds**: bind a key to each loadout. Pressing the key outside menus
  sends `/loadout`, selects the slot, and closes the menu.

### Utils

- Reserved for future utilities.

## Organization

Features are separated by area under `src/client/java/dev/zeya/fabricmoon/`:

- `hud/`: HUD, health reading, and ping/TPS statistics.
- `visual/`: visual options and hitbox rendering.
- `macros/`: auto-click, fishing, oxygen recovery, and Blaze Dagger.
- `inventory/`: loadout shortcuts.
- `ui/`: main menu and screen components.
- `mixin/`: required integrations with the Minecraft client.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.20.1 or later
- Fabric API 0.161.0+26.2 or later
- Java 25
- Mod Menu is optional.
