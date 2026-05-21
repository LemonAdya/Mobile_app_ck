package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.local.NoteEntity
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(
        val artwork: Artwork,
        val iiifUrl: String,
        val isFavorite: Boolean,
        val note: NoteEntity? = null
    ) : DetailUiState
    data class Error(val message: String, val type: ErrorType) : DetailUiState
}

@HiltViewModel
class ArtDetailViewModel @Inject constructor(
    private val repository: ArtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artworkId: Int = savedStateHandle.get<Int>("id") ?: 0

    private val _detailData = MutableStateFlow<DetailData?>(null)

    private data class DetailData(
        val artwork: Artwork,
        val iiifUrl: String,
        val note: NoteEntity?
    )

    init {
        loadDetail()
    }

    val uiState: StateFlow<DetailUiState> = combine(
        _detailData,
        repository.getFavoritesFlow()
    ) { detailData, favorites ->
        if (detailData == null) {
            DetailUiState.Loading
        } else {
            val isFav = favorites.any { it.id == artworkId }
            DetailUiState.Success(detailData.artwork, detailData.iiifUrl, isFav, detailData.note)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

    private fun loadDetail() {
        viewModelScope.launch {
            _detailData.value = null
            try {
                val (artwork, iiifUrl) = repository.getArtworkDetails(artworkId)
                val note = repository.getNoteByArtworkId(artworkId)
                _detailData.value = DetailData(artwork, iiifUrl, note)
                repository.addToHistory(artwork)
            } catch (e: Exception) {
                val errorType = when {
                    e.message?.contains("network", ignoreCase = true) == true -> ErrorType.NETWORK
                    e.message?.contains("timeout", ignoreCase = true) == true -> ErrorType.NETWORK
                    else -> ErrorType.UNKNOWN
                }
                _detailData.value = null
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is DetailUiState.Success) {
                repository.toggleFavorite(currentState.artwork)
            }
        }
    }

    fun saveNote(text: String) {
        viewModelScope.launch {
            repository.saveNote(artworkId, text)
            val currentState = uiState.value
            if (currentState is DetailUiState.Success) {
                val note = repository.getNoteByArtworkId(artworkId)
                _detailData.value = currentState.run { DetailData(artwork, iiifUrl, note) }
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            repository.deleteNote(artworkId)
            val currentState = uiState.value
            if (currentState is DetailUiState.Success) {
                _detailData.value = currentState.run { DetailData(artwork, iiifUrl, null) }
            }
        }
    }

    fun addToCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.addArtworkToCollection(collectionId, artworkId)
        }
    }

    fun removeFromCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.removeArtworkFromCollection(collectionId, artworkId)
        }
    }

    fun retry() {
        loadDetail()
    }
}
