plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")

    // Add Firebase plugins
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "ke.nucho.sportshublive"
    compileSdk = 34

    defaultConfig {
        applicationId = "ke.nucho.sportshublive"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ========== ADD YOUR API KEYS HERE ==========
        // Replace "YOUR_API_KEY_HERE" with your actual API-Football key
        buildConfigField("String", "API_FOOTBALL_KEY", "\"dfa5bc422e979517069be14236ec78e5\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            // Optional: Use a different key for debug builds
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true  // ⭐ IMPORTANT: Enable BuildConfig generation
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Compose BOM (manages all Compose versions)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose integration with other libraries
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Networking - Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Image Loading - Coil (better for Compose than Glide)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // RecyclerView & SwipeRefreshLayout (if needed for hybrid views)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // YouTube Player (for highlights)
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // WorkManager (for background updates)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ==================== FIREBASE ====================

    // Firebase BOM (Bill of Materials) - manages all Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Firebase Performance Monitoring
    implementation("com.google.firebase:firebase-perf-ktx")

    // Firebase Remote Config
    implementation("com.google.firebase:firebase-config-ktx")

    // Firebase Firestore (for future caching)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase Auth (optional - for user features)
    implementation("com.google.firebase:firebase-auth-ktx")

    // Debug dependencies for Compose
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

// IMPORTANT: This must be at the bottom of the file
apply(plugin = "com.google.gms.google-services")