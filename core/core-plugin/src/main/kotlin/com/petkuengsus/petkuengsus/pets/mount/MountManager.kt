package com.petkuengsus.petkuengsus.pets.mount

import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.pets.PetDisplay
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import java.util.*

object MountManager : Listener {
    private val mountedPlayers = mutableMapOf<UUID, MountSession>()

    private data class MountSession(
        val petEntityId: Int,
        val wasSmall: Boolean
    )

    fun mount(player: Player): Boolean {
        val pet = player.activePet ?: run {
            player.sendMessage(StringUtils.format(plugin.langYml.getMessage("mount.no-active-pet")))
            return false
        }

        if (player.uniqueId in mountedPlayers) {
            player.sendMessage(StringUtils.format(plugin.langYml.getMessage("mount.already-mounted")))
            return false
        }

        val entity = PetDisplay.get(player) ?: run {
            player.sendMessage(StringUtils.format(plugin.langYml.getMessage("mount.no-entity")))
            return false
        }

        if (!entity.passengers.isEmpty()) {
            player.sendMessage(StringUtils.format(plugin.langYml.getMessage("mount.pet-occupied")))
            return false
        }

        val wasSmall = if (entity is ArmorStand) {
            entity.isSmall = false
            entity.setGravity(true)
            true
        } else false

        val mountLoc = player.location.clone().apply { y -= 0.5 }
        entity.teleport(mountLoc)
        entity.addPassenger(player)

        mountedPlayers[player.uniqueId] = MountSession(entity.entityId, wasSmall)

        player.sendMessage(
            StringUtils.format(
                plugin.langYml.getMessage("mount.mounted")
                    .replace("%pet%", pet.name)
            )
        )
        return true
    }

    fun dismount(player: Player, teleport: Boolean = true) {
        val session = mountedPlayers.remove(player.uniqueId) ?: return
        val entity = PetDisplay.get(player) ?: return

        entity.removePassenger(player)

        if (entity is ArmorStand && session.wasSmall) {
            entity.isSmall = true
            entity.setGravity(false)
        }

        if (teleport) {
            val landLoc = player.location.clone().apply { y += 1.0 }
            player.teleport(landLoc)
        }

        val pet = player.activePet
        if (pet != null) {
            player.sendMessage(
                StringUtils.format(
                    plugin.langYml.getMessage("mount.dismounted")
                        .replace("%pet%", pet.name)
                )
            )
        }
    }

    fun isMounted(player: Player): Boolean = player.uniqueId in mountedPlayers

    private fun cleanup(player: Player) {
        val session = mountedPlayers.remove(player.uniqueId) ?: return
        val entity = PetDisplay.get(player) ?: return
        entity.removePassenger(player)
        if (entity is ArmorStand && session.wasSmall) {
            entity.isSmall = true
            entity.setGravity(false)
        }
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) return
        val player = event.player
        if (player.uniqueId !in mountedPlayers) return
        dismount(player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        cleanup(event.player)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        cleanup(event.player)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        cleanup(event.player)
    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        cleanup(event.player)
    }
}
