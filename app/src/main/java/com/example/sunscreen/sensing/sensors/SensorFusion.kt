package com.example.sunscreen.sensing.sensors

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SensorFusion(
    lightSensor: LightSensor,
    locationSensor: LocationSensor,
    uvApiSensor: UVApiSensor
) {
    val uvExposed: LiveData<Boolean> get() = _uvExposed
    private val _uvExposed = MutableLiveData(false)

    // outside detection variables
    private val _firstOutsideToday = MutableLiveData<Boolean>()
    val firstOutsideToday: LiveData<Boolean> get() = _firstOutsideToday
    private val _outsideTwoHours = MutableLiveData<Boolean>()
    val outsideTwoHours: LiveData<Boolean> get() = _outsideTwoHours

    private var lastLightValue: Float? = null
    private var lastLocation: Location? = null

    private var lastuvValue: Double? = null
    private var outsideStartTime: Long? = null
    private var hasNotifiedFirstOutsideToday = false

    private val TWO_HOURS_MS = TimeUnit.HOURS.toMillis(2)


    init {
        lightSensor.lightData.observeForever {
            lastLightValue = it
            this.fuseSensorData()
        }

        // Location sensor observer
        locationSensor.locationData.observeForever {
            lastLocation = it
            updateOutdoorTimers()
            this.fuseSensorData()
        }

        uvApiSensor.uvData.observeForever {
            lastuvValue = it
            this.fuseSensorData()
        }
    }

    // This runs whenever one of the source sensor data values changes.
    private fun fuseSensorData(){
        val exposed = isUVExposed() && isUVHigh()
        _uvExposed.postValue(exposed)
    }

    private fun isOutside(): Boolean {
        val loc = lastLocation ?: return false

        // checks location based on walking speed
        return loc.accuracy <= 50f && loc.speed >= 0.5f
    }

    // Combines the current sensor data to infer whether the User is being exposed to UV.
    // The user could be outside, but in shade => not UV exposed.
    private fun isUVExposed(): Boolean {
        val light = lastLightValue ?: return false

        // Fusion algorithm
        var exposed = light > 10000

        return exposed
    }

    private fun isUVHigh(): Boolean {
        if (lastuvValue == null) {
            return false
        }

        return lastuvValue!! >= 3
    }

    // updates timers user is outdoors
    private fun updateOutdoorTimers() {
        val now = System.currentTimeMillis()
        val outside = isOutside()

        // Reset daily flag at midnight
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (now < cal.timeInMillis) {
            hasNotifiedFirstOutsideToday = false
        }

        if (outside) {
            if (!hasNotifiedFirstOutsideToday) {
                _firstOutsideToday.postValue(true)
                hasNotifiedFirstOutsideToday = true
            }
            if (outsideStartTime == null) outsideStartTime = now
            val elapsed = now - outsideStartTime!!
            _outsideTwoHours.postValue((elapsed % TWO_HOURS_MS).toInt() == 0)
        } else {
            outsideStartTime = null
            _outsideTwoHours.postValue(false)
        }
    }
}
