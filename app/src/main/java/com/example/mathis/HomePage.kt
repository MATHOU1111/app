package com.example.mathis

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    val searchText = remember { mutableStateOf("") }
    val movies = remember { mutableStateOf(emptyList<Movie>()) }
    val isLoading = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Structure de la page principale
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barre de recherche et bouton de favoris
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Rechercher des films...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    // Recherche de films lorsque l'utilisateur appuie sur "Entrée"
                    coroutineScope.launch {
                        isLoading.value = true
                        try {
                            movies.value = searchMovies(searchText.value)
                        } finally {
                            isLoading.value = false
                        }
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )
            IconButton(
                onClick = { navController.navigate("favorites") },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favoris",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        // Gestion de l'affichage selon l'état de chargement et les résultats
        if (isLoading.value) {
            // Affichage d'un indicateur de chargement
            CircularProgressIndicator()
        } else if (movies.value.isEmpty()) {
            // Image affichée en l'absence de résultats
            Image(
                painter = painterResource(id = R.drawable.opm),
                contentDescription = "No Results",
                modifier = Modifier.width(400.dp).height(1200.dp)
            )
        } else {
            // Affichage des films trouvés dans une liste déroulante
            LazyColumn {
                items(movies.value) { movie ->
                    MovieRow(movie, navController)
                }
            }
        }
    }
}

// Fonction suspendue pour rechercher des films à partir d'une API
suspend fun searchMovies(query: String): List<Movie> {
    val apikey = ""
    // Utilisation d'un appel asynchrone pour éviter de bloquer le thread principal
    return withContext(Dispatchers.IO) {
        val json = Json { ignoreUnknownKeys = true }
        val client = OkHttpClient()
        val url = "https://api.themoviedb.org/3/search/movie?api_key=$apikey&query=${query.replace(" ", "+")}"
        // Exécution de la requête
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Échec de la requête : ${response.message}")
            val body = response.body?.string()
            if (body != null) {
                json.decodeFromString<MovieResponse>(body).results
            } else {
                emptyList()
            }
        }
    }
}