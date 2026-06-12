package com.petkuengsus.petkuengsus.talents

import com.petkuengsus.petkuengsus.internal.SimpleConfig

data class Talent(
    val id: String,
    val displayName: String,
    val description: List<String>,
    val icon: String,
    val maxLevel: Int,
    val costPerLevel: Int,
    val effects: Map<String, Double>
) {
    companion object {
        fun fromConfig(id: String, section: SimpleConfig): Talent {
            return Talent(
                id = id,
                displayName = section.getString("name") ?: id,
                description = section.getStringList("description"),
                icon = section.getString("icon", "NETHER_STAR") ?: "NETHER_STAR",
                maxLevel = section.getInt("max-level", 10),
                costPerLevel = section.getInt("cost-per-level", 1),
                effects = parseEffects(section.getSubsection("effects"))
            )
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
}
