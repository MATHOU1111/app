package com.example.mathis

import FavoritesList
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomePage(navController)
        }
        composable("movieDetail/{movieId}") { backStackEntry ->
            backStackEntry.arguments?.getString("movieId")?.let { movieId ->
                MovieDetailPage(movieId = movieId, navController)
            }
        }
        composable("favorites") {
            FavoritesList(navController)
        }
    }
}
