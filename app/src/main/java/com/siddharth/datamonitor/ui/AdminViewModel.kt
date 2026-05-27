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

data class PublicReleaseInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val _uploadStatus = MutableStateFlow("Ready")
    val uploadStatus: StateFlow<String> = _uploadStatus.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _publicRelease = MutableStateFlow<PublicReleaseInfo?>(null)
    val publicRelease: StateFlow<PublicReleaseInfo?> = _publicRelease.asStateFlow()

    // Dual-Repo Public Target Configuration details
    val githubOwner = MutableStateFlow("sidd-harth830")
    val githubRepo = MutableStateFlow("Data-Monitor-Releases")

    private val db = FirebaseFirestore.getInstance()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun fetchLatestPublicRelease(
        onSuccess: (PublicReleaseInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        val owner = githubOwner.value.trim()
        val repo = githubRepo.value.trim()

        if (owner.isEmpty() || repo.isEmpty()) {
            onError("Error: Target owner and public repository name are required.")
            return
        }

        _isUploading.value = true
        _uploadStatus.value = "Connecting to public repository gateway..."

        viewModelScope.launch {
            try {
                val info = withContext(Dispatchers.IO) {
                    val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
                    
                    // Unauthenticated request ensures 100% security for client/admin
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
                        throw IOException("Null tag name returned from GitHub release info.")
                    }

                    // Clean the leading version prefix if existing
                    val versionName = if (tagName.startsWith("v")) tagName.substring(1) else tagName
                    val releaseNotes = json.optString("body", "Production release verification compiled via Dual-Repo runner.")

                    val assets = json.optJSONArray("assets")
                    if (assets == null || assets.length() == 0) {
                        throw IOException("The compiled public release does not specify any assets.")
                    }
                    val mainAsset = assets.getJSONObject(0)
                    val downloadUrl = mainAsset.getString("browser_download_url")

                    PublicReleaseInfo(
                        versionName = versionName,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl
                    )
                }

                _publicRelease.value = info
                _uploadStatus.value = "Ready"
                _isUploading.value = false
                onSuccess(info)
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Failed to contact GitHub gateway.")
            }
        }
    }

    fun approveAndBroadcastPublicRelease(
        versionCode: Int,
        isMandatory: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val release = _publicRelease.value
        if (release == null) {
            onError("Error: Please fetch public staging parameters first.")
            return
        }

        _isUploading.value = true
        _uploadStatus.value = "Broadcasting production update parameters to clients..."

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val metaUpdate = mapOf(
                        "versionCode" to versionCode,
                        "versionName" to release.versionName,
                        "releaseNotes" to release.releaseNotes,
                        "downloadUrl" to release.downloadUrl,
                        "isMandatory" to isMandatory,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )

                    val task = db.collection("app_config").document("latest_update").set(metaUpdate)
                    
                    // Direct synchronous block inside thread worker
                    var count = 0
                    while (!task.isComplete && count < 80) {
                        Thread.sleep(100)
                        count++
                    }

                    if (!task.isComplete) {
                        throw IOException("Broadcast write timed out after 8 seconds.")
                    }
                    if (!task.isSuccessful) {
                        val errorMsg = task.exception?.localizedMessage ?: "Unknown firestore write error"
                        throw IOException("Broadcast transaction failed on Firestore: $errorMsg")
                    }
                }

                _uploadStatus.value = "Completed!"
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Connection dropped before live promotion commit.")
            }
        }
    }
}
