# SpyHunt-Compass 🧭

**SpyHunt-Compass** is a standalone, lightweight, and high-performance tracking compass plugin for Minecraft servers. Originally developed as a core mechanic for the **SpyHunts** manhunt gamemode, this plugin brings smart player tracking logic to any survival or minigame server.

## ✨ Features

### 🎯 **Smart Tracking Logic**
- **Real-Time Tracking**: The compass updates every second to point directly to the target player.
- **Cross-Dimension Support**:
  - **Overworld Tracking**: Points directly to the player.
  - **Nether/End Tracking**: If the target is in a different dimension, the compass tracks the **last known portal location** (or where they entered the dimension).
  - **Smart Recovery**: If a target disconnects or is in a different world without portal data, the compass points to their **last seen location**.

### 🛠️ **Advanced Management**
- **Multi-Target System**: Add multiple players to a single tracker.
  - **Cycle Targets**: Right-click your compass to switch between tracked players.
- **Auto-Inventory Management**: 
  - Automatically gives a compass if the player doesn't have one.
  - Updates existing compasses in the inventory.
- **Privacy Focused**: Player names are **hidden** from tab completion when adding/removing targets to prevent meta-gaming via command suggestions.

## 📜 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/track <player>` | Instantly get a compass tracking a specific player (clears other targets). | `spyhuntcompass.use` (default: true) |
| `/tracker` | Get an empty tracker compass. | `spyhuntcompass.use` |
| `/tracker add <player>` | Add a player to your tracking list. | `spyhuntcompass.use` |
| `/tracker remove <player>` | Remove a player from your tracking list. | `spyhuntcompass.use` |
| `/tracker help` | View all available commands. | `spyhuntcompass.use` |

## 🚀 Installation

1. Download the latest `SpyHunt-Compass.jar`.
2. Place it in your server's `plugins/` folder.
3. Restart your server.
4. Enjoy smart tracking!

## 🔧 Configuration

Currently, SpyHunt-Compass is designed to be plug-and-play with zero configuration required. All data is stored persistently on the compass items themselves.

---
*Developed by SpyGamingOG*
