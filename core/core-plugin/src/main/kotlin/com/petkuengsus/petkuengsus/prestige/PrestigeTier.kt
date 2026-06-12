package com.petkuengsus.petkuengsus.prestige

import com.petkuengsus.petkuengsus.internal.SimpleConfig
import org.bukkit.entity.Player

data class PrestigeTier(
    val tier: Int,
    val displayName: String,
    val description: List<String>,
    val requiredLevel: Int,
    val requiredPets: Int,
    val requiredPrestigeLevel: Int,
    val cost: Double,
    val rewards: List<PrestigeReward>,
    val xpMultiplier: Double,
    val extraSlots: Int
) {
    companion object {
        fun fromConfig(section: SimpleConfig, tierNum: Int): PrestigeTier {
            return PrestigeTier(
                tier = tierNum,
                displayName = section.getString("name") ?: "Prestige $tierNum",
                description = section.getStringList("description"),
                requiredLevel = section.getInt("requirements.level", 0),
                requiredPets = section.getInt("requirements.pets", 0),
                requiredPrestigeLevel = section.getInt("requirements.prestige", 0),
                cost = section.getDouble("cost", 0.0),
                rewards = parseRewards(section.getSubsection("rewards")),
                xpMultiplier = section.getDouble("xp-multiplier", 1.0),
                extraSlots = section.getInt("extra-slots", 0)
            )
        }

        private fun parseRewards(section: SimpleConfig?): List<PrestigeReward> {
            val rewards = mutableListOf<PrestigeReward>()
            if (section == null) return rewards
            for (key in section.keys()) {
                val sub = section.getSubsection(key) ?: continue
                val type = sub.getString("type") ?: continue
                val value = sub.getString("value") ?: ""
                val amount = sub.getDouble("amount", 1.0)
                rewards.add(PrestigeReward(type, value, amount))
            }
            return rewards
        }
    }

    fun canPrestige(player: Player): Boolean {
        return true
    }
}

data class PrestigeReward(
    val type: String,
    val value: String,
    val amount: Double
)
