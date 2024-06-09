package com.example.simpleweather.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    private const val WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
    private const val GEOCODING_URL = "https://api.openweathermap.org/geo/1.0/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    val geocodingApi: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEOCODING_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }
}