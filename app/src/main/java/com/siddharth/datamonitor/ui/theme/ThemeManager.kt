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
        val FONT_KEY = stringPreferencesKey("font")
        val DATA_LIMIT_KEY = stringPreferencesKey("data_limit_mb")
        val ALERTS_ENABLED_KEY = booleanPreferencesKey("alerts_enabled")
        val TRACK_SEPARATED_KEY = booleanPreferencesKey("track_separated")
        val BILLING_CYCLE_DAY_KEY = intPreferencesKey("billing_cycle_day")
        val DASHBOARD_LAYOUT_KEY = stringPreferencesKey("dashboard_layout")
        val APP_ICON_KEY = stringPreferencesKey("app_icon")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            AppTheme.CYBER_NEON.name -> AppTheme.CYBER_NEON
            AppTheme.MINIMAL_LIGHT.name -> AppTheme.MINIMAL_LIGHT
            AppTheme.PREMIUM_GLASS.name -> AppTheme.PREMIUM_GLASS
            AppTheme.MIDNIGHT_AMOLED.name -> AppTheme.MIDNIGHT_AMOLED
            AppTheme.SOLARIZED_LIGHT.name -> AppTheme.SOLARIZED_LIGHT
            else -> AppTheme.OLED_DARK
        }
    }

    val fontFlow: Flow<AppFont> = context.dataStore.data.map { preferences ->
        when (preferences[FONT_KEY]) {
            AppFont.CLEAN_SANS.name -> AppFont.CLEAN_SANS
            AppFont.TECH_MODE.name -> AppFont.TECH_MODE
            else -> AppFont.PREMIUM_SERIF
        }
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
}

enum class AppTheme {
    OLED_DARK,
    CYBER_NEON,
    MINIMAL_LIGHT,
    PREMIUM_GLASS,
    MIDNIGHT_AMOLED,
    SOLARIZED_LIGHT
}

enum class AppFont {
    PREMIUM_SERIF,
    CLEAN_SANS,
    TECH_MODE
}

enum class DashboardLayoutPreference {
    STANDARD,
    PRO,
    GRID
}

