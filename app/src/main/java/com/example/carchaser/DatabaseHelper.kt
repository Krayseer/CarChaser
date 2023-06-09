package com.example.carchaser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.carchaser.common.Constants

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, Constants.DB_NAME, null, 1) {

    /**
     * Инициализация таблицы в БД
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ${Constants.DB_TABLE} (_id INTEGER PRIMARY KEY, " +
                "${Constants.DB_COLUMN_DATE} TEXT, ${Constants.DB_COLUMN_PLACE} TEXT, ${Constants.DB_COLUMN_ACTIVITY} INTEGER, " +
                "${Constants.DB_COLUMN_LATITUDE} REAL, ${Constants.DB_COLUMN_LONGITUDE} REAL, " +
                "${Constants.DB_COLUMN_PHOTO} TEXT, ${Constants.DB_COLUMN_NOTE} TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    /**
     * Сохранение в БД данных о текущей стоянке
     */
    fun insertData(date: String, place: String, latitude: Double, longitude: Double) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(Constants.DB_COLUMN_DATE, date)
        values.put(Constants.DB_COLUMN_PLACE, place)
        values.put(Constants.DB_COLUMN_ACTIVITY, Constants.IS_ACTIVE_CODE)
        values.put(Constants.DB_COLUMN_LATITUDE, latitude)
        values.put(Constants.DB_COLUMN_LONGITUDE, longitude)
        values.put(Constants.DB_COLUMN_PHOTO, "null")
        values.put(Constants.DB_COLUMN_NOTE, "null")

        db.insert(Constants.DB_TABLE, null, values)

        db.close()
    }

    /**
     * Обновить/добавить фотографию в стоянке
     */
    fun updatePhoto(photoName: String) {
        val db = this.writableDatabase
        val query = "UPDATE ${Constants.DB_TABLE} SET ${Constants.DB_COLUMN_PHOTO} = \"$photoName\" " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.IS_ACTIVE_CODE}"
        db.execSQL(query)

        db.close()
    }

    /**
     * Обновить/добавить дополнительную информацию в стоянке
     */
    fun updateNote(text: String) {
        val db = this.writableDatabase
        val query = "UPDATE ${Constants.DB_TABLE} SET ${Constants.DB_COLUMN_NOTE} = \"$text\" " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.IS_ACTIVE_CODE}"
        db.execSQL(query)

        db.close()
    }

    /**
     * Обновление в БД позиции маркера при его передвижении на новое место
     */
    fun updatePosition(latitude: Double, longitude: Double, place: String) {
        val db = this.writableDatabase
        val query = "UPDATE ${Constants.DB_TABLE} SET ${Constants.DB_COLUMN_LATITUDE} = $latitude, " +
                "${Constants.DB_COLUMN_LONGITUDE} = $longitude, ${Constants.DB_COLUMN_PLACE} = '$place' " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.IS_ACTIVE_CODE}"
        db.execSQL(query)

        db.close()
    }

    /**
     * Установить статус стоянки как неактивной
     */
    fun updateActivity() {
        val db = this.writableDatabase
        val query = "UPDATE ${Constants.DB_TABLE} SET ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.NOT_ACTIVE_CODE} " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.IS_ACTIVE_CODE}"
        db.execSQL(query)

        db.close()
    }

    /**
     * Получить фотографию стоянки
     */
    fun getPhotoData(): List<String> {
        val data = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${Constants.DB_TABLE} " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.IS_ACTIVE_CODE}", null)

        if (cursor.moveToFirst()) {
            do {
                val photo = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_PHOTO))
                data.add(photo)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return data
    }

    /**
     * Получить список неактивных(удаленных) стоянок
     */
    fun getDataNotActive(): List<InfoEntity> {
        val data = mutableListOf<InfoEntity>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${Constants.DB_TABLE} " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.NOT_ACTIVE_CODE}", null)

        if (cursor.moveToFirst()) {
            do {
                val entity = InfoEntity()
                entity.date = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_DATE))
                entity.place = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_PLACE))
                entity.isActive = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_ACTIVITY))
                entity.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_LATITUDE))
                entity.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_LONGITUDE))

                data.add(entity)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return data
    }

    /**
     * Поулчить данные об активной стоянке
     */
    fun getDataActive(): InfoEntity? {
        val data = mutableListOf<InfoEntity>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${Constants.DB_TABLE} " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.IS_ACTIVE_CODE}", null)

        if (cursor.moveToFirst()) {
            do {
                val entity = InfoEntity()
                entity.date = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_DATE))
                entity.place = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_PLACE))
                entity.isActive = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_ACTIVITY))
                entity.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_LATITUDE))
                entity.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_LONGITUDE))
                entity.note = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DB_COLUMN_NOTE))

                data.add(entity)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return if (data.isNotEmpty()) data[0] else null
    }

    /**
     * Удалить все стоянки из истории
     */
    fun deleteAllData() {
        val db = this.writableDatabase
        val query = "DELETE FROM ${Constants.DB_TABLE} " +
                "WHERE ${Constants.DB_COLUMN_ACTIVITY} = ${Constants.NOT_ACTIVE_CODE}"
        db.execSQL(query)
        db.close()
    }

    /**
     * Удалить конкретную стоянку из истории
     */
    fun deleteParkingData(parkingData: String) {
        val db = this.writableDatabase
        val query = "DELETE FROM ${Constants.DB_TABLE} " +
                "WHERE ${Constants.DB_COLUMN_DATE} = \"$parkingData\""
        db.execSQL(query)
        db.close()
    }

}