# PetKuEngSus — Fork Summary

## Objective
Create a standalone Paper plugin (`PetKuEngSus`) by forking [EcoPets](https://github.com/Auxilor/EcoPets) (GPL-3) and removing ALL runtime dependencies on `eco` and `libreforge`.

## Architecture
- **Package**: `com.willfp.ecopets` (preserved)
- **Build**: Gradle (`eco-core/`), Paper 1.20.6 API, Kotlin
- **`plugin.yml`**: renamed to `ecopets-standalone`, no `depend:` or `softdepend:` on eco/libreforge
- **`plugin.yml` entries**: Register all commands directly (ecopets, pets, ecopetsadmin), register all libreforge effects/conditions as Bukkit events instead of via Libreforge's registry

## Completed Work

### ✅ Pet model and data layer
- `Pet.kt` — full pet YAML config deserialization, XP/level calculations, spawn egg item, display entity, crafting recipe, effects/conditions. Replaced `PetDataStore.profile` with direct NBT-driven data access (`PetDataStore.getDouble/getBoolean/getInt` reimplemented locally). **Pending**: `PetDataStore.getBoolean` still used in core logic (line 264) — need to add to `PetDataStore.kt`.
- `PetDataStore.kt` — standalone NBT-based data persistence. Has `getDouble`, `getInt`, `setDouble`, `setInt`. **Missing**: `getBoolean`, `setBoolean`.
- `Pets.kt` — standalone pet registry (YAML-loaded, map-based). No eco references.
- `PetLevelListener.kt` — still imports `com.willfp.eco.core.sound.PlayableSound` and `com.willfp.eco.util.SoundUtils`
- `PetDisplay.kt` — still imports `com.willfp.eco.util.NumberUtils` (fastSin) and `com.willfp.eco.util.formatEco`
- `SpawnEggHandler.kt` — still imports `com.willfp.eco.util.StringUtils`

### ✅ Plugin lifecycle
- `EcoPetsPlugin.kt` — standalone JavaPlugin. No eco extension. Uses direct NMS/PacketEvents for pet entities.
- `EcoPetsAPI.java` — standalone interface, no eco references.

### ✅ Command base layer
- `PetCommand.kt` — standalone Bukkit `CommandExecutor`/`TabCompleter` with subcommand routing.
- `PetSubcommand` — standalone abstract class (no eco dependency).
- `registerCommand()` — standalone helper.

### ✅ Event system
- All custom events in `events/` — standalone Bukkit events.
- `PlayerPetLevelUpEvent` exists and is used by `PetLevelListener`.

### ✅ Packet/entity system
- `PacketListener.kt` — PacketEvents-based, no eco refs.
- `PetEntity.java` — NMS spawn via PacketEvents, no eco refs.

### ✅ PlaceholderAPI expansion
- `EcoPetsExpansion.kt` — separate module, standalone.

## Remaining Work (by category)

### 1️⃣ Commands — rewrite 11 objects to use PetSubcommand (not eco `Subcommand`/`PluginCommand`)
| File | Uses (eco) |
|---|---|
| `CommandEcoPets.kt` | `PluginCommand` — wrapper that holds subcommands |
| `CommandPets.kt` | `PluginCommand` — wrapper that holds subcommands |
| `CommandActivate.kt` | `Subcommand`, `StringUtils.FormatOption` |
| `CommandActivateOther.kt` | `Subcommand`, `StringUtils.FormatOption` |
| `CommandDeactivate.kt` | `Subcommand`, `StringUtils.FormatOption` |
| `CommandDeactivateOther.kt` | `Subcommand`, `StringUtils.FormatOption` |
| `CommandGive.kt` | `Subcommand`, `StringUtils.FormatOption`, `savedDisplayName` |
| `CommandGiveEgg.kt` | `Subcommand`, `DropQueue`, `StringUtils.FormatOption`, `savedDisplayName` |
| `CommandGiveCurrentXP.kt` | `Subcommand`, `StringUtils.FormatOption`, `savedDisplayName`, `toNiceString` |
| `CommandGiveXP.kt` | `Subcommand`, `StringUtils.FormatOption`, `savedDisplayName`, `toNiceString` |
| `CommandReload.kt` | `Subcommand`, `StringUtils.FormatOption`, `toNiceString` |
| `CommandReset.kt` | `Subcommand`, `StringUtils.FormatOption`, `savedDisplayName` |
| `CommandTake.kt` | `Subcommand`, `StringUtils.FormatOption`, `savedDisplayName` |

**Strategy**: Each command extends `PetSubcommand`. Replace:
- `StringUtils.FormatOption.WITHOUT_PLACEHOLDERS` → remove the argument (message still works)
- `player.savedDisplayName` → `player.name` (or PlayerDisplayEvent-based)
- `amount.toNiceString()` → `String.format("%,.0f", amount)` or simple `amount.toLong().toString()`
- `DropQueue(player)` → `player.world.dropItemNaturally(player.location, item)` or `player.inventory.addItem(item)`

### 2️⃣ Libreforge — rewrite 8 files (remove `Config` from eco, effect registration from libreforge)

| File | Current eco dependency |
|---|---|
| `EffectActivatePet.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `EffectDeactivatePet.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `EffectGivePetXp.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `EffectPetXpMultiplier.kt` | `Config`, `SimpleConditionList`, `SimpleEffectList` |
| `ConditionHasActivePet.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `ConditionHasPet.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `ConditionHasPetLevel.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `FilterPet.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `MutatorPlayerToPet.kt` | `com.willfp.eco.core.config.interfaces.Config` |
| `MutatorPlayerToPetLocation.kt` | `com.willfp.eco.core.config.interfaces.Config` |

**Strategy**: Replace `Config` with `Map<String, Any>` or a thin wrapper. Remove `libreforge` effect registration; keep them as plain effect handlers called from pet tick/events.

### 3️⃣ PetDisplay.kt — replace eco utils
- `NumberUtils.fastSin(tick / (2 * PI) * 0.5)` → `kotlin.math.sin(tick / (2 * PI) * 0.5)`
- `formatEco(player)` → strip eco placeholders (return raw string or use PAPI)

### 4️⃣ PetLevelListener.kt — replace eco sound utils
- `PlayableSound.create(plugin.configYml.getSubsection("level-up.sound"))?.playTo(player)` → use Bukkit `Sound` or `SoundEffect` directly from config values
- Remove `SoundUtils` import

### 5️⃣ SpawnEggHandler.kt — replace StringUtils
- `StringUtils.FormatOption.WITHOUT_PLACEHOLDERS` → simple string message

### 6️⃣ LevelInjectable.kt — replace eco placeholders
- Replace `com.willfp.eco.core.placeholder.InjectablePlaceholder`, `PlaceholderInjectable`, `StaticPlaceholder` — either remove (if unused) or reimplement as simple map-based placeholders

### 7️⃣ Model — add missing PetDataStore methods
- Add `getBoolean(key: String, def: Boolean)` and `setBoolean(key: String, value: Boolean)` to `PetDataStore.kt`

### 8️⃣ Build — update Gradle dependencies
- Remove `eco` and `libreforge` from `build.gradle.kts` / libs.versions.toml
- Ensure Paper API is the only dependency

### 9️⃣ plugin.yml — command registration
- Register all commands (`ecopets`, `pets`, `ecopetsadmin`) directly with `BukkitCommandMap` or via `plugin.yml`
- Wire them to the corresponding `PetCommand` instances in `EcoPetsPlugin.kt`

## ✅ New Features (v1.0.0+)
- **Mount System**: Players can mount their pets via `/pets mount` or GUI button.
- **3D Model Support**: Added support for ModelEngine via `ModelEnginePetEntity.kt` (using reflection to remain decoupled).
- **Transformation System**: Added `/pets transform` to cycle pet display models (`skull`, `item_display`, `block_display`).
- **UI Upgrade**: Rewrote `PetsGUI.kt` with custom layout masks, model toggle buttons, mounting, and transformation controls.
- **Dependency Fixes**: Fixed `kotlin-stdlib` shadowing issues to utilize server-provided Kotlin or properly shadow only what's needed without exploding JAR size. Fixed default `example.yml` loading.
- **Metadata Update**: Updated plugin author to "Cytech Team Development" and added descriptive description.