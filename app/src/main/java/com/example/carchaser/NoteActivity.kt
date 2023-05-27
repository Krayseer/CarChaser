package com.example.carchaser

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.carchaser.common.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editText:EditText
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var textViewActiveMarker: TextView
    private lateinit var buttonReturn: Button
    private lateinit var takePictureButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        dbHelper = DatabaseHelper(this)
        editText = findViewById(R.id.editText)
        imageView = findViewById(R.id.photo)
        textViewActiveMarker = findViewById(R.id.TextViewAddres)
        buttonReturn = findViewById(R.id.button_return)
        takePictureButton = findViewById(R.id.button_photo)

        val activeMarkerData = dbHelper.getDataActive()
        if (activeMarkerData != null) {
            textViewActiveMarker.text = activeMarkerData.place
            if(activeMarkerData.note != "null")
                editText.setText(activeMarkerData.note)
        }
        setImageView(imageView)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                dbHelper.updateNote(s.toString())
            }
        })

        takePictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), Constants.CAMERA_PERMISSION_REQUEST_CODE)
            }
        }

        buttonReturn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter, androidx.appcompat.R.anim.abc_popup_exit)
        }
    }

    /**
     * Устанавливает фото в ImageView
     */
    private fun setImageView(imageView: ImageView) {
        val photoFile: File? = findPhotoFile(dbHelper.getPhotoData()[0])
        if (photoFile != null) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
    }

    /**
     * Код, выполняемый при получении разрешений
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }


    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(captureIntent, Constants.CAMERA_REQUEST_CODE)
    }

    /**
     * Код выполняемый при фотографировании
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap?
            val photoFile = createImageFile()
            try {
                val outputStream = FileOutputStream(photoFile)
                photo?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                MediaStore.Images.Media.insertImage(contentResolver, photoFile.absolutePath, photoFile.name, null)
                setImageView(imageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createImageFile(): File {
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