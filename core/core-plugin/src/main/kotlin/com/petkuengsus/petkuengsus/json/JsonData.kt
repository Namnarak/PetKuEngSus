package com.petkuengsus.petkuengsus.json

import com.petkuengsus.petkuengsus.plugin
import com.petkuengsus.petkuengsus.storage.JsonStorage
import com.petkuengsus.petkuengsus.storage.load
import com.petkuengsus.petkuengsus.storage.save

import java.io.File

object JsonData {
    val storage by lazy { JsonStorage(File(plugin.dataFolder, "data")) }

    inline fun <reified T> load(name: String, default: T): T {
        return storage.load(name, default)
    }

    fun <T> save(name: String, data: T) {
        storage.save(name, data, Unit::class.java)
    }

    fun delete(name: String) {
        storage.delete(name)
    }
}
