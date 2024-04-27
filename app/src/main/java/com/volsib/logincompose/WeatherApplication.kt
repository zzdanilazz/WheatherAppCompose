package com.volsib.logincompose

import android.app.Application
import com.volsib.logincompose.data.AppContainer
import com.volsib.logincompose.data.AppDataContainer

class WeatherApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}