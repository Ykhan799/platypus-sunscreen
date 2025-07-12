package com.example.sunscreen.models.api

import com.google.gson.annotations.SerializedName

/**
 * Code adapted from tutorial https://www.delasign.com/blog/android-studio-kotlin-api-call/
 */
data class UVIndexResponse(
    @SerializedName("result")
    val result: UVIndexResult
)

data class UVIndexResult(
    @SerializedName("uv")
    val uv: Double
)