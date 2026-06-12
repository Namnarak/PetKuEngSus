package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandDeactivateOther : PetSubcommand(
    "deactivate",
    "petkuengsus.command.deactivateother"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("needs-player"))
            return true
        }

        val playerName = args[0]

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(playerName)

        if (!player.hasPlayedBefore() && player !is Player) {
            sender.sendMessage(
                plugin.langYml.getMessage("invalid-player")
                    .replace("%player%", playerName)
            )
            return true
        }

        if (player.activePet == null) {
            sender.sendMessage(
                plugin.langYml.getMessage("no-pet-active")
                    .replace("%player%", playerName)
            )
            return true
        }

        sender.sendMessage(
            plugin.langYml.getMessage("deactivated-pet")
                .replace("%pet%", player.activePet?.name ?: "")
                .replace("%player%", playerName)
        )

        player.activePet = null
        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        return emptyList()
    }
}
