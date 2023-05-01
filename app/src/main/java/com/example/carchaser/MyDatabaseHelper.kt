package com.example.carchaser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "my_database", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE my_table (_id INTEGER PRIMARY KEY, date TEXT, place TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade logic goes here
    }

    fun insertData(date: String, place: String) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put("date", date)
        values.put("place", place)

        db.insert("my_table", null, values)

        db.close()
    }

    fun getData(): List<String> {
        val data = mutableListOf<String>()

        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM my_table", null)

        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val place = cursor.getString(cursor.getColumnIndexOrThrow("place"))

                data.add("$date - $place")
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return data
    }
}