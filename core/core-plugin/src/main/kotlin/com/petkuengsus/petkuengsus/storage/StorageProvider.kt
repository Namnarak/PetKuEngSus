package com.petkuengsus.petkuengsus.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

interface StorageProvider {
    fun <T> load(name: String, default: T, type: java.lang.reflect.Type): T
    fun <T> save(name: String, data: T, type: java.lang.reflect.Type)
    fun delete(name: String)
    fun shutdown()
}

inline fun <reified T> StorageProvider.load(name: String, default: T): T {
    return load(name, default, object : TypeToken<T>() {}.type)
}

inline fun <reified T> StorageProvider.save(name: String, data: T) {
    save(name, data, object : TypeToken<T>() {}.type)
}
