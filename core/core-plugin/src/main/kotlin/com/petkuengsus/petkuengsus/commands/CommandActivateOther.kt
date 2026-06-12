package com.petkuengsus.petkuengsus.commands

import com.petkuengsus.petkuengsus.api.event.PlayerPetSwapEvent
import com.petkuengsus.petkuengsus.internal.PetSubcommand
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.hasPet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandActivateOther : PetSubcommand(
    "activate",
    "petkuengsus.command.activateother"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("needs-player"))
            return true
        }

        if (args.size == 1) {
            sender.sendMessage(plugin.langYml.getMessage("needs-pet"))
            return true
        }

        val playerName = args[0]

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(playerName)

        if (!player.hasPlayedBefore() && player !is Player) {
            sender.sendMessage(
                plugin.langYml.getMessage("invalid-player")
                    .replace("%player%", playerName)
            )
            return true
        }

        val pet = Pets.getByID(args[1])

        if (pet == null) {
            sender.sendMessage(
                plugin.langYml.getMessage("invalid-pet")
                    .replace("%player%", playerName)
            )
            return true
        }

        if (!player.hasPet(pet)) {
            sender.sendMessage(
                plugin.langYml.getMessage("doesnt-have-pet")
                    .replace("%player%", playerName)
            )
            return true
        }

        if (player.activePet == pet) {
            sender.sendMessage(
                plugin.langYml.getMessage("pet-already-active")
                    .replace("%player%", playerName)
            )
            return true
        }

        sender.sendMessage(
            plugin.langYml.getMessage("activated-pet")
                .replace("%pet%", pet.name)
                .replace("%player%", playerName)
        )
        player.activePet?.let { oldPet ->
            val event = PlayerPetSwapEvent(player, pet, oldPet)
            Bukkit.getServer().pluginManager.callEvent(event)
            if (event.isCancelled) {
                sender.sendMessage(plugin.langYml.getMessage("cancelled-swap-other"))
                return true
            }
        }
        player.activePet = pet
        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 2) {
            return Pets.values().map { it.id }
        }

        return emptyList()
    }
}
