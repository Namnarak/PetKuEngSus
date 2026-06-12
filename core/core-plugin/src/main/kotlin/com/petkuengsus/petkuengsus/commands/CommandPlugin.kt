package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetCommand
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.command.CommandSender

object CommandPlugin : PetCommand(
    "petkuengsus",
    "petkuengsus.command.petkuengsus"
) {
    init {
        registerSubcommand(CommandReload)
        registerSubcommand(CommandGive)
        registerSubcommand(CommandTake)
        registerSubcommand(CommandGiveEgg)
        registerSubcommand(CommandGiveXP)
        registerSubcommand(CommandReset)
        registerSubcommand(CommandGiveCurrentXP)
        registerSubcommand(CommandActivateOther)
        registerSubcommand(CommandDeactivateOther)
    }

    override fun handle(sender: CommandSender, args: Array<out String>): Boolean {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
        return true
    }
}
