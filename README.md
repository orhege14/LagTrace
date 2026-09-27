# LagTrace

> Minecraft server performance diagnostics and lag hotspot detection.

LagTrace is a Minecraft server diagnostics plugin designed to help server administrators find **where unusual server activity is happening** instead of simply reporting that the server is lagging.

Rather than only showing TPS or MSPT, LagTrace analyzes loaded chunks across your worlds and identifies the **top activity hotspots**, providing precise coordinates that can be investigated directly in-game.

---

## Features

### 🔎 Hotspot Detection

LagTrace scans currently loaded chunks across all loaded worlds and identifies the top 3 activity hotspots.

Each hotspot contains:

- World
- Exact X/Y/Z coordinates
- Activity score
- Entity count
- Block entity count
- Entity activity types
- Block entity activity types
- Dropped item quantities

---

### 📍 Precise Coordinates

Every detected hotspot includes its exact location.

Coordinates are clickable in chat and automatically prepare a teleport command:

```text
/tp X Y Z
```

This makes it easy to immediately investigate a suspicious area.

---

### 🌍 Multi-World Analysis

LagTrace does not only analyze the world where the player is currently located.

It checks loaded chunks across all loaded worlds.

For example:

```text
world
world_nether
world_the_end
```

can all be analyzed.

---

### ⚡ Safe Chunk Scanning

LagTrace only analyzes **already-loaded chunks**.

It does not intentionally force-load or generate unloaded chunks during the scan.

This prevents the diagnostic command from generating large amounts of new terrain just to perform an analysis.

---

### 👾 Entity Analysis

LagTrace analyzes entities found inside hotspot areas.

Examples include:

```text
SHEEP
VILLAGER
ZOMBIE
ITEM
MINECART
```

Entity activity is grouped by type so administrators can quickly see what is concentrated in an area.

Example:

```text
ENTITY ACTIVITY

• SHEEP × 16
• ITEM × 8
• VILLAGER × 5
```

---

### 📦 Dropped Item Analysis

LagTrace counts the **actual number of dropped items**, rather than simply counting item entities.

For example, Minecraft may have multiple item entities containing different stack amounts.

Instead of reporting only:

```text
ITEM × 72
```

LagTrace calculates the actual item quantity:

```text
Dropped Items: 4608
```

It can also show the item types:

```text
DROPPED ITEM TYPES

• DIAMOND × 4608
• IRON_INGOT × 2048
• COBBLESTONE × 1024
```

---

### 🧱 Block Entity Analysis

LagTrace also detects block entities located inside hotspots.

Examples include:

```text
HOPPER
CHEST
FURNACE
TRIAL_SPAWNER
DECORATED_POT
```

Example:

```text
BLOCK ENTITY ACTIVITY

• DECORATED_POT × 1
• TRIAL_SPAWNER × 1
```

---

## Example

Running:

```text
/lagtrace
```

can produce a report similar to:

```text
⚠ LAGTRACE — TOP 3 HOTSPOTS
━━━━━━━━━━━━━━━━━━━━━━━━━━

#1 HOTSPOT

World: world
📍 124 68 -392

Activity Score: 184
Entities: 42
Block Entities: 12
Dropped Items: 4608

DROPPED ITEM TYPES

• DIAMOND × 4608

ENTITY ACTIVITY

• SHEEP × 16
• ITEM × 12
• VILLAGER × 8

BLOCK ENTITY ACTIVITY

• HOPPER × 8
• CHEST × 4


#2 HOTSPOT

World: world
📍 -63 72 821

Activity Score: 121
Entities: 31
Block Entities: 6

ENTITY ACTIVITY

• VILLAGER × 18
• SHEEP × 9
• ITEM × 4


#3 HOTSPOT

World: world
📍 842 64 114

Activity Score: 96
Entities: 20
Block Entities: 9
```

---

## Installation

1. Download the latest LagTrace `.jar`.
2. Make sure your server is running **Paper**.
3. Place the plugin into:

```text
plugins/
```

4. Start or restart the server.
5. Use:

```text
/lagtrace
```

---

## Requirements

| Requirement | Version |
|---|---|
| Minecraft | 1.21.11 |
| Server | Paper |
| Java | 21 |

> LagTrace 1.0.0 is currently developed and tested for Paper 1.21.11.

---

## Commands

### `/lagtrace`

Scans currently loaded chunks and displays the top 3 detected activity hotspots.

```text
/lagtrace
```

---

## How It Works

LagTrace divides loaded world activity into small spatial areas and calculates an activity score based on detected entities and block entities.

The highest-scoring areas are then returned as hotspots.

The current system is designed as a diagnostic activity analyzer rather than a complete JVM-level profiler.

This means the reported activity score should be interpreted as an **activity indicator**, not as a direct measurement of CPU time consumed by a specific block or entity.

---

## Performance Considerations

LagTrace intentionally analyzes only chunks that are already loaded.

It does not scan every coordinate in a Minecraft world and does not intentionally generate unloaded terrain during analysis.

This keeps the diagnostic process from causing unnecessary world generation.

For very large numbers of loaded chunks, scanning can still require additional processing because the plugin examines entities and block entities in those chunks.

---

## Roadmap

Future versions are planned to expand LagTrace with deeper performance diagnostics.

Potential features include:

- [ ] Real-time MSPT sampling
- [ ] Tick spike detection
- [ ] Historical hotspot tracking
- [ ] Improved hotspot scoring
- [ ] More detailed redstone analysis
- [ ] Entity activity history
- [ ] Chunk activity history
- [ ] Plugin performance analysis
- [ ] Automatic lag diagnosis
- [ ] Heatmap visualization
- [ ] Web dashboard
- [ ] Historical performance reports
- [ ] More Minecraft versions

---

## Development

LagTrace is developed by **Redstone Circuits**.

The project uses:

- Java
- Gradle
- Paper API
- Adventure Components

Build the project with:

```bash
./gradlew build
```

The compiled plugin will be available in:

```text
build/libs/
```

---

## Project Structure

```text
LagTrace/
├── gradle/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── redstonecircuits/
│       │           └── lagTrace/
│       │               └── LagTrace.java
│       └── resources/
│           └── plugin.yml
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

---

## Version

Current version:

```text
1.0.0
```

Release target:

```text
Paper 1.21.11
```

---

## License

This project currently does not specify an open-source license.

All rights reserved by **Redstone Circuits** unless otherwise stated.

---

## Redstone Circuits

**Redstone Circuits** is focused on building practical Minecraft server tools and plugins.

> Engineering Minecraft.
