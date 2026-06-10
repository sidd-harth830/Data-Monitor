package com.siddharth.datamonitor.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddharth.datamonitor.BuildConfig
import com.siddharth.datamonitor.data.DataUsageRecord
import com.siddharth.datamonitor.utils.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiOptimizerViewModel : ViewModel() {

    private val _insights = MutableStateFlow<String?>(null)
    val insights: StateFlow<String?> = _insights.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchInsights(weekRecords: List<DataUsageRecord>, todayMobile: Long, todayWifi: Long) {
        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "dummy_key") {
                    _error.value = "Please configure your Gemini API Key in the settings."
                    _isLoading.value = false
                    return@launch
                }

                // Prepare data summary
                var summary = "Here is my data usage for the past week:\\n"
                weekRecords.forEach { record ->
                    summary += "Date: \${record.dateStr}, Cellular: \${formatBytes(record.mobileBytes)}, Wi-Fi: \${formatBytes(record.wifiBytes)}\\n"
                }
                summary += "\\nToday's Usage:\\nCellular: \${formatBytes(todayMobile)}, Wi-Fi: \${formatBytes(todayWifi)}\\n"
                
                val prompt = "You are an AI Data Setup Optimizer assistant. Based on the following usage:\\n\$summary\\n" +
                        "1. Summarize my usage pattern in 1 sentence.\\n" +
                        "2. Provide 2 concise, strict, and actionable recommendations to save cellular data and optimize app usage. "

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)), role = "user")
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val aiText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (aiText != null) {
                    _insights.value = aiText
                } else {
                    _error.value = "Could not generate insights at this time."
                }
            } catch (e: Exception) {
                _error.value = "Error: \${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
