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
import org.bukkit.util.StringUtil

object CommandActivate : PetSubcommand(
    "activate",
    "petkuengsus.command.activate"
) {
    override fun onCommand(sender: CommandSender, args: Array<out String>): Boolean {
        val player = sender as Player

        if (args.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("needs-pet"))
            return true
        }

        val id = args[0]

        val pet = Pets.getByID(id)

        if (pet == null || !player.hasPet(pet)) {
            player.sendMessage(plugin.langYml.getMessage("invalid-pet"))
            return true
        }

        if (player.activePet == pet) {
            player.sendMessage(plugin.langYml.getMessage("pet-already-active"))
            return true
        }

        if (!pet.canActivate(player)) {
            player.sendMessage(
                plugin.langYml.getMessage("cannot-activate-pet")
                    .replace("%pet%", pet.name)
            )
            return true
        }

        player.sendMessage(
            plugin.langYml.getMessage("activated-pet")
                .replace("%pet%", pet.name)
        )
        player.activePet?.let { oldPet ->
            val event = PlayerPetSwapEvent(player, pet, oldPet)
            Bukkit.getServer().pluginManager.callEvent(event)
            if (event.isCancelled) {
                player.sendMessage(plugin.langYml.getMessage("cancelled-swap"))
                return true
            }
        }
        player.activePet = pet
        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (sender !is Player) {
            return emptyList()
        }

        val completions = mutableListOf<String>()
        if (args.isEmpty()) {
            return Pets.values().filter { sender.hasPet(it) }.map { it.id }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Pets.values().filter { sender.hasPet(it) }.map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
