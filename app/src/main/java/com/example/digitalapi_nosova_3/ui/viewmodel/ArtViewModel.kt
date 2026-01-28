package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface ArtUiState {
    object Loading : ArtUiState
    data class Success(val artworks: List<Artwork>, val iiifUrl: String) : ArtUiState
    data class Error(val message: String) : ArtUiState
    object Empty : ArtUiState
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val artwork: Artwork, val iiifUrl: String) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class ArtViewModel(private val repository: ArtRepository) : ViewModel() {
    var uiState: ArtUiState by mutableStateOf(ArtUiState.Loading)
        private set

    var detailUiState: DetailUiState by mutableStateOf(DetailUiState.Loading)
        private set

    var favorites by mutableStateOf(setOf<Int>())
        private set

    var searchQuery by mutableStateOf("")
        private set

    private var searchJob: Job? = null

    init {
        loadArtworks()
    }

    fun loadArtworks(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = ArtUiState.Loading
            uiState = try {
                val (data, url) = repository.getArtworks(forceRefresh)
                if (data.isEmpty()) ArtUiState.Empty else ArtUiState.Success(data, url)
            } catch (e: Exception) {
                ArtUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadArtworkDetails(id: Int) {
        if (id <= 0 || (detailUiState is DetailUiState.Loading && lastLoadedId == id)) {
            return
        }

        lastLoadedId = id

        viewModelScope.launch {
            detailUiState = DetailUiState.Loading
            detailUiState = try {
                val (artwork, url) = repository.getArtworkDetails(id)
                DetailUiState.Success(artwork, url)
            } catch (e: Exception) {
                DetailUiState.Error(e.message ?: "Failed to load details")
            }
        }
    }

    private var lastLoadedId: Int = -1  // Добавь в класс ViewModel

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            loadArtworks()
        } else {
            searchArtworks(query)
        }
    }

    private fun searchArtworks(query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            uiState = ArtUiState.Loading
            uiState = try {
                val (data, url) = repository.searchArtworks(query)
                if (data.isEmpty()) ArtUiState.Empty else ArtUiState.Success(data, url)
            } catch (e: Exception) {
                ArtUiState.Error(e.message ?: "Search error")
            }
        }
    }


    fun toggleFavorite(id: Int) {
        favorites = if (favorites.contains(id)) {
            favorites - id
        } else {
            favorites + id
        }
    }
}