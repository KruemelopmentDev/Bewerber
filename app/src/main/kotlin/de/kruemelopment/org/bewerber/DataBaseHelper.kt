package de.kruemelopment.org.bewerber

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_ADDITIONAL (ID INTEGER PRIMARY KEY AUTOINCREMENT, Wort TEXT)"
        )
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_SAVED_WORDS (ID INTEGER PRIMARY KEY AUTOINCREMENT, Wort TEXT)"
        )
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (ID INTEGER PRIMARY KEY AUTOINCREMENT, Spruch TEXT UNIQUE, Wort TEXT, Timestamp INTEGER)"
        )
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_CUSTOM_TEMPLATES (ID INTEGER PRIMARY KEY AUTOINCREMENT, Vorlage TEXT UNIQUE)"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Bestehende Tabellen (erganzung & worter) bewusst NICHT löschen, damit Nutzerdaten erhalten bleiben!
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_ADDITIONAL (ID INTEGER PRIMARY KEY AUTOINCREMENT, Wort TEXT)"
        )
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_SAVED_WORDS (ID INTEGER PRIMARY KEY AUTOINCREMENT, Wort TEXT)"
        )
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (ID INTEGER PRIMARY KEY AUTOINCREMENT, Spruch TEXT UNIQUE, Wort TEXT, Timestamp INTEGER)"
        )
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_CUSTOM_TEMPLATES (ID INTEGER PRIMARY KEY AUTOINCREMENT, Vorlage TEXT UNIQUE)"
        )
    }

    fun getWords(tableName: String): List<String> {
        val result = mutableListOf<String>()
        val db = this.readableDatabase
        db.rawQuery("SELECT Wort FROM $tableName ORDER BY ID ASC", null).use { cursor ->
            while (cursor.moveToNext()) {
                val word = cursor.getString(0)
                if (!word.isNullOrBlank()) {
                    result.add(word)
                }
            }
        }
        return result
    }

    fun insertWordIfNotExists(word: String, tableName: String): Boolean {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return false
        val db = this.writableDatabase
        val exists = db.rawQuery(
            "SELECT 1 FROM $tableName WHERE Wort = ? LIMIT 1",
            arrayOf(trimmed)
        ).use { cursor ->
            cursor.moveToFirst()
        }
        if (exists) return false

        val values = ContentValues().apply {
            put("Wort", trimmed)
        }
        return db.insert(tableName, null, values) != -1L
    }

    fun deleteWord(word: String, tableName: String) {
        val db = this.writableDatabase
        db.delete(tableName, "Wort = ?", arrayOf(word))
    }

    fun ensureDefaultWords(): Pair<List<String>, List<String>> {
        var saved = getWords(TABLE_SAVED_WORDS)
        if (saved.isEmpty()) {
            SloganGenerator.DEFAULT_SAVED_WORDS.forEach { word ->
                insertWordIfNotExists(word, TABLE_SAVED_WORDS)
            }
            saved = getWords(TABLE_SAVED_WORDS)
        }

        var additional = getWords(TABLE_ADDITIONAL)
        if (additional.isEmpty()) {
            SloganGenerator.DEFAULT_ADDITIONAL_WORDS.forEach { word ->
                insertWordIfNotExists(word, TABLE_ADDITIONAL)
            }
            additional = getWords(TABLE_ADDITIONAL)
        }

        return Pair(saved, additional)
    }

    fun getFavoriteSlogans(): List<String> {
        val result = mutableListOf<String>()
        val db = this.readableDatabase
        db.rawQuery("SELECT Spruch FROM $TABLE_FAVORITES ORDER BY ID DESC", null).use { cursor ->
            while (cursor.moveToNext()) {
                val slogan = cursor.getString(0)
                if (!slogan.isNullOrBlank()) {
                    result.add(slogan)
                }
            }
        }
        return result
    }

    fun addFavoriteSlogan(slogan: String, word: String = ""): Boolean {
        val trimmed = slogan.trim()
        if (trimmed.isEmpty()) return false
        if (isFavoriteSlogan(trimmed)) return false

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("Spruch", trimmed)
            put("Wort", word.trim())
            put("Timestamp", System.currentTimeMillis())
        }
        return db.insertWithOnConflict(
            TABLE_FAVORITES,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    fun removeFavoriteSlogan(slogan: String) {
        val db = this.writableDatabase
        db.delete(TABLE_FAVORITES, "Spruch = ?", arrayOf(slogan.trim()))
    }

    fun isFavoriteSlogan(slogan: String): Boolean {
        val trimmed = slogan.trim()
        if (trimmed.isEmpty()) return false
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT 1 FROM $TABLE_FAVORITES WHERE Spruch = ? LIMIT 1",
            arrayOf(trimmed)
        ).use { cursor ->
            cursor.moveToFirst()
        }
    }

    fun getCustomTemplates(): List<String> {
        val result = mutableListOf<String>()
        val db = this.readableDatabase
        db.rawQuery("SELECT Vorlage FROM $TABLE_CUSTOM_TEMPLATES ORDER BY ID DESC", null)
            .use { cursor ->
                while (cursor.moveToNext()) {
                    val template = cursor.getString(0)
                    if (!template.isNullOrBlank()) {
                        result.add(template)
                    }
                }
            }
        return result
    }

    fun addCustomTemplate(template: String): Boolean {
        val trimmed = template.trim()
        if (trimmed.isEmpty()) return false
        val db = this.writableDatabase
        val exists = db.rawQuery(
            "SELECT 1 FROM $TABLE_CUSTOM_TEMPLATES WHERE Vorlage = ? LIMIT 1",
            arrayOf(trimmed)
        ).use { cursor ->
            cursor.moveToFirst()
        }
        if (exists) return false

        val values = ContentValues().apply {
            put("Vorlage", trimmed)
        }
        return db.insertWithOnConflict(
            TABLE_CUSTOM_TEMPLATES,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    fun removeCustomTemplate(template: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CUSTOM_TEMPLATES, "Vorlage = ?", arrayOf(template.trim()))
    }

    companion object {
        const val DATABASE_NAME = "Werbesprüche.db"
        const val DATABASE_VERSION = 2
        const val TABLE_ADDITIONAL = "erganzung"
        const val TABLE_SAVED_WORDS = "worter"
        const val TABLE_FAVORITES = "favoriten"
        const val TABLE_CUSTOM_TEMPLATES = "eigene_sprueche"
    }
}
