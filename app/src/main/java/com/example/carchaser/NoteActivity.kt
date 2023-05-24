package com.example.carchaser

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 1
    val dbHelper = DatabaseHelper(this)
    private lateinit var currentPhotoPath: String
    private val dbHelper = MyDatabaseHelper(this)
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val textViewAdres = findViewById<TextView>(R.id.TextViewAddres)
        val dataAdres = dbHelper.getDataActive()
        if (dataAdres.isNotEmpty()) {
            textViewAdres.text = dataAdres[0].place
        }

//        val arguments = intent.extras
//        if (arguments != null) {
//            coord = arguments.get("position") as LatLng
//        }

        val buttonReturn = findViewById<Button>(R.id.button_return)
        val takePictureButton = findViewById<Button>(R.id.button_photo)
        imageView = findViewById(R.id.photo)
        setImageView(imageView)

        takePictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        }

        buttonReturn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter, androidx.appcompat.R.anim.abc_popup_exit)
        }
    }

    private fun setImageView(imageView: ImageView) {
        val photoFile: File? = findPhotoFile(dbHelper.getPhotoData()[0])

        if (photoFile != null) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(bitmap)
        } else {
            // Обработка ситуации, когда фото не найдено
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, выполняем необходимые операции
                openCamera()
            } else {
                // Разрешение не получено, обрабатываем ситуацию
                // например, выводим сообщение пользователю о том, что без разрешения на использование камеры функциональность недоступна
            }
        }
    }


    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(captureIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap? // Получаем фото

            // Создаем файл для сохранения фото
            val photoFile = createImageFile()

            // Сохраняем фото в файл
            try {
                val outputStream = FileOutputStream(photoFile)
                photo?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                // Уведомляем галерею о добавленном файле, чтобы она могла его обнаружить
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    photoFile.absolutePath,
                    photoFile.name,
                    null
                )
                setImageView(imageView)
            } catch (e: IOException) {
                e.printStackTrace()
                // Обработка ошибки сохранения фото
            }
        }
    }

    private fun createImageFile(): File {
        // Создаем имя файла с помощью временной метки
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFileName = "IMG_$timeStamp.jpg"
        val imageFile = File(storageDir, imageFileName)
        dbHelper.updatePhoto(imageFileName)
        return imageFile
    }

    private fun findPhotoFile(fileName: String): File? {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val files = storageDir?.listFiles()

        if (files != null) {
            for (file in files) {
                if (file.name == fileName) {
                    return file
                }
            }
        }

        return null
    }

}