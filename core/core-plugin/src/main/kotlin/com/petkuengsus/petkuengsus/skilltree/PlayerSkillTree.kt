package com.petkuengsus.petkuengsus.skilltree

import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.getPetLevel
import com.petkuengsus.petkuengsus.plugin
import com.petkuengsus.petkuengsus.storage.load
import com.petkuengsus.petkuengsus.storage.save
import org.bukkit.entity.Player

object PlayerSkillTree {
    data class SkillTreeData(
        val nodeLevels: MutableMap<String, Int> = mutableMapOf()
    )

    private fun getData(player: Player): SkillTreeData {
        return plugin.storageProvider!!.load("skilltree_${player.uniqueId}", SkillTreeData())
    }

    private fun saveData(player: Player, data: SkillTreeData) {
        plugin.storageProvider!!.save("skilltree_${player.uniqueId}", data)
    }

    fun getNodeLevel(player: Player, nodeId: String): Int {
        val data = getData(player)
        return data.nodeLevels[nodeId] ?: 0
    }

    fun setNodeLevel(player: Player, nodeId: String, level: Int) {
        val data = getData(player)
        data.nodeLevels[nodeId] = level
        saveData(player, data)
    }

    fun canUnlock(player: Player, node: SkillTreeNode): Boolean {
        val currentLevel = getNodeLevel(player, node.id)
        if (currentLevel >= node.maxLevel) return false

        val talentsPoints = com.petkuengsus.petkuengsus.talents.PlayerTalents.getAvailablePoints(player)
        if (talentsPoints < node.costTalentPoints) return false

        if (node.requiredLevel > 0) {
            val pet = player.activePet
            if (pet != null) {
                val petLevel = player.getPetLevel(pet)
                if (petLevel < node.requiredLevel) return false
            }
        }

        if (node.requiredPrestigeLevel > 0) {
            val prestigeLevel = com.petkuengsus.petkuengsus.prestige.PlayerPrestige.getLevel(player)
            if (prestigeLevel < node.requiredPrestigeLevel) return false
        }

        val data = getData(player)
        for (requiredId in node.requiredNodeIds) {
            if ((data.nodeLevels[requiredId] ?: 0) <= 0) return false
        }

        return true
    }

    fun unlockNode(player: Player, node: SkillTreeNode): Boolean {
        if (!canUnlock(player, node)) return false

        val currentLevel = getNodeLevel(player, node.id)
        setNodeLevel(player, node.id, currentLevel + 1)
        com.petkuengsus.petkuengsus.talents.PlayerTalents.setAvailablePoints(
            player,
            com.petkuengsus.petkuengsus.talents.PlayerTalents.getAvailablePoints(player) - node.costTalentPoints
        )
        return true
    }

    fun getEffectValue(player: Player, effectKey: String): Double {
        val config = plugin.skillTreeConfig ?: return 0.0
        var total = 0.0
        val data = getData(player)
        for (tree in config) {
            for (node in tree.nodes) {
                val level = data.nodeLevels[node.id] ?: 0
                if (level > 0 && node.effects.containsKey(effectKey)) {
                    total += node.effects[effectKey]!! * level
                }
            }
        }
        return total
    }
}
