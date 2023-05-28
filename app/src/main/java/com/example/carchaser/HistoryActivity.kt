package com.example.carchaser

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.carchaser.common.Constants
import com.example.carchaser.common.Messages
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Активность страницы об истории стоянок
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var layout: LinearLayout
    private lateinit var btnReturn: Button
    private lateinit var btnDelete: Button

    private val dbHelper = DatabaseHelper(this)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        btnReturn = findViewById(R.id.button_return_2)
        btnDelete = findViewById(R.id.button_delete_history)
        layout = findViewById(R.id.layout_history)

        for (markerData in dbHelper.getDataNotActive().reversed()) {
            val itemView = LayoutInflater.from(this).inflate(R.layout.history_item, null, false)
            val date = markerData.date.split(", ")[0].split(" ")

            itemView.findViewById<TextView>(R.id.textView_address).text = "• " + markerData.place
            itemView.findViewById<TextView>(R.id.textView_date).text = "• " + date[1] + " " + date[0] + " " + date[2]
            itemView.findViewById<TextView>(R.id.textView_time).text = "• " + markerData.date.split(", ")[1]

            itemView.findViewById<Button>(R.id.button_delete_parking).setOnClickListener {
                deleteListener {
                    dbHelper.deleteParkingData(markerData.date)
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }

            itemView.setOnClickListener {
                val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)).toString()
                val address = markerData.place
                showDialog(currentDate, address, markerData.latitude, markerData.longitude)
            }
            layout.addView(itemView)
        }

        btnReturn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            deleteListener {
                dbHelper.deleteAllData()
                layout.removeViews(Constants.START_DELETE_MARKER_INDEX, layout.childCount - Constants.START_DELETE_MARKER_INDEX)
            }
        }
    }

    private fun deleteListener(deleteAction: () -> Unit) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle(Messages.ALERT_DELETE)
        alertDialog.setMessage(Messages.REMOVAL_QUESTION)
        alertDialog.setPositiveButton(Messages.POSITIVE) { _, _ ->
            deleteAction()
        }
        alertDialog.setNegativeButton(Messages.CLOSE) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun showDialog(date: String, address: String, latitude: Double, longitude: Double) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle(Messages.PARKING)
        alertDialog.setMessage(Messages.RESET_MARKER_QUESTION)
        alertDialog.setPositiveButton(Messages.OK) { _, _ ->
            dbHelper.updateActivity()
            dbHelper.insertData(date, address, latitude, longitude)
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        alertDialog.setNegativeButton(Messages.CLOSE) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

}