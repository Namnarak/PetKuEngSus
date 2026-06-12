package com.petkuengsus.petkuengsus.integration

import com.petkuengsus.petkuengsus.PetKuEngSusPlugin
import com.petkuengsus.petkuengsus.pets.Pet
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object AxTradeHook {
    var enabled: Boolean = false
        private set

    fun init() {
        enabled = try {
            Class.forName("com.artillexstudios.axtrade.api.AxTradeAPI")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun isTrading(player: Player): Boolean {
        if (!enabled) return false
        return try {
            val apiClass = Class.forName("com.artillexstudios.axtrade.api.AxTradeAPI")
            val method = apiClass.getMethod("isTrading", Player::class.java)
            method.invoke(null, player) as Boolean
        } catch (e: Exception) {
            false
        }
    }
}
