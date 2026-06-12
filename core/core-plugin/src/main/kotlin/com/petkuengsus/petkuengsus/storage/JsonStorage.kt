package com.petkuengsus.petkuengsus.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

class JsonStorage(private val dataFolder: File) : StorageProvider {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    init {
        dataFolder.mkdirs()
    }

    override fun <T> load(name: String, default: T, type: Type): T {
        val file = File(dataFolder, "$name.json")
        if (!file.exists()) return default
        return try {
            FileReader(file).use { reader ->
                gson.fromJson(reader, type) ?: default
            }
        } catch (e: Exception) {
            default
        }
    }

    override fun <T> save(name: String, data: T, type: Type) {
        val file = File(dataFolder, "$name.json")
        FileWriter(file).use { writer ->
            gson.toJson(data, writer)
        }
    }

    override fun delete(name: String) {
        val file = File(dataFolder, "$name.json")
        file.delete()
    }

    override fun shutdown() {}
}
