package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetCommand
import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.internal.getPlayer
import com.petkuengsus.petkuengsus.prestige.PlayerPrestige
import com.petkuengsus.petkuengsus.prestige.PrestigeGUI
import org.bukkit.command.CommandSender

object CommandPrestige : PetCommand("prestige", "petkuengsus.command.prestige") {
    init {
        registerSubcommand(object : PetSubcommand("set", "petkuengsus.command.prestige.admin") {
            override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
                if (args.size < 2) {
                    sender.sendMessage("§cUsage: /prestige set <player> <level>")
                    return true
                }
                val target = sender.server.getPlayer(args[0])
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!")
                    return true
                }
                val level = args[1].toIntOrNull() ?: 0
                PlayerPrestige.setLevel(target, level)
                sender.sendMessage("§aSet ${target.name}'s prestige level to $level")
                return true
            }
        })
    }

    override fun handle(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender.getPlayer() ?: run {
            sender.sendMessage("§cPlayers only!")
            return true
        }
        PrestigeGUI.open(player)
        return true
    }
}
