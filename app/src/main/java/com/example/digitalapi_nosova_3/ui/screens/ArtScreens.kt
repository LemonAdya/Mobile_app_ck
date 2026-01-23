package com.example.digitalapi_nosova_3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.digitalapi_nosova_3.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtListScreen(
    state: ArtUiState,
    favorites: Set<Int>,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onArtworkClick: (Int) -> Unit  
) {
    var query by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Art App") },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Refresh") }
                }
            )
        }
    ) { p ->
        Column(Modifier.padding(p)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; onSearch(it) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                placeholder = { Text("Search...") }
            )
            when (state) {
                is ArtUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is ArtUiState.Error -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("Error: ${state.message}"); Button(onClick = onRefresh) { Text("Retry") }
                }
                is ArtUiState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No results") }
                is ArtUiState.Success -> LazyColumn {
                    items(state.artworks) { art ->
                        ListItem(
                            headlineContent = { Text(art.title ?: "") },
                            supportingContent = { Text(art.artistTitle ?: "") },
                            leadingContent = {
                                AsyncImage(
                                    model = "${state.iiifUrl}/${art.imageId}/full/200,/0/default.jpg",
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Crop
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { onToggleFavorite(art.id) }) {
                                    Icon(if (favorites.contains(art.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                                }
                            },
                            modifier = Modifier.clickable { onArtworkClick(art.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtDetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Details") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            })
        }
    ) { p ->
        Box(Modifier.padding(p)) {
            when (state) {
                is DetailUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is DetailUiState.Error -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("Error: ${state.message}"); Button(onClick = onRetry) { Text("Retry") }
                }
                is DetailUiState.Success -> Column(Modifier.padding(16.dp)) {
                    AsyncImage(
                        model = "${state.iiifUrl}/${state.artwork.imageId}/full/843,/0/default.jpg",
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(state.artwork.title ?: "", style = MaterialTheme.typography.headlineMedium)
                    Text(state.artwork.artistTitle ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(state.artwork.description ?: "No description")
                }
            }
        }
    }
}

