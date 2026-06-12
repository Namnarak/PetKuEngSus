<div align="center">
  
# 🐾 PetKuEngSus

*A modular Pet & RPG Framework for Minecraft.*

[![Paper 1.20+](https://img.shields.io/badge/Paper-1.20%2B-blue.svg)](https://papermc.io/)
[![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-success.svg)](#)
[![Author](https://img.shields.io/badge/Author-Cytech_Team_Development-purple.svg)](#)

</div>

**PetKuEngSus** is an advanced, standalone Pet & RPG framework designed for modern Minecraft servers. Originally forked from EcoPets, this plugin has been completely rewritten to run independently without the need for `eco` or `libreforge` dependencies, giving you a lightweight but incredibly powerful pet companion system.

---

## ✨ Features

- ⚡ **100% Standalone:** No bloated dependencies. Just drop it into your plugins folder!
- 🏔️ **3D Model Support:** Native reflection-based compatibility with **ModelEngine** for breathtaking 3D animated pets.
- 🐎 **Mount System:** Ride your pets into battle! Players can mount their active pets via `/pets mount` or directly from the beautiful GUI.
- 🎭 **Transformation System:** Don't have ModelEngine? No problem. Cycle your pet's visual representation dynamically between `Skull`, `Item Display`, and `Block Display` via `/pets transform`.
- 📊 **RPG Mechanics:** Complete leveling system, EXP gain, Talents, Skill Trees, Prestige, and Synergies.
- 🎨 **Beautiful UI:** Fully customizable, highly interactive GUI menus for managing pets, leveling up, and toggling visibility.
- ⚔️ **Combat & Effects:** Pets can give potion effects, buff players, and participate in combat.

---

## 💻 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pets` | Opens the main Pet GUI. | `petkuengsus.command.pets` |
| `/pets mount` | Mounts or dismounts your currently active pet. | `petkuengsus.command.mount` |
| `/pets transform` | Cycles the visual display model of your pet. | `petkuengsus.command.transform` |
| `/ecopetsadmin` | Administrator commands for giving pets/XP. | `petkuengsus.command.admin` |

---

## 🛠️ Installation

1. Download the latest `PetKuEngSus-1.0.0-all.jar` release.
2. Drop the file into your server's `plugins/` directory.
3. Restart your server.
4. *(Optional)* Install **PlaceholderAPI**, **Vault**, or **ModelEngine** for advanced integrations.

---

## 📖 Creating Custom Pets

Creating a custom pet is incredibly easy! Navigate to `plugins/PetKuEngSus/pets/` and create a new `.yml` file.

```yaml
tiger:
  # The display name of your pet
  name: "&6Fierce Tiger"
  # Optional: Hook into ModelEngine!
  model-engine-id: "tiger_model"
  # Default skin if ModelEngine is missing
  head: "eyJ0ZXh0dXJlcyI..."
  # Combat properties
  effects:
    - "add_potion_effect:SPEED:2:ambient"
```

---

<div align="center">
  <i>Developed with ❤️ by <b>Cytech Team Development</b></i>
</div>
