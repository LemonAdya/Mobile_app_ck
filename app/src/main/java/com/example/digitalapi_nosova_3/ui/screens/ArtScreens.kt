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
fun ArtListScreen(viewModel: ArtViewModel, onArtworkClick: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Art App") },
                actions = {
                    IconButton(onClick = { viewModel.loadArtworks(true) }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { p ->
        Column(Modifier.padding(p)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; viewModel.searchArtworks(it) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                placeholder = { Text("Search...") }
            )
            when (val s = viewModel.uiState) {
                is ArtUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ArtUiState.Error -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("Error: ${s.message}")
                    Button(onClick = { viewModel.loadArtworks() }) { Text("Retry") }
                }
                is ArtUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No results") }
                is ArtUiState.Success -> LazyColumn {
                    items(s.artworks) { art ->
                        ListItem(
                            headlineContent = { Text(art.title ?: "") },
                            supportingContent = { Text(art.artistTitle ?: "") },
                            leadingContent = {
                                AsyncImage(
                                    model = "${s.iiifUrl}/${art.imageId}/full/200,/0/default.jpg",
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Crop
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.toggleFavorite(art.id) }) {
                                    Icon(
                                        if (viewModel.favorites.contains(art.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtDetailScreen(id: Int, viewModel: ArtViewModel, onBack: () -> Unit) {
    LaunchedEffect(id) { viewModel.loadArtworkDetails(id) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Details") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            })
        }
    ) { p ->
        Box(Modifier.padding(p)) {
            when (val s = viewModel.detailUiState) {
                is DetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is DetailUiState.Error -> Text("Error: ${s.message}")
                is DetailUiState.Success -> Column(Modifier.padding(16.dp)) {
                    AsyncImage(
                        model = "${s.iiifUrl}/${s.artwork.imageId}/full/843,/0/default.jpg",
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(s.artwork.title ?: "", style = MaterialTheme.typography.headlineMedium)
                    Text(s.artwork.artistTitle ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(s.artwork.description ?: "No description")
                }
            }
        }
    }
}

