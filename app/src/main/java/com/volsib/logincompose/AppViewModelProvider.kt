package com.volsib.logincompose

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.volsib.logincompose.ui.greetings.GreetingsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for GreetingsViewModel
        initializer {
            GreetingsViewModel(weatherApplication().container.userRepository)
        }

//        // Initializer for HomeViewModel
//        initializer {
//            HomeViewModel()
//        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [WeatherApplication].
 */
fun CreationExtras.weatherApplication(): WeatherApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as WeatherApplication)