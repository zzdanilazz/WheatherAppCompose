package com.volsib.logincompose.ui.widget

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.volsib.logincompose.R
import com.volsib.logincompose.WeatherApplication
import com.volsib.logincompose.data.User
import com.volsib.logincompose.data.WeatherRepository
import com.volsib.logincompose.ui.MainActivity
import com.volsib.logincompose.ui.weather.WeatherUiState
import com.volsib.logincompose.ui.weather.toWeatherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class WeatherWidget: GlanceAppWidget() {
    private var weatherUiState by mutableStateOf(WeatherUiState())

    private var iconBitmap by mutableStateOf<Bitmap?>(null)

    private var isNotSignedIn by mutableStateOf(false)

    private var isNotRightsProvided by mutableStateOf(false)

    @SuppressLint("MissingPermission", "CoroutineCreationDuringComposition")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Обнова виджета каждый час
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weatherWidgetWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequest.Builder(
                WeatherWidgetWorker::class.java,
                60.minutes.toJavaDuration()
            ).build()
        )

        provideContent {
            val userRepository = (context.applicationContext as WeatherApplication).container.userRepository
            // Засчет by при обнове меняется переменная, если просто юзнуть =, то не заработает
            val currentUser by userRepository.getCurrentUserStream().collectAsState(initial = User(0,"",""))
            val weatherRepository = (context.applicationContext as WeatherApplication).container.weatherRepository

            val coroutineScope = rememberCoroutineScope()
            val callback : () -> Unit = {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val lat = location.latitude
                            val lon = location.longitude
                            coroutineScope.launch {
                                getWeatherByCoordinates(weatherRepository, lat, lon)
                                iconBitmap = loadImageAsBitmap(weatherUiState.weatherDetails.iconUrl)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("WeatherWidget", "Error getting location: ${e.message}")
                    }
            }

            isNotSignedIn = currentUser == null

            isNotRightsProvided = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED

            GlanceTheme {
                // Не авторизован
                if (isNotSignedIn) {
                    DefaultContent()
                } else {
                    // Не даны права
                    if (isNotRightsProvided) {
                        NoRightsContent()
                    } else {
                        WeatherContent(callback)
                    }
                }
            }
        }
    }

    private suspend fun getWeatherByCoordinates(
        weatherRepository: WeatherRepository,
        lat: Double,
        lon: Double
    ) {
        val response = weatherRepository.getWeatherByCoordinates(lat, lon)
        if (response.isSuccessful) {
            response.body()?.let { weatherResponse ->
                weatherUiState = weatherResponse.toWeatherUiState()
            }
        }
    }

    private suspend fun loadImageAsBitmap(url: String): Bitmap? {
        return try {
            withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    private fun DefaultContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<MainActivity>())
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Please, authorize")
        }
    }

    @Composable
    private fun NoRightsContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<MainActivity>())
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Please, provide the geo rights")
        }
    }

    @Composable
    private fun WeatherContent(callback: () -> Unit) {
        LaunchedEffect(key1 = Unit) {
            callback()
        }
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<MainActivity>())
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconBitmap == null) {
                Row(modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "Updated at ${weatherUiState.updatedAt}")
                    Image(
                        modifier = GlanceModifier.size(40.dp).clickable(callback),
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Refresh icon"
                    )
                }
                Text(text = weatherUiState.weatherDetails.city, modifier = GlanceModifier.padding(bottom = 12.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "${weatherUiState.weatherDetails.temperature}°C",
                        style = TextStyle(fontSize = 32.sp)
                    )
                    Spacer(modifier = GlanceModifier.width(5.dp))
                    Image(
                        modifier = GlanceModifier.size(40.dp),
                        provider = if (iconBitmap != null) {
                            ImageProvider(iconBitmap!!)
                        } else {
                            ImageProvider(R.drawable.ic_launcher_foreground)
                        },
                        contentDescription = "Weather icon")
                    Column(
                        modifier = GlanceModifier.padding(horizontal = 5.dp)
                    ) {
                        Text(text = weatherUiState.weatherDetails.description)
                        Text(text = "Feels like ${weatherUiState.weatherDetails.feelsLike}°C")
                    }
                }
            }
        }
    }
}
