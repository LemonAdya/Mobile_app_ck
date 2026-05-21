package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.local.NoteEntity
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(
        val artwork: Artwork,
        val iiifUrl: String,
        val isFavorite: Boolean,
        val note: NoteEntity? = null,
        val collectionIds: List<Long> = emptyList()
    ) : DetailUiState
    data class Error(val message: String, val type: ErrorType) : DetailUiState
}

@HiltViewModel
class ArtDetailViewModel @Inject constructor(
    private val repository: ArtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artworkId: Int = savedStateHandle.get<Int>("id") ?: 0

    private val _loadTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        _loadTrigger.tryEmit(Unit)
    }

    val uiState: StateFlow<DetailUiState> = combine(
        _loadTrigger,
        repository.getFavoritesFlow()
    ) { _, favorites ->
        favorites.any { it.id == artworkId }
    }
        .flatMapLatest { isFavorite ->
            flow {
                emit(DetailUiState.Loading)
                
                try {
                    val (artwork, iiifUrl) = repository.getArtworkDetails(artworkId)
                    val note = repository.getNoteByArtworkId(artworkId)
                    emit(DetailUiState.Success(artwork, iiifUrl, isFavorite, note))
                    repository.addToHistory(artwork)
                } catch (e: Exception) {
                    val errorType = when {
                        e.message?.contains("network", ignoreCase = true) == true -> ErrorType.NETWORK
                        e.message?.contains("timeout", ignoreCase = true) == true -> ErrorType.NETWORK
                        else -> ErrorType.UNKNOWN
                    }
                    emit(DetailUiState.Error(e.message ?: "Unknown Error", errorType))
                }
            }
        }
        .catch { e ->
            emit(DetailUiState.Error(e.message ?: "Unknown Error", ErrorType.UNKNOWN))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

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
                emitDetailStateWithNote(currentState, note)
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            repository.deleteNote(artworkId)
            val currentState = uiState.value
            if (currentState is DetailUiState.Success) {
                emitDetailStateWithNote(currentState, null)
            }
        }
    }

    private fun emitDetailStateWithNote(current: DetailUiState.Success, note: NoteEntity?) {
        // Note is updated via loadTrigger re-emit
        viewModelScope.launch {
            _loadTrigger.emit(Unit)
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
        viewModelScope.launch {
            _loadTrigger.emit(Unit)
        }
    }
}
