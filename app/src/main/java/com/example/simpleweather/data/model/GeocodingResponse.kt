package com.example.simpleweather.data.model

data class GeocodingResponse (
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)