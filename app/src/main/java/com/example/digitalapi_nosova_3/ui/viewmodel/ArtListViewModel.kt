package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ArtListUiState {
    object Loading : ArtListUiState
    data class Success(
        val artworks: List<Artwork>,
        val iiifUrl: String,
        val favoriteIds: Set<Int>,
        val isOffline: Boolean = false
    ) : ArtListUiState
    data class Error(val message: String, val type: ErrorType) : ArtListUiState
    object Empty : ArtListUiState
}

enum class ErrorType {
    NETWORK,
    UNKNOWN
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ArtListViewModel @Inject constructor(
    private val repository: ArtRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    private val _listData = MutableStateFlow<ListData?>(null)

    private data class ListData(
        val artworks: List<Artwork>,
        val iiifUrl: String,
        val isOffline: Boolean
    )

    val autoSync = preferencesRepository.autoSyncFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val theme = preferencesRepository.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val cacheTtlDays = preferencesRepository.cacheTtlDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    init {
        loadArtworks()
        
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest {
                    loadArtworks()
                }
        }
    }

    val uiState: StateFlow<ArtListUiState> = combine(
        _listData,
        repository.getFavoritesFlow(),
        repository.getFavoriteArtworks(),
        _showOnlyFavorites
    ) { listData, favorites, favoriteArtworks, onlyFavorites ->
        val favoriteIds = favorites.map { it.id }.toSet()
        
        when {
            onlyFavorites -> {
                if (favoriteArtworks.isEmpty()) {
                    ArtListUiState.Empty
                } else {
                    ArtListUiState.Success(
                        artworks = favoriteArtworks,
                        iiifUrl = listData?.iiifUrl ?: "https://www.artic.edu/iiif/2",
                        favoriteIds = favoriteIds,
                        isOffline = false
                    )
                }
            }
            listData == null -> ArtListUiState.Loading
            else -> {
                if (listData.artworks.isEmpty()) {
                    ArtListUiState.Empty
                } else {
                    ArtListUiState.Success(
                        artworks = listData.artworks,
                        iiifUrl = listData.iiifUrl,
                        favoriteIds = favoriteIds,
                        isOffline = listData.isOffline
                    )
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ArtListUiState.Loading
        )

    private fun loadArtworks(forceRefresh: Boolean = false) {
        val query = _searchQuery.value
        viewModelScope.launch {
            _listData.value = null
            var isOffline = false
            val ttlDays = cacheTtlDays.value
            val (artworks, iiifUrl) = try {
                if (query.isBlank()) {
                    repository.getArtworks(forceRefresh, ttlDays)
                } else {
                    repository.searchArtworks(query)
                }
            } catch (e: Exception) {
                isOffline = true
                val cached = repository.getAllCachedArtworks().first()
                val filtered = if (query.isBlank()) {
                    cached
                } else {
                    cached.filter {
                        it.title.contains(query, ignoreCase = true) ||
                            it.artistTitle?.contains(query, ignoreCase = true) == true
                    }
                }
                Pair(filtered.map { it.toArtwork() }, "https://www.artic.edu/iiif/2")
            }
            _listData.value = ListData(artworks, iiifUrl, isOffline)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun refresh() {
        loadArtworks(forceRefresh = true)
    }

    fun toggleFavorite(artwork: Artwork) {
        viewModelScope.launch {
            repository.toggleFavorite(artwork)
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSync(enabled)
            if (enabled) {
                syncScheduler.scheduleSync()
            } else {
                syncScheduler.cancelSync()
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun setCacheTtlDays(days: Int) {
        viewModelScope.launch { preferencesRepository.setCacheTtlDays(days) }
    }

    val collections = repository.getAllCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCollection(name: String, description: String?) {
        viewModelScope.launch { repository.createCollection(name, description) }
    }

    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch { repository.deleteCollection(collection) }
    }

    fun addToCollection(collectionId: Long, artwork: Artwork) {
        viewModelScope.launch { repository.addArtworkToCollection(collectionId, artwork) }
    }

    fun removeFromCollection(collectionId: Long, artworkId: Int) {
        viewModelScope.launch { repository.removeArtworkFromCollection(collectionId, artworkId) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun getArtworksInCollectionFlow(collectionId: Long): Flow<List<Artwork>> =
        repository.getArtworksInCollection(collectionId)
}

private fun com.example.digitalapi_nosova_3.data.local.CachedArtworkEntity.toArtwork(): com.example.digitalapi_nosova_3.data.model.Artwork {
    return com.example.digitalapi_nosova_3.data.model.Artwork(
        id = id,
        title = title,
        artistTitle = artistTitle,
        imageId = imageId,
        description = description,
        dateDisplay = dateDisplay,
        mediumDisplay = mediumDisplay
    )
}
