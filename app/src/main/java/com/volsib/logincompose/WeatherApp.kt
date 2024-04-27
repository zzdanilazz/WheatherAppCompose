package com.volsib.logincompose

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.volsib.logincompose.ui.navigation.WeatherNavHost

@Composable
fun WeatherApp (navController: NavHostController = rememberNavController()) {
    WeatherNavHost(navController = navController)
}