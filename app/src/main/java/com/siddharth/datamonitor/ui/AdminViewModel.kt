package com.siddharth.datamonitor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

data class StagingUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val runId: String,
    val status: String,
    val downloadUrl: String
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val _uploadStatus = MutableStateFlow("Ready")
    val uploadStatus: StateFlow<String> = _uploadStatus.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _stagingUpdate = MutableStateFlow<StagingUpdateInfo?>(null)
    val stagingUpdate: StateFlow<StagingUpdateInfo?> = _stagingUpdate.asStateFlow()

    // Dual-Repo Public Target Configuration details - used to build public release links
    val githubOwner = MutableStateFlow("sid-yadav7307")
    val githubRepo = MutableStateFlow("Data-Monitor-Releases")

    private val db = FirebaseFirestore.getInstance()

    init {
        // Real-time Firestore synchronizer listener on the staging node
        db.collection("app_config")
            .document("staging_update")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val code = snapshot.getLong("versionCode")?.toInt() ?: 50
                    val name = snapshot.getString("versionName") ?: "3.5.1"
                    val runId = snapshot.getString("runId") ?: ""
                    val status = snapshot.getString("status") ?: "PENDING_REVIEW"
                    
                    // Deduce the download url based on target public releases
                    val owner = githubOwner.value.trim()
                    val repo = githubRepo.value.trim()
                    val derivedUrl = "https://github.com/$owner/$repo/releases/download/v$name/app-release-live.apk"

                    _stagingUpdate.value = StagingUpdateInfo(
                        versionCode = code,
                        versionName = name,
                        runId = runId,
                        status = status,
                        downloadUrl = derivedUrl
                    )
                } else {
                    _stagingUpdate.value = null
                }
            }
    }

    fun approveAndRolloutLive(
        isMandatory: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val staging = _stagingUpdate.value
        if (staging == null) {
            onError("Error: No pending staging build found to approve.")
            return
        }

        _isUploading.value = true
        _uploadStatus.value = "Promoting build live and releasing parameters..."

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Update the live production document
                    val prodUpdate = mapOf(
                        "versionCode" to staging.versionCode,
                        "versionName" to staging.versionName,
                        "sourceRunId" to staging.runId,
                        "downloadUrl" to staging.downloadUrl,
                        "isMandatory" to isMandatory,
                        "releaseNotes" to "Official production rollout compiled via Dual-Repo runner (Source Build ID: ${staging.runId}).",
                        "timestamp" to Timestamp.now()
                    )

                    // Write to live node
                    val writeTask = db.collection("app_config").document("latest_update").set(prodUpdate)
                    var count = 0
                    while (!writeTask.isComplete && count < 80) {
                        Thread.sleep(100)
                        count++
                    }
                    if (!writeTask.isSuccessful) {
                        throw IOException("Firestore production write failed.")
                    }

                    // Delete the staging review document
                    val deleteTask = db.collection("app_config").document("staging_update").delete()
                    count = 0
                    while (!deleteTask.isComplete && count < 80) {
                        Thread.sleep(100)
                        count++
                    }
                    if (!deleteTask.isSuccessful) {
                        throw IOException("Firestore staging deletion failed.")
                    }
                }

                _uploadStatus.value = "Ready"
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Failed to promote the staging build.")
            }
        }
    }

    fun broadcastManualUpdate(
        versionCode: Int,
        versionName: String,
        downloadUrl: String,
        isMandatory: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isUploading.value = true
        _uploadStatus.value = "Broadcasting manual production update..."

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val prodUpdate = mapOf(
                        "versionCode" to versionCode,
                        "versionName" to versionName,
                        "downloadUrl" to downloadUrl,
                        "isMandatory" to isMandatory,
                        "releaseNotes" to "Manual production release broadcasted via Admin override.",
                        "timestamp" to Timestamp.now()
                    )

                    val writeTask = db.collection("app_config").document("latest_update").set(prodUpdate)
                    var count = 0
                    while (!writeTask.isComplete && count < 80) {
                        Thread.sleep(100)
                        count++
                    }
                    if (!writeTask.isSuccessful) {
                        throw IOException("Firestore manual production write failed.")
                    }
                }

                _uploadStatus.value = "Ready"
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Failed to broadcast manual update.")
            }
        }
    }

    fun rejectBuild(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isUploading.value = true
        _uploadStatus.value = "Rejecting & deleting staging build..."

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val deleteTask = db.collection("app_config").document("staging_update").delete()
                    var count = 0
                    while (!deleteTask.isComplete && count < 80) {
                        Thread.sleep(100)
                        count++
                    }
                    if (!deleteTask.isSuccessful) {
                        throw IOException("Firestore staging deletion failed.")
                    }
                }

                _uploadStatus.value = "Ready"
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadStatus.value = "Error"
                _isUploading.value = false
                onError(e.localizedMessage ?: "Failed to reject staging build.")
            }
        }
    }
}
