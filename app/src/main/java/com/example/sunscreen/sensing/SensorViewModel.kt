package com.example.sunscreen.sensing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.android.volley.toolbox.Volley
import com.example.sunscreen.sensing.sensors.LightSensor
import com.example.sunscreen.sensing.sensors.SensorFusion
import com.example.sunscreen.sensing.sensors.SensorInterface
import com.example.sunscreen.sensing.sensors.UVApiSensor

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val lightSensor = LightSensor(application.applicationContext)
    val lightData: LiveData<Float> get() = lightSensor.lightData
    private val sensors: List<SensorInterface> = listOf(lightSensor)

    private val requestQueue = Volley.newRequestQueue(application.applicationContext)
    private val uvApiSensor = UVApiSensor(requestQueue)
    val uvData: LiveData<Double> get() = uvApiSensor.uvData

    private val sensorFusion = SensorFusion(lightSensor)
    val uvExposed: LiveData<Boolean> get() = sensorFusion.uvExposed

    init {
        registerSensors()
    }

    fun fetchCurrentUvIndex(lat: Double, lng: Double) {
        uvApiSensor.fetchUvIndex(lat, lng)
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensors()
        requestQueue.cancelAll { true }
    }

    fun registerSensors() {
        sensors.forEach { it.register() }
    }

    fun unregisterSensors() {
        sensors.forEach { it.unregister() }
    }
}
