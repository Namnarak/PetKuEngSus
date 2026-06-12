package com.petkuengsus.petkuengsus.pets.entity

import com.petkuengsus.petkuengsus.pets.Pet
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity

class BlockDisplayPetEntity(
    pet: Pet,
    private val blockData: BlockData?
) : PetEntity(pet) {
    override fun spawn(location: Location): Entity {
        val data = blockData ?: Material.STONE.createBlockData()

        return location.world!!.spawn(location, BlockDisplay::class.java) {
            it.block = data
            it.isCustomNameVisible = true
            @Suppress("DEPRECATION")
            it.customName = pet.name
        }
    }
}
