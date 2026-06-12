package com.petkuengsus.petkuengsus.synergy

import com.petkuengsus.petkuengsus.plugin
import org.bukkit.entity.Player

object SynergyManager {
    fun getActiveSynergies(player: Player): List<SynergyPair> {
        val config = plugin.synergyConfig ?: return emptyList()
        return config.filter { it.isActive(player) }
    }

    fun getEffectValue(player: Player, effectKey: String): Double {
        val synergies = getActiveSynergies(player)
        var total = 0.0
        for (synergy in synergies) {
            total += synergy.effects[effectKey] ?: 0.0
        }
        return total
    }
}
