package com.petkuengsus.petkuengsus.talents

import com.petkuengsus.petkuengsus.api.event.PlayerPetLevelUpEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TalentListener : Listener {

    @EventHandler
    fun onPetLevelUp(event: PlayerPetLevelUpEvent) {
        val player = event.player
        PlayerTalents.addPoints(player, 1)
    }
}
