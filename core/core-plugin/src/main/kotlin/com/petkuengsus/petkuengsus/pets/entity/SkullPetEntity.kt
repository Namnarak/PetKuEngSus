package com.petkuengsus.petkuengsus.pets.entity

import com.petkuengsus.petkuengsus.pets.Pet
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

class SkullPetEntity(pet: Pet) : PetEntity(pet) {
    override fun spawn(location: Location): Entity {
        val stand = emptyArmorStandAt(location, pet, isSkull = true)

        val texture = pet.entityTexture
        if (texture != null) {
            val skull: ItemStack = buildSkull(texture)
            @Suppress("UNNECESSARY_SAFE_CALL")
            stand.equipment?.helmet = skull
        }

        return stand
    }
}
