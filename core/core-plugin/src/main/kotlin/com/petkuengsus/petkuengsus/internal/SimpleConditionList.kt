package com.petkuengsus.petkuengsus.internal

class SimpleConditionList(
    private val conditionsConfig: SimpleConfig?
) {
    fun check(player: org.bukkit.entity.Player): Boolean {
        return ConditionList(conditionsConfig, player).areMet()
    }

    companion object {
        fun fromConfig(config: SimpleConfig, path: String): SimpleConditionList {
            return SimpleConditionList(config.getSubsection(path))
        }
    }
}
