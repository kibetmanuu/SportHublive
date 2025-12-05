package ke.nucho.sportshublive

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import ke.nucho.sportshublive.data.api.ApiKeyManager

/**
 * Application class for SportsHub Live
 * Initializes Firebase and other app-wide configurations
 */
class SportsHubApplication : Application() {

    companion object {
        private const val TAG = "SportsHubApplication"

        lateinit var instance: SportsHubApplication
            private set

        lateinit var analytics: FirebaseAnalytics
            private set

        lateinit var remoteConfig: FirebaseRemoteConfig
            private set
    }

    override fun onCreate() {
        super.onCreate()

        try {
            Log.d(TAG, "🚀 Starting SportsHub Application...")

            instance = this

            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "✓ Firebase initialized")

            // Initialize Analytics
            analytics = Firebase.analytics
            Log.d(TAG, "✓ Analytics initialized")

            // Initialize Crashlytics
            initializeCrashlytics()

            // Initialize Performance Monitoring
            initializePerformanceMonitoring()

            // Initialize Remote Config
            initializeRemoteConfig()

            // Log app start
            logEvent("app_opened", null)

            Log.d(TAG, "✓ Application initialization complete")

        } catch (e: Exception) {
            Log.e(TAG, "✗ Application initialization failed", e)
            e.printStackTrace()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cleanup ApiKeyManager coroutines
        ApiKeyManager.cleanup()
    }

    /**
     * Initialize Firebase Crashlytics
     */
    private fun initializeCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
                log("SportsHub Live initialized")
            }
            Log.d(TAG, "✓ Crashlytics initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Crashlytics initialization failed", e)
        }
    }

    /**
     * Initialize Firebase Performance Monitoring
     */
    private fun initializePerformanceMonitoring() {
        try {
            FirebasePerformance.getInstance().apply {
                isPerformanceCollectionEnabled = true
            }
            Log.d(TAG, "✓ Performance Monitoring initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Performance Monitoring initialization failed", e)
        }
    }

    /**
     * Initialize Firebase Remote Config
     */
    private fun initializeRemoteConfig() {
        try {
            remoteConfig = Firebase.remoteConfig

            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                    60 // 1 minute for debug builds
                } else {
                    3600 // 1 hour for release builds
                }
            }

            remoteConfig.setConfigSettingsAsync(configSettings)

            // Set default values including API keys
            remoteConfig.setDefaultsAsync(
                mapOf(
                    // API Keys Configuration (JSON array format)
                    "api_keys_json" to """["dfa5bc422e979517069be14236ec78e5"]""",
                    "api_key_selection_mode" to "random", // "random" or "round_robin"

                    // App Configuration
                    "enable_analytics" to true,
                    "auto_refresh_interval" to 30000L,
                    "cache_duration" to 300000L,

                    // Sports Feature Flags
                    "enable_football" to true,
                    "enable_basketball" to true,
                    "enable_hockey" to true,
                    "enable_formula1" to true,
                    "enable_volleyball" to true,
                    "enable_rugby" to true,

                    // Maintenance
                    "maintenance_mode" to false,
                    "maintenance_message" to "We're updating the app. Please try again shortly.",

                    // API Key Reset Interval (in hours) - changed to Double
                    "api_key_reset_interval_hours" to 0.25 // 15 minutes
                )
            )

            // Initialize ApiKeyManager with default values
            // This starts the automatic reset timer
            ApiKeyManager.initialize(remoteConfig)
            Log.d(TAG, "✓ ApiKeyManager initialized with defaults")

            // Fetch remote config values
            fetchRemoteConfig()

            Log.d(TAG, "✓ Remote Config initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Remote Config initialization failed", e)
        }
    }

    /**
     * Fetch remote config from Firebase
     */
    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Remote config fetched. Updated: $updated")

                    // Reload API keys if config was updated
                    // This will restart the automatic reset timer with new interval
                    if (updated) {
                        ApiKeyManager.reloadFromRemoteConfig(remoteConfig)
                        Log.d(TAG, "✓ API keys reloaded from Remote Config")

                        // Log the current reset interval
                        val intervalHours = remoteConfig.getDouble("api_key_reset_interval_hours")
                        val intervalMinutes = intervalHours * 60
                        Log.d(TAG, "✓ API key reset interval: $intervalMinutes minutes")
                    }

                    logEvent("remote_config_fetched", mapOf("updated" to updated.toString()))
                } else {
                    Log.e(TAG, "Remote config fetch failed", task.exception)
                    logError("remote_config_fetch_failed", task.exception)
                }
            }
    }

    /**
     * Log analytics event
     */
    fun logEvent(eventName: String, params: Map<String, String>?) {
        try {
            val bundle = android.os.Bundle()
            params?.forEach { (key, value) ->
                bundle.putString(key, value)
            }
            analytics.logEvent(eventName, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event: $eventName", e)
        }
    }

    /**
     * Log error to Crashlytics
     */
    fun logError(message: String, exception: Exception?) {
        try {
            FirebaseCrashlytics.getInstance().apply {
                log(message)
                exception?.let { recordException(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error to Crashlytics", e)
        }
    }

    /**
     * Set user property for analytics
     */
    fun setUserProperty(name: String, value: String) {
        try {
            analytics.setUserProperty(name, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user property", e)
        }
    }

    /**
     * Manually refresh Remote Config
     * Call this when user triggers a manual refresh
     */
    fun refreshRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ApiKeyManager.reloadFromRemoteConfig(remoteConfig)
                    Log.d(TAG, "✓ Remote Config manually refreshed")
                }
            }
    }

    /**
     * Get API Key Manager debug info
     */
    fun getApiKeyDebugInfo(): String {
        return ApiKeyManager.getDebugInfo()
    }

    /**
     * Manually trigger API key reset (for testing/debugging)
     */
    fun manuallyResetApiKeys() {
        ApiKeyManager.resetFailedKeys()
        Log.d(TAG, "✓ Manually reset all failed API keys")
    }
}