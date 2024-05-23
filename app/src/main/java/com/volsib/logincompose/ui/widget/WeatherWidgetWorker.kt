package com.volsib.logincompose.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WeatherWidgetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        WeatherWidget().apply {
//            applicationContext.weatherWidgetStore.loadWeather()
            // Call update/updateAll in case a Worker for the widget is not currently running.
            updateAll(applicationContext)
        }
        return Result.success()
    }
}