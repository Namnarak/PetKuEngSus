package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.internal.toNiceString
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.givePetExperience
import com.petkuengsus.petkuengsus.pets.hasPet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object CommandGiveCurrentXP : PetSubcommand(
    "givecurrentxp",
    "petkuengsus.command.givecurrentxp"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("needs-player"))
            return true
        }

        if (args.size == 1) {
            sender.sendMessage(plugin.langYml.getMessage("needs-amount"))
            return true
        }

        val playerName = args[0]

        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return true
        }

        val pet = player.activePet

        if (pet == null) {
            sender.sendMessage(plugin.langYml.getMessage("no-pet"))
            return true
        }

        if (!player.hasPet(pet)) {
            sender.sendMessage(plugin.langYml.getMessage("doesnt-have-pet"))
            return true
        }

        val amount = args[1].toDoubleOrNull()

        if (amount == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return true
        }

        player.givePetExperience(
            pet,
            amount
        )

        sender.sendMessage(
            plugin.langYml.getMessage("gave-current-xp")
                .replace("%player%", player.name)
                .replace("%xp%", amount.toNiceString())
        )
        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 2) {
            return listOf("10", "100", "1000", "10000")
        }

        return emptyList()
    }
}
