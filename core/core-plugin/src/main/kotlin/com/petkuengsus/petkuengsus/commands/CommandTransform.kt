package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.transform.TransformationManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.plugin

object CommandTransform : PetSubcommand(
    "transform",
    "petkuengsus.command.transform"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender as Player
        if (player.activePet == null) {
            player.sendMessage(StringUtils.format(plugin.langYml.getMessage("mount.no-active-pet") ?: "&cYou don't have an active pet."))
            return true
        }
        
        val newType = TransformationManager.cycleModelType(player)
        player.sendMessage(StringUtils.format("&aTransformed your pet to use the &e$newType&a model!"))
        return true
    }
}
