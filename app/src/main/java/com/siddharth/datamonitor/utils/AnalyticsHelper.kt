package com.siddharth.datamonitor.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AnalyticsHelper {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    fun logFeatureError(featureName: String, errorMsg: String) {
        // Log to Crashlytics
        FirebaseCrashlytics.getInstance().recordException(Exception("Feature: $featureName, Error: $errorMsg"))
        
        // Log to Analytics
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
            putString("error_message", errorMsg.take(100))
        }
        firebaseAnalytics?.logEvent("feature_error", bundle)
    }
    
    fun logCrash(crashName: String, exception: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(exception)
        
        val bundle = Bundle().apply {
            putString("crash_name", crashName)
            putString("error_message", exception.localizedMessage?.take(100) ?: "Unknown")
        }
        firebaseAnalytics?.logEvent("app_crash", bundle)
    }
}
