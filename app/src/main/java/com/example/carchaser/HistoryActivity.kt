package com.example.carchaser

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryActivity : AppCompatActivity() {
    private lateinit var layout: LinearLayout
    private lateinit var btnReturn: Button
    private lateinit var btnDelete: Button
    private lateinit var coord: LatLng
    private val dbHelper = MyDatabaseHelper(this)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val dbHelper = MyDatabaseHelper(this)
        val arguments = intent.extras
        if (arguments != null) {
            coord = arguments.get("position") as LatLng
        }
        val data = dbHelper.getDataNotActive()
        btnReturn = findViewById(R.id.button_return_2)
        btnDelete = findViewById(R.id.button_delete_history)
        layout = findViewById(R.id.layout_history)

        val inflater = LayoutInflater.from(this)
        for (i in data.reversed()) {
            // Inflate the view from the layout resource.
            val itemView = inflater.inflate(R.layout.history_item, null, false)

            // Здесь можно настроить отображение данных внутри TextView.
            itemView.findViewById<TextView>(R.id.textView_address).text = i.place
            itemView.findViewById<TextView>(R.id.textView_date).text = i.date.split(", ")[0]
            itemView.findViewById<TextView>(R.id.textView_time).text = i.date.split(", ")[1]

            itemView.setOnClickListener {
                val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a")).toString()
                val adress = i.place
                showDialog(date, adress, i.latitude, i.longitude)
            }
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

    private fun showDialog(date: String, adress: String, latitude: Double, longitude: Double) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("Парковка")
        alertDialog.setMessage("Желаете возобновить эту метку?")
        alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
            // Действие, которое выполнится при нажатии на кнопку "OK"
            dbHelper.updateActivity()
            dbHelper.insertData(date, adress, 1, latitude, longitude)
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        })
        alertDialog.setNegativeButton("Закрыть", DialogInterface.OnClickListener { dialog, _ ->
            // Действие, которое выполнится при нажатии на кнопку "Закрыть"
            dialog.dismiss() // Закрыть диалоговое окно
        })

        alertDialog.show()
    }
}