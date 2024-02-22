package de.kruemelopment.org.bewerber

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context?) : SQLiteOpenHelper(context, Database_Name, null, 1) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("Create Table $Table1 (ID INTEGER PRIMARY KEY AUTOINCREMENT, Wort TEXT)")
        sqLiteDatabase.execSQL("Create Table $Table2 (ID INTEGER PRIMARY KEY AUTOINCREMENT, Wort TEXT)")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $Table1")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $Table2")
    }

    fun insertData(wort: String?, tablename: String?) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("Wort", wort)
        database.insert(tablename, null, contentValues)
        database.close()
    }

    fun getAllData(tablename: String?): Cursor {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.rawQuery("Select * from $tablename", null)
    }

    fun deleteData(wort: String, tablename: String?) {
        val db = this.writableDatabase
        db.delete(tablename, "Wort=?", arrayOf(wort))
    }

    companion object {
        private const val Database_Name = "Werbesprüche.db"
        private const val Table1 = "erganzung"
        private const val Table2 = "worter"
    }
}
