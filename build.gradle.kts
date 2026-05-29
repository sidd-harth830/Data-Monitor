// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
