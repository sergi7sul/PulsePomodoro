package com.cherrytime.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            workDurationMin = prefs[Keys.WORK_DURATION] ?: 25,
            shortBreakMin = prefs[Keys.SHORT_BREAK] ?: 5,
            longBreakMin = prefs[Keys.LONG_BREAK] ?: 15,
            longBreakInterval = prefs[Keys.LONG_BREAK_INTERVAL] ?: 4,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
        )
    }

    suspend fun setWorkDuration(minutes: Int) =
        dataStore.edit { it[Keys.WORK_DURATION] = minutes.coerceIn(1, 60) }

    suspend fun setShortBreak(minutes: Int) =
        dataStore.edit { it[Keys.SHORT_BREAK] = minutes.coerceIn(1, 30) }

    suspend fun setLongBreak(minutes: Int) =
        dataStore.edit { it[Keys.LONG_BREAK] = minutes.coerceIn(1, 30) }

    suspend fun setLongBreakInterval(sessions: Int) =
        dataStore.edit { it[Keys.LONG_BREAK_INTERVAL] = sessions.coerceIn(2, 8) }

    suspend fun setSoundEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }

    suspend fun setVibrationEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }

    private object Keys {
        val WORK_DURATION = intPreferencesKey("work_duration_min")
        val SHORT_BREAK = intPreferencesKey("short_break_min")
        val LONG_BREAK = intPreferencesKey("long_break_min")
        val LONG_BREAK_INTERVAL = intPreferencesKey("long_break_interval")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }
}
