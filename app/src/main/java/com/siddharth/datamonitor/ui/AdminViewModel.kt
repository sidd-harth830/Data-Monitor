package com.siddharth.datamonitor.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val _uploadStatus = MutableStateFlow("Ready")
    val uploadStatus: StateFlow<String> = _uploadStatus.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _selectedApkUri = MutableStateFlow<Uri?>(null)
    val selectedApkUri: StateFlow<Uri?> = _selectedApkUri.asStateFlow()

    private val _selectedApkFileName = MutableStateFlow<String?>(null)
    val selectedApkFileName: StateFlow<String?> = _selectedApkFileName.asStateFlow()

    // Configurable GitHub details (with default placeholders that users can override in UI)
    var githubToken = MutableStateFlow("ghp_XXXXXXXXXXXXXXplaceholder")
    var githubOwner = MutableStateFlow("leocarnivas")
    var githubRepo = MutableStateFlow("datamonitor")

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun selectApk(uri: Uri?, fileName: String?) {
        _selectedApkUri.value = uri
        _selectedApkFileName.value = fileName
    }

    fun uploadApkToGithub(
        versionCode: Int,
        versionName: String,
        releaseNotes: String,
        isMandatory: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uri = _selectedApkUri.value
        if (uri == null) {
            onError("Error: Please select an APK file first.")
            return
        }

        val token = githubToken.value.trim()
        val owner = githubOwner.value.trim()
        val repo = githubRepo.value.trim()

        if (token.isEmpty() || token.startsWith("ghp_XXX")) {
            onError("Error: Please provide a valid GitHub Personal Access Token (PAT).")
            return
        }
        if (owner.isEmpty() || repo.isEmpty()) {
            onError("Error: GitHub Owner and Repository are required.")
            return
        }

        _isUploading.value = true
        _uploadStatus.value = "Creating GitHub Release..."

        viewModelScope.launch {
            try {
                // Perform Network Operations on IO dispatcher
                val downloadUrl = withContext(Dispatchers.IO) {
                    // Step 1: Create a Release
                    val createReleaseUrl = "https://api.github.com/repos/$owner/$repo/releases"
                    val releaseJson = JSONObject().apply {
                        put("tag_name", "v$versionName")
                        put("name", "v$versionName")
                        put("body", releaseNotes)
                        put("draft", false)
                        put("prerelease", false)
                    }

                    val releaseRequest = Request.Builder()
                        .url(createReleaseUrl)
                        .post(releaseJson.toString().toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Accept", "application/vnd.github+json")
                        .addHeader("X-GitHub-Api-Version", "2022-11-28")
                        .build()

                    val releaseResponse = httpClient.newCall(releaseRequest).execute()
                    val releaseResponseBodyStr = releaseResponse.body?.string() ?: ""
                    
                    if (!releaseResponse.isSuccessful) {
                        throw IOException("Failed to create release: HTTP ${releaseResponse.code}. Details: $releaseResponseBodyStr")
                    }

                    val releaseJsonObj = JSONObject(releaseResponseBodyStr)
                    val rawUploadUrl = releaseJsonObj.getString("upload_url")
                    
                    // The upload URL from GitHub looks like: https://uploads.github.com/.../releases/123/assets{?name,label}
                    // We must strip the optional suffix template to upload correctly
                    val cleanUploadUrl = if (rawUploadUrl.contains("{")) {
                        rawUploadUrl.substring(0, rawUploadUrl.indexOf("{"))
                    } else {
                        rawUploadUrl
                    }

                    _uploadStatus.value = "Uploading APK bytes (this may take a few moments)..."

                    // Read binary bytes of APK from selected content URI
                    val contentResolver = getApplication<Application>().contentResolver
                    val inputStream = contentResolver.openInputStream(uri) 
                        ?: throw IOException("Unable to open ContentResolver stream for the selected APK file.")
                    
                    val apkBytes = inputStream.use { it.readBytes() }
                    if (apkBytes.isEmpty()) {
                        throw IOException("Selected APK file is empty.")
                    }

                    val uploadUrlWithParams = "$cleanUploadUrl?name=DataMonitor_v${versionName}.apk"
                    val uploadRequest = Request.Builder()
                        .url(uploadUrlWithParams)
                        .post(apkBytes.toRequestBody("application/vnd.android.package-archive".toMediaType()))
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Accept", "application/vnd.github+json")
                        .addHeader("X-GitHub-Api-Version", "2022-11-28")
                        .addHeader("Content-Type", "application/vnd.android.package-archive")
                        .build()

                    val uploadResponse = httpClient.newCall(uploadRequest).execute()
                    val uploadResponseBodyStr = uploadResponse.body?.string() ?: ""

                    if (!uploadResponse.isSuccessful) {
                        throw IOException("Failed to upload APK asset: HTTP ${uploadResponse.code}. Details: $uploadResponseBodyStr")
                    }

                    val uploadJsonObj = JSONObject(uploadResponseBodyStr)
                    val browserDownloadUrl = uploadJsonObj.getString("browser_download_url")

                    _uploadStatus.value = "Syncing release parameters to cloud node..."

                    // Step 4: Write metadata variables to the central Firestore doc `app_config/latest_update`
                    val db = FirebaseFirestore.getInstance()
                    val metaUpdate = mapOf(
                        "versionCode" to versionCode,
                        "versionName" to versionName,
                        "releaseNotes" to releaseNotes,
                        "downloadUrl" to browserDownloadUrl,
                        "isMandatory" to isMandatory,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )

                    // Write to Firestore synchronously inside the coroutine (using await task behavior helper)
                    val task = db.collection("app_config").document("latest_update").set(metaUpdate)
                    var firestoreSuccess = false
                    var firestoreError: Exception? = null

                    task.addOnCompleteListener { action ->
                        firestoreSuccess = action.isSuccessful
                        firestoreError = action.exception
                    }

                    // Spin-wait safely for a brief moment in IO thread for completion of Task 
                    val maxWaitMs = 8000
                    val startTime = System.currentTimeMillis()
                    while (!task.isComplete && (System.currentTimeMillis() - startTime) < maxWaitMs) {
                        Thread.sleep(100)
                    }

                    if (!task.isComplete) {
                        throw IOException("Firestore synchronization write timed out after launch.")
                    }
                    if (!task.isSuccessful) {
                        throw IOException("Firestore write failed: " + (firestoreError?.localizedMessage ?: "Unknown Error"))
                    }

                    browserDownloadUrl
                }

                _uploadStatus.value = "Completed!"
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "An unexpected transport protocol error occurred.")
            }
        }
    }
}
