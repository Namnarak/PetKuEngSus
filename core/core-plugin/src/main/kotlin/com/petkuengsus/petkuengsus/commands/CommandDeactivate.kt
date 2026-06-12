package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandDeactivate : PetSubcommand(
    "deactivate",
    "petkuengsus.command.deactivate"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender as Player

        if (player.activePet == null) {
            player.sendMessage(plugin.langYml.getMessage("no-pet-active"))
            return true
        }

        player.sendMessage(
            plugin.langYml.getMessage("deactivated-pet")
                .replace("%pet%", player.activePet?.name ?: "")
        )

        player.activePet = null
        return true
    }
}
