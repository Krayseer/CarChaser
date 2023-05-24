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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryActivity : AppCompatActivity() {
    private lateinit var layout: LinearLayout
    private lateinit var btnReturn: Button
    private lateinit var btnDelete: Button
    private val dbHelper = DatabaseHelper(this)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val dbHelper = DatabaseHelper(this)
        val data = dbHelper.getDataNotActive()
        btnReturn = findViewById(R.id.button_return_2)
        btnDelete = findViewById(R.id.button_delete_history)
        layout = findViewById(R.id.layout_history)

        val inflater = LayoutInflater.from(this)
        for (i in data.reversed()) {
            val itemView = inflater.inflate(R.layout.history_item, null, false)
            val button = Button(this)
            button.text = "Удалить"

            // Здесь можно настроить отображение данных внутри TextView.
            itemView.findViewById<TextView>(R.id.textView_address).text = "• " +i.place
            val date = i.date.split(", ")[0].split(" ")
            itemView.findViewById<TextView>(R.id.textView_date).text = "• " + date[1] + " " + date[0] + " " + date[2]
            itemView.findViewById<TextView>(R.id.textView_time).text = "• " + i.date.split(", ")[1]
            itemView.findViewById<Button>(R.id.button_delete_parking).setOnClickListener {
                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
                alertDialog.setTitle("Удаление")
                alertDialog.setMessage("Желаете удалить эту метку?")
                alertDialog.setPositiveButton("Да", DialogInterface.OnClickListener { dialog, _ ->
                    dbHelper.deleteStroke(i.date)
                    //recreate() на устройствах Xiaomi вызывает ошибку, она не роняет приложение, но все равно неприятно
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                })
                alertDialog.setNegativeButton("Закрыть", DialogInterface.OnClickListener { dialog, _ ->
                    // Действие, которое выполнится при нажатии на кнопку "Закрыть"
                    dialog.dismiss() // Закрыть диалоговое окно
                })

                alertDialog.show()
            }

            itemView.setOnClickListener {
                val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a")).toString()
                val adress = i.place
                showDialog(date, adress, i.latitude, i.longitude)
            }
            layout.addView(itemView)
        }

        btnReturn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("Удаление")
            alertDialog.setMessage("Желаете удалить историю парковок?")
            alertDialog.setPositiveButton("Да", DialogInterface.OnClickListener { dialog, _ ->
                // Действие, которое выполнится при нажатии на кнопку "OK"
                dbHelper.deleteAllData()
                layout.removeViews(2, layout.childCount - 2 )
            })
            alertDialog.setNegativeButton("Закрыть", DialogInterface.OnClickListener { dialog, _ ->
                // Действие, которое выполнится при нажатии на кнопку "Закрыть"
                dialog.dismiss() // Закрыть диалоговое окно
            })

            alertDialog.show()
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