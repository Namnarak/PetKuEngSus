package com.petkuengsus.petkuengsus.integration

import com.petkuengsus.petkuengsus.PetKuEngSusPlugin
import org.bukkit.Bukkit

class IntegrationManager(private val plugin: PetKuEngSusPlugin) {
    fun initIntegrations() {
        val integrations = mapOf(
            "PlaceholderAPI" to "com.petkuengsus.petkuengsus.integration.PlaceholderAPIHook",
            "Vault" to "com.petkuengsus.petkuengsus.integration.VaultHook",
            "LuckPerms" to "com.petkuengsus.petkuengsus.integration.LuckPermsHook",
            "AxTrade" to "com.petkuengsus.petkuengsus.integration.AxTradeHook"
        )

        for ((depName, className) in integrations) {
            if (Bukkit.getPluginManager().isPluginEnabled(depName)) {
                try {
                    val clazz = Class.forName(className)
                    when (depName) {
                        "PlaceholderAPI" -> {
                            val constructor = clazz.getConstructor(PetKuEngSusPlugin::class.java)
                            val instance = constructor.newInstance(plugin)
                            instance::class.java.getMethod("register").invoke(instance)
                        }
                        "Vault" -> clazz.getMethod("init").invoke(clazz.getField("INSTANCE").get(null))
                        "LuckPerms" -> clazz.getMethod("init").invoke(clazz.getField("INSTANCE").get(null))
                        "AxTrade" -> clazz.getMethod("init").invoke(clazz.getField("INSTANCE").get(null))
                    }
                    plugin.logger.info("Loaded integration: $depName")
                } catch (e: ReflectiveOperationException) {
                    plugin.logger.warning("Failed to load integration $depName: ${e.message}")
                }
            } else {
                plugin.logger.info("Integration $depName not available, skipping")
            }
        }
    }
}
