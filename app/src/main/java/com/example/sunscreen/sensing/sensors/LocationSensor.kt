package com.example.sunscreen.sensing.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class LocationSensor(context: Context) : SensorInterface {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _locationData = MutableLiveData<Location>()
    val locationData: MutableLiveData<Location> = _locationData

    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    override fun register() {
        // updates location request if new location is more than 1 meter minimum
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1_000L       // 1 second for dev
        )
            .setMinUpdateDistanceMeters(1f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { _locationData.postValue(it) }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun unregister() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
