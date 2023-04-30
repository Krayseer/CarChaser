package com.example.carchaser

import android.content.pm.PackageManager.*
import android.Manifest.permission.*
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.carchaser.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var markerIsAdd: Boolean = false
    private lateinit var position: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnCreateNote = findViewById<Button>(R.id.btn_info)

        val btnAddMarker = findViewById<Button>(R.id.btn_add_marker)

        val arguments = intent.extras
        if (arguments != null) {
            btnCreateNote.isEnabled = arguments.getBoolean("ButtonInfo")
            btnAddMarker.isEnabled = arguments.getBoolean("ButtonAddMark")
        }
        btnAddMarker.setOnClickListener {
            if (!markerIsAdd){
                markerIsAdd = true
                addParkingPlace()
                btnCreateNote.isEnabled = true
                btnAddMarker.text = "Удалить"
                btnAddMarker.isEnabled = false
            }
        }

        btnCreateNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra("position", position)
            startActivity(intent)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val arguments = intent.extras
        if (arguments != null) {
            position = arguments.get("coordinates") as LatLng
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(position).title("Последняя стоянка"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18f))
        }
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            LocationServices
                .getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentUserPosition = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, 18f))
                    }
                }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 0)
        }
    }

    /**
     * Функция построения маршрута от пользователя до маркера(автомобиля)
     */
    private fun showRoute(origin: LatLng, destination: LatLng) {
        val directionsApi = GeoApiContext.Builder()
            .apiKey("AIzaSyBg8K2t1NK9KDDD-rj-Zf-d-gqNaW3Xwb0") // замените на свой API-ключ
            .build()

        val request = DirectionsApi.newRequest(directionsApi)
            .mode(TravelMode.WALKING) // выбираем режим перемещения (в данном случае - езда на машине)
            .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))

        val response = request.await()

        val legs = response.routes[0].legs
        for (i in 0 until legs.size) {
            val steps = legs[i].steps
            for (j in 0 until steps.size) {
                val points = steps[j].polyline.decodePath()
                for (point in points) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(point.lat, point.lng))
                            .color(Color.BLUE)
                            .width(5f)
                    )
                }
            }
        }
    }

    /**
     * Функция добавления маркера на текущее местоположение пользователя (не работает, возможно
     * из-за того, что нужен платный API ключ, но ошибка указывает именно на API ключ
     */
    private fun addParkingPlace() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            LocationServices
                .getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentUserPosition = LatLng(location.latitude, location.longitude)
                        position = currentUserPosition

                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(currentUserPosition).title("Последняя стоянка"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, 18f))
                    }
                }
        }
    }
}