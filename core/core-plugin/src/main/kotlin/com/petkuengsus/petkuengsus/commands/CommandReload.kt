package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.internal.toNiceString
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.command.CommandSender

object CommandReload : PetSubcommand(
    "reload",
    "petkuengsus.command.reload"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        val start = System.currentTimeMillis()
        plugin.handleReload()
        val time = System.currentTimeMillis() - start
        sender.sendMessage(
            plugin.langYml.getMessage("reloaded")
                .replace("%time%", "${time}ms")
                .replace("%count%", Pets.values().size.toString())
        )
        return true
    }
}
