package com.example.simpleweather.data.network

import com.example.simpleweather.data.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("direct")
    suspend fun getCoordinates(
        @Query("q") cityName: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>
}