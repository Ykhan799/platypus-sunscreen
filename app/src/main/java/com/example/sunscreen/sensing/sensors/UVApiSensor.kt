package com.example.sunscreen.sensing.sensors

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.example.sunscreen.models.api.UVIndexResponse
import com.example.sunscreen.utils.data.GsonRequest

/**
 * Code adapted from tutorial https://www.delasign.com/blog/android-studio-kotlin-api-call/
 */
class UVApiSensor(private val apiRequestQueue: RequestQueue) {

    private val _uvData = MutableLiveData<Double>()
    val uvData: LiveData<Double> = _uvData

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    fun fetchUvIndex(
        lat: Double,
        lng: Double,
        tag: String = "UVApiSensor"
    ) {
        val url = Uri.parse("https://api.openuv.io/api/v1/uv")
            .buildUpon()
            .appendQueryParameter("lat", lat.toString())
            .appendQueryParameter("lng", lng.toString())
            .build()
            .toString()

        val headers = mutableMapOf<String, String>()
        headers["x-access-token"] = "openuv-42d9mrmcr69w09-io"  // openuv.io API key | 50 requests per day

        val request = GsonRequest(
            url = url,
            clazz = UVIndexResponse::class.java,
            method = Request.Method.GET,
            headers = headers,
            jsonPayload = null,
            listener = { response ->
                val uv = response.result.uv
                Log.i(tag, "Successfully fetched UV index: $uv")
                _uvData.postValue(uv)
            },
            errorListener = { error ->
                val errorMessage = "Failed to fetch UV data: ${error.message}"
                Log.e(tag, errorMessage)
                _error.postValue(error)
            }
        )

        apiRequestQueue.add(request)
    }
}