package com.siddharth.datamonitor.ui.theme

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
        val APP_ACCENT_COLOR_KEY = stringPreferencesKey("app_accent")
        val DATA_LIMIT_KEY = stringPreferencesKey("data_limit_mb")
        val ALERTS_ENABLED_KEY = booleanPreferencesKey("alerts_enabled")
        val TRACK_SEPARATED_KEY = booleanPreferencesKey("track_separated")
        val BILLING_CYCLE_DAY_KEY = intPreferencesKey("billing_cycle_day")
        val DASHBOARD_LAYOUT_KEY = stringPreferencesKey("dashboard_layout")
        val APP_ICON_KEY = stringPreferencesKey("app_icon")
        val DATA_SAVER_ACTIVE_KEY = booleanPreferencesKey("data_saver_active")
        val SKIP_LOGIN_KEY = booleanPreferencesKey("skip_login")
    }

    val themeFlow: Flow<AppTheme?> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            AppTheme.SPRING.name -> AppTheme.SPRING
            AppTheme.DESERT.name -> AppTheme.DESERT
            AppTheme.FOREST.name -> AppTheme.FOREST
            AppTheme.MIDNIGHT_AMOLED.name -> AppTheme.MIDNIGHT_AMOLED
            AppTheme.SOLARIZED_LIGHT.name -> AppTheme.SOLARIZED_LIGHT
            AppTheme.OCEAN_DEEP.name -> AppTheme.OCEAN_DEEP
            AppTheme.SUNSET_BLAZE.name -> AppTheme.SUNSET_BLAZE
            AppTheme.CYBERPUNK.name -> AppTheme.CYBERPUNK
            AppTheme.LAVENDER_HAZE.name -> AppTheme.LAVENDER_HAZE
            AppTheme.MATRIX.name -> AppTheme.MATRIX
            else -> null
        }
    }

    val appAccentFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_ACCENT_COLOR_KEY] ?: "#19B1DC"
    }

    val dashboardLayoutFlow: Flow<DashboardLayoutPreference> = context.dataStore.data.map { preferences ->
        when (preferences[DASHBOARD_LAYOUT_KEY]) {
            DashboardLayoutPreference.PRO.name -> DashboardLayoutPreference.PRO
            DashboardLayoutPreference.GRID.name -> DashboardLayoutPreference.GRID
            else -> DashboardLayoutPreference.STANDARD
        }
    }

    val appIconFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_ICON_KEY] ?: "DEFAULT"
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

    val dataSaverActiveFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DATA_SAVER_ACTIVE_KEY] ?: false
    }

    val skipLoginFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SKIP_LOGIN_KEY] ?: false
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setAppAccent(accentHex: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_ACCENT_COLOR_KEY] = accentHex
        }
    }

    suspend fun setDashboardLayout(layout: DashboardLayoutPreference) {
        context.dataStore.edit { preferences ->
            preferences[DASHBOARD_LAYOUT_KEY] = layout.name
        }
    }

    suspend fun setAppIcon(iconName: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_ICON_KEY] = iconName
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

    suspend fun setDataSaverActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DATA_SAVER_ACTIVE_KEY] = active
        }
    }

    suspend fun setSkipLogin(skip: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SKIP_LOGIN_KEY] = skip
        }
    }
}

enum class AppTheme {
    SPRING,
    DESERT,
    FOREST,
    MIDNIGHT_AMOLED,
    SOLARIZED_LIGHT,
    OCEAN_DEEP,
    SUNSET_BLAZE,
    CYBERPUNK,
    LAVENDER_HAZE,
    MATRIX
}

enum class DashboardLayoutPreference {
    STANDARD,
    PRO,
    GRID
}

