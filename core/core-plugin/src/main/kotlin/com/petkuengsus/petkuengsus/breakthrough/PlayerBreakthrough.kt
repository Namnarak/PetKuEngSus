package com.petkuengsus.petkuengsus.breakthrough

import com.petkuengsus.petkuengsus.plugin
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object PlayerBreakthrough {
    private val breakthroughLevelKey by lazy {
        com.petkuengsus.petkuengsus.internal.DataKeys.get("breakthrough_level", plugin)
    }

    fun getLevel(player: Player): Int {
        val pdc = player.persistentDataContainer
        return pdc.get(breakthroughLevelKey, PersistentDataType.INTEGER) ?: 0
    }

    fun setLevel(player: Player, level: Int) {
        val pdc = player.persistentDataContainer
        pdc.set(breakthroughLevelKey, PersistentDataType.INTEGER, level)
    }

    fun canAdvance(player: Player): Boolean {
        val config = plugin.breakthroughConfig ?: return false
        val currentLevel = getLevel(player)
        return currentLevel < config.size
    }

    fun getCurrentTierConfig(player: Player): BreakthroughTier? {
        val config = plugin.breakthroughConfig ?: return null
        val level = getLevel(player)
        return config.getOrNull(level)
    }

    fun getNextTierConfig(player: Player): BreakthroughTier? {
        val config = plugin.breakthroughConfig ?: return null
        val level = getLevel(player) + 1
        return config.getOrNull(level)
    }

    fun applyRewards(player: Player, tier: BreakthroughTier) {
        for (reward in tier.rewards) {
            when (reward.type.lowercase()) {
                "command" -> {
                    val cmd = reward.value.replace("%player%", player.name)
                    plugin.server.dispatchCommand(plugin.server.consoleSender, cmd)
                }
                "permission" -> {
                    val perm = reward.value
                    plugin.server.dispatchCommand(
                        plugin.server.consoleSender,
                        "lp user ${player.name} permission set $perm true"
                    )
                }
                "vault" -> {
                    val vault =         com.petkuengsus.petkuengsus.integration.VaultHook
                    if (vault.enabled) {
                        vault.deposit(player, reward.amount)
                    }
                }
            }
        }
    }
}
