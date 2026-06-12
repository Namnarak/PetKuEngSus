package com.petkuengsus.petkuengsus.internal

import org.bukkit.entity.Player

interface PlayerPlaceholder {
    val identifier: String
    fun getValue(player: Player): String
}

class PlayerStaticPlaceholder(
    override val identifier: String,
    private val supplier: (Player) -> String
) : PlayerPlaceholder {
    override fun getValue(player: Player): String = supplier(player)
}

interface InjectablePlaceholder

class StaticPlaceholder(
    val identifier: String,
    private val supplier: () -> String
) : InjectablePlaceholder {
    fun getValue(): String = supplier()
}

interface PlaceholderInjectable {
    fun getPlaceholderInjections(): List<InjectablePlaceholder>
    fun addInjectablePlaceholder(placeholders: Iterable<InjectablePlaceholder>)
    fun clearInjectedPlaceholders()
}

fun String.injectPlaceholders(
    player: Player? = null,
    extraPlaceholders: Map<String, String> = emptyMap()
): String {
    var result = this
    // Replace %player_name% etc
    if (player != null) {
        result = result.replace("%player_name%", player.name)
        result = result.replace("%player_displayname%", player.displayName)
        result = result.replace("%player_uuid%", player.uniqueId.toString())
    }
    for ((key, value) in extraPlaceholders) {
        result = result.replace("%$key%", value)
    }
    return result
}

fun List<String>.injectPlaceholders(
    player: Player? = null,
    extraPlaceholders: Map<String, String> = emptyMap()
): List<String> {
    return this.map { it.injectPlaceholders(player, extraPlaceholders) }
}
