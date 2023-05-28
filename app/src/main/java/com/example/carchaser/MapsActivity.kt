package com.example.carchaser

import android.content.pm.PackageManager.*
import android.Manifest.permission.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.carchaser.common.Constants
import com.example.carchaser.common.Messages
import com.example.carchaser.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var btnAddMarker: Button
    private lateinit var btnCreateNote: Button
    private lateinit var btnDarkMode: Button
    private lateinit var btnHistory: Button
    private lateinit var btnShared: Button
    private lateinit var btnRefresh: Button

    private val defaultPosition: LatLng = LatLng(56.8519, 60.6122)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMapsBinding.inflate(layoutInflater).root)

        btnCreateNote = findViewById(R.id.btn_info)
        btnAddMarker = findViewById(R.id.btn_add_marker)
        btnDarkMode = findViewById(R.id.button_dark_mode)
        btnHistory = findViewById(R.id.btn_history)
        btnShared = findViewById(R.id.btn_shared)
        btnRefresh = findViewById(R.id.btn_refresh)

        if(!isNetworkConnected()){
            Toast.makeText(this, Messages.MAP_LOADING_ERROR, Toast.LENGTH_LONG).show()
            btnRefresh.isVisible = true
            listOf(btnCreateNote, btnAddMarker, btnDarkMode, btnHistory, btnShared).forEach { btn ->
                btn.isVisible = false
            }
            btnRefresh.setOnClickListener {
                this.recreate()
            }
            return;
        }

        dbHelper = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val sharedPref = this.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (isFirstTimeAppLaunch()) {
            sharedPref.edit().putBoolean(Constants.NIGHT_MODE_PREFIX, false).apply()
        }

        btnAddMarker.setOnClickListener {
            if(dbHelper.getDataActive() == null) {
                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    if (isGpsEnabled()) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                val userPosition = LatLng(location.latitude, location.longitude)
                                addParkingPlace(userPosition, isSave = true)
                            }
                        }
                    } else {
                        addParkingPlace(defaultPosition, isSave = true)
                    }
                } else {
                    addParkingPlace(defaultPosition, isSave = true)
                }
            } else {
                deleteParkingPlace()
            }
        }

        if (sharedPref.getBoolean(Constants.NIGHT_MODE_PREFIX, false)) {
            btnDarkMode.foreground = resources.getDrawable(R.drawable.nightmode_foreground, null)
        } else {
            btnDarkMode.foreground = resources.getDrawable(R.drawable.daymode_foreground, null)
        }

        btnShared.setOnClickListener {
            val markerPosition = dbHelper.getDataActive()
            val uri = Uri.parse("carchaser://maps?${Constants.REQUEST_PARAM_LATITUDE}=${markerPosition?.latitude}" +
                    "&${Constants.REQUEST_PARAM_LONGITUDE}=${markerPosition?.longitude}")
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, uri.toString())
            val chooserIntent = Intent.createChooser(intent, Messages.SHARE_LINK)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(chooserIntent)
        }

        btnDarkMode.setOnClickListener {
            if (sharedPref.getBoolean(Constants.NIGHT_MODE_PREFIX, false)) {
                btnDarkMode.foreground = resources.getDrawable(R.drawable.daymode_foreground, null)
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
                sharedPref.edit().putBoolean(Constants.NIGHT_MODE_PREFIX, false).apply()
            }
            else {
                btnDarkMode.foreground = resources.getDrawable(R.drawable.nightmode_foreground, null)
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))
                sharedPref.edit().putBoolean(Constants.NIGHT_MODE_PREFIX, true).apply()
            }
        }

        btnCreateNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter, androidx.appcompat.R.anim.abc_popup_exit)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        initCheckGeoPosition()
        mMap = googleMap

        val sharedPref = this.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(Constants.NIGHT_MODE_PREFIX, false)) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))
        }
        else {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
        }

        val intent = intent
        if (intent != null && intent.data != null && intent.scheme == "carchaser") {
            val uri = intent.data
            val lat = uri?.getQueryParameter(Constants.REQUEST_PARAM_LATITUDE)?.toDouble()
            val lng = uri?.getQueryParameter(Constants.REQUEST_PARAM_LONGITUDE)?.toDouble()
            if (lat != null && lng != null) {
                val place = getAddressFromCoordinates(LatLng(lat, lng)).toString()
                dbHelper.updatePosition(lat, lng, place)
            }
        }

        val dataActive = dbHelper.getDataActive()
        if (dataActive != null) {
            val markerPosition = LatLng(dataActive.latitude, dataActive.longitude)
            addParkingPlace(markerPosition)
        }

        updateParkingPositionListener()
        initMoveCamera()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        initMoveCamera()
    }

    /**
     * Функция построения маршрута от пользователя до маркера(автомобиля)
     * НЕ РАБОТАЕТ из за того, что нужен платный API ключ.
     */
    private fun showRoute(origin: LatLng, destination: LatLng) {
        val request = DirectionsApi
            .newRequest(GeoApiContext
                .Builder()
                .apiKey("AIzaSyBg8K2t1NK9KDDD-rj-Zf-d-gqNaW3Xwb0")
                .build())
            .mode(TravelMode.WALKING)
            .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))

        val legs = request.await().routes[0].legs
        for (leg in legs) {
            for (step in leg.steps) {
                for (point in step.polyline.decodePath()) {
                    val pos = LatLng(point.lat, point.lng)
                    mMap.addPolyline(PolylineOptions().add(pos).color(Color.BLUE).width(Constants.POLYLINE_WIDTH))
                }
            }
        }
    }

    /**
     * Функция добавления маркера на текущее местоположение пользователя
     * @param markerPosition позиция, на которую нужно поставить маркер
     * @param isSave является ли маркер новым. Если true, то сохраняем данные в БД
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addParkingPlace(markerPosition: LatLng, isSave: Boolean = false) {
        mMap.addMarker(MarkerOptions().position(markerPosition).title(Messages.LAST_STOP).draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, Constants.CAMERA_ZOOM))
        btnCreateNote.isEnabled = true
        btnShared.isEnabled = true
        btnAddMarker.text = Messages.DELETE

        if (isSave) {
            val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)).toString()
            val address = getAddressFromCoordinates(markerPosition).toString()
            dbHelper.insertData(date, address, markerPosition.latitude, markerPosition.longitude)
        }
    }

    /**
     * Удалить маркер с карты и сохранить данные о парковке в БД
     */
    private fun deleteParkingPlace() {
        dbHelper.updateActivity()
        mMap.clear()
        btnCreateNote.isEnabled = false
        btnShared.isEnabled = false
        btnAddMarker.text = Messages.PARK
    }

    /**
     * Функция получения адреса по координатам
     * @param coordinates координаты, по которым нужно получить адрес
     * @return полученный адрес по координатам
     */
    fun getAddressFromCoordinates(coordinates: LatLng): String? {
        return Geocoder(this)
            .getFromLocation(coordinates.latitude, coordinates.longitude, 1)
            ?.get(0)
            ?.getAddressLine(0)
    }

    /**
     * Функция, которая при первом заходе в приложение запрашивает использование местоположения
     */
    private fun initCheckGeoPosition() {
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 0)
    }

    /**
     * Функция, которая инициализирует местоположение камеры с учетом возможности использования
     * геопозиции пользователя
     */
    private fun initMoveCamera() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, Constants.DEFAULT_CAMERA_ZOOM))
            return
        }

        if (!isGpsEnabled()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, Constants.DEFAULT_CAMERA_ZOOM))
        } else {
            mMap.isMyLocationEnabled = true
            LocationServices.getFusedLocationProviderClient(this)
                .lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location !== null) {
                        val userPosition = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.CAMERA_ZOOM))
                    }
                }
        }
    }

    /**
     * Функция устанавливает на карту слушатель, который обновляет глобальное значение position
     * на текущее местоположение метки парковки
     */
    private fun updateParkingPositionListener() {
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) { }

            override fun onMarkerDrag(marker: Marker) { }

            override fun onMarkerDragEnd(marker: Marker) {
                val markerPosition = marker.position
                val updatedPlace = getAddressFromCoordinates(markerPosition).toString()
                dbHelper.updatePosition(markerPosition.latitude, markerPosition.longitude, updatedPlace)
            }
        })
    }

    /**
     * Функция проверяет, запущено приложение первый раз или нет
     */
    private fun isFirstTimeAppLaunch(): Boolean {
        val sharedPref = this.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean(Constants.FIRST_TIME_PREFIX, true)
        if (isFirstTime) {
            sharedPref.edit().putBoolean(Constants.FIRST_TIME_PREFIX, false).apply()
        }
        return isFirstTime
    }

    /**
     * Функция проверяет, подключен ли телефон к интернету
     */
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    /**
     * Проверить, включен ли у телефона GPS
     */
    private fun isGpsEnabled(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}