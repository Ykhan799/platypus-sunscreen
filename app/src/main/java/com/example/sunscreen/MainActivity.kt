package com.example.sunscreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.sunscreen.sensing.SensorViewModel
import com.example.sunscreen.ui.theme.SunscreenTheme

class MainActivity : ComponentActivity() {

    private val sensorViewModel: SensorViewModel by viewModels()
    private lateinit var sunscreenNotificationManager: SunscreenNotificationManager

    // Configures location permissions
    private val locPerm = Manifest.permission.ACCESS_FINE_LOCATION
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) sensorViewModel.registerSensors()   // triggers LocationSensor.register()
        }

    // Enables permissions necessary to track GPS location
    private fun ensureLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, locPerm) ==
            PackageManager.PERMISSION_GRANTED) {
            sensorViewModel.registerSensors()
        } else {
            requestLocationPermission.launch(locPerm)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ensureLocationPermission()

        // Observe various relevant sensor data
        sensorViewModel.registerSensors()

        // Fetch UV index for current user location
        sensorViewModel.locationData.observe(this) { location ->
            sensorViewModel.fetchCurrentUvIndex(location.latitude, location.longitude)
        }

        // Notify user when they're exposed
        sunscreenNotificationManager = SunscreenNotificationManager(applicationContext, sensorViewModel, lifecycleScope)
        sunscreenNotificationManager.start()

        setContent {
            SunscreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainDisplay(
                        sensorViewModel = sensorViewModel,
                        modifier = Modifier.padding(innerPadding).padding(16.dp)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorViewModel.unregisterSensors()
    }
}

@Composable
fun MainDisplay(
    sensorViewModel: SensorViewModel,
    modifier: Modifier = Modifier
) {
    val lightValue by rememberUpdatedState(sensorViewModel.lightData.observeAsState(initial = 0f).value)
    val uvExposed by rememberUpdatedState(sensorViewModel.uvExposed.observeAsState(initial = false).value)
    val uvValue by rememberUpdatedState(sensorViewModel.uvData.observeAsState(initial = 0.0).value)
    val location by rememberUpdatedState(sensorViewModel.locationData.observeAsState(initial = null).value)

    Column(modifier = modifier) {

        Text(text = "Current Light Sensor Value:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$lightValue lux", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Current Location Value:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Lat: ${location?.latitude ?: "--"}, Lng: ${location?.longitude ?: "--"}")
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Current UV Index Value:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$uvValue", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Fused UV Exposure Value:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (uvExposed) "⚠️ UV Exposed" else "✅ Safe from UV",
            color = if (uvExposed) Color.Red else Color.Green
        )
        Spacer(modifier = Modifier.height(16.dp))

        PlatypusImage()
    }
}

@Composable
fun PlatypusImage() {
    Image(
        painter = painterResource(id = R.drawable.platypus_sunscreen),
        contentDescription = "Platypus sunscreen",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxHeight()
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SunscreenTheme {
        Greeting("Android")
    }
}
