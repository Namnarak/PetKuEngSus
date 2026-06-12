package com.petkuengsus.petkuengsus.skilltree

import com.petkuengsus.petkuengsus.internal.ItemStackBuilder
import com.petkuengsus.petkuengsus.internal.SimpleItems
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Material
import org.bukkit.entity.Player

object SkillTreeGUI {
    fun open(player: Player, treeIndex: Int = 0) {
        val config = plugin.skillTreeConfig ?: run {
            player.sendMessage("§cNo skill tree config found.")
            return
        }

        if (treeIndex >= config.size) {
            player.sendMessage("§cInvalid skill tree.")
            return
        }

        val tree = config[treeIndex]
        val menu = SimpleMenu(
            (plugin.langYml.getMessage("skilltree.gui.title").replace("%tree%", tree.displayName)),
            6
        )

        for (node in tree.nodes) {
            val level = PlayerSkillTree.getNodeLevel(player, node.id)
            val canUnlock = PlayerSkillTree.canUnlock(player, node)

            val material = try {
                Material.valueOf(node.icon.uppercase())
            } catch (e: Exception) {
                if (level > 0) Material.LIME_DYE else Material.GRAY_DYE
            }

            val itemBuilder = ItemStackBuilder(material)
                .setDisplayName("${if (level > 0) "§a" else if (canUnlock) "§e" else "§c"}${node.displayName}")

            val lore = mutableListOf<String>()
            lore.add("§7Level: §f$level§7/§f${node.maxLevel}")
            if (node.description.isNotEmpty()) {
                lore.add("")
                lore.addAll(node.description.map { "§7$it" })
            }
            lore.add("")
            if (node.requiredLevel > 0) lore.add("§8Required Pet Level: §f${node.requiredLevel}")
            if (node.requiredPrestigeLevel > 0) lore.add("§8Required Prestige: §f${node.requiredPrestigeLevel}")
            if (node.costTalentPoints > 0) lore.add("§8Cost: §f${node.costTalentPoints} TP")
            lore.add("")
            if (level >= node.maxLevel) {
                lore.add("§a§lMAXED")
            } else if (canUnlock) {
                lore.add("§e§lCLICK to unlock")
            } else {
                lore.add("§cRequirements not met")
            }

            itemBuilder.setLore(lore)

            val slot = node.y * 9 + node.x
            if (slot in 0..53) {
                menu.setItem(slot, itemBuilder.build()) { _, e ->
                    if (e.isLeftClick && canUnlock) {
                        if (PlayerSkillTree.unlockNode(player, node)) {
                            player.sendMessage("§aUnlocked ${node.displayName}!")
                            open(player, treeIndex)
                        }
                    }
                }
            }
        }

        menu.open(player)
    }
}
