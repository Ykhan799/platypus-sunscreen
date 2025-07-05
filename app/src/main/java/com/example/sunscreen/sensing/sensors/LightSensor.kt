package com.example.sunscreen.sensing.sensors

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class LightSensor(private val context: Context) : SensorInterface, SensorEventListener {
    private val _lightData = MutableLiveData<Float>()
    val lightData: LiveData<Float> = _lightData
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    override fun register() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            _lightData.postValue(lux)
        }
    }

}