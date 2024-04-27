package com.volsib.logincompose.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.volsib.logincompose.ui.greetings.GreetingsDestination
import com.volsib.logincompose.ui.greetings.GreetingsScreen
import com.volsib.logincompose.ui.weather.WeatherScreen
import com.volsib.logincompose.ui.weather.WeatherDestination

@Composable
fun WeatherNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController, startDestination = GreetingsDestination.route, modifier = modifier
    ) {
        composable(route = GreetingsDestination.route) {
            GreetingsScreen(
                navigateToWeather = { navController.navigate(WeatherDestination.route) }
            )
        }
        composable(route = WeatherDestination.route) {
            WeatherScreen()
        }
    }
}