package com.example.carchaser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView

class NoteActivity : AppCompatActivity() {
    private lateinit var coord: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val arguments = intent.extras
        if (arguments != null) {
            coord = arguments.get("position") as LatLng
        }

        val buttonReturn = findViewById<Button>(R.id.button_return)

        buttonReturn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("coordinates", coord)
            intent.putExtra("ButtonAddMark", false)
            intent.putExtra("ButtonInfo", true)
            startActivity(intent)
        }
    }
}