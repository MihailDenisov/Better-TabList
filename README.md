# Better TabList

A server-side Minecraft mod that fully customizes the in-game tab list with animated headers/footers, AFK detection, player sorting, and FTB Ranks or LuckPerms integration.

Supports **NeoForge**, **Fabric**, and **Forge** for Minecraft 1.21–1.21.1.

## Features

- **Animated Header & Footer** — Multiple frames that cycle automatically, with configurable speed
- **AFK Detection** — Greyed-out names and an `#AFK` placeholder after a configurable timeout
- **Player Sorting** — Alphabetical or rank-based (via FTB Ranks power or LuckPerms group weight) tab list ordering
- **FTB Ranks Integration** — Optional; uses rank permissions for display names and sorting
- **LuckPerms Integration** — Optional; supports prefixes, suffixes, primary groups, and weighted sorting
- **Player Status** — Shows each player's dimension and health directly in their tab-list entry
- **Hex Color Support** — Full `&#RRGGBB` hex colors alongside standard `&` color codes
- **Efficient Updates** — Only sends packets when content actually changes

## Installation

1. Download the JAR for your mod loader from the releases page.
2. Place it in your server's `mods/` folder.
3. Start the server — a `config/tablist.toml` file will be generated with defaults.
4. Edit the config and restart (or reload) to apply changes.

## Configuration

All settings live in `config/tablist.toml`.

### Placeholders

| Placeholder    | Description                                        |
|----------------|----------------------------------------------------|
| `#N`           | New line                                           |
| `#SERVERNAME`  | Server name (set in config)                        |
| `#TPS`         | Ticks per second                                   |
| `#CTPS`        | TPS with automatic color (green/yellow/red)        |
| `#MSPT`        | Milliseconds per tick                              |
| `#PLAYERCOUNT` | Number of online players                           |
| `#MAXPLAYERS`  | Maximum player slots                               |
| `#PLAYERNAME`  | Viewing player's name                              |
| `#PING`        | Player ping in ms                                  |
| `#RANK`        | Player rank or primary LuckPerms group             |
| `#AFK`         | Shows "AFK" if the player is AFK, empty otherwise  |
| `#WORLD`       | Player's current dimension                         |
| `#MEMORY`      | Memory usage (used / max)                          |
| `#UPTIME`      | Server uptime                                      |
| `#DATE`        | Real date (yyyy-MM-dd)                             |
| `#TIME`        | Real time (HH:mm)                                  |

### Color Codes

- Standard: `&a` (green), `&c` (red), `&l` (bold), `&r` (reset), etc.
- Hex: `&#FF5555` for any RGB color

### Display Name Format

The `display_name_format` option controls how player names appear in the tab list. Set `name_formatting_provider` to `NONE`, `FTB`, or `LP`:

| Placeholder       | Description |
|-------------------|-------------|
| `{name}`          | Player name |
| `{rank}`          | FTB rank or primary LuckPerms group |
| `{prefix}`        | LuckPerms prefix |
| `{suffix}`        | LuckPerms suffix |
| `{primary_group}` | LuckPerms primary group |
| `{dimension}`     | `§aⓌ§r` Overworld, `§cⓃ§r` Nether, or `§dⒺ§r` End |
| `{health}`        | Current health plus absorption, displayed in red (for example, `[20❤]`) |

```
display_name_format = "{dimension} {name} {health} &7#AFK"
name_formatting_provider = "NONE"
```

### Example Config

```toml
[appearance]
server_name = "My Server"
header = [
    "#N        &#FF5555&l#SERVERNAME        #N&#AAAAAA&m            #N",
    "#N        &#5555FF&l#SERVERNAME        #N&#AAAAAA&m            #N"
]
footer = [
    "&fOnline: &e#PLAYERCOUNT&7/&e#MAXPLAYERS #N&7TPS: #CTPS &7| MSPT: &#55FFFF#MSPT #N&7Memory: &#AA55FF#MEMORY &7| Uptime: &#FFAA00#UPTIME"
]
display_name_format = "{dimension} {name} {health} &7#AFK"
update_interval = 500
animation_interval = 4

[sorting]
sort_mode = "NONE"

[afk]
afk_enabled = true
afk_timeout = 300
```

## Building from Source

Requires Java 21.

```sh
./gradlew build
```

Output JARs:
- `neoforge/build/libs/` — NeoForge
- `fabric/build/libs/` — Fabric
- `forge/build/libs/` — Forge
