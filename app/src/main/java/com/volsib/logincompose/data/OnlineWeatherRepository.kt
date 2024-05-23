package com.volsib.logincompose.data

import com.volsib.logincompose.api.RetrofitInstance
import com.volsib.logincompose.models.WeatherResponse
import retrofit2.Response

class OnlineWeatherRepository: WeatherRepository {
    override suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Response<WeatherResponse> {
        return RetrofitInstance.api.getWeatherByCoordinates(lat,lon)
    }

    override suspend fun getWeatherByCity(city: String): Response<WeatherResponse> {
        return RetrofitInstance.api.getWeatherByCity(city)
    }
}
