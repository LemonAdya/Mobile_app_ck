package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import kotlinx.coroutines.launch

sealed interface ArtUiState {
    object Loading : ArtUiState
    data class Success(val artworks: List<Artwork>, val iiifUrl: String) : ArtUiState
    data class Error(val message: String) : ArtUiState
    object Empty : ArtUiState
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val artwork: Artwork, val iiifUrl: String) : DetailUiState // Оставляем так
    data class Error(val message: String) : DetailUiState
}


class ArtViewModel(private val repository: ArtRepository) : ViewModel() {

    var uiState: ArtUiState by mutableStateOf(ArtUiState.Loading)
    var detailUiState: DetailUiState by mutableStateOf(DetailUiState.Loading)

    var favorites by mutableStateOf(setOf<Int>())

    init { loadArtworks() }

    fun loadArtworks(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = ArtUiState.Loading
            try {
                val (data, url) = repository.getArtworks(forceRefresh)
                uiState = if (data.isEmpty()) ArtUiState.Empty else ArtUiState.Success(data, url)
            } catch (e: Exception) { uiState = ArtUiState.Error(e.message ?: "Error") }
        }
    }

    fun searchArtworks(query: String) {
        if (query.isBlank()) { loadArtworks(); return }
        viewModelScope.launch {
            uiState = ArtUiState.Loading
            try {
                val (data, url) = repository.searchArtworks(query)
                uiState = if (data.isEmpty()) ArtUiState.Empty else ArtUiState.Success(data, url)
            } catch (e: Exception) { uiState = ArtUiState.Error(e.message ?: "Error") }
        }
    }

    fun loadArtworkDetails(id: Int) {
        viewModelScope.launch {
            detailUiState = DetailUiState.Loading
            try {
                val (artwork, url) = repository.getArtworkDetails(id)
                if (artwork != null) {
                    detailUiState = DetailUiState.Success(artwork, url)
                } else {
                    detailUiState = DetailUiState.Error("Artwork not found")
                }
            } catch (e: Exception) {
                detailUiState = DetailUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun toggleFavorite(id: Int) {
        favorites = if (favorites.contains(id)) favorites - id else favorites + id
    }
}
