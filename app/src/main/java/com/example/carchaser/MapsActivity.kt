package com.example.carchaser

import android.content.pm.PackageManager.*
import android.Manifest.permission.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geocoder: Geocoder
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isNight: Boolean = false
    private var markerIsAdd: Boolean = false
    private lateinit var position: LatLng

    private lateinit var btnAddMarker: Button
    private lateinit var btnCreateNote: Button
    private lateinit var btnDarkMode: Button
    private lateinit var btnHistory: Button


    //private lateinit var dbHelper: SQLiteOpenHelper
    private lateinit var dateTime:String
    private lateinit var adres:String


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geocoder = Geocoder(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = MyDatabaseHelper(this)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnCreateNote = findViewById(R.id.btn_info)
        btnAddMarker = findViewById(R.id.btn_add_marker)
        btnDarkMode = findViewById(R.id.button_dark_mode)
        btnHistory = findViewById(R.id.btn_history)
        btnCreateNote.isEnabled = false

        val sharedPref = this.getSharedPreferences("MyAppPref", Context.MODE_PRIVATE)
        if (isFirstTimeAppLaunch(this)) {
            sharedPref.edit().putBoolean("isNight", false).apply()
        }


//        val arguments = intent.extras
//        if (arguments != null) {
//            btnCreateNote.isEnabled = arguments.getBoolean("ButtonInfo")
//            btnAddMarker.isEnabled = arguments.getBoolean("ButtonAddMark")
//        }

        btnAddMarker.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED){
                if (!markerIsAdd){
                    markerIsAdd = true
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                val currentUserPosition = LatLng(location.latitude, location.longitude)
                                position = currentUserPosition
                                addParkingPlace(position)
                                val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a")).toString()
                                val adress = getAddressFromCoordinates(position).toString()
                                dbHelper.insertData(date, adress, 1, position.latitude, position.longitude)
                            }
                        }
                    btnCreateNote.isEnabled = true
                    btnAddMarker.text = "Удалить"
                }
                else {
                    dbHelper.updateActivity()
                    mMap.clear()
                    markerIsAdd = false
                    btnCreateNote.isEnabled = false
                    btnAddMarker.text = "Парковаться"
//                    dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a")).toString()
//                    adres = getAddressFromCoordinates(position).toString()
//                    val db: SQLiteDatabase = dbHelper.writableDatabase
//                    val values: ContentValues = ContentValues()
//                    values.put("date", dateTime)
//                    values.put("place", adres)
//
//                    db.insert("my_table", null, values)
//                    db.close()
                }
            }
        }

        /**
         * Тут надо сделать считывание переменной isNight из БД
         */
        btnDarkMode.setOnClickListener {
            //Считывать переменную isNight из настроек
            if (sharedPref.getBoolean("isNight", false)) {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
                sharedPref.edit().putBoolean("isNight", false).apply()
            }
            else {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))
                sharedPref.edit().putBoolean("isNight", true).apply()
            }


//            isNight = if(!isNight){
//                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))
//                true
//            } else {
//                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
//                false
//            }
        }

        btnCreateNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            //intent.putExtra("position", position)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter, androidx.appcompat.R.anim.abc_popup_exit)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            if (markerIsAdd)
                intent.putExtra("position", position)
            startActivity(intent)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sharedPref = this.getSharedPreferences("MyAppPref", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("isNight", false)) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode))
        }
        else {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))
        }
        // Устанавливать значение исходя из настройки
        //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.light_mode))

        val dbHelper = MyDatabaseHelper(this)
        val dataActive = dbHelper.getDataActive()
        if (dataActive.isNotEmpty()) {
            position = LatLng(dataActive[0].latitude, dataActive[0].longitude)
            addParkingPlace(position)
            markerIsAdd = true
            btnCreateNote.isEnabled = true
            btnAddMarker.text = "Удалить"
        }

        updateParkingPositionListener()
        initCheckGeoPosition()
        initMoveCamera()

//        val arguments = intent.extras
//        if (arguments != null) {
//            position = arguments.get("coordinates") as LatLng
//            addParkingPlace(position)
//        }
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
                    mMap.addPolyline(PolylineOptions().add(pos).color(Color.BLUE).width(5f))
                }
            }
        }
    }

    /**
     * Функция добавления маркера на текущее местоположение пользователя
     */
    private fun addParkingPlace(markerPosition: LatLng) {
        mMap.addMarker(MarkerOptions()
            .position(markerPosition)
            .title("Последняя стоянка")
            .draggable(true))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 18f))
    }

    /**
     * Функция получения адреса по координатам
     * @param coordinates координаты, по которым нужно получить адрес
     * @return полученный адрес по координатам
     */
    private fun getAddressFromCoordinates(coordinates: LatLng): String? {
        return geocoder
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
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            LocationServices
                .getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentUserPosition = LatLng(location.latitude, location.longitude)
                        position = currentUserPosition

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, 18f))
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
                position = marker.position
                // Добавить обновление позиции в бд активной метки
            }
        })
    }

    /**
     * Функция проверяет, запущено приложение первый раз или нет
     */
    fun isFirstTimeAppLaunch(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("MyAppPref", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            // сохраняем флаг, что приложение было запущено в первый раз
            sharedPref.edit().putBoolean("isFirstTime", false).apply()
        }

        return isFirstTime
    }

}