package com.petkuengsus.petkuengsus.integration

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.RegisteredServiceProvider

object VaultHook {
    private var economy: Economy? = null
    var enabled: Boolean = false
        private set

    fun init() {
        val rsp: RegisteredServiceProvider<Economy>? = Bukkit.getServicesManager().getRegistration(Economy::class.java)
        if (rsp != null) {
            economy = rsp.provider
            enabled = true
        }
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        val vault = economy ?: return false
        return vault.depositPlayer(player, amount).transactionSuccess()
    }

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val vault = economy ?: return false
        return vault.withdrawPlayer(player, amount).transactionSuccess()
    }

    fun getBalance(player: OfflinePlayer): Double {
        return economy?.getBalance(player) ?: 0.0
    }

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        return economy?.has(player, amount) ?: false
    }

    fun format(amount: Double): String {
        return economy?.format(amount) ?: "$%,.2f".format(amount)
    }
}
