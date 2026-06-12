package com.petkuengsus.petkuengsus.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type
import java.sql.Connection
import java.sql.DriverManager

class SqliteStorage(private val dataFolder: File) : StorageProvider {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var connection: Connection? = null

    init {
        dataFolder.mkdirs()
        connection = try {
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:${File(dataFolder, "data.db").absolutePath}")
        } catch (e: Exception) {
            null
        }
        connection?.let { conn ->
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS pet_data (" +
                "  key TEXT PRIMARY KEY," +
                "  value TEXT NOT NULL" +
                ")"
            )
        }
    }

    override fun <T> load(name: String, default: T, type: Type): T {
        val conn = connection ?: return default
        return try {
            val stmt = conn.prepareStatement("SELECT value FROM pet_data WHERE key = ?")
            stmt.setString(1, name)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                gson.fromJson(rs.getString("value"), type) ?: default
            } else default
        } catch (e: Exception) {
            default
        }
    }

    override fun <T> save(name: String, data: T, type: Type) {
        val conn = connection ?: return
        try {
            val json = gson.toJson(data)
            val stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO pet_data (key, value) VALUES (?, ?)"
            )
            stmt.setString(1, name)
            stmt.setString(2, json)
            stmt.executeUpdate()
        } catch (_: Exception) {}
    }

    override fun delete(name: String) {
        val conn = connection ?: return
        try {
            val stmt = conn.prepareStatement("DELETE FROM pet_data WHERE key = ?")
            stmt.setString(1, name)
            stmt.executeUpdate()
        } catch (_: Exception) {}
    }

    override fun shutdown() {
        try { connection?.close() } catch (_: Exception) {}
    }
}
