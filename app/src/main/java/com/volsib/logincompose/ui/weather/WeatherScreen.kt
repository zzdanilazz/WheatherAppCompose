package com.volsib.logincompose.ui.weather

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.volsib.logincompose.AppViewModelProvider
import com.volsib.logincompose.R
import com.volsib.logincompose.ui.navigation.NavigationDestination
import com.volsib.logincompose.ui.theme.LoginComposeTheme
import com.volsib.logincompose.ui.widget.WeatherWidget
import kotlinx.coroutines.launch

object WeatherDestination : NavigationDestination {
    override val route = "weather"
    override val titleRes = R.string.weather_title
}

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun WeatherScreen(
    navigateToGreetings: () -> Unit,
    viewModel: WeatherViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var city by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text(stringResource(R.string.enter_city)) },
            singleLine = true
        )
        Button(
            onClick = { if (city.isNotBlank()) {
                viewModel.getWeatherByEnteredCity(city)
            } else {
                Toast.makeText(context, "Введите непустое значение!", Toast.LENGTH_SHORT).show()
            }
                   },
        ) {
            Text(text = stringResource(R.string.update_weather_by_city))
        }
        Button(
            onClick = { viewModel.getWeatherByCurrentPosition(context) },
        ) {
            Text(text = stringResource(R.string.update_weather_by_current_place))
        }

        Text(text = "Temperature: " + viewModel.weatherUiState.weatherDetails.temperature.toString() + "°C")
        Text(text = "Feels like: " + viewModel.weatherUiState.weatherDetails.feelsLike.toString()  + "°C")
        Text(text = "Pressure: " + viewModel.weatherUiState.weatherDetails.pressure.toString() + " мм р.с.")

        Text(text = "Coordinates: " + viewModel.weatherUiState.weatherDetails.lon.toString() + ", " + viewModel.weatherUiState.weatherDetails.lat.toString())

        GlideImage(
            modifier = Modifier.size(200.dp),
            model = viewModel.weatherUiState.weatherDetails.iconUrl,
            contentDescription = stringResource(R.string.weather_icon),
            contentScale = ContentScale.Fit
        )

        Text(text = viewModel.weatherUiState.weatherDetails.description)

        Button(
            onClick = {
                coroutineScope.launch {
                    val statusMessage = viewModel.unauthorizeUser()
                    if (statusMessage == "Success") {
                        // Updating the app widget
                        WeatherWidget().updateAll(context)

                        // Navigating to the greetings screen
                        navigateToGreetings()
                    } else {
                        //TODO обработать ошибку
                    }
                }
                      },
        ) {
            Text(text = stringResource(R.string.exit))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenPreview() {
    LoginComposeTheme {
        WeatherScreen({})
    }
}