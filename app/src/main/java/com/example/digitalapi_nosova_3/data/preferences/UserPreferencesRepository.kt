package com.example.digitalapi_nosova_3.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val ITEMS_PER_PAGE_KEY = intPreferencesKey("items_per_page")
        val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync")
        val CACHE_TTL_DAYS_KEY = intPreferencesKey("cache_ttl_days")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    val itemsPerPageFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ITEMS_PER_PAGE_KEY] ?: 20
    }

    val autoSyncFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SYNC_KEY] ?: false
    }

    val cacheTtlDaysFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CACHE_TTL_DAYS_KEY] ?: 7
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setItemsPerPage(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[ITEMS_PER_PAGE_KEY] = count
        }
    }

    suspend fun setAutoSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SYNC_KEY] = enabled
        }
    }

    suspend fun setCacheTtlDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[CACHE_TTL_DAYS_KEY] = days
        }
    }
}
