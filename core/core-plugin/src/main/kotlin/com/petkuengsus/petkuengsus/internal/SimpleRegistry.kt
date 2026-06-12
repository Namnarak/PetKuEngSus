package com.petkuengsus.petkuengsus.internal

class SimpleRegistry<T> {
    private val items = mutableMapOf<String, T>()
    private val values = mutableListOf<T>()

    fun register(id: String, item: T) {
        items[id.lowercase()] = item
        values.add(item)
    }

    operator fun get(id: String): T? = items[id.lowercase()]
    fun values(): List<T> = values.toList()
    fun size(): Int = values.size
    fun clear() {
        items.clear()
        values.clear()
    }
}
