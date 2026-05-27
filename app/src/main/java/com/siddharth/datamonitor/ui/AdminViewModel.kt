package com.siddharth.datamonitor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class FetchedReleaseInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val _uploadStatus = MutableStateFlow("Ready")
    val uploadStatus: StateFlow<String> = _uploadStatus.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _fetchedRelease = MutableStateFlow<FetchedReleaseInfo?>(null)
    val fetchedRelease: StateFlow<FetchedReleaseInfo?> = _fetchedRelease.asStateFlow()

    // Configurable repo details (authenticated is not needed for GET /releases/latest)
    var githubOwner = MutableStateFlow("sidd-harth830")
    var githubRepo = MutableStateFlow("Data-Monitor")

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun fetchLatestGitHubRelease(
        onSuccess: (FetchedReleaseInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        val owner = githubOwner.value.trim()
        val repo = githubRepo.value.trim()

        if (owner.isEmpty() || repo.isEmpty()) {
            onError("Error: GitHub Owner and Repository are required.")
            return
        }

        _isUploading.value = true
        _uploadStatus.value = "Fetching latest release from GitHub..."

        viewModelScope.launch {
            try {
                val info = withContext(Dispatchers.IO) {
                    val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("Accept", "application/vnd.github+json")
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val bodyStr = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}: $bodyStr")
                    }

                    val json = JSONObject(bodyStr)
                    val tagName = json.optString("tag_name", "")
                    if (tagName.isEmpty()) {
                        throw IOException("Latest release tag_name is empty or not found.")
                    }

                    // Strip leading 'v' if present for clean display
                    val versionName = if (tagName.startsWith("v")) tagName.substring(1) else tagName
                    val releaseNotes = json.optString("body", "No release notes specified.")

                    val assets = json.optJSONArray("assets")
                    if (assets == null || assets.length() == 0) {
                        throw IOException("The latest release contains no binaries/assets.")
                    }
                    val firstAsset = assets.getJSONObject(0)
                    val downloadUrl = firstAsset.getString("browser_download_url")

                    FetchedReleaseInfo(
                        versionName = versionName,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl
                    )
                }

                _fetchedRelease.value = info
                _uploadStatus.value = "Ready"
                _isUploading.value = false
                onSuccess(info)
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Failed to query the GitHub Releases gateway.")
            }
        }
    }

    fun broadcastUpdateToUsers(
        versionCode: Int,
        isMandatory: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fetched = _fetchedRelease.value
        if (fetched == null) {
            onError("Error: Please fetch latest GitHub release details first.")
            return
        }

        _isUploading.value = true
        _uploadStatus.value = "Broadcasting update to users..."

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val metaUpdate = mapOf(
                        "versionCode" to versionCode,
                        "versionName" to fetched.versionName,
                        "releaseNotes" to fetched.releaseNotes,
                        "downloadUrl" to fetched.downloadUrl,
                        "isMandatory" to isMandatory,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )

                    val task = db.collection("app_config").document("latest_update").set(metaUpdate)
                    
                    // safely await task completion
                    var count = 0
                    while (!task.isComplete && count < 80) {
                        Thread.sleep(100)
                        count++
                    }

                    if (!task.isComplete) {
                        throw IOException("Firestore sync timed out.")
                    }
                    if (!task.isSuccessful) {
                        val errorMsg = task.exception?.localizedMessage ?: "Unknown cloud write exception"
                        throw IOException("Firestore synchronized push failed: $errorMsg")
                    }
                }

                _uploadStatus.value = "Completed!"
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Transmission protocol failure.")
            }
        }
    }
}
