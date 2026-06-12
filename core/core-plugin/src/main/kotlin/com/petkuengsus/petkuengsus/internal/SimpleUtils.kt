package com.petkuengsus.petkuengsus.internal

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object StringUtils {
    enum class FormatOption {
        WITH_PLACEHOLDERS,
        WITHOUT_PLACEHOLDERS
    }

    fun format(string: String, option: FormatOption = FormatOption.WITH_PLACEHOLDERS): String {
        return ChatColor.translateAlternateColorCodes('&', string)
    }

    fun toNiceString(string: String): String {
        return string.lowercase()
            .replace('_', ' ')
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    fun toNumeral(number: Int): String {
        return when {
            number == 1 -> "I"
            number == 2 -> "II"
            number == 3 -> "III"
            number == 4 -> "IV"
            number == 5 -> "V"
            number == 6 -> "VI"
            number == 7 -> "VII"
            number == 8 -> "VIII"
            number == 9 -> "IX"
            number == 10 -> "X"
            else -> number.toString()
        }
    }
}

object NumberUtils {
    fun format(number: Number): String {
        return String.format("%,.2f", number.toDouble())
    }

    fun evaluateExpression(expression: String): Double {
        return try {
            expression.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun fastSin(x: Double): Double = kotlin.math.sin(x)

    fun fastCos(x: Double): Double = kotlin.math.cos(x)
}

fun Number.formatBalance(): String {
    return String.format("%,.2f", this.toDouble())
}

fun String.formatBalance(): String {
    return String.format("%,.2f", this.toDoubleOrNull() ?: 0.0)
}

fun Number.toNiceString(): String {
    return StringUtils.toNiceString(this.toString())
}

fun String.toNumeral(): String {
    return StringUtils.toNumeral(this.toIntOrNull() ?: 0)
}

fun Player.sendFormattedMessage(message: String) {
    val colored = ChatColor.translateAlternateColorCodes('&', message)
    sendMessage(colored)
}

object LangYml {
    private val messages = mutableMapOf<String, String>()

    fun load(plugin: org.bukkit.plugin.java.JavaPlugin) {
        val file = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
            java.io.File(plugin.dataFolder, "lang.yml")
        )
        if (!file.contains("messages")) return
        val section = file.getConfigurationSection("messages") ?: return
        for (key in section.getKeys(true)) {
            messages[key] = section.getString(key) ?: continue
        }
    }

    fun getMessage(key: String): String {
        return messages[key] ?: "&cMessage not found: $key"
    }

    fun getMessage(key: String, option: StringUtils.FormatOption): String {
        return getMessage(key)
    }
}
