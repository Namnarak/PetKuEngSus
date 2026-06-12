package com.petkuengsus.petkuengsus.skilltree

import com.petkuengsus.petkuengsus.internal.SimpleConfig
import org.bukkit.entity.Player

data class SkillTree(
    val id: String,
    val displayName: String,
    val description: List<String>,
    val icon: String,
    val nodes: List<SkillTreeNode>
) {
    companion object {
        fun fromConfig(id: String, section: SimpleConfig): SkillTree {
            val nodesSection = section.getSubsection("nodes") ?: SimpleConfig(com.petkuengsus.petkuengsus.util.DummyFile)
            val nodes = mutableListOf<SkillTreeNode>()
            for (key in nodesSection.keys()) {
                val nodeSection = nodesSection.getSubsection(key)
                if (nodeSection != null) {
                    nodes.add(SkillTreeNode.fromConfig(key, nodeSection))
                }
            }
            return SkillTree(
                id = id,
                displayName = section.getString("name") ?: id,
                description = section.getStringList("description"),
                icon = section.getString("icon", "BOOK") ?: "BOOK",
                nodes = nodes.sortedBy { it.x + it.y * 100 }
            )
        }
    }
}

data class SkillTreeNode(
    val id: String,
    val displayName: String,
    val description: List<String>,
    val icon: String,
    val x: Int,
    val y: Int,
    val maxLevel: Int,
    val costTalentPoints: Int,
    val requiredLevel: Int,
    val requiredNodeIds: List<String>,
    val requiredPrestigeLevel: Int,
    val effects: Map<String, Double>
) {
    companion object {
        fun fromConfig(id: String, section: SimpleConfig): SkillTreeNode {
            return SkillTreeNode(
                id = id,
                displayName = section.getString("name") ?: id,
                description = section.getStringList("description"),
                icon = section.getString("icon", "PAPER") ?: "PAPER",
                x = section.getInt("position.x", 0),
                y = section.getInt("position.y", 0),
                maxLevel = section.getInt("max-level", 5),
                costTalentPoints = section.getInt("cost", 1),
                requiredLevel = section.getInt("requirements.level", 0),
                requiredNodeIds = section.getStringList("requirements.nodes"),
                requiredPrestigeLevel = section.getInt("requirements.prestige", 0),
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

    fun canUnlock(player: Player, unlockedNodes: Map<String, Int>): Boolean {
        val currentLevel = unlockedNodes[id] ?: 0
        if (currentLevel >= maxLevel) return false

        for (requiredId in requiredNodeIds) {
            if ((unlockedNodes[requiredId] ?: 0) <= 0) return false
        }

        return true
    }
}
