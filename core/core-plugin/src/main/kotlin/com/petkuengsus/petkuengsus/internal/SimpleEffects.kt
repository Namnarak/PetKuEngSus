package com.petkuengsus.petkuengsus.internal

import org.bukkit.entity.Player

class ConditionList(
    private val conditionsConfig: SimpleConfig? = null,
    private val player: Player? = null
) {
    fun areMet(): Boolean {
        if (conditionsConfig == null) return true
        // Basic permission-based conditions
        val permission = conditionsConfig.getString("permission") ?: return true
        return player?.hasPermission(permission) ?: true
    }
}

class EffectList(
    private val effectsConfig: SimpleConfig? = null,
    private val player: Player? = null
) {
    fun trigger() {
        if (effectsConfig == null) return
        // Basic command execution on trigger
        val commands = effectsConfig.getStringList("commands")
        for (command in commands) {
            val parsed = command
                .replace("%player%", player?.name ?: "")
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), parsed)
        }

        // Basic potion effects
        val potionEffects = effectsConfig.getStringList("potion-effects")
        for (effectLine in potionEffects) {
            val parts = effectLine.split(" ")
            if (parts.size >= 2 && player != null) {
                try {
                    val type = org.bukkit.potion.PotionEffectType.getByName(parts[0].uppercase())
                    val amplifier = if (parts.size >= 3) parts[2].toInt() - 1 else 0
                    val duration = parts[1].toInt() * 20
                    if (type != null) {
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(type, duration, amplifier))
                    }
                } catch (e: Exception) {
                    // ignore bad effect config
                }
            }
        }
    }
}

interface Holder {
    val id: String
}

class SimpleProvidedHolder(
    override val id: String,
    private val config: SimpleConfig? = null
) : Holder

fun Player.toDispatcher(): Any = this

object SimpleEffectEngine {
    private val registeredEffects = mutableMapOf<String, (SimpleConfig, Player) -> Boolean>()
    private val registeredConditions = mutableMapOf<String, (SimpleConfig, Player) -> Boolean>()
    private val registeredTriggers = mutableMapOf<String, (Player) -> Unit>()

    fun registerEffect(id: String, handler: (SimpleConfig, Player) -> Boolean) {
        registeredEffects[id] = handler
    }

    fun registerCondition(id: String, handler: (SimpleConfig, Player) -> Boolean) {
        registeredConditions[id] = handler
    }

    fun registerTrigger(id: String, handler: (Player) -> Unit) {
        registeredTriggers[id] = handler
    }
}
