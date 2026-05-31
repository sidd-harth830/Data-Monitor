# ProGuard and R8 Keep Rules for Data Usage Monitor v3.5.3

# De-obfuscation and Crashlytics mapping attributes
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Google Play Services and Firebase classes
-keep class com.google.android.gms.** { <fields>; <methods>; }
-keep class com.google.firebase.** { <fields>; <methods>; }
-keep class com.google.android.play.** { <fields>; <methods>; }

# Firebase Auth keep rules to prevent stripping of provider and token classes
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Firebase Firestore keep rules for data transfer objects / serialization
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Firebase Crashlytics & Analytics keep rules
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-dontwarn com.google.firebase.analytics.**

# Kotlin Coroutines keep rules
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Google / Firebase Property annotations
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
}

# Preserve standard Android components and initialization classes from being minified/optimized out of existence
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep application package components to ensure safe serialization & reflection
-keep class com.siddharth.datamonitor.data.** { *; }
-keep class com.siddharth.datamonitor.ui.** { *; }
-keep class com.siddharth.datamonitor.utils.** { *; }
-keep class com.siddharth.datamonitor.worker.** { *; }

# Third-Party Libraries Support
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okio.**
-dontwarn com.squareup.moshi.**

# Vico chart rendering package keep rules
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# Keep Google Fonts provider classes safe from minification
-keep class androidx.compose.ui.text.googlefonts.** { *; }
-dontwarn androidx.compose.ui.text.googlefonts.**
-keep class androidx.core.provider.FontsContractCompat** { *; }

