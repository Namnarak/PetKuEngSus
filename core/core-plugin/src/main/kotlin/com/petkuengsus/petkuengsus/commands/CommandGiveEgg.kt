package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object CommandGiveEgg : PetSubcommand(
    "giveegg",
    "petkuengsus.command.giveegg"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("needs-player"))
            return true
        }

        if (args.size == 1) {
            sender.sendMessage(plugin.langYml.getMessage("needs-pet"))
            return true
        }

        val playerName = args[0]

        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return true
        }

        val pet = Pets.getByID(args[1])

        val egg = pet?.spawnEgg

        if (pet == null || egg == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-pet"))
            return true
        }

        val leftover = player.inventory.addItem(egg)
        leftover.values.forEach { player.world.dropItem(player.location, it) }

        sender.sendMessage(
            plugin.langYml.getMessage("gave-pet-egg")
                .replace("%player%", player.name)
                .replace("%pet%", pet.name)
        )
        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        val completions = mutableListOf<String>()
        if (args.isEmpty()) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                completions
            )
            return completions
        }

        if (args.size == 2) {
            StringUtil.copyPartialMatches(
                args[1],
                Pets.values().map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
