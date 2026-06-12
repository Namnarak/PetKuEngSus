package com.petkuengsus.petkuengsus.prestige

import com.petkuengsus.petkuengsus.internal.ItemStackBuilder
import com.petkuengsus.petkuengsus.internal.LangYml
import com.petkuengsus.petkuengsus.internal.SimpleItems
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.internal.formatBalance
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Material
import org.bukkit.entity.Player

object PrestigeGUI {
    fun open(player: Player) {
        val config = plugin.prestigeConfig ?: run {
            player.sendMessage("§cNo prestige config found.")
            return
        }

        val menu = SimpleMenu(
            plugin.langYml.getMessage("prestige.gui.title"),
            6
        )

        val currentLevel = PlayerPrestige.getLevel(player)

        for ((index, tier) in config.withIndex()) {
            val unlocked = index <= currentLevel
            val canPrestige = index == currentLevel
            val locked = index > currentLevel

            val material = if (unlocked) Material.GREEN_STAINED_GLASS_PANE
            else if (canPrestige) Material.LIME_STAINED_GLASS_PANE
            else Material.RED_STAINED_GLASS_PANE

            val itemBuilder = ItemStackBuilder(material)
                .setDisplayName("${if (unlocked) "§a" else if (canPrestige) "§e" else "§c"}${tier.displayName}")

            val lore = mutableListOf<String>()
            if (unlocked) {
                lore.add("§a§lCOMPLETED")
            } else {
                if (tier.requiredLevel > 0) lore.add("§7Required Max Level: §f${tier.requiredLevel}")
                if (tier.requiredPets > 0) lore.add("§7Required Pets: §f${tier.requiredPets}")
                if (tier.cost > 0.0) lore.add("§7Cost: §f$${tier.cost}")
                lore.add("")
                lore.add("§6XP Multiplier: §f${tier.xpMultiplier}x")
                if (tier.extraSlots > 0) lore.add("§6Extra Slots: §f+${tier.extraSlots}")
                if (canPrestige) lore.add("")
            }

            if (tier.description.isNotEmpty()) {
                lore.addAll(tier.description.map { "§7$it" })
            }

            if (canPrestige) {
                lore.add("")
                lore.add("§e§lCLICK to prestige!")
            } else if (locked) {
                lore.add("")
                lore.add("§cLocked")
            }

            itemBuilder.setLore(lore)

            menu.setItem(index, itemBuilder.build()) { _, e ->
                if (e.isLeftClick && canPrestige) {
                    if (PlayerPrestige.canPrestige(player)) {
                        doPrestigeConfirm(player, tier)
                    } else {
                        player.sendMessage("§cYou don't meet the requirements to prestige!")
                    }
                }
            }
        }

        menu.open(player)
    }

    private fun doPrestigeConfirm(player: Player, tier: PrestigeTier) {
        PlayerPrestige.doPrestige(player)
        player.sendMessage("§a§lPRESTIGE COMPLETE!")
        player.sendMessage("§7You have reached ${tier.displayName}§7!")
        open(player)
    }
}
