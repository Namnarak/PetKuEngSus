package com.petkuengsus.petkuengsus.util

import com.petkuengsus.petkuengsus.internal.InjectablePlaceholder
import com.petkuengsus.petkuengsus.internal.PlaceholderInjectable
import com.petkuengsus.petkuengsus.internal.StaticPlaceholder

class LevelInjectable(
    level: Int
) : PlaceholderInjectable {
    private val placeholders = listOf(
        StaticPlaceholder(
            "level"
        ) { level.toString() }
    )

    override fun getPlaceholderInjections(): List<InjectablePlaceholder> {
        return placeholders
    }

    override fun addInjectablePlaceholder(p0: Iterable<InjectablePlaceholder>) {
        return
    }

    override fun clearInjectedPlaceholders() {
        return
    }
}