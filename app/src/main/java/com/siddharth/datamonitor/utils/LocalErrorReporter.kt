package com.siddharth.datamonitor.utils

import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import java.io.PrintWriter
import java.io.StringWriter

object LocalErrorReporter {
    private const val TAG = "LocalErrorReporter"

    fun reportError(throwable: Throwable) {
        // 1. Send to Crashlytics
        try {
            com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("GlobalException", throwable)
            Log.d(TAG, "Logged to Firebase Crashlytics and Analytics")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send exception to Crashlytics", e)
        }

        // 2. Synced Live to Cloud Firestore telemetry
        try {
            val db = FirebaseFirestore.getInstance()
            
            val writer = StringWriter()
            throwable.printStackTrace(PrintWriter(writer))
            val fullStackTrace = writer.toString()
            val truncatedStackTrace = if (fullStackTrace.length > 500) {
                fullStackTrace.substring(0, 500)
            } else {
                fullStackTrace
            }

            val crashData = hashMapOf(
                "errorMessage" to (throwable.localizedMessage ?: throwable.message ?: "Unknown Exception"),
                "stackTrace" to truncatedStackTrace,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "deviceModel" to Build.MODEL
            )

            db.collection("crashes")
                .add(crashData)
                .addOnSuccessListener { ref ->
                    Log.d(TAG, "Crash saved to Firestore: ${ref.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed syncing crash to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Global Firestore telemetry failed", e)
        }
    }

    fun install() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Intercept and sync first
            reportError(throwable)
            
            // Pass to native Android thread crash systems
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
