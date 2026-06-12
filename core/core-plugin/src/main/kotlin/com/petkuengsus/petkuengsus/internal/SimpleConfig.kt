package com.petkuengsus.petkuengsus.internal

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SimpleConfig {
    private val config: YamlConfiguration

    constructor(file: File) {
        config = YamlConfiguration.loadConfiguration(file)
    }

    constructor(data: Map<String, Any>) {
        config = YamlConfiguration()
        for ((key, value) in data) {
            config.set(key, value)
        }
    }

    fun getBool(path: String, default: Boolean = false): Boolean = config.getBoolean(path, default)
    fun getInt(path: String, default: Int = 0): Int = config.getInt(path, default)
    fun getDouble(path: String, default: Double = 0.0): Double = config.getDouble(path, default)
    fun getString(path: String, default: String? = null): String? = config.getString(path, default)
    fun getStringList(path: String): List<String> = config.getStringList(path)
    fun getIntegerList(path: String): List<Int> = config.getIntegerList(path)
    fun getFormattedStrings(path: String): List<String> = config.getStringList(path)
    fun getSubsection(path: String): SimpleConfig? {
        val section = config.getConfigurationSection(path) ?: return null
        val wrapper = SimpleConfig(File(""))
        wrapper.setSection(section)
        return wrapper
    }

    fun keys(): Set<String> = config.getKeys(false)
    fun contains(path: String): Boolean = config.contains(path)
    fun getKeysDeep(): Set<String> = config.getKeys(true)

    fun getLocation(path: String): Location? = config.getLocation(path)
    fun getItemStack(path: String): ItemStack? = config.getItemStack(path)
    fun getColor(path: String): Color? = config.getColor(path)

    private var section: ConfigurationSection? = null
    private fun setSection(section: ConfigurationSection) {
        this.section = section
        // copy all values from section to internal config
        for (key in section.getKeys(true)) {
            config.set(key, section.get(key))
        }
    }

    fun getConfig(): YamlConfiguration = config
}
