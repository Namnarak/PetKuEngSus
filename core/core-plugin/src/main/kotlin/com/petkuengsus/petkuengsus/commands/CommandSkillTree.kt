package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetCommand
import com.petkuengsus.petkuengsus.internal.getPlayer
import com.petkuengsus.petkuengsus.skilltree.SkillTreeGUI
import org.bukkit.command.CommandSender

object CommandSkillTree : PetCommand("skilltree", "petkuengsus.command.skilltree") {
    override fun handle(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender.getPlayer() ?: run {
            sender.sendMessage("§cPlayers only!")
            return true
        }
        val treeIndex = args.firstOrNull()?.toIntOrNull() ?: 0
        SkillTreeGUI.open(player, treeIndex)
        return true
    }
}
