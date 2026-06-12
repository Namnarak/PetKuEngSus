package com.petkuengsus.petkuengsus.combat

import com.petkuengsus.petkuengsus.internal.SimpleConfig
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

data class CombatEffect(
    val id: String,
    val displayName: String,
    val description: List<String>,
    val type: EffectType,
    val baseChance: Double,
    val baseDuration: Int,
    val baseAmplifier: Double,
    val scalingPerLevel: Double,
    val durationPerLevel: Int,
    val particles: Boolean
) {
    companion object {
        fun fromConfig(id: String, section: SimpleConfig): CombatEffect {
            return CombatEffect(
                id = id,
                displayName = section.getString("name") ?: id,
                description = section.getStringList("description"),
                type = EffectType.valueOf((section.getString("type", "BLEED") ?: "BLEED").uppercase()),
                baseChance = section.getDouble("base-chance", 0.1),
                baseDuration = section.getInt("base-duration", 100),
                baseAmplifier = section.getDouble("base-amplifier", 1.0),
                scalingPerLevel = section.getDouble("scaling-per-level", 0.5),
                durationPerLevel = section.getInt("duration-per-level", 10),
                particles = section.getBool("particles", true)
            )
        }
    }
}

enum class EffectType {
    BLEED,
    POISON,
    BURN,
    FREEZE,
    CRITICAL,
    ARMOR_BREAK,
    LIFE_STEAL
}
