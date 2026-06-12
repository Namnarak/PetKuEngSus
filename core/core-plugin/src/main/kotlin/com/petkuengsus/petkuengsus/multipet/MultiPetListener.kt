package com.petkuengsus.petkuengsus.multipet

import com.petkuengsus.petkuengsus.PetKuEngSusPlugin
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class MultiPetListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val activeIds = MultiPetManager.getActivePetIds(player)
        val valid = activeIds.filter { id ->
            Pets.values().any { it.id == id }
        }
        if (valid.size != activeIds.size) {
            MultiPetManager.setActivePets(player, valid.mapNotNull { id ->
                Pets.values().find { it.id == id }
            })
        }
    }
}
