package com.petkuengsus.petkuengsus.breakthrough

import com.petkuengsus.petkuengsus.internal.ItemStackBuilder
import com.petkuengsus.petkuengsus.internal.LangYml
import com.petkuengsus.petkuengsus.internal.SimpleItems
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.internal.formatBalance
import com.petkuengsus.petkuengsus.integration.VaultHook
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Material
import org.bukkit.entity.Player

object BreakthroughGUI {
    fun open(player: Player) {
        val config = plugin.breakthroughConfig ?: run {
            player.sendMessage("§cNo breakthrough config found.")
            return
        }

        val menu = SimpleMenu(
            plugin.langYml.getMessage("breakthrough.gui.title"),
            6
        )

        val currentLevel = PlayerBreakthrough.getLevel(player)

        for ((index, tier) in config.withIndex()) {
            val unlocked = index <= currentLevel
            val canUnlock = index == currentLevel + 1
            val locked = index > currentLevel + 1

            val material = if (unlocked) Material.GREEN_STAINED_GLASS_PANE
            else if (canUnlock) Material.LIME_STAINED_GLASS_PANE
            else Material.RED_STAINED_GLASS_PANE

            val itemBuilder = ItemStackBuilder(material)
                .setDisplayName("${if (unlocked) "§a" else if (canUnlock) "§e" else "§c"}${tier.displayName}")

            val lore = mutableListOf<String>()
            if (unlocked) {
                lore.add("§a§lUNLOCKED")
            } else {
                if (tier.requiredLevel > 0) lore.add("§7Required Pet Level: §f${tier.requiredLevel}")
                if (tier.requiredPets > 0) lore.add("§7Required Pets: §f${tier.requiredPets}")
                if (tier.cost > 0.0) lore.add("§7Cost: §f$${tier.cost}")
                if (canUnlock) lore.add("")
            }

            if (tier.description.isNotEmpty()) {
                if (lore.isNotEmpty()) lore.add("")
                lore.addAll(tier.description.map { "§7$it".formatBalance() })
            }

            itemBuilder.setLore(lore)

            menu.setItem(index, itemBuilder.build()) { _, e ->
                if (e.isLeftClick && canUnlock) {
                    attemptBreakthrough(player, tier)
                }
            }
        }

        menu.open(player)
    }

    private fun attemptBreakthrough(player: Player, tier: BreakthroughTier) {
        if (tier.cost > 0.0) {
            val vault = VaultHook
            if (vault.enabled && !vault.hasBalance(player, tier.cost)) {
                player.sendMessage("§cYou need $${tier.cost} to breakthrough!")
                return
            }
            vault.withdraw(player, tier.cost)
        }

        PlayerBreakthrough.setLevel(player, tier.tier)
        PlayerBreakthrough.applyRewards(player, tier)
        player.sendMessage("§aBreakthrough to ${tier.displayName}§a complete!")
        open(player)
    }
}
