package com.petkuengsus.petkuengsus.pets.entity

import com.petkuengsus.petkuengsus.pets.Pet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemDisplayPetEntity(
    pet: Pet
) : PetEntity(pet) {
    override fun spawn(location: Location): Entity {
        val texture = pet.entityTexture
        val itemStack = parseItem(texture)

        return location.world!!.spawn(location, ItemDisplay::class.java) {
            it.setItemStack(itemStack)
            it.isCustomNameVisible = true
            @Suppress("DEPRECATION")
            it.customName = pet.name
            it.teleportDuration = plugin.configYml.getInt("pet-entity.item-display.teleport-duration", 3)
        }
    }

    private fun parseItem(texture: String?): ItemStack {
        if (texture == null) return ItemStack(Material.PLAYER_HEAD)

        if (texture.startsWith("player_head:")) {
            val base64 = texture.removePrefix("player_head:")
            return buildSkull(base64)
        }

        if (texture.startsWith("item:")) {
            val raw = texture.removePrefix("item:")
            val parts = raw.split(":")
            val mat = try {
                Material.valueOf(parts[0].uppercase())
            } catch (_: Exception) {
                Material.PLAYER_HEAD
            }
            val stack = ItemStack(mat)
            if (parts.size >= 2) {
                val cmd = parts[1].toIntOrNull()
                if (cmd != null && cmd > 0) {
                    stack.editMeta { meta: ItemMeta -> meta.setCustomModelData(cmd) }
                }
            }
            return stack
        }

        if (texture.contains(":")) return ItemStack(Material.PLAYER_HEAD)

        return buildSkull(texture)
    }
}
