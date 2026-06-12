package com.petkuengsus.petkuengsus.internal

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class PlayableSound private constructor(
    private val sound: Sound,
    private val volume: Float = 1.0f,
    private val pitch: Float = 1.0f
) {
    fun playTo(player: Player) {
        player.playSound(player.location, sound, volume, pitch)
    }

    fun playAt(location: Location) {
        location.world?.playSound(location, sound, volume, pitch)
    }

    companion object {
        fun create(section: SimpleConfig?): PlayableSound? {
            if (section == null) return null
            val soundName = section.getString("sound") ?: return null
            val s = try {
                Sound.valueOf(soundName.uppercase())
            } catch (e: IllegalArgumentException) {
                return null
            }
            return PlayableSound(
                sound = s,
                volume = section.getDouble("volume", 1.0).toFloat(),
                pitch = section.getDouble("pitch", 1.0).toFloat()
            )
        }
    }
}

object SoundUtils {
    fun playSound(sound: Sound, player: Player) {
        player.playSound(player.location, sound, 1.0f, 1.0f)
    }
}
