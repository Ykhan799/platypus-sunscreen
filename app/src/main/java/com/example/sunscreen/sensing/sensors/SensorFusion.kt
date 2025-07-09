package com.example.sunscreen.sensing.sensors

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SensorFusion(
    private val lightSensor: LightSensor
) {
    val uvExposed: LiveData<Boolean> get() = _uvExposed
    private val _uvExposed = MutableLiveData<Boolean>(false)

    private var lastLightValue: Float? = null

    init {
        lightSensor.lightData.observeForever {
            lastLightValue = it
            this.fuseSensorData()
        }
    }

    // This runs whenever one of the source sensor data values changes.
    private fun fuseSensorData(){
        val exposed = isUVExposed() && isUVHigh()
        _uvExposed.postValue(exposed)
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
        // TODO interrogate UV API
        return true
    }
}
