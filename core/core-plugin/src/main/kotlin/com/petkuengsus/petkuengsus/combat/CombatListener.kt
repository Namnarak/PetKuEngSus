package com.petkuengsus.petkuengsus.combat

import com.petkuengsus.petkuengsus.multipet.MultiPetManager
import com.petkuengsus.petkuengsus.pets.getPetLevel
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class CombatListener : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val target = event.entity as? org.bukkit.entity.LivingEntity ?: return

        val config = plugin.combatConfig ?: return
        if (config.isEmpty()) return

        val activePets = MultiPetManager.getActivePets(damager)
        if (activePets.isEmpty()) return

        for (pet in activePets) {
            val petLevel = damager.getPetLevel(pet)
            for (effect in config) {
                CombatEffectManager.applyEffect(target, damager, effect, petLevel)
            }
        }
    }
}
