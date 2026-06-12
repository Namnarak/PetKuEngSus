package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.api.event.PetAdoptEvent
import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.pets.hasPet
import com.petkuengsus.petkuengsus.pets.setPetLevel
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

object CommandGive : PetSubcommand(
    "give",
    "petkuengsus.command.give"
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

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(playerName)

        if (!player.hasPlayedBefore() && player !is Player) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return true
        }

        val pet = Pets.getByID(args[1])

        if (pet == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-pet"))
            return true
        }

        if (player.hasPet(pet)) {
            sender.sendMessage(plugin.langYml.getMessage("already-has-pet"))
            return true
        }

        val event = PetAdoptEvent(player, pet)
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            sender.sendMessage(plugin.langYml.getMessage("cancelled-adoption"))
            return true
        }

        player.setPetLevel(pet, 1)
        sender.sendMessage(
            plugin.langYml.getMessage("gave-pet")
                .replace("%player%", player.name ?: playerName)
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
