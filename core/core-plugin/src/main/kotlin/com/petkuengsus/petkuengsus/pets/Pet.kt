package com.petkuengsus.petkuengsus.pets

import com.github.benmanes.caffeine.cache.Caffeine
import com.petkuengsus.petkuengsus.api.event.PlayerPetActivateEvent
import com.petkuengsus.petkuengsus.api.event.PlayerPetDeactivateEvent
import com.petkuengsus.petkuengsus.api.event.PlayerPetExpGainEvent
import com.petkuengsus.petkuengsus.api.event.PlayerPetLevelUpEvent
import com.petkuengsus.petkuengsus.internal.NumberUtils
import com.petkuengsus.petkuengsus.internal.SimpleConditionList
import com.petkuengsus.petkuengsus.internal.SimpleEffectList
import com.petkuengsus.petkuengsus.internal.SimpleConfig
import com.petkuengsus.petkuengsus.internal.SimpleItems
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.internal.LangYml
import com.petkuengsus.petkuengsus.internal.PlayerPlaceholder
import com.petkuengsus.petkuengsus.internal.PlayerStaticPlaceholder
import com.petkuengsus.petkuengsus.pets.entity.PetEntity
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.abs

class Pet(
    val id: String,
    configMap: Map<String, Any>
) {
    private val config = SimpleConfig(configMap)

    val name = StringUtils.format(config.getString("name") ?: id)

    val description = StringUtils.format(config.getString("description") ?: "")

    internal val levelKey = NamespacedKey(plugin, "${id}_level")

    internal val xpKey = NamespacedKey(plugin, "${id}_xp")

    private val spawnEggBacker: ItemStack? = run {
        if (!config.getBool("spawn-egg.enabled")) {
            return@run null
        }

        val lookupName = config.getString("spawn-egg.item") ?: return@run null
        val lookup = SimpleItems.lookup(lookupName) ?: return@run null

        val builder = SimpleItems.builder(lookup)
        val eggName = config.getString("spawn-egg.name")
        if (eggName != null) {
            builder.setDisplayName(StringUtils.format(eggName))
        }
        builder.addLoreLines(config.getStringList("spawn-egg.lore").map { StringUtils.format(it) })

        val item = builder.build()
        item.petEgg = this@Pet
        item
    }

    val spawnEgg: ItemStack?
        get() = this.spawnEggBacker?.clone()

    val recipe: org.bukkit.inventory.Recipe? = spawnEgg
        ?.takeIf { config.getBool("spawn-egg.craftable") }
        ?.let { egg ->
            val recipeStrings = config.getStringList("spawn-egg.recipe")
            if (recipeStrings.isEmpty()) return@let null

            val key = NamespacedKey(plugin, "${id}_spawn_egg")
            val shapeless = config.getBool("spawn-egg.shapeless")

            if (shapeless) {
                val r = ShapelessRecipe(key, egg)
                val chars = mutableSetOf<Char>()
                for (row in recipeStrings) {
                    for (c in row) {
                        if (c != ' ') chars.add(c)
                    }
                }
                for (c in chars) {
                    val ingredientName = config.getString("spawn-egg.ingredients.$c") ?: c.toString()
                    val ingredient = SimpleItems.lookup(ingredientName)?.type ?: Material.STONE
                    r.addIngredient(ingredient)
                }
                Bukkit.addRecipe(r)
                r
            } else {
                val r = ShapedRecipe(key, egg)
                r.shape(
                    recipeStrings.getOrElse(0) { "   " },
                    recipeStrings.getOrElse(1) { "   " },
                    recipeStrings.getOrElse(2) { "   " }
                )
                val chars = mutableSetOf<Char>()
                for (row in recipeStrings) {
                    for (c in row) {
                        if (c != ' ') chars.add(c)
                    }
                }
                for (c in chars) {
                    val ingredientName = config.getString("spawn-egg.ingredients.$c") ?: c.toString()
                    val ingredient = SimpleItems.lookup(ingredientName)?.type ?: Material.STONE
                    r.setIngredient(c, ingredient)
                }
                Bukkit.addRecipe(r)
                r
            }
        }

    val entityTexture = config.getString("entity-texture")

    private val xpFormula = config.getString("xp-formula")

    private val levelXpRequirements = run {
        val list = config.getStringList("level-xp-requirements")
        if (list.isNotEmpty()) {
            listOf(0) + list.mapNotNull { it.trim().toIntOrNull() }
        } else {
            val single = config.getString("level-xp-requirements")
            if (single != null) {
                listOf(0) + single.split(",").mapNotNull { it.trim().toIntOrNull() }
            } else {
                emptyList()
            }
        }
    }

    val maxLevel = config.getInt("max-level").takeIf { it > 0 } ?: if (levelXpRequirements.size > 1) levelXpRequirements.size else 100

    val levelGUI = PetLevelGUI(this)

    private val baseItem: ItemStack = SimpleItems.lookup(config.getString("icon") ?: "stone") ?: ItemStack(Material.STONE)

    private val effects = SimpleEffectList.fromConfig(config, "effects")

    private val conditions = SimpleConditionList.fromConfig(config, "conditions")

    private val activateConditions = SimpleConditionList.fromConfig(config, "activate-conditions")

    private val levels = Caffeine.newBuilder().build<Int, PetLevel>()

    private val effectsDescription = Caffeine.newBuilder().build<Int, List<String>>()

    private val rewardsDescription = Caffeine.newBuilder().build<Int, List<String>>()

    private val levelUpMessages = Caffeine.newBuilder().build<Int, List<String>>()

    private val levelPlaceholders = run {
        val section = config.getSubsection("level-placeholders") ?: return@run emptyList<LevelPlaceholder>()
        section.keys().mapNotNull { key ->
            val sub = section.getSubsection(key) ?: return@mapNotNull null
            val pid = sub.getString("id") ?: return@mapNotNull null
            val value = sub.getString("value") ?: return@mapNotNull null
            LevelPlaceholder(pid) {
                NumberUtils.evaluateExpression(value.replace("%level%", it.toString()))
            }
        }
    }

    init {
        if (xpFormula == null && levelXpRequirements.isEmpty()) {
            plugin.logger.warning("Pet $id has no xp-formula or level-xp-requirements")
        }

        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("active_pet_level") {
                it.activePet?.let { pet -> it.getPetLevel(pet).toString() } ?: ""
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("active_pet_level_numeral") {
                it.activePet?.let { pet -> StringUtils.toNumeral(it.getPetLevel(pet)) } ?: ""
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("active_pet_current_xp") {
                it.activePet?.let { pet -> NumberUtils.format(it.getPetXP(pet)) } ?: ""
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("active_pet_required_xp") {
                it.activePet?.let { pet -> pet.getFormattedExpForLevel(it.getPetLevel(pet) + 1) } ?: ""
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("active_pet_percentage_progress") {
                it.activePet?.let { pet ->
                    NumberUtils.format((it.getPetProgress(pet) * 100))
                } ?: ""
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("active_pet_description") {
                it.activePet?.description ?: ""
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("${id}_percentage_progress") {
                NumberUtils.format((it.getPetProgress(this) * 100))
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder(id) {
                it.getPetLevel(this).toString()
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("${id}_current_xp") {
                NumberUtils.format(it.getPetXP(this))
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("${id}_required_xp") {
                it.getPetXPRequired(this).toString()
            }
        )
        plugin.placeholderRegistry.add(
            object : PlayerPlaceholder {
                override val identifier = "${id}_name"
                override fun getValue(player: Player) = this@Pet.name
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("${id}_level") {
                it.getPetLevel(this).toString()
            }
        )
        plugin.placeholderRegistry.add(
            PlayerStaticPlaceholder("${id}_can_activate") {
                canActivate(it).toString()
            }
        )
    }

    val levelUpEffects = SimpleEffectList.fromConfig(config, "level-up-effects")

    fun makePetEntity(player: org.bukkit.entity.Player? = null): PetEntity {
        return PetEntity.create(this, player)
    }

    fun getLevel(level: Int): PetLevel = levels.get(level) {
        PetLevel(this, it, effects, conditions)
    }

    fun canActivate(player: Player): Boolean {
        return activateConditions.check(player)
    }

    private fun getLevelUpMessages(level: Int, whitespace: Int = 0): List<String> = levelUpMessages.get(level) {
        var highestConfiguredLevel = 1
        val section = this.config.getSubsection("level-up-messages")
        if (section != null) {
            for (messagesLevel in section.keys().mapNotNull { it.toIntOrNull() }) {
                if (messagesLevel > level) continue
                if (messagesLevel > highestConfiguredLevel) {
                    highestConfiguredLevel = messagesLevel
                }
            }
        }

        this.config.getStringList("level-up-messages.$highestConfiguredLevel")
            .map {
                levelPlaceholders.format(it, level)
            }
            .map {
                " ".repeat(whitespace) + it
            }
    }

    private fun getEffectsDescription(level: Int, whitespace: Int = 0): List<String> = effectsDescription.get(level) {
        var highestConfiguredLevel = 1
        val section = this.config.getSubsection("effects-description")
        if (section != null) {
            for (messagesLevel in section.keys().mapNotNull { it.toIntOrNull() }) {
                if (messagesLevel > level) continue
                if (messagesLevel > highestConfiguredLevel) {
                    highestConfiguredLevel = messagesLevel
                }
            }
        }

        this.config.getStringList("effects-description.$highestConfiguredLevel")
            .map {
                levelPlaceholders.format(it, level)
            }
            .map {
                " ".repeat(whitespace) + it
            }
    }

    private fun getRewardsDescription(level: Int, whitespace: Int = 0): List<String> = rewardsDescription.get(level) {
        var highestConfiguredLevel = 1
        val section = this.config.getSubsection("rewards-description")
        if (section != null) {
            for (messagesLevel in section.keys().mapNotNull { it.toIntOrNull() }) {
                if (messagesLevel > level) continue
                if (messagesLevel > highestConfiguredLevel) {
                    highestConfiguredLevel = messagesLevel
                }
            }
        }

        this.config.getStringList("rewards-description.$highestConfiguredLevel")
            .map {
                levelPlaceholders.format(it, level)
            }
            .map {
                " ".repeat(whitespace) + it
            }
    }

    fun injectPlaceholdersInto(lore: List<String>, player: Player, forceLevel: Int? = null): List<String> {
        val level = forceLevel ?: player.getPetLevel(this)
        val regex = Regex("%level_(-?\\d+)(_numeral)?%")

        val withPlaceholders = lore.map { line ->
            var result = line
                .replace("%percentage_progress%", NumberUtils.format(player.getPetProgress(this) * 100))
                .replace("%current_xp%", NumberUtils.format(player.getPetXP(this)))
                .replace("%required_xp%", this.getFormattedExpForLevel(level + 1))
                .replace("%description%", this.description)
                .replace("%pet%", this.name)
                .replace("%level%", level.toString())
                .replace("%level_numeral%", StringUtils.toNumeral(level))

            result = regex.replace(result) { match ->
                val offset = match.groupValues[1].toIntOrNull() ?: return@replace match.value
                val isNumeral = match.groupValues[2].isNotEmpty()
                val newLevel = level + offset

                if (isNumeral) StringUtils.toNumeral(newLevel) else newLevel.toString()
            }

            result
        }.toMutableList()

        val processed = mutableListOf<List<String>>()

        for (s in withPlaceholders) {
            val whitespace = s.length - s.replace(" ", "").length

            processed.add(
                when {
                    s.contains("%effects%") -> getEffectsDescription(level, whitespace)
                    s.contains("%rewards%") -> getRewardsDescription(level, whitespace)
                    s.contains("%level_up_messages%") -> getLevelUpMessages(level, whitespace)
                    else -> listOf(s)
                }
            )
        }

        return processed.flatten().map { StringUtils.format(it) }
    }

    fun getIcon(player: Player): ItemStack {
        val base = baseItem.clone()

        val level = player.getPetLevel(this)
        val isActive = player.activePet == this

        val baseLoreLocation = if (level == this.maxLevel) "max-level-lore" else "lore"

        return SimpleItems.builder(base)
            .setDisplayName(
                StringUtils.format(
                    plugin.configYml.getString("gui.pet-icon.name") ?: ""
                )
                    .replace("%level%", level.toString())
                    .replace("%pet%", this.name)
            )
            .addLoreLines(
                injectPlaceholdersInto(
                    plugin.configYml.getStringList("gui.pet-icon.$baseLoreLocation").map { StringUtils.format(it) },
                    player
                ) + if (isActive) {
                    plugin.configYml.getStringList("gui.pet-icon.active-lore").map { StringUtils.format(it) }
                } else {
                    plugin.configYml.getStringList("gui.pet-icon.not-active-lore").map { StringUtils.format(it) }
                }
            )
            .build()
    }

    fun getPetInfoIcon(player: Player): ItemStack {
        val base = baseItem.clone()

        val prefix = if (player.getPetLevel(this) == this.maxLevel) "max-level-" else ""

        return SimpleItems.builder(base)
            .setDisplayName(
                StringUtils.format(
                    plugin.configYml.getString("gui.pet-info.active.name") ?: ""
                )
                    .replace("%level%", player.getPetLevel(this).toString())
                    .replace("%pet%", this.name)
            )
            .addLoreLines(
                injectPlaceholdersInto(
                    plugin.configYml.getStringList("gui.pet-info.active.${prefix}lore").map { StringUtils.format(it) },
                    player
                )
            )
            .build()
    }

    fun getExpForLevel(level: Int): Double {
        if (level !in 1..maxLevel) {
            return Double.POSITIVE_INFINITY
        }

        if (xpFormula != null) {
            return NumberUtils.evaluateExpression(
                xpFormula.replace("%level%", (level - 1).toString())
            )
        }

        return levelXpRequirements.getOrElse(level - 1) { 0 }.toDouble()
    }

    fun getFormattedExpForLevel(level: Int): String {
        val required = getExpForLevel(level)
        return if (required.isInfinite()) {
            StringUtils.format(LangYml.getMessage("infinity"))
        } else {
            NumberUtils.format(required)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Pet) {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(this.id)
    }
}

private class LevelPlaceholder(
    val id: String,
    private val function: (Int) -> Double
) {
    operator fun invoke(level: Int) = function(level)
}

private fun Collection<LevelPlaceholder>.format(string: String, level: Int): String {
    var process = string
    for (placeholder in this) {
        process = process.replace("%${placeholder.id}%", NumberUtils.format(placeholder(level)))
    }
    return process
}

object PetDataStore {
    private fun getPlayer(player: OfflinePlayer): Player? {
        return if (player.isOnline) player.player else null
    }

    fun getInt(player: OfflinePlayer, key: NamespacedKey, default: Int = 0): Int {
        val p = getPlayer(player) ?: return default
        return p.persistentDataContainer.getOrDefault(key, PersistentDataType.INTEGER, default)
    }

    fun setInt(player: OfflinePlayer, key: NamespacedKey, value: Int) {
        val p = getPlayer(player) ?: return
        p.persistentDataContainer.set(key, PersistentDataType.INTEGER, value)
    }

    fun getDouble(player: OfflinePlayer, key: NamespacedKey, default: Double = 0.0): Double {
        val p = getPlayer(player) ?: return default
        return p.persistentDataContainer.getOrDefault(key, PersistentDataType.DOUBLE, default)
    }

    fun setDouble(player: OfflinePlayer, key: NamespacedKey, value: Double) {
        val p = getPlayer(player) ?: return
        p.persistentDataContainer.set(key, PersistentDataType.DOUBLE, value)
    }

    fun getString(player: OfflinePlayer, key: NamespacedKey, default: String = ""): String {
        val p = getPlayer(player) ?: return default
        return p.persistentDataContainer.getOrDefault(key, PersistentDataType.STRING, default)
    }

    fun setString(player: OfflinePlayer, key: NamespacedKey, value: String) {
        val p = getPlayer(player) ?: return
        p.persistentDataContainer.set(key, PersistentDataType.STRING, value)
    }

    fun getBoolean(player: OfflinePlayer, key: NamespacedKey, default: Boolean = false): Boolean {
        val p = getPlayer(player) ?: return default
        return p.persistentDataContainer.getOrDefault(key, PersistentDataType.BOOLEAN, default)
    }

    fun setBoolean(player: OfflinePlayer, key: NamespacedKey, value: Boolean) {
        val p = getPlayer(player) ?: return
        p.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, value)
    }
}

private val activePetKey = NamespacedKey(plugin, "active_pet")

private val shouldHidePetKey = NamespacedKey(plugin, "hide_pet")

private val petEggKey = NamespacedKey(plugin, "pet_egg")

var ItemStack.petEgg: Pet?
    get() {
        val meta = this.itemMeta ?: return null
        val id = meta.persistentDataContainer.get(petEggKey, PersistentDataType.STRING) ?: return null
        return Pets.getByID(id)
    }
    set(value) {
        val meta = this.itemMeta ?: return
        value ?: return
        meta.persistentDataContainer.set(petEggKey, PersistentDataType.STRING, value.id)
        this.itemMeta = meta
    }

var OfflinePlayer.activePet: Pet?
    get() = Pets.getByID(PetDataStore.getString(this, activePetKey))
    set(value) {
        if (value == null) {
            val deactivateEvent = PlayerPetDeactivateEvent(this)
            Bukkit.getPluginManager().callEvent(deactivateEvent)
            if (deactivateEvent.isCancelled) return
        } else {
            val activateEvent = PlayerPetActivateEvent(this, value)
            Bukkit.getPluginManager().callEvent(activateEvent)
            if (activateEvent.isCancelled) return
        }
        PetDataStore.setString(this, activePetKey, value?.id ?: "")
    }

val OfflinePlayer.activePetLevel: PetLevel?
    get() {
        val active = this.activePet ?: return null
        return this.getPetLevelObject(active)
    }

var OfflinePlayer.shouldHidePet: Boolean
    get() = PetDataStore.getBoolean(this, shouldHidePetKey)
    set(value) = PetDataStore.setBoolean(this, shouldHidePetKey, value)

fun OfflinePlayer.getPetLevel(pet: Pet): Int =
    PetDataStore.getInt(this, pet.levelKey)

fun OfflinePlayer.setPetLevel(pet: Pet, level: Int) =
    PetDataStore.setInt(this, pet.levelKey, level)

fun OfflinePlayer.getPetProgress(pet: Pet): Double {
    val currentXP = this.getPetXP(pet)
    val requiredXP = pet.getExpForLevel(this.getPetLevel(pet) + 1)
    return currentXP / requiredXP
}

fun OfflinePlayer.getPetLevelObject(pet: Pet): PetLevel =
    pet.getLevel(this.getPetLevel(pet))

fun OfflinePlayer.hasPet(pet: Pet): Boolean =
    this.getPetLevel(pet) > 0

fun OfflinePlayer.getPetXP(pet: Pet): Double =
    PetDataStore.getDouble(this, pet.xpKey)

fun OfflinePlayer.setPetXP(pet: Pet, xp: Double) =
    PetDataStore.setDouble(this, pet.xpKey, xp)

fun OfflinePlayer.getPetXPRequired(pet: Pet) =
    PetDataStore.getDouble(this, pet.xpKey)

fun Player.givePetExperience(pet: Pet, experience: Double, noMultiply: Boolean = false) {
    val exp = abs(if (noMultiply) experience else experience * this.petExperienceMultiplier)

    val gainEvent = PlayerPetExpGainEvent(this, pet, exp, !noMultiply)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    this.giveExactPetExperience(pet, gainEvent.amount)
}

fun Player.giveExactPetExperience(pet: Pet, experience: Double) {
    val level = this.getPetLevel(pet)

    val progress = this.getPetXP(pet) + experience

    if (progress >= pet.getExpForLevel(level + 1) && level + 1 <= pet.maxLevel) {
        val overshoot = progress - pet.getExpForLevel(level + 1)
        this.setPetXP(pet, 0.0)
        this.setPetLevel(pet, level + 1)
        val levelUpEvent = PlayerPetLevelUpEvent(this, pet, level + 1)
        Bukkit.getPluginManager().callEvent(levelUpEvent)
        this.giveExactPetExperience(pet, overshoot)
    } else {
        this.setPetXP(pet, progress)
    }
}


