package com.petkuengsus.petkuengsus.prestige

import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.getPetLevel
import com.petkuengsus.petkuengsus.pets.hasPet
import com.petkuengsus.petkuengsus.pets.setPetLevel
import com.petkuengsus.petkuengsus.pets.setPetXP
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object PlayerPrestige {
    private val prestigeLevelKey by lazy {
        com.petkuengsus.petkuengsus.internal.DataKeys.get("prestige_level", plugin)
    }

    fun getLevel(player: Player): Int {
        val pdc = player.persistentDataContainer
        return pdc.get(prestigeLevelKey, PersistentDataType.INTEGER) ?: 0
    }

    fun setLevel(player: Player, level: Int) {
        val pdc = player.persistentDataContainer
        pdc.set(prestigeLevelKey, PersistentDataType.INTEGER, level)
    }

    fun canPrestige(player: Player): Boolean {
        val config = plugin.prestigeConfig ?: return false
        val currentLevel = getLevel(player)
        val nextTier = config.getOrNull(currentLevel) ?: return false

        if (nextTier.requiredPrestigeLevel > currentLevel) return false

        val hasLevel = nextTier.requiredLevel <= 0 || player.activePet?.let {
            player.getPetLevel(it)
        } ?: 0 >= nextTier.requiredLevel

        val hasPets = if (nextTier.requiredPets > 0) {
            Pets.values().count { player.hasPet(it) } >= nextTier.requiredPets
        } else true

        return hasLevel && hasPets
    }

    fun doPrestige(player: Player) {
        val config = plugin.prestigeConfig ?: return
        val currentLevel = getLevel(player)
        val tier = config.getOrNull(currentLevel) ?: return

        for (pet in Pets.values()) {
            if (player.hasPet(pet)) {
                player.setPetLevel(pet, 1)
                player.setPetXP(pet, 0.0)
            }
        }

        setLevel(player, currentLevel + 1)

        for (reward in tier.rewards) {
            when (reward.type.lowercase()) {
                "command" -> {
                    val cmd = reward.value.replace("%player%", player.name)
                    plugin.server.dispatchCommand(plugin.server.consoleSender, cmd)
                }
                "permission" -> {
                    plugin.server.dispatchCommand(
                        plugin.server.consoleSender,
                        "lp user ${player.name} permission set ${reward.value} true"
                    )
                }
                "vault" -> {
                    val vault = com.petkuengsus.petkuengsus.integration.VaultHook
                    if (vault.enabled) vault.deposit(player, reward.amount)
                }
            }
        }

        if (tier.extraSlots > 0) {
            val perm = "petkuengsus.slots.${tier.extraSlots}"
            plugin.server.dispatchCommand(
                plugin.server.consoleSender,
                "lp user ${player.name} permission set $perm true"
            )
        }
    }

    fun getMultiplier(player: Player): Double {
        val config = plugin.prestigeConfig ?: return 1.0
        val level = getLevel(player)
        var mult = 1.0
        for (i in 0 until level.coerceAtMost(config.size)) {
            mult *= config[i].xpMultiplier
        }
        return mult
    }
}
