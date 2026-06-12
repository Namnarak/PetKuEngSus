package com.petkuengsus.petkuengsus.internal


import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object DataKeys {
    private val keys = mutableMapOf<String, NamespacedKey>()

    fun get(key: String, plugin: JavaPlugin): NamespacedKey {
        return keys.getOrPut(key) { NamespacedKey(plugin, key) }
    }
}

class PersistentDataKey<T : Any>(
    private val key: String,
    private val type: PersistentDataType<T, Any>,
    private val plugin: JavaPlugin
) {
    @Suppress("UNCHECKED_CAST")
    fun get(player: Player): Any? {
        val pdc = player.persistentDataContainer
        val nsk = DataKeys.get(key, plugin)
        return pdc.get<T, Any>(nsk, type)
    }

    fun set(player: Player, value: Any) {
        val pdc = player.persistentDataContainer
        val nsk = DataKeys.get(key, plugin)
        @Suppress("UNCHECKED_CAST")
        pdc.set(nsk, type as PersistentDataType<Any, Any>, value)
    }

    fun has(player: Player): Boolean {
        val pdc = player.persistentDataContainer
        val nsk = DataKeys.get(key, plugin)
        return pdc.has(nsk)
    }

    fun remove(player: Player) {
        val pdc = player.persistentDataContainer
        val nsk = DataKeys.get(key, plugin)
        pdc.remove(nsk)
    }
}

object PersistentDataKeyType {
    val STRING = PersistentDataType.STRING
    val INTEGER = PersistentDataType.INTEGER
    val DOUBLE = PersistentDataType.DOUBLE
    val BOOLEAN = PersistentDataType.BOOLEAN
    val INTEGER_ARRAY = PersistentDataType.INTEGER_ARRAY
    val BYTE_ARRAY = PersistentDataType.BYTE_ARRAY
}

val Player.profile: PlayerProfileWrapper
    get() = PlayerProfileWrapper(this)

class PlayerProfileWrapper(val player: Player) {
    inline fun <reified T> getOrSetDefault(key: String, default: T, plugin: JavaPlugin): T {
        val nsk = DataKeys.get(key, plugin)
        val pdc = player.persistentDataContainer
        return when (T::class) {
            Int::class -> {
                val v = pdc.get(nsk, PersistentDataType.INTEGER)
                @Suppress("UNCHECKED_CAST")
                (v ?: (default as Int)) as T
            }
            Double::class -> {
                val v = pdc.get(nsk, PersistentDataType.DOUBLE)
                @Suppress("UNCHECKED_CAST")
                (v ?: (default as Double)) as T
            }
            String::class -> {
                val v = pdc.get(nsk, PersistentDataType.STRING)
                @Suppress("UNCHECKED_CAST")
                (v ?: (default as String)) as T
            }
            Boolean::class -> {
                val v = pdc.get(nsk, PersistentDataType.BOOLEAN)
                @Suppress("UNCHECKED_CAST")
                (v ?: (default as Boolean)) as T
            }
            else -> default
        }
    }

    inline fun <reified T> set(key: String, value: T, plugin: JavaPlugin) {
        val nsk = DataKeys.get(key, plugin)
        val pdc = player.persistentDataContainer
        when (T::class) {
            Int::class -> pdc.set(nsk, PersistentDataType.INTEGER, value as Int)
            Double::class -> pdc.set(nsk, PersistentDataType.DOUBLE, value as Double)
            String::class -> pdc.set(nsk, PersistentDataType.STRING, value as String)
            Boolean::class -> pdc.set(nsk, PersistentDataType.BOOLEAN, value as Boolean)
        }
    }

    fun remove(key: String, plugin: JavaPlugin) {
        val nsk = DataKeys.get(key, plugin)
        player.persistentDataContainer.remove(nsk)
    }
}
