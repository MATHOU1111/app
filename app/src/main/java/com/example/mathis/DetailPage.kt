package com.example.mathis

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

// Configuration JSON pour la désérialisation
private val json1 = Json { ignoreUnknownKeys = true }

// Fonction pour récupérer les détails d'un film depuis l'API
suspend fun fetchMovieDetails(movieId: String): MovieDetail? {
    val apikey = ""
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.themoviedb.org/3/movie/$movieId?api_key=$apikey")
        .build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    json1.decodeFromString(MovieDetail.serializer(), responseBody)
                }
            } else null
        }
    }
}
// Fonction pour marquer un film comme favori

suspend fun markAsFavorite(movieId: Int): Boolean {
    val requestBody = json1.encodeToString(FavoriteBody.serializer(), FavoriteBody("movie", movieId, true))
    val request = Request.Builder()
        .url("https://api.themoviedb.org/3/account/{accountId}/favorite?api_key=d4e2d76de72776487370ac5a45dc8d35")
        .post(requestBody.toRequestBody("application/json".toMediaType()))
        .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJkNGUyZDc2ZGU3Mjc3NjQ4NzM3MGFjNWE0NWRjOGQzNSIsInN1YiI6IjY2NDA3OWE4YzdjOWE4OTU2N2ExYWJmZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.PACmYQvQnmJT9QRMpv_hn3u-BExNuHlpDeTB47CtBDs")
        .build()


    // Utilisation de la coroutine pour effectuer la requête de manière asynchrone
    return withContext(Dispatchers.IO) {
        OkHttpClient().newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }
}

// Fonction Composable pour afficher les détails d'un film
@Composable
fun MovieDetailPage(movieId: String, navController: NavController) {
    var movie by remember { mutableStateOf<MovieDetail?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Effet lancé lors du chargement de la page pour récupérer les détails du film
    LaunchedEffect(movieId) {
        isLoading = true
        movie = fetchMovieDetails(movieId)
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        movie?.let { movieDetail ->
            MovieDetailView(movieDetail, navController)
        }
    }
}


// Fonction Composable pour afficher les détails d'un film
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailView(movie: MovieDetail, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Affichage des détails du film dans une colonne
    Column {
        // Barre d'application avec bouton de retour
        TopAppBar(
            title = { Text("Details") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
        )
        // Liste déroulante pour afficher les détails du film
        LazyColumn(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Affichage de l'image du film s'il est disponible
                if (movie.poster_path != null) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                        contentDescription = "Movie Poster",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                } else {
                    // Affichage d'un texte si aucune image n'est disponible
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image Available", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Affichage des autres détails du film
                Text(text = movie.title, style = MaterialTheme.typography.headlineMedium)
                Text(text = "Date de sortie: ${movie.release_date}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Rating: ${movie.vote_average} (${movie.vote_count} votes)", style = MaterialTheme.typography.bodyLarge)
                Text(text = movie.overview, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                // Bouton pour ajouter le film aux favoris
                Button(onClick = {
                    coroutineScope.launch {
                        val result = markAsFavorite(movie.id)
                        if (result) {
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Ajouter aux favoris")
                }
            }
        }
    }
}