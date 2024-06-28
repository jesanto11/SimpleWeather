package com.example.simpleweather.ui.components

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simpleweather.data.utils.checkAndRequestLocationPermissions
import com.example.simpleweather.data.utils.getCurrentLocation
import com.example.simpleweather.data.utils.isLocationServicesEnabled
import com.example.simpleweather.data.utils.promptEnableLocationServices
import com.example.simpleweather.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@Composable
fun WeatherApp(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    viewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var city by remember { mutableStateOf("") }
    val isLoading by viewModel.loading

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Enter City") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (context is Activity) {
                if (!isLocationServicesEnabled(context)) {
                    promptEnableLocationServices(context)
                } else if (checkAndRequestLocationPermissions(context, requestPermissionLauncher)) {
                    scope.launch {
                        viewModel.loading.value = true
                        val locationResult = getCurrentLocation(context)
                        locationResult.fold(
                            onSuccess = { location ->
                                Log.d("WeatherApp", "Location obtained: $location")
                                viewModel.fetchWeather(location.latitude, location.longitude)
                                viewModel.loading.value = false
                            },
                            onFailure = { error ->
                                Log.e("WeatherApp", "Error fetching location", error)
                                viewModel.setErrorMessage(error.message ?: "Error fetching location")
                                viewModel.loading.value = false
                            }
                        )
                    }
                } else {
                    viewModel.setErrorMessage("Location permission not granted")
                }
            }
        }) {
            Text("Get Current Location Weather")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.fetchCoordinates(city) }) {
            Text("Get Weather")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            viewModel.weatherResponse.value?.let { weather ->
                Text(text = "Temperature: ${weather.main.temp} °C")
                Text(text = "Humidity: ${weather.main.humidity} %")
                Text(text = "Description: ${weather.weather[0].description}")
            }

            viewModel.errorMessage.value?.let { error ->
                Text(text = "Error: $error", color = Color.Red)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val mockRequestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        //Not used for now
    }

    WeatherApp(requestPermissionLauncher = mockRequestPermissionLauncher)
}