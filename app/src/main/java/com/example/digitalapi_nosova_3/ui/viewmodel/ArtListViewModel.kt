package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.local.ArtEntity
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
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
        val favoriteIds: Set<Int>
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
    private val repository: ArtRepository
) : ViewModel() {

    // Источник 1: Поисковый запрос (UI)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Источник 2: Фильтр "только избранное" (UI)
    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    // Источник 3: Избранное из Room (Data Layer)
    private val favoritesFlow: Flow<List<ArtEntity>> = repository.getFavoritesFlow()

    // Триггер для ручного обновления
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        _refreshTrigger.tryEmit(Unit) // Начальная загрузка
    }

    // Реактивная композиция потоков
    val uiState: StateFlow<ArtListUiState> = combine(
        // Поток 1: Поиск с debounce и distinctUntilChanged
        _searchQuery
            .debounce(500) // Задержка 500мс после ввода
            .distinctUntilChanged() // Избегаем дублирующихся запросов
            .flatMapLatest { query ->
                // Отменяем предыдущий поиск при новом запросе
                flow {
                    emit(query)
                }
            },
        // Поток 2: Фильтр "только избранное"
        _showOnlyFavorites,
        // Поток 3: Избранное из Room
        favoritesFlow,
        // Поток 4: Триггер обновления
        _refreshTrigger
    ) { query, onlyFavorites, favorites, _ ->
        // Объединяем все источники
        Triple(query, onlyFavorites, favorites)
    }
        .flatMapLatest { (query, onlyFavorites, favorites) ->
            flow {
                emit(ArtListUiState.Loading)
                
                try {
                    // Загружаем данные
                    val (artworks, iiifUrl) = if (query.isBlank()) {
                        repository.getArtworks()
                    } else {
                        repository.searchArtworks(query)
                    }

                    // Применяем фильтр "только избранное"
                    val favoriteIds = favorites.map { it.id }.toSet()
                    val filteredArtworks = if (onlyFavorites) {
                        artworks.filter { it.id in favoriteIds }
                    } else {
                        artworks
                    }

                    // Определяем состояние
                    if (filteredArtworks.isEmpty()) {
                        emit(ArtListUiState.Empty)
                    } else {
                        emit(
                            ArtListUiState.Success(
                                artworks = filteredArtworks,
                                iiifUrl = iiifUrl,
                                favoriteIds = favoriteIds
                            )
                        )
                    }
                } catch (e: Exception) {
                    val errorType = when {
                        e.message?.contains("network", ignoreCase = true) == true -> ErrorType.NETWORK
                        e.message?.contains("timeout", ignoreCase = true) == true -> ErrorType.NETWORK
                        else -> ErrorType.UNKNOWN
                    }
                    emit(ArtListUiState.Error(e.message ?: "Unknown Error", errorType))
                }
            }
        }
        .catch { e ->
            emit(ArtListUiState.Error(e.message ?: "Unknown Error", ErrorType.UNKNOWN))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ArtListUiState.Loading
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    fun toggleFavorite(artwork: Artwork) {
        viewModelScope.launch {
            repository.toggleFavorite(artwork)
            // Room автоматически обновит favoritesFlow, что вызовет пересчет uiState
        }
    }
}
