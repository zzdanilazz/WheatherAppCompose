package com.volsib.logincompose.data

import android.content.Context

interface AppContainer {
    val userRepository: UserRepository
    val weatherRepository: WeatherRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val userRepository: UserRepository by lazy {
        OfflineUsersRepository(ApplicationDatabase.getDatabase(context).userDao())
    }
    override val weatherRepository: WeatherRepository by lazy {
        OnlineWeatherRepository()
    }
}