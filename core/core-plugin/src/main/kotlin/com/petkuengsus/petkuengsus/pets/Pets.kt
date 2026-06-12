package com.petkuengsus.petkuengsus.pets

import com.petkuengsus.petkuengsus.internal.SimpleRegistry
import com.petkuengsus.petkuengsus.plugin
import java.io.File

object Pets {
    private val registry = SimpleRegistry<Pet>()

    @JvmStatic
    fun values(): List<Pet> = registry.values()

    @JvmStatic
    fun getByID(name: String): Pet? = registry[name]

    fun reload() {
        registry.clear()
        val petsFolder = File(plugin.dataFolder, "pets")
        if (!petsFolder.exists()) {
            petsFolder.mkdirs()
            saveDefaultPets()
        }
        for (file in petsFolder.listFiles() ?: emptyArray()) {
            if (file.name.endsWith(".yml")) {
                val id = file.name.removeSuffix(".yml")
                try {
                    val config = file.reader().use { reader ->
                        org.yaml.snakeyaml.Yaml().loadAs(reader, java.util.LinkedHashMap::class.java) as? Map<String, Any>
                    } ?: continue
                    registry.register(id, Pet(id, config))
                } catch (e: Exception) {
                    plugin.logger.warning("Failed to load pet '$id': ${e.message}")
                }
            }
        }
        plugin.logger.info("Loaded ${registry.values().size} pets")
    }

    private fun saveDefaultPets() {
        plugin.saveResource("pets/_example.yml", false)
    }
}
