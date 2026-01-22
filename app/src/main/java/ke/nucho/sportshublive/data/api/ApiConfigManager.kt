package ke.nucho.sportshublive.data.api

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

/**
 * Centralized API Configuration Manager
 * Supports both API-Sports and Football-Data.org
 */
class ApiConfigManager {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        private const val TAG = "ApiConfigManager"
        private const val KEY_API_PROVIDER = "api_provider" // "api_sports" or "football_data"
        private const val KEY_API_CONFIG = "api_config_json"

        // Default values - MUST be overridden in Firebase Remote Config
        private const val DEFAULT_PROVIDER = "api_sports"
        private const val DEFAULT_CONFIG = """
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
        """
    }

    data class ApiConfig(
        val provider: ApiProvider,
        val apiKey: String,
        val baseUrl: String,
        val rateLimitPerMinute: Int,
        val features: ApiFeatures
    )

    enum class ApiProvider {
        API_SPORTS,      // api-football.com (RapidAPI)
        FOOTBALL_DATA    // football-data.org
    }

    data class ApiFeatures(
        val supportsLiveScores: Boolean,
        val supportsHistoricalData: Boolean,
        val supportsPredictions: Boolean,
        val supportsStatistics: Boolean,
        val supportsH2H: Boolean,
        val supportsLineups: Boolean,
        val maxDaysBack: Int,
        val maxDaysForward: Int
    )

    init {
        // Configure remote config settings with shorter fetch interval for testing
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0 // Set to 0 for testing (allows immediate fetch)
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default values
        val defaults = mapOf(
            KEY_API_PROVIDER to DEFAULT_PROVIDER,
            KEY_API_CONFIG to DEFAULT_CONFIG
        )
        remoteConfig.setDefaultsAsync(defaults)

        Log.d(TAG, "✅ Remote Config initialized")
    }

    /**
     * Fetch and activate remote config
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            Log.d(TAG, "📡 Fetching Remote Config from Firebase...")

            // First, try to fetch
            remoteConfig.fetch(0).await() // 0 = no cache, fetch immediately
            Log.d(TAG, "   Fetch completed, now activating...")

            // Then activate
            val activated = remoteConfig.activate().await()
            Log.d(TAG, "   Activation result: $activated")

            if (activated) {
                Log.d(TAG, "✅ Remote Config fetched and activated successfully")

                // Log what we got
                val provider = remoteConfig.getString(KEY_API_PROVIDER)
                val config = remoteConfig.getString(KEY_API_CONFIG)
                Log.d(TAG, "   Provider: $provider")
                Log.d(TAG, "   Config length: ${config.length} characters")

                true
            } else {
                Log.w(TAG, "⚠️ No changes in Remote Config, using existing values")
                // Still return true because we have values
                true
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch Remote Config", e)
            Log.e(TAG, "   Error: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Get current API configuration
     */
    fun getApiConfig(): Result<ApiConfig> {
        return try {
            val provider = getApiProvider()
            val configJson = remoteConfig.getString(KEY_API_CONFIG)

            Log.d(TAG, "🔍 Getting API config...")
            Log.d(TAG, "   Provider: $provider")
            Log.d(TAG, "   Config JSON length: ${configJson.length}")

            if (configJson.isEmpty()) {
                throw Exception("Remote Config 'api_config_json' is empty. Please set it in Firebase Console.")
            }

            val jsonObject = JSONObject(configJson)
            val providerKey = when (provider) {
                ApiProvider.API_SPORTS -> "api_sports"
                ApiProvider.FOOTBALL_DATA -> "football_data"
            }

            if (!jsonObject.has(providerKey)) {
                throw Exception("Configuration for '$providerKey' not found in api_config_json")
            }

            val providerConfig = jsonObject.getJSONObject(providerKey)
            val featuresJson = providerConfig.getJSONObject("features")

            val apiKey = providerConfig.getString("api_key")

            // Check if API key is set
            if (apiKey.contains("YOUR_") || apiKey.length < 10) {
                throw Exception(
                    "API key not configured properly in Firebase Remote Config.\n\n" +
                            "Please:\n" +
                            "1. Go to Firebase Console → Remote Config\n" +
                            "2. Edit 'api_config_json' parameter\n" +
                            "3. Replace 'YOUR_API_SPORTS_KEY_HERE' with your actual API key\n" +
                            "4. Publish changes"
                )
            }

            val config = ApiConfig(
                provider = provider,
                apiKey = apiKey,
                baseUrl = providerConfig.getString("base_url"),
                rateLimitPerMinute = providerConfig.getInt("rate_limit"),
                features = ApiFeatures(
                    supportsLiveScores = featuresJson.getBoolean("live_scores"),
                    supportsHistoricalData = featuresJson.getBoolean("historical_data"),
                    supportsPredictions = featuresJson.getBoolean("predictions"),
                    supportsStatistics = featuresJson.getBoolean("statistics"),
                    supportsH2H = featuresJson.getBoolean("h2h"),
                    supportsLineups = featuresJson.getBoolean("lineups"),
                    maxDaysBack = featuresJson.getInt("max_days_back"),
                    maxDaysForward = featuresJson.getInt("max_days_forward")
                )
            )

            Log.d(TAG, "✅ Config loaded successfully for: ${config.provider}")
            Log.d(TAG, "   Base URL: ${config.baseUrl}")
            Log.d(TAG, "   API Key length: ${config.apiKey.length}")

            Result.success(config)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get API config", e)
            Result.failure(e)
        }
    }

    /**
     * Get current API provider
     */
    private fun getApiProvider(): ApiProvider {
        val providerString = remoteConfig.getString(KEY_API_PROVIDER)
        return when (providerString.lowercase()) {
            "football_data" -> ApiProvider.FOOTBALL_DATA
            else -> ApiProvider.API_SPORTS
        }
    }

    /**
     * Get provider name for display
     */
    fun getProviderName(): String {
        return when (getApiProvider()) {
            ApiProvider.API_SPORTS -> "API-Sports (RapidAPI)"
            ApiProvider.FOOTBALL_DATA -> "Football-Data.org"
        }
    }

    /**
     * Check if a feature is supported
     */
    fun isFeatureSupported(feature: String): Boolean {
        return getApiConfig().getOrNull()?.let { config ->
            when (feature.lowercase()) {
                "predictions" -> config.features.supportsPredictions
                "statistics" -> config.features.supportsStatistics
                "h2h" -> config.features.supportsH2H
                "lineups" -> config.features.supportsLineups
                else -> false
            }
        } ?: false
    }
}