package com.example.aunthenticator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class SettingsDataStore(private val context: Context) {
    private val SWITCH_KEY = booleanPreferencesKey("switch_key")
    private val SHOULD_SHOW_BIOMETRIC_KEY = booleanPreferencesKey("should_show_biometric")

    //Flaga wartości switcha
    val switchState: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SWITCH_KEY] ?: false
        }
    //funkajc ustawiająca flagę switch
    suspend fun saveSwitchState(isChecked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SWITCH_KEY] = isChecked
        }
    }

    //flaga wartości biometri do autoryzacji logowania na stronie
    val shouldShowBiometricFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOULD_SHOW_BIOMETRIC_KEY] ?: false
        }
    //funkcja ustawiająca flagę biometri
    suspend fun setShouldShowBiometric(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOULD_SHOW_BIOMETRIC_KEY] = show
        }
    }
}