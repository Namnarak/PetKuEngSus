package com.petkuengsus.petkuengsus.multipet

import com.petkuengsus.petkuengsus.PetKuEngSusPlugin
import com.petkuengsus.petkuengsus.pets.Pet
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.pets.getPetLevel
import com.petkuengsus.petkuengsus.pets.getPetXP
import com.petkuengsus.petkuengsus.pets.setPetLevel
import com.petkuengsus.petkuengsus.pets.setPetXP
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object MultiPetManager {
    private val activePetsKey by lazy {
        com.petkuengsus.petkuengsus.internal.DataKeys.get("multipet_active", plugin)
    }

    fun getActivePetIds(player: Player): List<String> {
        val pdc = player.persistentDataContainer
        val raw = pdc.get(activePetsKey, PersistentDataType.STRING) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun getActivePets(player: Player): List<Pet> {
        val ids = getActivePetIds(player)
        return ids.mapNotNull { id -> Pets.values().find { it.id == id } }
    }

    fun setActivePets(player: Player, pets: List<Pet>) {
        val ids = pets.map { it.id }.joinToString(",")
        val pdc = player.persistentDataContainer
        if (ids.isEmpty()) {
            pdc.remove(activePetsKey)
        } else {
            pdc.set(activePetsKey, PersistentDataType.STRING, ids)
        }
    }

    fun addActivePet(player: Player, pet: Pet): Boolean {
        val current = getActivePetIds(player).toMutableList()
        if (current.contains(pet.id)) return false
        if (current.size >= getMaxSlots(player)) return false
        current.add(pet.id)
        setActivePets(player, current.mapNotNull { id -> Pets.values().find { it.id == id } })
        return true
    }

    fun removeActivePet(player: Player, pet: Pet): Boolean {
        val current = getActivePetIds(player).toMutableList()
        if (!current.remove(pet.id)) return false
        setActivePets(player, current.mapNotNull { id -> Pets.values().find { it.id == id } })
        return true
    }

    fun hasActivePet(player: Player, pet: Pet): Boolean {
        return getActivePetIds(player).contains(pet.id)
    }

    fun getMaxSlots(player: Player): Int {
        for (perm in listOf("5", "3", "2", "1")) {
            if (player.hasPermission("petkuengsus.slots.$perm")) {
                return perm.toInt()
            }
        }
        return 1
    }

    fun distributeXP(player: Player, amounts: Map<Pet, Double>) {
        for ((pet, xp) in amounts) {
            val currentXP = player.getPetXP(pet)
            val currentLevel = player.getPetLevel(pet)
            val required = pet.getExpForLevel(currentLevel + 1)
            var newXP = currentXP + xp
            var newLevel = currentLevel

            while (newXP >= required && newLevel < pet.maxLevel) {
                newXP -= required
                newLevel++
            }

            if (newLevel >= pet.maxLevel) {
                newXP = 0.0
            }

            player.setPetLevel(pet, newLevel)
            player.setPetXP(pet, newXP)
        }
    }
}
