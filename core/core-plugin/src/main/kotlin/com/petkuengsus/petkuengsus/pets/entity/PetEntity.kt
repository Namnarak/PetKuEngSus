package com.petkuengsus.petkuengsus.pets.entity

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.petkuengsus.petkuengsus.pets.Pet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

abstract class PetEntity(
    val pet: Pet
) {
    abstract fun spawn(location: Location): Entity

    companion object {
        private val registrations = mutableMapOf<String, (Pet, String) -> PetEntity>()

        @JvmStatic
        fun registerPetEntity(id: String, parse: (Pet, String) -> PetEntity) {
            registrations[id] = parse
        }

        @JvmStatic
        fun create(pet: Pet, player: Player? = null): PetEntity {
            val texture = pet.entityTexture
            val override = player?.let { getPlayerModelType(it) }

            if (override != null) {
                return when (override) {
                    "item_display" -> ItemDisplayPetEntity(pet)
                    "block_display" -> BlockDisplayPetEntity(pet, null)
                    "skull" -> SkullPetEntity(pet)
                    else -> createFromTexture(pet, texture)
                }
            }

            return createFromTexture(pet, texture)
        }

        private fun createFromTexture(pet: Pet, texture: String?): PetEntity {
            if (texture == null) {
                return if (plugin.configYml.getBool("pet-entity.item-display.enabled")) {
                    ItemDisplayPetEntity(pet)
                } else {
                    SkullPetEntity(pet)
                }
            }

            if (texture.startsWith("item:")) {
                return ItemDisplayPetEntity(pet)
            }

            if (texture.startsWith("block:")) {
                val raw = texture.removePrefix("block:")
                val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { Material.STONE }
                return BlockDisplayPetEntity(pet, mat.createBlockData())
            }

            if (texture.startsWith("player_head:")) {
                return if (plugin.configYml.getBool("pet-entity.item-display.enabled")) {
                    ItemDisplayPetEntity(pet)
                } else {
                    SkullPetEntity(pet)
                }
            }

            if (texture.contains(":")) {
                val id = texture.split(":")[0]
                val parse = registrations[id] ?: return SkullPetEntity(pet)
                return parse(pet, texture.removePrefix("$id:"))
            }

            return if (plugin.configYml.getBool("pet-entity.item-display.enabled")) {
                ItemDisplayPetEntity(pet)
            } else {
                SkullPetEntity(pet)
            }
        }

        private val modelTypeKey = org.bukkit.NamespacedKey(plugin, "pet_model_type")

        fun getPlayerModelType(player: Player): String? {
            val value = player.persistentDataContainer.get(modelTypeKey, org.bukkit.persistence.PersistentDataType.STRING)
            return value?.takeIf { it.isNotEmpty() }
        }

        fun setPlayerModelType(player: Player, type: String?) {
            if (type == null) {
                player.persistentDataContainer.remove(modelTypeKey)
            } else {
                player.persistentDataContainer.set(modelTypeKey, org.bukkit.persistence.PersistentDataType.STRING, type)
            }
        }
    }
}

internal fun buildSkull(texture: String): ItemStack {
    val skull = ItemStack(Material.PLAYER_HEAD)
    val meta = skull.itemMeta as SkullMeta

    try {
        val profile = Bukkit.createProfile(UUID.randomUUID(), null)
        profile.setProperty(ProfileProperty("textures", texture))
        meta.setPlayerProfile(profile)
    } catch (e: Exception) {
        plugin.logger.warning("Failed to set skull texture: ${e.message}")
    }

    skull.itemMeta = meta
    return skull
}

private fun ArmorStand.applyScale(isSkull: Boolean) {
    if (!isSkull) return

    val scale = plugin.configYml.getDouble("pet-entity.scale")

    if (scale !in 0.0625..16.0) {
        plugin.logger.warning("Invalid scale value '$scale' in config.yml. Must be between 0.0625 and 16.")
        return
    }

    val scaleAttribute = getAttribute(Attribute.SCALE)
    if (scaleAttribute == null) {
        plugin.logger.warning("Failed to set scale - SCALE attribute not found on ArmorStand")
        return
    }

    scaleAttribute.baseValue = scale
}

internal fun emptyArmorStandAt(location: Location, pet: Pet, isSkull: Boolean): ArmorStand {
    val stand = location.world!!.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand

    stand.apply {
        isVisible = false
        isInvulnerable = true
        isSmall = true
        setGravity(false)
        isCollidable = false
        isPersistent = false

        for (slot in EquipmentSlot.entries) {
            stand.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING)
        }

        isCustomNameVisible = true
        @Suppress("DEPRECATION")
        customName = pet.name

        applyScale(isSkull)
    }

    return stand
}
