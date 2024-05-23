package com.volsib.logincompose.ui.weather

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.volsib.logincompose.data.UserRepository
import com.volsib.logincompose.data.WeatherRepository
import com.volsib.logincompose.models.Coord
import com.volsib.logincompose.models.Main
import com.volsib.logincompose.models.Weather
import com.volsib.logincompose.models.WeatherResponse
import com.volsib.logincompose.util.Constants.Companion.IMG_URL
import com.volsib.logincompose.util.Constants.Companion.PERMISSION_REQUEST_CODE
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherViewModel (
    private val weatherRepository: WeatherRepository,
    private val userRepository: UserRepository
): ViewModel() {
    /**
     * Holds current weather ui state
     */
    var weatherUiState by mutableStateOf(WeatherUiState())
        private set

    fun updateWeatherUiState(
        weatherDetails: WeatherDetails,
    ) {
        weatherUiState = WeatherUiState(
            weatherDetails = weatherDetails,
        )
    }

    fun getWeatherByCurrentPosition(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val lat = location.latitude
                    val lon = location.longitude
                    viewModelScope.launch {
                        getWeatherByCoordinates(lat, lon)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("WeatherScreen", "Error getting location: ${e.message}")
            }
    }

    fun getWeatherByEnteredCity(city: String) {
        viewModelScope.launch {
            val response = weatherRepository.getWeatherByCity(city)
            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    weatherUiState = weatherResponse.toWeatherUiState()
                }
            } else {
                // Handle error here
                if (response.code() == 404) {
                    // Ошибка 404: город не найден
//                    weatherUiState.error = "Город не найден!"
                } else {
                    // Другие ошибки HTTP
//                    weatherUiState.error = "Ошибка запроса!"
                }
            }
        }
    }

    private suspend fun getWeatherByCoordinates(lat: Double, lon: Double) {
        val response = weatherRepository.getWeatherByCoordinates(lat, lon)
        if (response.isSuccessful) {
            response.body()?.let { weatherResponse ->
                weatherUiState = weatherResponse.toWeatherUiState()
            }
        }
    }

    suspend fun unauthorizeUser() : String {
        val currentUser = userRepository.getCurrentUserStream().firstOrNull()
        if (currentUser != null) {
            currentUser.isSignedIn = false
            userRepository.updateUser(currentUser)
            return "Success"
        } else {
            //TODO кинуть exception
        }
        return "Error"
    }

}

data class WeatherUiState(
    val weatherDetails: WeatherDetails = WeatherDetails(),
    val updatedAt: String? = null,
    var error: String? = null
)

data class WeatherDetails(
    val lon: Double = 0.0,
    val lat: Double = 0.0,
    val temperature: Int = 0,
    val feelsLike: Int = 0,
    val pressure: Double = 0.0,
    val description: String = "",
    val iconUrl: String = "",
    val city: String = ""
)

fun WeatherDetails.toWeatherResponse(): WeatherResponse = WeatherResponse(
    coord = Coord(lon, lat),
    weather = listOf(Weather(description, iconUrl)),
    main = Main(
        temp = temperature.toDouble(),
        feelsLike = feelsLike.toDouble(),
        pressure = pressure,
    ),
    name = city
)

fun WeatherResponse.toWeatherUiState(): WeatherUiState = WeatherUiState(
    weatherDetails = this.toWeatherDetails(),
    updatedAt = this.toUpdatedAt(),
)

fun WeatherResponse.toWeatherDetails(): WeatherDetails = WeatherDetails(
    lon = coord.lon,
    lat = coord.lat,
    temperature = kelvinToCelsius(main.temp),
    feelsLike = kelvinToCelsius(main.feelsLike),
    pressure = hPaToMm(main.pressure),
    description = weather[0].description,
    iconUrl = iconIdToUrl(weather[0].icon),
    city = name
)

fun WeatherResponse.toUpdatedAt(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.ROOT)
    val currentTime = Date()
    return dateFormat.format(currentTime)
}

fun kelvinToCelsius(kelvin: Double): Int{
    val celsius = kelvin - 273
    return celsius.toInt()
}

fun hPaToMm(hPa: Double): Double {
    return  hPa*0.75
}

fun iconIdToUrl(iconId: String): String {
    return "$IMG_URL$iconId.png"
}