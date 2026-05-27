package com.siddharth.datamonitor.utils

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.siddharth.datamonitor.BuildConfig

object UserTelemetrySync {
    private const val TAG = "UserTelemetrySync"

    fun sync(userUid: String, email: String?, authProvider: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(userUid)

            val userData = hashMapOf(
                "email" to (email ?: "Guest User"),
                "authProvider" to authProvider,
                "lastLoginTimestamp" to FieldValue.serverTimestamp(),
                "appVersion" to BuildConfig.VERSION_NAME
            )

            userDoc.set(userData)
                .addOnSuccessListener {
                    Log.d(TAG, "Synced user telemetry: $userUid")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed syncing user telemetry", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}
