import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.mathis.Movie
import com.example.mathis.MovieDetail
import com.example.mathis.MovieRow
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Fonction pour convertir les détails d'un film en un objet Movie
fun convertMovieDetailToMovie(movieDetail: MovieDetail): Movie {
    return Movie(
        id = movieDetail.id,
        title = movieDetail.title,
        overview = movieDetail.overview,
        poster_path = movieDetail.poster_path,
        release_date = movieDetail.release_date,
        vote_average = movieDetail.vote_average,
        original_title = movieDetail.original_title,
        vote_count = movieDetail.vote_count
    )
}

// Modèle de réponse pour la liste de films favoris
@kotlinx.serialization.Serializable
data class MovieResponse(val results: List<MovieDetail>)

private val json = Json { ignoreUnknownKeys = true }

// Fonction pour récupérer les films favoris avec une gestion améliorée des erreurs
suspend fun fetchFavoriteMovies(): List<MovieDetail> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.themoviedb.org/3/account/21265061/favorite/movies?api_key=d4e2d76de72776487370ac5a45dc8d35")
        .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJkNGUyZDc2ZGU3Mjc3NjQ4NzM3MGFjNWE0NWRjOGQzNSIsInN1YiI6IjY2NDA3OWE4YzdjOWE4OTU2N2ExYWJmZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.PACmYQvQnmJT9QRMpv_hn3u-BExNuHlpDeTB47CtBDs")
        .build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    json.decodeFromString<MovieResponse>(responseBody).results
                } ?: run {
                    // Si le corps de la réponse est nul, afficher un message d'erreur
                    Log.e("FetchFavoriteMovies", "Response body was null")
                    emptyList()
                }
            } else {
                // En cas d'échec de la requête, afficher un message d'erreur avec le code et le message de réponse
                Log.e("FetchFavoriteMovies", "Failed to fetch movies: ${response.code}, ${response.message}")
                emptyList()
            }
        }
    }
}

// Composable pour afficher la liste des films favoris
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesList(navController: NavController) {
    // États pour la liste de films, l'indicateur de chargement et les messages d'erreur
    val movies = remember { mutableStateOf<List<MovieDetail>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf("") }

    // Effet lancé lors de la création du composant pour récupérer les films favoris
    LaunchedEffect(key1 = Unit) {
        try {
            movies.value = fetchFavoriteMovies()
        } catch (e: Exception) {
            errorMessage.value = e.localizedMessage ?: "Unknown error occurred"
        }
        isLoading.value = false
    }

    // Affichage de la liste de films favoris dans une colonne
    Column {
        // Barre d'application avec bouton de retour
        TopAppBar(
            title = { Text("Films favoris", color = androidx.compose.ui.graphics.Color.White) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF6751A5)
            )
        )

        // Affichage conditionnel selon l'état de chargement et les messages d'erreur
        if (isLoading.value) {
            CircularProgressIndicator()
        } else if (errorMessage.value.isNotEmpty()) {
            Text("Error: ${errorMessage.value}")
        } else {
            // Affichage de la liste des films favoris dans une LazyColumn
            LazyColumn {
                items(movies.value) { movieDetail ->
                    MovieRow(convertMovieDetailToMovie(movieDetail), navController)
                }
            }
        }
    }
}
