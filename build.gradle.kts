// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Add Google Services plugin
    id("com.google.gms.google-services") version "4.4.0" apply false

    // Add Firebase Crashlytics plugin
    id("com.google.firebase.crashlytics") version "2.9.9" apply false

    // Add Firebase Performance plugin
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}