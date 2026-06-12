package com.petkuengsus.petkuengsus.internal

class SimpleEffectList(
    private val effectsConfig: SimpleConfig?
) {
    fun trigger(dispatcher: Any) {
        if (effectsConfig == null) return
        val player = (dispatcher as? org.bukkit.entity.Player) ?: return
        EffectList(effectsConfig, player).trigger()
    }

    companion object {
        fun fromConfig(config: SimpleConfig, path: String): SimpleEffectList {
            return SimpleEffectList(config.getSubsection(path))
        }
    }
}
