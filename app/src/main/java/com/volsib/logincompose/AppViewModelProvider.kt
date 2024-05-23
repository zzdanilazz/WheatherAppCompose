package com.volsib.logincompose

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.volsib.logincompose.ui.greetings.GreetingsViewModel
import com.volsib.logincompose.ui.weather.WeatherViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            GreetingsViewModel(weatherApplication().container.userRepository)
        }

        initializer {
            WeatherViewModel(
                weatherApplication().container.weatherRepository,
                weatherApplication().container.userRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [WeatherApplication].
 */
fun CreationExtras.weatherApplication(): WeatherApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as WeatherApplication)