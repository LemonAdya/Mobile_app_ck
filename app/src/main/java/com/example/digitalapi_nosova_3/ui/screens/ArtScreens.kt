package com.example.digitalapi_nosova_3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtListScreen(
    state: ArtUiState,
    searchQuery: String,
    favorites: Set<Int>,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onArtworkClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Art App") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                placeholder = { Text("Search...") },
                singleLine = true
            )

            when (state) {
                is ArtUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ArtUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error: ${state.message}")
                        Button(onClick = onRefresh) { Text("Retry") }
                    }
                }
                is ArtUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results")
                    }
                }
                is ArtUiState.Success -> {
                    LazyColumn {
                        items(
                            items = state.artworks,
                            key = { it.id }
                        ) { art ->
                            ListItem(
                                headlineContent = { Text(art.title ?: "Untitled") },
                                supportingContent = { Text(art.artistTitle ?: "Unknown artist") },
                                leadingContent = {
                                    AsyncImage(
                                        model = "${state.iiifUrl}/${art.imageId}/full/200,/0/default.jpg",
                                        contentDescription = "Artwork image",
                                        modifier = Modifier.size(60.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { onToggleFavorite(art.id) }) {
                                        Icon(
                                            imageVector = if (favorites.contains(art.id))
                                                Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (favorites.contains(art.id))
                                                "Remove from favorites" else "Add to favorites"
                                        )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtDetailScreen(
    state: com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (state) {
                is com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error: ${state.message}")
                    }
                }
                is com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState.Success -> {
                    Column(Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = "${state.iiifUrl}/${state.artwork.imageId}/full/843,/0/default.jpg",
                            contentDescription = "Artwork detail image",
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            state.artwork.title ?: "Untitled",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            state.artwork.artistTitle ?: "Unknown artist",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(state.artwork.description ?: "No description")
                    }
                }
            }
        }
    }
}
