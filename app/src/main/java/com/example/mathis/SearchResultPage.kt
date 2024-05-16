package com.example.mathis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.navigation.NavController




// Composable pour afficher une ligne de film
@Composable
fun MovieRow(movie: Movie, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("movieDetail/${movie.id}") }
            .padding(bottom = 12.dp)
    ) {
        // Affichage de l'image du film s'il est disponible
        if (movie.poster_path != null) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = "Movie Poster",
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .padding(end = 8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .padding(end = 8.dp)
                    .background(Color.Gray),
            ) {
                Text("Image non disponible", color = Color.White)
            }
        }
        // Affichage des informations du film dans une colonne
        Column {
            Text(movie.title, style = MaterialTheme.typography.titleMedium)
            Text(
                // Affichage d'un résumé du film (maximum 100 caractères)
                "Résumé: ${movie.overview.take(100)}${if (movie.overview.length > 100) "..." else ""}",
                style = MaterialTheme.typography.bodySmall
            )
            Text("Année: ${movie.release_date}", style = MaterialTheme.typography.bodySmall)
            Text("Rating: ${movie.vote_average}/10", style = MaterialTheme.typography.bodySmall)
        }
    }
}