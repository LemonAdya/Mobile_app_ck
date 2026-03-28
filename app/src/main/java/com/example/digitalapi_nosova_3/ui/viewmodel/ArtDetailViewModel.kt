package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        val isFavorite: Boolean
    ) : DetailUiState
    data class Error(val message: String, val type: ErrorType) : DetailUiState
}

@HiltViewModel
class ArtDetailViewModel @Inject constructor(
    private val repository: ArtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artworkId: Int = savedStateHandle.get<Int>("id") ?: 0

    // Триггер для загрузки деталей
    private val _loadTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        _loadTrigger.tryEmit(Unit)
    }

    // Реактивная композиция: детали артворка + статус избранного из Room
    val uiState: StateFlow<DetailUiState> = combine(
        _loadTrigger,
        repository.getFavoritesFlow() // Автоматически обновляется при изменении избранного
    ) { _, favorites ->
        favorites.any { it.id == artworkId }
    }
        .flatMapLatest { isFavorite ->
            flow {
                emit(DetailUiState.Loading)
                
                try {
                    val (artwork, iiifUrl) = repository.getArtworkDetails(artworkId)
                    emit(DetailUiState.Success(artwork, iiifUrl, isFavorite))
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
                // Room автоматически обновит getFavoritesFlow(), что вызовет пересчет uiState
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _loadTrigger.emit(Unit)
        }
    }
}
