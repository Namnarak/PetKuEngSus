package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetCommand
import com.petkuengsus.petkuengsus.pets.PetsGUI
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandPets : PetCommand(
    "pets",
    "petkuengsus.command.pets"
) {
    init {
        registerSubcommand(CommandActivate)
        registerSubcommand(CommandDeactivate)
        registerSubcommand(CommandMount)
        registerSubcommand(CommandTransform)
    }

    override fun handle(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender as Player
        PetsGUI.open(player)
        return true
    }
}
