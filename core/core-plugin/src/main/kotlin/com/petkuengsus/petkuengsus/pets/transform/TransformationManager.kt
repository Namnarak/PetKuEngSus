package com.petkuengsus.petkuengsus.pets.transform

import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.pets.PetDisplay
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.entity.PetEntity
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object TransformationManager {
    private val transformationModeKey = NamespacedKey(plugin, "pet_transform_mode")

    val MODEL_TYPES = listOf("skull", "item_display", "block_display")

    fun getModelType(player: Player): String {
        return PetEntity.getPlayerModelType(player) ?: "skull"
    }

    fun setModelType(player: Player, type: String) {
        PetEntity.setPlayerModelType(player, type)
        respawnPet(player)
    }

    fun cycleModelType(player: Player): String {
        val current = getModelType(player)
        val idx = MODEL_TYPES.indexOf(current)
        val next = MODEL_TYPES[(idx + 1) % MODEL_TYPES.size]
        setModelType(player, next)
        return next
    }

    fun hasTransformationMode(player: Player): Boolean {
        return player.persistentDataContainer.getOrDefault(transformationModeKey, PersistentDataType.BOOLEAN, false)
    }

    fun setTransformationMode(player: Player, enabled: Boolean) {
        player.persistentDataContainer.set(transformationModeKey, PersistentDataType.BOOLEAN, enabled)
    }

    fun toggleTransformationMode(player: Player): Boolean {
        val current = hasTransformationMode(player)
        setTransformationMode(player, !current)
        return !current
    }

    fun respawnPet(player: Player) {
        val pet = player.activePet ?: return
        PetDisplay.respawn(player)
        playTransformEffect(player)
    }

    private fun playTransformEffect(player: Player) {
        val loc = player.location.clone().add(0.0, 1.0, 0.0)
        player.world.spawnParticle(Particle.ENCHANT, loc, 30, 0.5, 0.5, 0.5, 0.5)
        player.world.spawnParticle(Particle.PORTAL, loc, 20, 0.3, 0.3, 0.3, 0.1)
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)

        player.sendMessage(StringUtils.format(plugin.langYml.getMessage("transform.activated")))
    }

    internal fun playTransformEffect(player: Player, from: Location, to: Location) {
        player.world.spawnParticle(Particle.FLASH, from, 1)
        player.world.spawnParticle(Particle.ENCHANT, from, 40, 0.5, 1.0, 0.5, 0.5)
        player.world.spawnParticle(Particle.INSTANT_EFFECT, to, 30, 0.3, 0.5, 0.3, 0.2)
        player.playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.2f)
    }
}
