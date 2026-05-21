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
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtListUiState
import com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState
import com.example.digitalapi_nosova_3.ui.viewmodel.ErrorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtListScreen(
    state: ArtListUiState,
    searchQuery: String,
    showOnlyFavorites: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (Artwork) -> Unit,
    onArtworkClick: (Int) -> Unit,
    onNavigateToCollections: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Art Gallery") },
                actions = {
                    IconButton(onClick = onToggleFavoritesFilter) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (showOnlyFavorites) "Показать все" else "Только избранное",
                            tint = if (showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Collections") },
                            onClick = { showMenu = false; onNavigateToCollections() },
                            leadingIcon = { Icon(Icons.Default.CollectionsBookmark, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = { showMenu = false; onNavigateToHistory() },
                            leadingIcon = { Icon(Icons.Default.History, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { showMenu = false; onNavigateToSettings() },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (state is ArtListUiState.Success && state.isOffline) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudOff, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Offline mode", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Поиск произведений...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                },
                singleLine = true
            )

            if (showOnlyFavorites) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Показаны только избранные",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            when (state) {
                is ArtListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ArtListUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = when (state.type) {
                                ErrorType.NETWORK -> "Ошибка сети"
                                ErrorType.UNKNOWN -> "Неизвестная ошибка"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Повторить")
                        }
                    }
                }
                is ArtListUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (showOnlyFavorites) "Нет избранных" else "Ничего не найдено",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                is ArtListUiState.Success -> {
                    LazyColumn {
                        items(
                            items = state.artworks,
                            key = { it.id }
                        ) { art ->
                            ListItem(
                                headlineContent = { Text(art.title ?: "Без названия") },
                                supportingContent = { Text(art.artistTitle ?: "Неизвестный автор") },
                                leadingContent = {
                                    AsyncImage(
                                        model = "${state.iiifUrl}/${art.imageId}/full/200,/0/default.jpg",
                                        contentDescription = "Изображение произведения",
                                        modifier = Modifier.size(60.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { onToggleFavorite(art) }) {
                                        Icon(
                                            imageVector = if (art.id in state.favoriteIds)
                                                Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (art.id in state.favoriteIds)
                                                "Удалить из избранного" else "Добавить в избранное",
                                            tint = if (art.id in state.favoriteIds)
                                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { onArtworkClick(art.id) }
                            )
                            HorizontalDivider()
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
    state: DetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    onSaveNote: (String) -> Unit = {},
    onDeleteNote: () -> Unit = {}
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is DetailUiState.Success) {
            noteText = state.note?.text ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state is DetailUiState.Success) {
                        IconButton(onClick = { showNoteDialog = true }) {
                            Icon(
                                if (state.note != null) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = "Note"
                            )
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (state.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                                tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (state) {
                is DetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DetailUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = when (state.type) {
                                ErrorType.NETWORK -> "Ошибка сети"
                                ErrorType.UNKNOWN -> "Неизвестная ошибка"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Повторить")
                        }
                    }
                }
                is DetailUiState.Success -> {
                    LazyColumn(Modifier.padding(16.dp)) {
                        item {
                            AsyncImage(
                                model = "${state.iiifUrl}/${state.artwork.imageId}/full/843,/0/default.jpg",
                                contentDescription = "Детальное изображение",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                state.artwork.title ?: "Без названия",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                state.artwork.artistTitle ?: "Неизвестный автор",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.artwork.dateDisplay != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    state.artwork.dateDisplay,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (state.artwork.mediumDisplay != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    state.artwork.mediumDisplay,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (state.artwork.description != null) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Описание",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    state.artwork.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (state.note != null) {
                                Spacer(Modifier.height(16.dp))
                                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Моя заметка", style = MaterialTheme.typography.titleSmall)
                                        Spacer(Modifier.height(8.dp))
                                        Text(state.note.text)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text(if (noteText.isBlank()) "Добавить заметку" else "Редактировать заметку") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Заметка") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onSaveNote(noteText)
                        }
                        showNoteDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                Row {
                    if (state is DetailUiState.Success && state.note != null) {
                        TextButton(
                            onClick = {
                                onDeleteNote()
                                noteText = ""
                                showNoteDialog = false
                            }
                        ) {
                            Text("Удалить")
                        }
                    }
                    TextButton(onClick = { showNoteDialog = false }) {
                        Text("Отмена")
                    }
                }
            }
        )
    }
}
