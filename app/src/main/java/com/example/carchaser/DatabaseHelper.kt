package com.example.carchaser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "my_database", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE my_table (_id INTEGER PRIMARY KEY, date TEXT, place TEXT, isActivity INTEGER, latitude REAL, longitude REAL, photo TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun insertData(date: String, place: String, isActivity: Int, latitude: Double, longitude: Double) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put("date", date)
        values.put("place", place)
        values.put("isActivity", isActivity)
        values.put("latitude", latitude)
        values.put("longitude", longitude)
        values.put("photo", "null")

        db.insert("my_table", null, values)

        db.close()
    }

    fun updatePhoto(photoName: String) {
        val db = this.writableDatabase
        val query = "UPDATE my_table SET photo = \"$photoName\" WHERE isActivity = 1"
        db.execSQL(query)

        db.close()
    }

    fun updatePosition(latitude: Double, longitude: Double) {
        val db = this.writableDatabase
        val query = "UPDATE my_table SET latitude = $latitude, longitude = $longitude WHERE isActivity = 1"
        db.execSQL(query)

        db.close()
    }

    fun updateActivity() {
        val db = this.writableDatabase
        val query = "UPDATE my_table SET isActivity = 0 WHERE isActivity = 1"
        db.execSQL(query)

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

    fun getPhotoData(): List<String> {
        val data = mutableListOf<String>()

        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM my_table WHERE isActivity = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val photo = cursor.getString(cursor.getColumnIndexOrThrow("photo"))

                data.add(photo)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return data
    }

    fun getDataNotActive(): List<InfoEntity> {
        val data = mutableListOf<InfoEntity>()

        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM my_table WHERE isActivity = 0", null)

        if (cursor.moveToFirst()) {
            do {
                val entity = InfoEntity()
                entity.date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                entity.place = cursor.getString(cursor.getColumnIndexOrThrow("place"))
                entity.isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActivity"))
                entity.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"))
                entity.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))

                data.add(entity)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return data
    }


    fun getDataActive(): List<InfoEntity> {
        val data = mutableListOf<InfoEntity>()
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM my_table WHERE isActivity = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val entity = InfoEntity()
                entity.date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                entity.place = cursor.getString(cursor.getColumnIndexOrThrow("place"))
                entity.isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActivity"))
                entity.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"))
                entity.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))

                data.add(entity)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return data
    }

    fun deleteAllData() {
        val db = this.writableDatabase
        val query = "DELETE FROM my_table WHERE isActivity = 0"
        db.execSQL(query)
        db.close()
    }

    fun deleteStroke(datee: String) {
        val db = this.writableDatabase
        val query = "DELETE FROM my_table WHERE date = \"$datee\""
        db.execSQL(query)
        db.close()
    }
}