package com.habitik.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habitik.ui.screens.builder.RoutineBuilderScreen
import com.habitik.ui.screens.home.HomeScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToBuilder = { navController.navigate("builder") }
            )
        }
        composable("builder") {
            RoutineBuilderScreen()
        }
    }
}
