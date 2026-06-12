package com.petkuengsus.petkuengsus.combat

import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CombatEffectManager {
    private val activeEffects = mutableMapOf<String, ActiveCombatEffect>()
    private val effectCooldowns = mutableMapOf<String, MutableMap<String, Long>>()

    data class ActiveCombatEffect(
        val effect: CombatEffect,
        val source: Player,
        val amplifier: Double,
        val remainingTicks: Int,
        val maxTicks: Int
    )

    fun applyEffect(
        target: LivingEntity,
        source: Player,
        effect: CombatEffect,
        petLevel: Int
    ) {
        val chance = effect.baseChance + (effect.scalingPerLevel * petLevel)
        if (Random.nextDouble() > chance) return

        val amplifier = effect.baseAmplifier + (effect.scalingPerLevel * petLevel)
        val duration = effect.baseDuration + (effect.durationPerLevel * petLevel)

        val cooldownKey = "${source.uniqueId}:${effect.id}"
        val now = System.currentTimeMillis()
        val lastApply = effectCooldowns.getOrPut(cooldownKey) { mutableMapOf() }[target.uniqueId.toString()] ?: 0
        if (now - lastApply < 1000) return
        effectCooldowns[cooldownKey]?.set(target.uniqueId.toString(), now)

        when (effect.type) {
            EffectType.BLEED -> applyBleed(target, source, effect, amplifier, duration)
            EffectType.POISON -> applyPoison(target, amplifier.toInt(), duration)
            EffectType.BURN -> applyBurn(target, amplifier, duration)
            EffectType.FREEZE -> applyFreeze(target, duration)
            EffectType.CRITICAL -> applyCritical(target, source, amplifier, duration)
            EffectType.ARMOR_BREAK -> applyArmorBreak(target, amplifier, duration)
            EffectType.LIFE_STEAL -> applyLifeSteal(source, amplifier)
        }
    }

    private fun applyBleed(target: LivingEntity, source: Player, effect: CombatEffect, amplifier: Double, duration: Int) {
        val key = "pet_bleed_${source.uniqueId}"
        target.setMetadata(key, FixedMetadataValue(plugin, duration))
        val taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (!target.isValid || target.isDead) {
                target.removeMetadata(key, plugin)
                return@Runnable
            }
            val meta = target.getMetadata(key).firstOrNull() ?: return@Runnable
            val remaining = meta.asInt()
            if (remaining <= 0) {
                target.removeMetadata(key, plugin)
                return@Runnable
            }
            target.damage(amplifier, source)
            target.setMetadata(key, FixedMetadataValue(plugin, remaining - 20))
        }, 0L, 20L).taskId
        target.setMetadata("${key}_task", FixedMetadataValue(plugin, taskId))
    }

    private fun applyPoison(target: LivingEntity, amplifier: Int, duration: Int) {
        target.addPotionEffect(PotionEffect(
            PotionEffectType.POISON,
            duration,
            max(0, amplifier - 1)
        ))
    }

    private fun applyBurn(target: LivingEntity, amplifier: Double, duration: Int) {
        target.fireTicks = min(target.fireTicks + duration, duration * 3)
    }

    private fun applyFreeze(target: LivingEntity, duration: Int) {
        target.addPotionEffect(PotionEffect(
            PotionEffectType.SLOWNESS,
            duration,
            4
        ))
        target.freezeTicks = min(target.freezeTicks + duration, duration * 2)
    }

    private fun applyCritical(target: LivingEntity, source: Player, amplifier: Double, duration: Int) {
        target.damage(amplifier, source)
    }

    private fun applyArmorBreak(target: LivingEntity, amplifier: Double, duration: Int) {
        target.addPotionEffect(PotionEffect(
            PotionEffectType.WEAKNESS,
            duration,
            max(0, amplifier.toInt() - 1)
        ))
    }

    private fun applyLifeSteal(source: Player, amplifier: Double) {
        val newHealth = min(source.health + amplifier, source.maxHealth)
        source.health = newHealth
    }
}
