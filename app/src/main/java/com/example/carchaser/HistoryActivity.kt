package com.example.carchaser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng

class HistoryActivity : AppCompatActivity() {
    private lateinit var layout: LinearLayout
    private lateinit var btnReturn: Button
    private lateinit var btnDelete: Button
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
        btnDelete = findViewById(R.id.button_delete_history)
        layout = findViewById(R.id.layout_history)

        val inflater = LayoutInflater.from(this)
        for (i in data) {
            // Inflate the view from the layout resource.
            val itemView = inflater.inflate(R.layout.history_item, null, false)

            // Здесь можно настроить отображение данных внутри TextView.
            val dataArray = i.split(" ")
            itemView.findViewById<TextView>(R.id.textView_address).text = dataArray.subList(6, dataArray.size).joinToString(separator = " ")
            itemView.findViewById<TextView>(R.id.textView_date).text = dataArray[1] + " " + dataArray[0] + " " + dataArray[2].removeRange(4, dataArray[2].length)
            itemView.findViewById<TextView>(R.id.textView_time).text = dataArray[3].removeRange(5, dataArray[3].length)

            layout.addView(itemView)
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

        btnDelete.setOnClickListener {
            dbHelper.deleteAllData()
            layout.removeViews(2, layout.childCount - 2 )

        }
    }
}