package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetCommand
import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.internal.getPlayer
import com.petkuengsus.petkuengsus.talents.PlayerTalents
import com.petkuengsus.petkuengsus.talents.TalentsGUI
import org.bukkit.command.CommandSender

object CommandTalents : PetCommand("talents", "petkuengsus.command.talents") {
    init {
        registerSubcommand(object : PetSubcommand("reset", "petkuengsus.talent.reset") {
            override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
                val player = sender.getPlayer() ?: run {
                    sender.sendMessage("§cPlayers only!")
                    return true
                }
                PlayerTalents.resetAllocations(player)
                player.sendMessage("§aTalent points have been reset!")
                return true
            }
        })

        registerSubcommand(object : PetSubcommand("add", "petkuengsus.command.talents.admin") {
            override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
                if (args.size < 2) {
                    sender.sendMessage("§cUsage: /talents add <player> <amount>")
                    return true
                }
                val target = sender.server.getPlayer(args[0])
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!")
                    return true
                }
                val amount = args[1].toIntOrNull() ?: 1
                PlayerTalents.addPoints(target, amount)
                sender.sendMessage("§aAdded $amount talent points to ${target.name}")
                return true
            }
        })
    }

    override fun handle(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender.getPlayer() ?: run {
            sender.sendMessage("§cPlayers only!")
            return true
        }
        TalentsGUI.open(player)
        return true
    }
}
