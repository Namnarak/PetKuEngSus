package com.petkuengsus.petkuengsus.talents

import com.petkuengsus.petkuengsus.plugin
import com.petkuengsus.petkuengsus.storage.load
import com.petkuengsus.petkuengsus.storage.save
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object PlayerTalents {
    private val talentPointsKey by lazy {
        com.petkuengsus.petkuengsus.internal.DataKeys.get("talent_points", plugin)
    }
    private val totalEarnedKey by lazy {
        com.petkuengsus.petkuengsus.internal.DataKeys.get("talent_total", plugin)
    }

    data class TalentData(
        val points: Int = 0,
        val totalEarned: Int = 0,
        val allocated: MutableMap<String, Int> = mutableMapOf()
    )

    private fun getData(player: Player): TalentData {
        return plugin.storageProvider!!.load("talents_${player.uniqueId}", TalentData())
    }

    private fun saveData(player: Player, data: TalentData) {
        plugin.storageProvider!!.save("talents_${player.uniqueId}", data)
    }

    fun getAvailablePoints(player: Player): Int {
        val pdc = player.persistentDataContainer
        return pdc.get(talentPointsKey, PersistentDataType.INTEGER) ?: 0
    }

    fun setAvailablePoints(player: Player, points: Int) {
        val pdc = player.persistentDataContainer
        pdc.set(talentPointsKey, PersistentDataType.INTEGER, points)
    }

    fun addPoints(player: Player, amount: Int) {
        setAvailablePoints(player, getAvailablePoints(player) + amount)
        val pdc = player.persistentDataContainer
        val total = pdc.get(totalEarnedKey, PersistentDataType.INTEGER) ?: 0
        pdc.set(totalEarnedKey, PersistentDataType.INTEGER, total + amount)
    }

    fun getSpentPoints(player: Player): Int {
        val data = getData(player)
        return data.allocated.values.sum()
    }

    fun getTotalEarned(player: Player): Int {
        val pdc = player.persistentDataContainer
        return pdc.get(totalEarnedKey, PersistentDataType.INTEGER) ?: 0
    }

    fun getAllocation(player: Player, talentId: String): Int {
        val data = getData(player)
        return data.allocated[talentId] ?: 0
    }

    fun allocate(player: Player, talent: Talent): Boolean {
        val current = getAllocation(player, talent.id)
        if (current >= talent.maxLevel) return false

        val available = getAvailablePoints(player)
        if (available < talent.costPerLevel) return false

        val data = getData(player)
        data.allocated[talent.id] = current + talent.costPerLevel
        saveData(player, data)

        setAvailablePoints(player, available - talent.costPerLevel)
        return true
    }

    fun resetAllocations(player: Player) {
        val data = getData(player)
        val refund = data.allocated.values.sum()
        data.allocated.clear()
        saveData(player, data)
        setAvailablePoints(player, getAvailablePoints(player) + refund)
    }

    fun getTalentLevel(player: Player, talentId: String): Int {
        val data = getData(player)
        return data.allocated[talentId] ?: 0
    }

    fun getEffectValue(player: Player, effectKey: String): Double {
        val config = plugin.talentsConfig ?: return 0.0
        var total = 0.0
        for (talent in config) {
            val level = getTalentLevel(player, talent.id)
            if (level > 0 && talent.effects.containsKey(effectKey)) {
                total += talent.effects[effectKey]!! * level
            }
        }
        return total
    }
}
