package com.example.digitalapi_nosova_3.ui.viewmodel


import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.model.Artwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

sealed interface ArtUiState {
    object Loading : ArtUiState
    data class Success(val artworks: List<Artwork>, val iiifUrl: String) : ArtUiState
    data class Error(val message: String) : ArtUiState
    object Empty : ArtUiState
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val artwork: Artwork, val iiifUrl: String, val isFavorite: Boolean) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class ArtViewModel @Inject constructor(
    private val repository: ArtRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var uiState: ArtUiState by mutableStateOf(ArtUiState.Loading)
        private set

    var detailUiState: DetailUiState by mutableStateOf(DetailUiState.Loading)
        private set

    val favorites = repository.getFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadArtworks(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = ArtUiState.Loading
            try {
                val (data, url) = repository.getArtworks(forceRefresh)
                uiState = if (data.isEmpty()) ArtUiState.Empty else ArtUiState.Success(data, url)
            } catch (e: Exception) {
                uiState = ArtUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun search(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            loadArtworks()
            return
        }
        viewModelScope.launch {
            uiState = ArtUiState.Loading
            try {
                val (data, url) = repository.searchArtworks(query)
                uiState = if (data.isEmpty()) ArtUiState.Empty else ArtUiState.Success(data, url)
            } catch (e: Exception) {
                uiState = ArtUiState.Error(e.message ?: "Search Error")
            }
        }
    }

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            detailUiState = DetailUiState.Loading
            try {
                val (art, url) = repository.getArtworkDetails(id)
                val isFav = repository.isFavorite(id)
                detailUiState = DetailUiState.Success(art, url, isFav)
            } catch (e: Exception) {
                detailUiState = DetailUiState.Error(e.message ?: "Detail Error")
            }
        }
    }

    fun toggleFavorite(art: Artwork) {
        viewModelScope.launch {
            repository.toggleFavorite(art)
            val current = detailUiState
            if (current is DetailUiState.Success && current.artwork.id == art.id) {
                detailUiState = current.copy(isFavorite = !current.isFavorite)
            }
        }
    }
}