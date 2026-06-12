package com.petkuengsus.petkuengsus.talents

import com.petkuengsus.petkuengsus.internal.ItemStackBuilder
import com.petkuengsus.petkuengsus.internal.SimpleItems
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Material
import org.bukkit.entity.Player

object TalentsGUI {
    fun open(player: Player) {
        val config = plugin.talentsConfig ?: run {
            player.sendMessage("§cNo talents config found.")
            return
        }

        val menu = SimpleMenu(
            plugin.langYml.getMessage("talents.gui.title"),
            (config.size / 9 + 1)
                .coerceIn(1, 6)
        )

        val points = PlayerTalents.getAvailablePoints(player)

        for ((index, talent) in config.withIndex()) {
            val level = PlayerTalents.getAllocation(player, talent.id)
            val maxed = level >= talent.maxLevel

            val icon = try {
                Material.valueOf(talent.icon.uppercase())
            } catch (e: Exception) {
                Material.NETHER_STAR
            }

            val itemBuilder = ItemStackBuilder(icon)
                .setDisplayName("${if (maxed) "§a" else "§e"}${talent.displayName}")

            val lore = mutableListOf<String>()
            lore.add("§7Level: §f$level§7/§f${talent.maxLevel}")
            lore.add("§7Cost per level: §f${talent.costPerLevel} points")
            if (talent.description.isNotEmpty()) {
                lore.add("")
                lore.addAll(talent.description.map { "§7$it" })
            }
            if (talent.effects.isNotEmpty()) {
                lore.add("")
                lore.add("§6Effects:")
                for ((key, value) in talent.effects) {
                    val currentValue = value * level
                    lore.add(" §7$key: §f+${String.format("%.1f", currentValue)}")
                }
            }
            lore.add("")
            if (maxed) {
                lore.add("§a§lMAXED")
            } else if (points >= talent.costPerLevel) {
                lore.add("§e§lCLICK to upgrade")
            } else {
                lore.add("§cNot enough points!")
            }

            itemBuilder.setLore(lore)

            menu.setItem(index, itemBuilder.build()) { _, e ->
                if (e.isLeftClick && !maxed && points >= talent.costPerLevel) {
                    if (PlayerTalents.allocate(player, talent)) {
                        player.sendMessage("§aUpgraded ${talent.displayName}!")
                        open(player)
                    }
                }
            }
        }

        menu.open(player)
    }
}
