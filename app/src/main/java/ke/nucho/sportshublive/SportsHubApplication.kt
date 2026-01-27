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
import ke.nucho.sportshublive.data.api.ApiConfigManager
import ke.nucho.sportshublive.data.repository.CachedFootballRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for Football Live Scores
 * Initializes Firebase, API configuration, and Repository with Caching
 *
 * ✅ ENHANCED: Now includes CachedFootballRepository singleton
 */
class SportsHubApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var apiConfigManager: ApiConfigManager

    companion object {
        private const val TAG = "SportsHubApp"

        lateinit var instance: SportsHubApplication
            private set

        lateinit var analytics: FirebaseAnalytics
            private set

        lateinit var remoteConfig: FirebaseRemoteConfig
            private set

        // ✅ NEW: Cached Repository Singleton
        lateinit var cachedRepository: CachedFootballRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()

        try {
            Log.d(TAG, "⚽ Starting Football Live Scores App...")

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

            // ✅ Initialize API Config Manager and Cached Repository
            initializeApiConfig()

            // Log app start
            logEvent("app_opened", mapOf("sport" to "football"))

            Log.d(TAG, "✓ Application initialization complete")

        } catch (e: Exception) {
            Log.e(TAG, "✗ Application initialization failed", e)
            e.printStackTrace()
        }
    }

    /**
     * Initialize Firebase Crashlytics
     */
    private fun initializeCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
                log("Football Live Scores initialized")
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

            // Set default values for football app
            remoteConfig.setDefaultsAsync(
                mapOf(
                    // API Provider Configuration
                    "api_provider" to "api_sports", // "api_sports" or "football_data"

                    // API Configuration JSON
                    "api_config_json" to """
                        {
                            "api_sports": {
                                "base_url": "https://v3.football.api-sports.io",
                                "api_key": "YOUR_API_SPORTS_KEY_HERE",
                                "rate_limit": 100,
                                "features": {
                                    "live_scores": true,
                                    "historical_data": true,
                                    "predictions": true,
                                    "statistics": true,
                                    "h2h": true,
                                    "lineups": true,
                                    "max_days_back": 365,
                                    "max_days_forward": 30
                                }
                            },
                            "football_data": {
                                "base_url": "https://api.football-data.org/v4",
                                "api_key": "YOUR_FOOTBALL_DATA_KEY_HERE",
                                "rate_limit": 10,
                                "features": {
                                    "live_scores": true,
                                    "historical_data": true,
                                    "predictions": false,
                                    "statistics": true,
                                    "h2h": true,
                                    "lineups": false,
                                    "max_days_back": 90,
                                    "max_days_forward": 30
                                }
                            }
                        }
                    """.trimIndent(),

                    // App Configuration
                    "enable_analytics" to true,
                    "auto_refresh_interval" to 30000L, // 30 seconds
                    "cache_duration" to 300000L, // 5 minutes

                    // Feature Flags (Football only)
                    "enable_live_view" to true,
                    "enable_predictions" to true,
                    "enable_statistics" to true,
                    "enable_lineups" to true,
                    "enable_h2h" to true,

                    // Maintenance
                    "maintenance_mode" to false,
                    "maintenance_message" to "We're updating the app. Please try again shortly.",

                    // UI Configuration
                    "default_leagues" to "39,140,78,135,61,2,3", // EPL, La Liga, Bundesliga, Serie A, Ligue 1, UCL, UEL
                    "show_league_filter" to true,
                    "show_date_selector" to true,
                    "max_live_refresh_count" to 60 // Maximum auto-refresh cycles
                )
            )

            // Fetch remote config values
            fetchRemoteConfig()

            Log.d(TAG, "✓ Remote Config initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Remote Config initialization failed", e)
        }
    }

    /**
     * Initialize API Config Manager and Cached Repository
     * ✅ ENHANCED: Now includes CachedFootballRepository initialization
     */
    private fun initializeApiConfig() {
        try {
            Log.d(TAG, "🔧 Initializing API Config and Repository...")

            // Initialize API Config Manager
            apiConfigManager = ApiConfigManager()

            // ✅ Initialize Cached Repository (with Firestore offline persistence)
            cachedRepository = CachedFootballRepository(apiConfigManager)
            Log.d(TAG, "✓ Cached Repository initialized")

            // Fetch and activate config asynchronously
            applicationScope.launch {
                try {
                    val success = apiConfigManager.fetchAndActivate()
                    if (success) {
                        val config = apiConfigManager.getApiConfig()
                        config.onSuccess {
                            Log.d(TAG, "✓ API Config loaded: ${it.provider}")
                            Log.d(TAG, "✓ Base URL: ${it.baseUrl}")
                            Log.d(TAG, "✓ Rate Limit: ${it.rateLimitPerMinute}/min")

                            // Log feature availability
                            logFeatureAvailability(it.features)
                        }.onFailure {
                            Log.e(TAG, "✗ Failed to load API config", it)
                        }
                    } else {
                        Log.w(TAG, "⚠ Using default API config")
                    }

                    // ✅ Log cache stats on startup
                    val stats = cachedRepository.getCacheStats()
                    Log.d(TAG, "📊 Cache Stats on Startup:")
                    Log.d(TAG, "   Total entries: ${stats.totalEntries}")
                    Log.d(TAG, "   Valid: ${stats.validEntries}")
                    Log.d(TAG, "   Expired: ${stats.expiredEntries}")
                    Log.d(TAG, "   Size: ${String.format("%.2f", stats.cacheSizeMB)} MB")
                    Log.d(TAG, "   Hit Rate: ${String.format("%.1f", stats.hitRate)}%")

                    // ✅ Clear expired cache on startup
                    val expiredCount = cachedRepository.clearExpiredCache()
                    if (expiredCount > 0) {
                        Log.d(TAG, "🧹 Cleared $expiredCount expired cache entries")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to initialize API config/cache", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

            Log.d(TAG, "✓ API Config Manager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ API Config Manager initialization failed", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    /**
     * Log feature availability
     */
    private fun logFeatureAvailability(features: ApiConfigManager.ApiFeatures) {
        Log.d(TAG, "📊 Available Features:")
        Log.d(TAG, "  - Live Scores: ${features.supportsLiveScores}")
        Log.d(TAG, "  - Historical Data: ${features.supportsHistoricalData}")
        Log.d(TAG, "  - Predictions: ${features.supportsPredictions}")
        Log.d(TAG, "  - Statistics: ${features.supportsStatistics}")
        Log.d(TAG, "  - H2H: ${features.supportsH2H}")
        Log.d(TAG, "  - Lineups: ${features.supportsLineups}")
        Log.d(TAG, "  - Max Days Back: ${features.maxDaysBack}")
        Log.d(TAG, "  - Max Days Forward: ${features.maxDaysForward}")
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

                    // Reload API config if updated
                    if (updated) {
                        applicationScope.launch {
                            val success = apiConfigManager.fetchAndActivate()
                            if (success) {
                                Log.d(TAG, "✓ API config reloaded from Remote Config")
                                val provider = apiConfigManager.getProviderName()
                                Log.d(TAG, "✓ Active provider: $provider")
                            }
                        }
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
     */
    fun refreshRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    applicationScope.launch {
                        apiConfigManager.fetchAndActivate()
                        Log.d(TAG, "✓ Remote Config manually refreshed")
                    }
                }
            }
    }

    /**
     * Get current API provider info
     */
    fun getApiProviderInfo(): String {
        return apiConfigManager.getProviderName()
    }

    /**
     * Check if a feature is supported
     */
    fun isFeatureSupported(feature: String): Boolean {
        return apiConfigManager.isFeatureSupported(feature)
    }

    /**
     * Get API config for debugging
     */
    fun getApiConfigDebugInfo(): String {
        val config = apiConfigManager.getApiConfig().getOrNull()
        return if (config != null) {
            buildString {
                appendLine("API Provider: ${config.provider}")
                appendLine("Base URL: ${config.baseUrl}")
                appendLine("Rate Limit: ${config.rateLimitPerMinute}/min")
                appendLine("\nFeatures:")
                appendLine("  Live Scores: ${config.features.supportsLiveScores}")
                appendLine("  Predictions: ${config.features.supportsPredictions}")
                appendLine("  Statistics: ${config.features.supportsStatistics}")
                appendLine("  H2H: ${config.features.supportsH2H}")
                appendLine("  Lineups: ${config.features.supportsLineups}")
            }
        } else {
            "API Config not loaded"
        }
    }

    /**
     * ✅ NEW: Get cache statistics for debugging
     */
    suspend fun getCacheDebugInfo(): String {
        return try {
            val stats = cachedRepository.getCacheStats()
            buildString {
                appendLine("Cache Statistics:")
                appendLine("  Total Entries: ${stats.totalEntries}")
                appendLine("  Valid: ${stats.validEntries}")
                appendLine("  Expired: ${stats.expiredEntries}")
                appendLine("  Size: ${String.format("%.2f", stats.cacheSizeMB)} MB")
                appendLine("  Hit Rate: ${String.format("%.1f", stats.hitRate)}%")
            }
        } catch (e: Exception) {
            "Cache stats unavailable: ${e.message}"
        }
    }

    /**
     * ✅ NEW: Clear all cache (for settings/debugging)
     */
    suspend fun clearAllCache(): Int {
        return try {
            cachedRepository.clearAllCache()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
            0
        }
    }
}