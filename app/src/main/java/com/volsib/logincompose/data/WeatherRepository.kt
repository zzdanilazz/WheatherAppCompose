package com.volsib.logincompose.data

import com.volsib.logincompose.models.WeatherResponse
import retrofit2.Response

interface WeatherRepository {
    suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Response<WeatherResponse>

    suspend fun getWeatherByCity(city: String): Response<WeatherResponse>
}
