package com.petkuengsus.petkuengsus.breakthrough

import com.petkuengsus.petkuengsus.internal.SimpleConfig
import org.bukkit.entity.Player

data class BreakthroughTier(
    val tier: Int,
    val displayName: String,
    val description: List<String>,
    val requiredLevel: Int,
    val requiredPets: Int,
    val requiredTiers: Map<String, Int>,
    val rewards: List<BreakthroughReward>,
    val cost: Double
) {
    companion object {
        fun fromConfig(section: SimpleConfig, tierNum: Int): BreakthroughTier {
            return BreakthroughTier(
                tier = tierNum,
                displayName = section.getString("name") ?: "Tier $tierNum",
                description = section.getStringList("description"),
                requiredLevel = section.getInt("requirements.level", 0),
                requiredPets = section.getInt("requirements.pets", 0),
                requiredTiers = parseTierRequirements(section.getSubsection("requirements.tiers")),
                rewards = parseRewards(section.getSubsection("rewards")),
                cost = section.getDouble("cost", 0.0)
            )
        }

        private fun parseTierRequirements(section: SimpleConfig?): Map<String, Int> {
            val map = mutableMapOf<String, Int>()
            if (section == null) return map
            for (key in section.keys()) {
                map[key] = section.getInt(key, 0)
            }
            return map
        }

        private fun parseRewards(section: SimpleConfig?): List<BreakthroughReward> {
            val rewards = mutableListOf<BreakthroughReward>()
            if (section == null) return rewards
            for (key in section.keys()) {
                val sub = section.getSubsection(key) ?: continue
                val type = sub.getString("type") ?: continue
                val value = sub.getString("value") ?: ""
                val amount = sub.getDouble("amount", 1.0)
                rewards.add(BreakthroughReward(type, value, amount))
            }
            return rewards
        }
    }

    fun canTier(player: Player): Boolean {
        return true
    }
}

data class BreakthroughReward(
    val type: String,
    val value: String,
    val amount: Double
)
