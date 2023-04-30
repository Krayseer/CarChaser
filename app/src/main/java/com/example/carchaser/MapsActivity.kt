package com.example.carchaser

import android.content.pm.PackageManager.*
import android.Manifest.permission.*
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geocoder: Geocoder

    private var markerIsAdd: Boolean = false
    private lateinit var position: LatLng

    private lateinit var btnAddMarker: Button
    private lateinit var btnCreateNote: Button
    private lateinit var btnDarkMode: Button

    private var isNight: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocoder = Geocoder(this)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnCreateNote = findViewById(R.id.btn_info)
        btnAddMarker = findViewById(R.id.btn_add_marker)
        btnDarkMode = findViewById(R.id.button_dark_mode)

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

        /**
         * Тут надо сделать считывание переменной isNight из БД
         */
        btnDarkMode.setOnClickListener {
            isNight = if(!isNight){
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))
                true
            } else {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
                false
            }
        }

        btnCreateNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra("position", position)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter, androidx.appcompat.R.anim.abc_popup_exit)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
        val arguments = intent.extras
        if (arguments != null) {
            position = arguments.get("coordinates") as LatLng
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
            .apiKey("AIzaSyBg8K2t1NK9KDDD-rj-Zf-d-gqNaW3Xwb0")
            .build()

        val request = DirectionsApi.newRequest(directionsApi)
            .mode(TravelMode.WALKING)
            .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))

        val response = request.await()

        val legs = response.routes[0].legs
        for (leg in legs) {
            for (step in leg.steps) {
                for (point in step.polyline.decodePath()) {
                    mMap.addPolyline(PolylineOptions()
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

    /**
     * Функция получения адреса по координатам
     */
    private fun getAddressFromCoordinates(latLng: LatLng): String? {
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return addresses?.get(0)?.getAddressLine(0)
    }

}