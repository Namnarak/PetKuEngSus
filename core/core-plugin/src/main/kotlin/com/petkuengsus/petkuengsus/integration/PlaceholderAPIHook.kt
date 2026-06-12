package com.petkuengsus.petkuengsus.integration

import com.petkuengsus.petkuengsus.PetKuEngSusPlugin
import com.petkuengsus.petkuengsus.breakthrough.PlayerBreakthrough
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.getPetLevel
import com.petkuengsus.petkuengsus.pets.getPetXP
import com.petkuengsus.petkuengsus.pets.hasPet
import com.petkuengsus.petkuengsus.multipet.MultiPetManager
import com.petkuengsus.petkuengsus.prestige.PlayerPrestige
import com.petkuengsus.petkuengsus.talents.PlayerTalents
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: PetKuEngSusPlugin) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "petkuengsus"

    override fun getAuthor(): String = "PetKuEngSus"

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return ""

        val args = params.split("_")
        if (args.isEmpty()) return ""

        return when (args[0].lowercase()) {
            "activepet" -> player.activePet?.name ?: "None"
            "activepet_level" -> {
                val pet = player.activePet ?: return "0"
                player.getPetLevel(pet).toString()
            }
            "activepet_xp" -> {
                val pet = player.activePet ?: return "0"
                player.getPetXP(pet).toString()
            }
            "activepet_xp_remaining" -> {
                val pet = player.activePet ?: return "0"
                val level = player.getPetLevel(pet)
                val currentXP = player.getPetXP(pet)
                val required = pet.getExpForLevel(level + 1)
                (required - currentXP).toString()
            }
            "activepet_xp_percent" -> {
                val pet = player.activePet ?: return "0"
                val level = player.getPetLevel(pet)
                val currentXP = player.getPetXP(pet)
                val required = pet.getExpForLevel(level + 1)
                if (required <= 0) return "100"
                ((currentXP.toDouble() / required) * 100).toInt().toString()
            }
            "activepet_name" -> player.activePet?.name ?: "None"

            "multipet_count" -> MultiPetManager.getActivePets(player).size.toString()
            "multipet_maxslots" -> MultiPetManager.getMaxSlots(player).toString()
            "multipet_slots_remaining" -> {
                val max = MultiPetManager.getMaxSlots(player)
                val used = MultiPetManager.getActivePets(player).size
                (max - used).toString()
            }

            "breakthrough_level" -> PlayerBreakthrough.getLevel(player).toString()
            "breakthrough_maxlevel" -> {
                val tiers = plugin.breakthroughConfig
                if (tiers == null) "0" else tiers.size.toString()
            }

            "talent_points" -> PlayerTalents.getAvailablePoints(player).toString()
            "talent_spent" -> PlayerTalents.getSpentPoints(player).toString()
            "talent_total" -> PlayerTalents.getTotalEarned(player).toString()

            "prestige_level" -> PlayerPrestige.getLevel(player).toString()
            "prestige_maxlevel" -> {
                val config = plugin.prestigeConfig
                if (config == null) "0" else config.size.toString()
            }
            "prestige_multiplier" -> String.format("%.2f", PlayerPrestige.getMultiplier(player))

            "total_pets" -> {
                val registry = com.petkuengsus.petkuengsus.pets.Pets
                registry.values().size.toString()
            }
            "unlocked_pets" -> {
                val all = com.petkuengsus.petkuengsus.pets.Pets.values()
                all.count { player.hasPet(it) }.toString()
            }

            else -> null
        }
    }

    private fun parsePetPlaceholder(player: Player, petName: String, remainder: List<String>): String? {
        val pet = com.petkuengsus.petkuengsus.pets.Pets.values().find {
            it.name.equals(petName, ignoreCase = true)
        } ?: return null

        return when (remainder.getOrNull(0)?.lowercase()) {
            "level" -> player.getPetLevel(pet).toString()
            "xp" -> player.getPetXP(pet).toString()
            "unlocked" -> player.hasPet(pet).toString()
            else -> null
        }
    }
}
