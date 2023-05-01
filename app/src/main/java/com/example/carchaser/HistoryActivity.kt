package com.example.carchaser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng

class HistoryActivity : AppCompatActivity() {
    private lateinit var layout: LinearLayout
    private lateinit var btnReturn: Button
    private lateinit var coord: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val dbHelper = MyDatabaseHelper(this)
        val arguments = intent.extras
        if (arguments != null) {
            coord = arguments.get("position") as LatLng
        }
        val data = dbHelper.getData()
        btnReturn = findViewById(R.id.button_return_2)

        layout = findViewById<LinearLayout>(R.id.layout_history)

        for (i in data) {
            val newTextView = TextView(this)
            newTextView.text = i
            layout.addView(newTextView)
        }

        btnReturn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            if (arguments != null) {
                intent.putExtra("coordinates", coord)
                intent.putExtra("ButtonAddMark", false)
                intent.putExtra("ButtonInfo", true)
            }
            startActivity(intent)
        }
    }
}