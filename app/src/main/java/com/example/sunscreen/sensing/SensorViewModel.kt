package com.example.sunscreen.sensing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.sunscreen.sensing.sensors.LightSensor
import com.example.sunscreen.sensing.sensors.SensorFusion
import com.example.sunscreen.sensing.sensors.SensorInterface


class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val lightSensor = LightSensor(application.applicationContext)
    val lightData: LiveData<Float> get() = lightSensor.lightData
    private val sensors: List<SensorInterface> = listOf(lightSensor)

    private val sensorFusion = SensorFusion(lightSensor)
    val uvExposed: LiveData<Boolean> get() = sensorFusion.uvExposed

    init {
        registerSensors()
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensors()
    }

    fun registerSensors() {
        sensors.forEach { it.register() }
    }

    fun unregisterSensors() {
        sensors.forEach { it.unregister() }
    }
}