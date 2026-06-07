package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import com.example.digitalapi_nosova_3.data.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    val theme: StateFlow<String> = preferencesRepository.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val autoSync: StateFlow<Boolean> = preferencesRepository.autoSyncFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val cacheTtlDays: StateFlow<Int> = preferencesRepository.cacheTtlDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    fun setTheme(theme: String) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
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

    fun setCacheTtlDays(days: Int) {
        viewModelScope.launch { preferencesRepository.setCacheTtlDays(days) }
    }
}
