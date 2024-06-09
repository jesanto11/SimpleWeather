package com.example.simpleweather.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simpleweather.ui.viewmodel.WeatherViewModel

@Composable
fun WeatherApp(viewModel: WeatherViewModel = viewModel()) {
    var city by remember { mutableStateOf("") }

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
        Button(onClick = { viewModel.fetchCoordinates(city) }) {
            Text("Get Weather")
        }
        Spacer(modifier = Modifier.height(16.dp))

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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WeatherApp()
}