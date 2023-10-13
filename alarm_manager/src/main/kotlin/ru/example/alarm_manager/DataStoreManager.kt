package ru.example.alarm_manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("data_store")

class DataStoreManager(private val context: Context) {
    suspend fun saveFlag(flag: Boolean) {
        context.dataStore.edit { preferences ->
            if (flag) preferences.remove(FLAG)
            else preferences[FLAG] = false
        }
    }

    fun getFlag() = context.dataStore.data.map { preferences ->
        preferences[FLAG] ?: true
    }

    companion object {
        val FLAG = booleanPreferencesKey("flag")
    }
}
