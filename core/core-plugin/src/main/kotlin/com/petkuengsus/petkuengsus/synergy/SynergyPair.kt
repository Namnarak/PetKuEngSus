package com.petkuengsus.petkuengsus.synergy

import com.petkuengsus.petkuengsus.internal.SimpleConfig
import com.petkuengsus.petkuengsus.pets.getPetLevel
import org.bukkit.entity.Player

data class SynergyPair(
    val id: String,
    val displayName: String,
    val description: List<String>,
    val petIds: List<String>,
    val requiredLevels: Map<String, Int>,
    val effects: Map<String, Double>
) {
    companion object {
        fun fromConfig(id: String, section: SimpleConfig): SynergyPair {
            return SynergyPair(
                id = id,
                displayName = section.getString("name") ?: id,
                description = section.getStringList("description"),
                petIds = section.getStringList("pets"),
                requiredLevels = parseRequiredLevels(section.getSubsection("required-levels")),
                effects = parseEffects(section.getSubsection("effects"))
            )
        }

        private fun parseRequiredLevels(section: SimpleConfig?): Map<String, Int> {
            val map = mutableMapOf<String, Int>()
            if (section == null) return map
            for (key in section.keys()) {
                map[key] = section.getInt(key, 1)
            }
            return map
        }

        private fun parseEffects(section: SimpleConfig?): Map<String, Double> {
            val map = mutableMapOf<String, Double>()
            if (section == null) return map
            for (key in section.keys()) {
                map[key] = section.getDouble(key, 0.0)
            }
            return map
        }
    }

    fun isActive(player: Player): Boolean {
        val activePets = com.petkuengsus.petkuengsus.multipet.MultiPetManager.getActivePets(player)
        val activeIds = activePets.map { it.id }
        if (!activeIds.containsAll(petIds)) return false

        for ((petId, requiredLevel) in requiredLevels) {
            val pet = activePets.find { it.id == petId } ?: return false
            val level = player.getPetLevel(pet)
            if (level < requiredLevel) return false
        }
        return true
    }
}
