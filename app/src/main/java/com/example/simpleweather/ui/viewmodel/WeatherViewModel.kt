package com.example.simpleweather.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleweather.data.model.GeocodingResponse
import com.example.simpleweather.data.model.WeatherResponse
import com.example.simpleweather.data.network.RetrofitInstance
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _weatherResponse = mutableStateOf<WeatherResponse?>(null)
    val weatherResponse = _weatherResponse

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage = _errorMessage

    private val coordinatesResponse = mutableStateOf<GeocodingResponse?>(null)

    private val apiKey = "API_KEY"

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.weatherApi.getWeather(latitude, longitude, apiKey)
                _weatherResponse.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchCoordinates(cityName: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.geocodingApi.getCoordinates(cityName, 1, apiKey)
                if (response.isNotEmpty()) {
                    coordinatesResponse.value = response[0]
                    fetchWeather(coordinatesResponse.value!!.lat, coordinatesResponse.value!!.lon)
                } else {
                    _errorMessage.value = "No Coordinates found for the specified city"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}