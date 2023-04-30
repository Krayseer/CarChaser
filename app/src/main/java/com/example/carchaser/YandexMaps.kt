package com.example.carchaser

import android.content.pm.PackageManager.*
import android.Manifest.permission.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.location.*
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

class YandexMaps : AppCompatActivity() {

    private lateinit var yandexMap: MapView
    private lateinit var userLocationLayer: UserLocationLayer

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var markerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("de9b0ed8-2f41-4a47-b937-cf363d3cb875")
        MapKitFactory.initialize(this)

        setContentView(R.layout.yandex_maps)
        yandexMap = findViewById(R.id.mapview)
        markerButton = findViewById(R.id.button_marker)

        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(yandexMap.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        locationManager = MapKitFactory.getInstance().createLocationManager()

        locationListener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                markerButton.setOnClickListener {
                    addMarker(location)
                }
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
            }
        }

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            locationManager.subscribeForLocationUpdates(2.0, 0L, 0.0, true, FilteringMode.ON, locationListener)
        }

        addDarkModeButton()
    }

    private fun addDarkModeButton() {
        findViewById<Button>(R.id.button_dark_mode).setOnClickListener {
            yandexMap.mapWindow.map.isNightModeEnabled = !yandexMap.mapWindow.map.isNightModeEnabled
        }
    }

    private fun addMarker(lastKnownLocation: Location) {
        val userLocation = Point(lastKnownLocation.position.latitude, lastKnownLocation.position.longitude)
        yandexMap.map.move(CameraPosition(userLocation, 19.0f, 0.0f, 0.0f))
        yandexMap.mapWindow.map.mapObjects.clear()
        yandexMap.mapWindow.map.mapObjects.addPlacemark(userLocation)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        yandexMap.onStart()
    }

    override fun onStop() {
        yandexMap.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}