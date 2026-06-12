package com.petkuengsus.petkuengsus.storage

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.lang.reflect.Type

class YamlStorage(private val dataFolder: File) : StorageProvider {
    private val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()

    init {
        dataFolder.mkdirs()
    }

    override fun <T> load(name: String, default: T, type: Type): T {
        val file = File(dataFolder, "$name.yml")
        if (!file.exists()) return default
        return try {
            val config = YamlConfiguration.loadConfiguration(file)
            val json = gson.toJsonTree(config.getValues(true))
            gson.fromJson(json, type) ?: default
        } catch (e: Exception) {
            default
        }
    }

    override fun <T> save(name: String, data: T, type: Type) {
        val file = File(dataFolder, "$name.yml")
        val json = gson.toJsonTree(data)
        val config = YamlConfiguration.loadConfiguration(file)
        if (json.isJsonObject) {
            for ((key, value) in json.asJsonObject.entrySet()) {
                config.set(key, gson.fromJson(value, Any::class.java))
            }
        }
        config.save(file)
    }

    override fun delete(name: String) {
        val file = File(dataFolder, "$name.yml")
        file.delete()
    }

    override fun shutdown() {}
}
