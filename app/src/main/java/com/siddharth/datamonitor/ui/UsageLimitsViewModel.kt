package com.siddharth.datamonitor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siddharth.datamonitor.data.AppDatabase
import com.siddharth.datamonitor.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsageLimitsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).userPreferencesDao()

    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getPreferences().collect { prefs ->
                if (prefs != null) {
                    _preferences.value = prefs
                } else {
                    dao.insertPreferences(UserPreferences())
                }
            }
        }
    }

    fun updateDailyLimit(limitKb: Long) {
        viewModelScope.launch {
            val updated = _preferences.value
            dao.insertPreferences(updated)
            _preferences.value = updated
        }
    }

    fun updateMonthlyLimit(limitKb: Long) {
        viewModelScope.launch {
            val updated = _preferences.value
            dao.insertPreferences(updated)
            _preferences.value = updated
        }
    }

    fun updateAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _preferences.value
            dao.insertPreferences(updated)
            _preferences.value = updated
        }
    }
}
