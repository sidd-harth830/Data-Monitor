package com.example.ui.theme

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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeManager(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val FONT_KEY = stringPreferencesKey("font")
        val DATA_LIMIT_KEY = stringPreferencesKey("data_limit_mb")
        val ALERTS_ENABLED_KEY = booleanPreferencesKey("alerts_enabled")
        val TRACK_SEPARATED_KEY = booleanPreferencesKey("track_separated")
        val BILLING_CYCLE_DAY_KEY = intPreferencesKey("billing_cycle_day")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            AppTheme.CYBERPUNK_NEON.name -> AppTheme.CYBERPUNK_NEON
            AppTheme.MINIMALIST_NORD.name -> AppTheme.MINIMALIST_NORD
            AppTheme.LIGHT_GLASS.name -> AppTheme.LIGHT_GLASS
            else -> AppTheme.OLED_PITCH_BLACK
        }
    }

    val fontFlow: Flow<AppFont> = context.dataStore.data.map { preferences ->
        when (preferences[FONT_KEY]) {
            AppFont.INTER.name -> AppFont.INTER
            AppFont.JETBRAINS_MONO.name -> AppFont.JETBRAINS_MONO
            else -> AppFont.ACORN
        }
    }

    val dataLimitFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DATA_LIMIT_KEY] ?: "2000"
    }

    val alertsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ALERTS_ENABLED_KEY] ?: true
    }

    val trackSeparatedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TRACK_SEPARATED_KEY] ?: true
    }

    val billingCycleDayFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[BILLING_CYCLE_DAY_KEY] ?: 1
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setFont(font: AppFont) {
        context.dataStore.edit { preferences ->
            preferences[FONT_KEY] = font.name
        }
    }

    suspend fun setDataLimit(limit: String) {
        context.dataStore.edit { preferences ->
            preferences[DATA_LIMIT_KEY] = limit
        }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ALERTS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setTrackSeparated(separated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TRACK_SEPARATED_KEY] = separated
        }
    }

    suspend fun setBillingCycleDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[BILLING_CYCLE_DAY_KEY] = day
        }
    }
}

enum class AppTheme {
    OLED_PITCH_BLACK,
    CYBERPUNK_NEON,
    MINIMALIST_NORD,
    LIGHT_GLASS
}

enum class AppFont {
    ACORN,
    INTER,
    JETBRAINS_MONO
}
