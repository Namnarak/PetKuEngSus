package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.mount.MountManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandMount : PetSubcommand(
    "mount",
    "petkuengsus.command.mount"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender as Player
        if (MountManager.isMounted(player)) {
            MountManager.dismount(player)
        } else {
            MountManager.mount(player)
        }
        return true
    }
}
