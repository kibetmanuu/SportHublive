package ke.nucho.sportshublive.data.repository

import android.util.Log
import com.google.gson.Gson
import ke.nucho.sportshublive.data.api.ApiConfigManager
import ke.nucho.sportshublive.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Unified Football Repository
 * Supports API-Sports and Football-Data.org
 * Provides comprehensive football data including:
 * - Live matches
 * - Match details
 * - Statistics
 * - Events
 * - Lineups
 * - Head-to-head
 * - Predictions
 * - Standings
 */
class UnifiedFootballRepository(
    private val apiConfigManager: ApiConfigManager
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private const val TAG = "UnifiedFootballRepo"

        // League mappings between different APIs
        private val LEAGUE_MAPPINGS = mapOf(
            // Premier League
            39 to 2021,
            2021 to 39,

            // La Liga
            140 to 2014,
            2014 to 140,

            // Bundesliga
            78 to 2002,
            2002 to 78,

            // Serie A
            135 to 2019,
            2019 to 135,

            // Ligue 1
            61 to 2015,
            2015 to 61,

            // Champions League
            2 to 2001,
            2001 to 2,

            // Europa League
            3 to 2018,
            2018 to 3
        )
    }

    // ============================================================================
    // PUBLIC API METHODS
    // ============================================================================

    /**
     * Get live matches
     */
    suspend fun getLiveMatches(leagueId: Int? = null): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsLiveMatches(config, leagueId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataLiveMatches(config, leagueId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting live matches", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get matches by date
     */
    suspend fun getMatchesByDate(date: String, leagueId: Int? = null): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsMatchesByDate(config, date, leagueId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataMatchesByDate(config, date, leagueId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting matches by date", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get fixture by ID
     */
    suspend fun getFixtureById(fixtureId: Int): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsFixtureById(config, fixtureId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataFixtureById(config, fixtureId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting fixture by ID", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get match statistics
     */
    suspend fun getMatchStatistics(fixtureId: Int): Result<List<TeamStatistics>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsStatistics(config, fixtureId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        // Football-Data doesn't provide detailed statistics
                        Result.success(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting statistics", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get match events
     */
    suspend fun getMatchEvents(fixtureId: Int): Result<List<MatchEvent>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsEvents(config, fixtureId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        // Football-Data doesn't provide detailed events
                        Result.success(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting events", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get match lineups
     */
    suspend fun getMatchLineups(fixtureId: Int): Result<List<TeamLineup>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsLineups(config, fixtureId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        // Football-Data doesn't provide detailed lineups
                        Result.success(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting lineups", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get head-to-head matches
     */
    suspend fun getHeadToHead(team1Id: Int, team2Id: Int): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsH2H(config, team1Id, team2Id)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataH2H(config, team1Id, team2Id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting H2H", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get predictions
     */
    suspend fun getPredictions(fixtureId: Int): Result<List<Prediction>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsPredictions(config, fixtureId)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        // Football-Data doesn't provide predictions
                        Result.success(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting predictions", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get league standings
     */
    suspend fun getStandings(leagueId: Int, season: Int): Result<List<LeagueStanding>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsStandings(config, leagueId, season)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataStandings(config, leagueId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting standings", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get top scorers for a league
     */
    suspend fun getTopScorers(leagueId: Int, season: Int): Result<List<TopScorerEntry>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsTopScorers(config, leagueId, season)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataTopScorers(config, leagueId, season)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting top scorers", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get fixtures for a league with optional date range
     * This is what you should use for the Fixtures tab
     */
    suspend fun getLeagueFixtures(
        leagueId: Int,
        season: Int,
        from: String? = null,
        to: String? = null,
        last: Int? = null,
        next: Int? = null
    ): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                val config = apiConfigManager.getApiConfig().getOrThrow()

                when (config.provider) {
                    ApiConfigManager.ApiProvider.API_SPORTS -> {
                        getApiSportsLeagueFixtures(config, leagueId, season, from, to, last, next)
                    }
                    ApiConfigManager.ApiProvider.FOOTBALL_DATA -> {
                        getFootballDataLeagueFixtures(config, leagueId, from, to)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting league fixtures", e)
                Result.failure(e)
            }
        }
    }
    // HYBRID API METHODS - Uses specific API for each purpose
    // ============================================================================

    /**
     * Get live matches - ALWAYS use API-Sports for real-time data
     */
    suspend fun getLiveMatchesHybrid(leagueId: Int? = null): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for live matches")
                val config = getApiSportsConfig()
                getApiSportsLiveMatches(config, leagueId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting live matches (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get matches by date - ALWAYS use API-Sports for real-time data
     */
    suspend fun getMatchesByDateHybrid(date: String, leagueId: Int? = null): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for matches by date")
                val config = getApiSportsConfig()
                getApiSportsMatchesByDate(config, date, leagueId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting matches by date (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get fixture by ID - ALWAYS use API-Sports for detailed match data
     */
    suspend fun getFixtureByIdHybrid(fixtureId: Int): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for fixture details")
                val config = getApiSportsConfig()
                getApiSportsFixtureById(config, fixtureId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting fixture by ID (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get match statistics - ALWAYS use API-Sports
     */
    suspend fun getMatchStatisticsHybrid(fixtureId: Int): Result<List<TeamStatistics>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for statistics")
                val config = getApiSportsConfig()
                getApiSportsStatistics(config, fixtureId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting statistics (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get match events - ALWAYS use API-Sports
     */
    suspend fun getMatchEventsHybrid(fixtureId: Int): Result<List<MatchEvent>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for events")
                val config = getApiSportsConfig()
                getApiSportsEvents(config, fixtureId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting events (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get match lineups - ALWAYS use API-Sports
     */
    suspend fun getMatchLineupsHybrid(fixtureId: Int): Result<List<TeamLineup>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for lineups")
                val config = getApiSportsConfig()
                getApiSportsLineups(config, fixtureId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting lineups (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get H2H - ALWAYS use API-Sports
     */
    suspend fun getHeadToHeadHybrid(team1Id: Int, team2Id: Int): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔴 HYBRID: Using API-Sports for H2H")
                val config = getApiSportsConfig()
                getApiSportsH2H(config, team1Id, team2Id)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting H2H (API-Sports)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get league fixtures - ALWAYS use Football-Data.org
     */
    suspend fun getLeagueFixturesHybrid(
        leagueId: Int,
        season: Int,
        from: String? = null,
        to: String? = null
    ): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔵 HYBRID: Using Football-Data for league fixtures")
                val config = getFootballDataConfig()
                getFootballDataLeagueFixtures(config, leagueId, from, to)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting league fixtures (Football-Data)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get standings - ALWAYS use Football-Data.org
     */
    suspend fun getStandingsHybrid(leagueId: Int, season: Int): Result<List<LeagueStanding>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔵 HYBRID: Using Football-Data for standings")
                val config = getFootballDataConfig()
                getFootballDataStandings(config, leagueId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting standings (Football-Data)", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get top scorers - ALWAYS use Football-Data.org
     */
    suspend fun getTopScorersHybrid(leagueId: Int, season: Int): Result<List<TopScorerEntry>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔵 HYBRID: Using Football-Data for top scorers")
                val config = getFootballDataConfig()
                getFootballDataTopScorers(config, leagueId, season)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting top scorers (Football-Data)", e)
                Result.failure(e)
            }
        }
    }
    /**
     * Get matches by date - Uses Football-Data.org for historical data
     */
    suspend fun getMatchesByDateFootballData(date: String, leagueId: Int? = null): Result<List<Fixture>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔵 HYBRID: Using Football-Data for historical matches")
                val config = getFootballDataConfig()
                getFootballDataMatchesByDate(config, date, leagueId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting matches by date (Football-Data)", e)
                Result.failure(e)
            }
        }
    }

    // ============================================================================
    // HELPER METHODS - Get specific API configs
    // ============================================================================


    /**
     * Get API-Sports configuration from Firebase Remote Config
     */
    private suspend fun getApiSportsConfig(): ApiConfigManager.ApiConfig {
        return try {
            // Get the full config JSON from Remote Config
            val configResult = apiConfigManager.getApiConfig()

            configResult.getOrThrow() // This will get the current provider's config

            // But we need to parse the JSON to get API-Sports specifically
            val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
            val configJson = remoteConfig.getString("api_config_json")

            if (configJson.isEmpty()) {
                throw Exception("Remote Config 'api_config_json' is empty. Please configure it in Firebase Console.")
            }

            val json = org.json.JSONObject(configJson)

            if (!json.has("api_sports")) {
                throw Exception("'api_sports' configuration not found in Remote Config")
            }

            val apiSportsJson = json.getJSONObject("api_sports")
            val featuresJson = apiSportsJson.getJSONObject("features")

            val apiKey = apiSportsJson.getString("api_key")

            // Validate API key
            if (apiKey.contains("YOUR_") || apiKey.length < 10) {
                throw Exception(
                    "API-Sports key not configured in Firebase Remote Config.\n\n" +
                            "Please:\n" +
                            "1. Go to Firebase Console → Remote Config\n" +
                            "2. Edit 'api_config_json' parameter\n" +
                            "3. Replace 'YOUR_API_SPORTS_KEY_HERE' with your actual API-Sports key\n" +
                            "4. Publish changes"
                )
            }

            Log.d(TAG, "✅ API-Sports config loaded from Remote Config")
            Log.d(TAG, "   Base URL: ${apiSportsJson.getString("base_url")}")
            Log.d(TAG, "   API Key length: ${apiKey.length}")

            ApiConfigManager.ApiConfig(
                provider = ApiConfigManager.ApiProvider.API_SPORTS,
                apiKey = apiKey,
                baseUrl = apiSportsJson.getString("base_url"),
                rateLimitPerMinute = apiSportsJson.getInt("rate_limit"),
                features = ApiConfigManager.ApiFeatures(
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
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get API-Sports config from Remote Config", e)
            throw Exception(
                "Failed to load API-Sports configuration from Firebase Remote Config.\n\n" +
                        "Error: ${e.message}\n\n" +
                        "Please ensure:\n" +
                        "• Firebase Remote Config is properly set up\n" +
                        "• 'api_config_json' parameter exists\n" +
                        "• API-Sports configuration is valid\n" +
                        "• Your API key is set correctly"
            )
        }
    }

    /**
     * Get Football-Data.org configuration from Firebase Remote Config
     */
    private suspend fun getFootballDataConfig(): ApiConfigManager.ApiConfig {
        return try {
            // Get the full config JSON from Remote Config
            val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
            val configJson = remoteConfig.getString("api_config_json")

            if (configJson.isEmpty()) {
                throw Exception("Remote Config 'api_config_json' is empty. Please configure it in Firebase Console.")
            }

            val json = org.json.JSONObject(configJson)

            if (!json.has("football_data")) {
                throw Exception("'football_data' configuration not found in Remote Config")
            }

            val footballDataJson = json.getJSONObject("football_data")
            val featuresJson = footballDataJson.getJSONObject("features")

            val apiKey = footballDataJson.getString("api_key")

            // Validate API key
            if (apiKey.contains("YOUR_") || apiKey.length < 10) {
                throw Exception(
                    "Football-Data.org key not configured in Firebase Remote Config.\n\n" +
                            "Please:\n" +
                            "1. Go to Firebase Console → Remote Config\n" +
                            "2. Edit 'api_config_json' parameter\n" +
                            "3. Replace 'YOUR_FOOTBALL_DATA_KEY_HERE' with your actual Football-Data.org key\n" +
                            "4. Publish changes"
                )
            }

            Log.d(TAG, "✅ Football-Data config loaded from Remote Config")
            Log.d(TAG, "   Base URL: ${footballDataJson.getString("base_url")}")
            Log.d(TAG, "   API Key length: ${apiKey.length}")

            ApiConfigManager.ApiConfig(
                provider = ApiConfigManager.ApiProvider.FOOTBALL_DATA,
                apiKey = apiKey,
                baseUrl = footballDataJson.getString("base_url"),
                rateLimitPerMinute = footballDataJson.getInt("rate_limit"),
                features = ApiConfigManager.ApiFeatures(
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
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get Football-Data config from Remote Config", e)
            throw Exception(
                "Failed to load Football-Data.org configuration from Firebase Remote Config.\n\n" +
                        "Error: ${e.message}\n\n" +
                        "Please ensure:\n" +
                        "• Firebase Remote Config is properly set up\n" +
                        "• 'api_config_json' parameter exists\n" +
                        "• Football-Data.org configuration is valid\n" +
                        "• Your API key is set correctly"
            )
        }
    }

    // ============================================================================
    // API-SPORTS METHODS
    // ============================================================================

    private fun getApiSportsLiveMatches(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int?
    ): Result<List<Fixture>> {
        val url = buildString {
            append("${config.baseUrl}/fixtures?live=all")
            if (leagueId != null) {
                append("&league=$leagueId")
            }
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return executeApiSportsRequest(request)
    }

    private fun getApiSportsMatchesByDate(
        config: ApiConfigManager.ApiConfig,
        date: String,
        leagueId: Int?
    ): Result<List<Fixture>> {
        // Extract season from date
        val season = date.substring(0, 4).toInt()

        val url = buildString {
            append("${config.baseUrl}/fixtures?date=$date")
            if (leagueId != null) {
                append("&league=$leagueId&season=$season")
            } else {
                // Major leagues if no specific league selected
                append("&season=$season")
                append("&league=39&league=140&league=78&league=135&league=61&league=2&league=3")
            }
        }

        Log.d(TAG, "🔗 API-Sports URL: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return executeApiSportsRequest(request)
    }

    private fun getApiSportsFixtureById(
        config: ApiConfigManager.ApiConfig,
        fixtureId: Int
    ): Result<List<Fixture>> {
        val url = "${config.baseUrl}/fixtures?id=$fixtureId"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return executeApiSportsRequest(request)
    }

    private fun getApiSportsStatistics(
        config: ApiConfigManager.ApiConfig,
        fixtureId: Int
    ): Result<List<TeamStatistics>> {
        val url = "${config.baseUrl}/fixtures/statistics?fixture=$fixtureId"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val responseArray = json.getJSONArray("response")

            val statistics = mutableListOf<TeamStatistics>()
            for (i in 0 until responseArray.length()) {
                val teamStatJson = responseArray.getJSONObject(i)
                statistics.add(parseApiSportsStatistics(teamStatJson))
            }

            Log.d(TAG, "✅ Loaded ${statistics.size} team statistics")
            Result.success(statistics)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading statistics", e)
            Result.failure(e)
        }
    }

    private fun getApiSportsEvents(
        config: ApiConfigManager.ApiConfig,
        fixtureId: Int
    ): Result<List<MatchEvent>> {
        val url = "${config.baseUrl}/fixtures/events?fixture=$fixtureId"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val responseArray = json.getJSONArray("response")

            val events = mutableListOf<MatchEvent>()
            for (i in 0 until responseArray.length()) {
                val eventJson = responseArray.getJSONObject(i)
                events.add(parseApiSportsEvent(eventJson))
            }

            Log.d(TAG, "✅ Loaded ${events.size} events")
            Result.success(events)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading events", e)
            Result.failure(e)
        }
    }

    private fun getApiSportsLineups(
        config: ApiConfigManager.ApiConfig,
        fixtureId: Int
    ): Result<List<TeamLineup>> {
        val url = "${config.baseUrl}/fixtures/lineups?fixture=$fixtureId"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val responseArray = json.getJSONArray("response")

            val lineups = mutableListOf<TeamLineup>()
            for (i in 0 until responseArray.length()) {
                val lineupJson = responseArray.getJSONObject(i)
                lineups.add(parseApiSportsLineup(lineupJson))
            }

            Log.d(TAG, "✅ Loaded ${lineups.size} lineups")
            Result.success(lineups)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading lineups", e)
            Result.failure(e)
        }
    }

    private fun getApiSportsH2H(
        config: ApiConfigManager.ApiConfig,
        team1Id: Int,
        team2Id: Int
    ): Result<List<Fixture>> {
        val url = "${config.baseUrl}/fixtures/headtohead?h2h=$team1Id-$team2Id&last=10"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return executeApiSportsRequest(request)
    }

    private fun getApiSportsPredictions(
        config: ApiConfigManager.ApiConfig,
        fixtureId: Int
    ): Result<List<Prediction>> {
        val url = "${config.baseUrl}/predictions?fixture=$fixtureId"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val responseArray = json.getJSONArray("response")

            val predictions = mutableListOf<Prediction>()
            for (i in 0 until responseArray.length()) {
                val predJson = responseArray.getJSONObject(i)
                predictions.add(parseApiSportsPrediction(predJson))
            }

            Log.d(TAG, "✅ Loaded ${predictions.size} predictions")
            Result.success(predictions)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading predictions", e)
            Result.failure(e)
        }
    }

    private fun getApiSportsStandings(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int,
        season: Int
    ): Result<List<LeagueStanding>> {
        val url = "${config.baseUrl}/standings?league=$leagueId&season=$season"

        Log.d(TAG, "🌐 Standings Request URL: $url")
        Log.d(TAG, "🔑 API Key: ${config.apiKey.take(10)}...")
        Log.d(TAG, "📊 Parameters: league=$leagueId, season=$season")

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return try {
            Log.d(TAG, "📤 Making standings request...")
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d(TAG, "📥 Standings Response Code: ${response.code}")
            Log.d(TAG, "📥 Standings Response Message: ${response.message}")

            if (body != null) {
                Log.d(TAG, "📦 Response Body Length: ${body.length} characters")
                Log.d(TAG, "📦 Response Preview: ${body.take(500)}")
            }

            if (!response.isSuccessful) {
                Log.e(TAG, "❌ API Error Response: $body")
                return Result.failure(Exception("API Error: ${response.code} - ${response.message}"))
            }

            val json = JSONObject(body ?: "")

            // Log JSON structure
            Log.d(TAG, "📋 JSON Keys: ${json.keys().asSequence().toList()}")

            // Check for errors
            if (json.has("errors") && json.getJSONObject("errors").length() > 0) {
                val errors = json.getJSONObject("errors")
                Log.e(TAG, "❌ API returned errors: $errors")
                return Result.failure(Exception("API Error: $errors"))
            }

            val results = json.optInt("results", -1)
            Log.d(TAG, "📊 Standings results count from API: $results")

            val responseArray = json.getJSONArray("response")
            Log.d(TAG, "📊 Standings response array length: ${responseArray.length()}")

            val standings = mutableListOf<LeagueStanding>()
            for (i in 0 until responseArray.length()) {
                val standingJson = responseArray.getJSONObject(i)
                standings.add(parseApiSportsStanding(standingJson))
            }

            Log.d(TAG, "✅ Successfully loaded ${standings.size} standings")
            Result.success(standings)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading standings with exception", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun getApiSportsTopScorers(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int,
        season: Int
    ): Result<List<TopScorerEntry>> {
        val url = "${config.baseUrl}/players/topscorers?league=$leagueId&season=$season"

        Log.d(TAG, "🌐 Top Scorers Request URL: $url")
        Log.d(TAG, "🔑 API Key: ${config.apiKey.take(10)}...")
        Log.d(TAG, "📊 Parameters: league=$leagueId, season=$season")

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return try {
            Log.d(TAG, "📤 Making top scorers request...")
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d(TAG, "📥 Top Scorers Response Code: ${response.code}")
            Log.d(TAG, "📥 Top Scorers Response Message: ${response.message}")

            if (body != null) {
                Log.d(TAG, "📦 Response Body Length: ${body.length} characters")
                Log.d(TAG, "📦 Response Preview: ${body.take(500)}")
            }

            if (!response.isSuccessful) {
                Log.e(TAG, "❌ API Error Response: $body")
                return Result.failure(Exception("API Error: ${response.code} - ${response.message}"))
            }

            val json = JSONObject(body ?: "")

            // Log JSON structure
            Log.d(TAG, "📋 JSON Keys: ${json.keys().asSequence().toList()}")

            // Check for errors
            if (json.has("errors") && json.getJSONObject("errors").length() > 0) {
                val errors = json.getJSONObject("errors")
                Log.e(TAG, "❌ API returned errors: $errors")
                return Result.failure(Exception("API Error: $errors"))
            }

            val results = json.optInt("results", -1)
            Log.d(TAG, "📊 Top scorers results count from API: $results")

            val responseArray = json.getJSONArray("response")
            Log.d(TAG, "📊 Top scorers response array length: ${responseArray.length()}")

            val topScorers = mutableListOf<TopScorerEntry>()
            for (i in 0 until responseArray.length()) {
                val scorerJson = responseArray.getJSONObject(i)
                topScorers.add(parseApiSportsTopScorer(scorerJson))
            }

            Log.d(TAG, "✅ Successfully loaded ${topScorers.size} top scorers")
            Result.success(topScorers)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading top scorers with exception", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get API-Sports league fixtures with flexible parameters
     */
    private fun getApiSportsLeagueFixtures(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int,
        season: Int,
        from: String?,
        to: String?,
        last: Int?,
        next: Int?
    ): Result<List<Fixture>> {
        val url = buildString {
            append("${config.baseUrl}/fixtures?league=$leagueId&season=$season")

            // Add date range if provided
            if (from != null) append("&from=$from")
            if (to != null) append("&to=$to")

            // Or use last/next N matches
            if (last != null) append("&last=$last")
            if (next != null) append("&next=$next")
        }

        Log.d(TAG, "🌐 Fixtures Request URL: $url")
        Log.d(TAG, "🔑 API Key: ${config.apiKey.take(10)}...")
        Log.d(TAG, "📊 Parameters: league=$leagueId, season=$season, from=$from, to=$to, last=$last, next=$next")

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", config.apiKey)
            .addHeader("x-rapidapi-host", "v3.football.api-sports.io")
            .build()

        return executeApiSportsRequest(request)
    }

    private fun executeApiSportsRequest(request: Request): Result<List<Fixture>> {
        return try {
            Log.d(TAG, "📤 Making request to: ${request.url}")

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d(TAG, "📥 Response Code: ${response.code}")
            Log.d(TAG, "📥 Response Message: ${response.message}")

            if (body != null) {
                Log.d(TAG, "📦 Response Body Length: ${body.length} characters")
                Log.d(TAG, "📦 Response Preview: ${body.take(500)}")
            }

            if (!response.isSuccessful) {
                Log.e(TAG, "❌ API Error Response: $body")
                return Result.failure(Exception("API Error: ${response.code} - ${response.message}"))
            }

            val json = JSONObject(body ?: "")

            // Log the full JSON response structure
            Log.d(TAG, "📋 JSON Keys: ${json.keys().asSequence().toList()}")

// Check if there's an error message in the response
            val errors = json.opt("errors")
            when {
                errors is JSONObject && errors.length() > 0 -> {
                    Log.e(TAG, "❌ API returned errors: $errors")
                    return Result.failure(Exception("API Error: $errors"))
                }
                errors is JSONArray && errors.length() > 0 -> {
                    Log.e(TAG, "❌ API returned errors: $errors")
                    return Result.failure(Exception("API Error: $errors"))
                }
                // If errors is an empty array [], continue normally
            }

            // Check results count
            val results = json.optInt("results", -1)
            Log.d(TAG, "📊 Results count from API: $results")

            val fixturesArray = json.getJSONArray("response")
            Log.d(TAG, "📊 Fixtures array length: ${fixturesArray.length()}")

            val fixtures = mutableListOf<Fixture>()
            for (i in 0 until fixturesArray.length()) {
                val fixtureJson = fixturesArray.getJSONObject(i)
                fixtures.add(parseApiSportsFixture(fixtureJson))
            }

            Log.d(TAG, "✅ Successfully parsed ${fixtures.size} fixtures from API-Sports")
            Result.success(fixtures)

        } catch (e: Exception) {
            Log.e(TAG, "❌ API-Sports request failed with exception", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ============================================================================
    // API-SPORTS PARSING METHODS
    // ============================================================================

    private fun parseApiSportsFixture(json: JSONObject): Fixture {
        val fixtureObj = json.getJSONObject("fixture")
        val leagueObj = json.getJSONObject("league")
        val teamsObj = json.getJSONObject("teams")
        val goalsObj = json.getJSONObject("goals")
        val scoreObj = json.optJSONObject("score")

        return Fixture(
            fixture = FixtureDetails(
                id = fixtureObj.getInt("id"),
                referee = fixtureObj.optString("referee", null),
                timezone = fixtureObj.getString("timezone"),
                date = fixtureObj.getString("date"),
                timestamp = fixtureObj.getLong("timestamp"),
                venue = Venue(
                    id = fixtureObj.getJSONObject("venue").optInt("id"),
                    name = fixtureObj.getJSONObject("venue").optString("name"),
                    city = fixtureObj.getJSONObject("venue").optString("city")
                ),
                status = MatchStatus(
                    long = fixtureObj.getJSONObject("status").getString("long"),
                    short = fixtureObj.getJSONObject("status").getString("short"),
                    elapsed = fixtureObj.getJSONObject("status").optInt("elapsed")
                )
            ),
            league = League(
                id = leagueObj.getInt("id"),
                name = leagueObj.getString("name"),
                country = leagueObj.getString("country"),
                logo = leagueObj.getString("logo"),
                flag = leagueObj.optString("flag"),
                season = leagueObj.getInt("season"),
                round = leagueObj.optString("round")
            ),
            teams = Teams(
                home = Team(
                    id = teamsObj.getJSONObject("home").getInt("id"),
                    name = teamsObj.getJSONObject("home").getString("name"),
                    logo = teamsObj.getJSONObject("home").getString("logo"),
                    winner = teamsObj.getJSONObject("home").optBoolean("winner")
                ),
                away = Team(
                    id = teamsObj.getJSONObject("away").getInt("id"),
                    name = teamsObj.getJSONObject("away").getString("name"),
                    logo = teamsObj.getJSONObject("away").getString("logo"),
                    winner = teamsObj.getJSONObject("away").optBoolean("winner")
                )
            ),
            goals = Goals(
                home = if (goalsObj.isNull("home")) null else goalsObj.getInt("home"),
                away = if (goalsObj.isNull("away")) null else goalsObj.getInt("away")
            ),
            score = scoreObj?.let {
                Score(
                    halftime = parseGoals(it.optJSONObject("halftime")),
                    fulltime = parseGoals(it.optJSONObject("fulltime")),
                    extratime = parseGoals(it.optJSONObject("extratime")),
                    penalty = parseGoals(it.optJSONObject("penalty"))
                )
            }
        )
    }

    private fun parseGoals(json: JSONObject?): Goals? {
        return json?.let {
            Goals(
                home = if (it.isNull("home")) null else it.getInt("home"),
                away = if (it.isNull("away")) null else it.getInt("away")
            )
        }
    }

    private fun parseApiSportsStatistics(json: JSONObject): TeamStatistics {
        val teamObj = json.getJSONObject("team")
        val statsArray = json.getJSONArray("statistics")

        val statistics = mutableListOf<Statistic>()
        for (i in 0 until statsArray.length()) {
            val statJson = statsArray.getJSONObject(i)
            statistics.add(
                Statistic(
                    type = statJson.getString("type"),
                    value = if (statJson.isNull("value")) null else statJson.get("value")
                )
            )
        }

        return TeamStatistics(
            team = Team(
                id = teamObj.getInt("id"),
                name = teamObj.getString("name"),
                logo = teamObj.getString("logo"),
                winner = null
            ),
            statistics = statistics
        )
    }

    private fun parseApiSportsEvent(json: JSONObject): MatchEvent {
        val timeObj = json.getJSONObject("time")
        val teamObj = json.getJSONObject("team")
        val playerObj = json.getJSONObject("player")
        val assistObj = json.optJSONObject("assist")

        return MatchEvent(
            time = EventTime(
                elapsed = timeObj.getInt("elapsed"),
                extra = if (timeObj.isNull("extra")) null else timeObj.getInt("extra")
            ),
            team = Team(
                id = teamObj.getInt("id"),
                name = teamObj.getString("name"),
                logo = teamObj.getString("logo"),
                winner = null
            ),
            player = Player(
                id = playerObj.getInt("id"),
                name = playerObj.getString("name")
            ),
            assist = assistObj?.let {
                Player(
                    id = it.getInt("id"),
                    name = it.getString("name")
                )
            },
            type = json.getString("type"),
            detail = json.getString("detail"),
            comments = json.optString("comments", null)
        )
    }

    private fun parseApiSportsLineup(json: JSONObject): TeamLineup {
        val teamObj = json.getJSONObject("team")
        val coachObj = json.getJSONObject("coach")
        val startXIArray = json.getJSONArray("startXI")
        val substitutesArray = json.getJSONArray("substitutes")

        val startXI = mutableListOf<LineupPlayer>()
        for (i in 0 until startXIArray.length()) {
            val playerJson = startXIArray.getJSONObject(i).getJSONObject("player")
            startXI.add(
                LineupPlayer(
                    player = PlayerDetails(
                        id = playerJson.getInt("id"),
                        name = playerJson.getString("name"),
                        number = playerJson.getInt("number"),
                        pos = playerJson.getString("pos"),
                        grid = playerJson.optString("grid", null)
                    )
                )
            )
        }

        val substitutes = mutableListOf<LineupPlayer>()
        for (i in 0 until substitutesArray.length()) {
            val playerJson = substitutesArray.getJSONObject(i).getJSONObject("player")
            substitutes.add(
                LineupPlayer(
                    player = PlayerDetails(
                        id = playerJson.getInt("id"),
                        name = playerJson.getString("name"),
                        number = playerJson.getInt("number"),
                        pos = playerJson.getString("pos"),
                        grid = playerJson.optString("grid", null)
                    )
                )
            )
        }

        return TeamLineup(
            team = Team(
                id = teamObj.getInt("id"),
                name = teamObj.getString("name"),
                logo = teamObj.getString("logo"),
                winner = null
            ),
            formation = json.getString("formation"),
            startXI = startXI,
            substitutes = substitutes,
            coach = Coach(
                id = coachObj.getInt("id"),
                name = coachObj.getString("name"),
                photo = coachObj.getString("photo")
            )
        )
    }

    private fun parseApiSportsPrediction(json: JSONObject): Prediction {
        val predictionsObj = json.getJSONObject("predictions")
        val leagueObj = json.getJSONObject("league")
        val teamsObj = json.getJSONObject("teams")

        return Prediction(
            predictions = PredictionDetails(
                winner = predictionsObj.optJSONObject("winner")?.let {
                    Winner(
                        id = it.getInt("id"),
                        name = it.getString("name"),
                        comment = it.getString("comment")
                    )
                },
                winOrDraw = predictionsObj.optBoolean("win_or_draw"),
                underOver = predictionsObj.optString("under_over"),
                goals = predictionsObj.optJSONObject("goals")?.let {
                    GoalsPrediction(
                        home = it.getString("home"),
                        away = it.getString("away")
                    )
                },
                advice = predictionsObj.optString("advice"),
                percent = predictionsObj.optJSONObject("percent")?.let {
                    PredictionPercent(
                        home = it.getString("home"),
                        draw = it.getString("draw"),
                        away = it.getString("away")
                    )
                }
            ),
            league = parseLeague(leagueObj),
            teams = parseTeams(teamsObj)
        )
    }

    private fun parseApiSportsStanding(json: JSONObject): LeagueStanding {
        val leagueObj = json.getJSONObject("league")
        val standingsArray = leagueObj.getJSONArray("standings")

        val standings = mutableListOf<List<Standing>>()
        for (i in 0 until standingsArray.length()) {
            val groupArray = standingsArray.getJSONArray(i)
            val group = mutableListOf<Standing>()

            for (j in 0 until groupArray.length()) {
                val standingJson = groupArray.getJSONObject(j)
                group.add(parseStanding(standingJson))
            }
            standings.add(group)
        }

        return LeagueStanding(
            league = StandingLeague(
                id = leagueObj.getInt("id"),
                name = leagueObj.getString("name"),
                country = leagueObj.getString("country"),
                logo = leagueObj.getString("logo"),
                flag = leagueObj.optString("flag", ""),
                season = leagueObj.getInt("season"),
                standings = standings
            )
        )
    }

    private fun parseStanding(json: JSONObject): Standing {
        val teamObj = json.getJSONObject("team")
        val allObj = json.getJSONObject("all")
        val homeObj = json.getJSONObject("home")
        val awayObj = json.getJSONObject("away")

        return Standing(
            rank = json.getInt("rank"),
            team = Team(
                id = teamObj.getInt("id"),
                name = teamObj.getString("name"),
                logo = teamObj.getString("logo"),
                winner = null
            ),
            points = json.getInt("points"),
            goalsDiff = json.getInt("goalsDiff"),
            group = json.getString("group"),
            form = json.getString("form"),
            status = json.getString("status"),
            description = json.optString("description"),
            all = parseStandingStats(allObj),
            home = parseStandingStats(homeObj),
            away = parseStandingStats(awayObj),
            update = json.getString("update")
        )
    }

    private fun parseApiSportsTopScorer(json: JSONObject): TopScorerEntry {
        val playerObj = json.getJSONObject("player")
        val statisticsArray = json.getJSONArray("statistics")

        val statistics = mutableListOf<TopScorerStatistics>()
        for (i in 0 until statisticsArray.length()) {
            val statJson = statisticsArray.getJSONObject(i)
            statistics.add(parseTopScorerStatistics(statJson))
        }

        return TopScorerEntry(
            player = TopScorerPlayer(
                id = playerObj.getInt("id"),
                name = playerObj.getString("name"),
                firstname = playerObj.optString("firstname", null),
                lastname = playerObj.optString("lastname", null),
                age = playerObj.optInt("age"),
                birth = playerObj.optJSONObject("birth")?.let {
                    PlayerBirth(
                        date = it.optString("date", null),
                        place = it.optString("place", null),
                        country = it.optString("country", null)
                    )
                },
                nationality = playerObj.optString("nationality", null),
                height = playerObj.optString("height", null),
                weight = playerObj.optString("weight", null),
                injured = playerObj.optBoolean("injured"),
                photo = playerObj.getString("photo")
            ),
            statistics = statistics
        )
    }

    private fun parseTopScorerStatistics(json: JSONObject): TopScorerStatistics {
        val teamObj = json.getJSONObject("team")
        val leagueObj = json.getJSONObject("league")
        val gamesObj = json.getJSONObject("games")
        val goalsObj = json.getJSONObject("goals")

        return TopScorerStatistics(
            team = Team(
                id = teamObj.getInt("id"),
                name = teamObj.getString("name"),
                logo = teamObj.getString("logo"),
                winner = null
            ),
            league = League(
                id = leagueObj.getInt("id"),
                name = leagueObj.getString("name"),
                country = leagueObj.getString("country"),
                logo = leagueObj.getString("logo"),
                flag = leagueObj.optString("flag"),
                season = leagueObj.getInt("season"),
                round = null
            ),
            games = TopScorerGames(
                appearances = gamesObj.optInt("appearences"),
                lineups = gamesObj.optInt("lineups"),
                minutes = gamesObj.optInt("minutes"),
                number = gamesObj.optInt("number"),
                position = gamesObj.optString("position", null),
                rating = gamesObj.optString("rating", null),
                captain = gamesObj.optBoolean("captain")
            ),
            goals = TopScorerGoals(
                total = goalsObj.optInt("total"),
                conceded = goalsObj.optInt("conceded"),
                assists = goalsObj.optInt("assists"),
                saves = goalsObj.optInt("saves")
            ),
            assists = json.optJSONObject("assists")?.let {
                TopScorerAssists(total = it.optInt("total"))
            },
            rating = json.optString("rating", null)
        )
    }

    private fun parseStandingStats(json: JSONObject): StandingStats {
        val goalsObj = json.getJSONObject("goals")

        return StandingStats(
            played = json.getInt("played"),
            win = json.getInt("win"),
            draw = json.getInt("draw"),
            lose = json.getInt("lose"),
            goals = StandingGoals(
                goalsFor = goalsObj.getInt("for"),
                against = goalsObj.getInt("against")
            )
        )
    }

    private fun parseLeague(json: JSONObject): League {
        return League(
            id = json.getInt("id"),
            name = json.getString("name"),
            country = json.getString("country"),
            logo = json.getString("logo"),
            flag = json.optString("flag"),
            season = json.getInt("season"),
            round = json.optString("round")
        )
    }

    private fun parseTeams(json: JSONObject): Teams {
        return Teams(
            home = parseTeam(json.getJSONObject("home")),
            away = parseTeam(json.getJSONObject("away"))
        )
    }

    private fun parseTeam(json: JSONObject): Team {
        return Team(
            id = json.getInt("id"),
            name = json.getString("name"),
            logo = json.getString("logo"),
            winner = json.optBoolean("winner")
        )
    }

// ============================================================================
// FOOTBALL-DATA.ORG METHODS
// ============================================================================

    private fun getFootballDataLiveMatches(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int?
    ): Result<List<Fixture>> {
        val mappedLeagueId = leagueId?.let { LEAGUE_MAPPINGS[it] } ?: leagueId

        val url = if (mappedLeagueId != null) {
            "${config.baseUrl}/competitions/$mappedLeagueId/matches?status=LIVE,IN_PLAY"
        } else {
            "${config.baseUrl}/matches?status=LIVE,IN_PLAY"
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", config.apiKey)
            .build()

        return executeFootballDataRequest(request)
    }

    private fun getFootballDataMatchesByDate(
        config: ApiConfigManager.ApiConfig,
        date: String,
        leagueId: Int?
    ): Result<List<Fixture>> {
        return try {
            if (leagueId != null) {
                // Specific league requested - use competition-specific endpoint
                val mappedLeagueId = LEAGUE_MAPPINGS[leagueId] ?: leagueId
                getSingleLeagueMatches(config, date, mappedLeagueId)
            } else {
                // "All Leagues" - query multiple major leagues
                getMultipleLeaguesMatches(config, date)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Football-Data request failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get matches for a single specific league
     */
    private fun getSingleLeagueMatches(
        config: ApiConfigManager.ApiConfig,
        date: String,
        leagueId: Int
    ): Result<List<Fixture>> {
        val url = "${config.baseUrl}/competitions/$leagueId/matches?dateFrom=$date&dateTo=$date"

        Log.d(TAG, "🔍 Football-Data Single League Request:")
        Log.d(TAG, "   📅 Date: $date")
        Log.d(TAG, "   🏆 League ID: $leagueId")
        Log.d(TAG, "   🔗 URL: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", config.apiKey)
            .build()

        return executeFootballDataRequest(request)
    }

    /**
     * Get matches for ALL major leagues (when "All Leagues" is selected)
     * Queries multiple leagues and combines results
     */
    private fun getMultipleLeaguesMatches(
        config: ApiConfigManager.ApiConfig,
        date: String
    ): Result<List<Fixture>> {
        // Major European leagues supported by Football-Data free tier
        val leagues = listOf(
            2021,  // Premier League
            2014,  // La Liga
            2002,  // Bundesliga
            2019,  // Serie A
            2015,  // Ligue 1
            2001   // Champions League
        )

        Log.d(TAG, "🔍 Football-Data Multi-League Request:")
        Log.d(TAG, "   📅 Date: $date")
        Log.d(TAG, "   🏆 Querying ${leagues.size} leagues")

        val allFixtures = mutableListOf<Fixture>()
        var successCount = 0
        var errorCount = 0

        // Query each league separately
        for (leagueId in leagues) {
            try {
                val url = "${config.baseUrl}/competitions/$leagueId/matches?dateFrom=$date&dateTo=$date"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("X-Auth-Token", config.apiKey)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val matchesArray = json.optJSONArray("matches")

                    if (matchesArray != null) {
                        for (i in 0 until matchesArray.length()) {
                            try {
                                val matchJson = matchesArray.getJSONObject(i)
                                allFixtures.add(parseFootballDataMatch(matchJson))
                            } catch (e: Exception) {
                                Log.w(TAG, "⚠️ Error parsing match from league $leagueId: ${e.message}")
                            }
                        }
                        successCount++
                        Log.d(TAG, "   ✅ League $leagueId: ${matchesArray.length()} matches")
                    } else {
                        Log.w(TAG, "   ⚠️ League $leagueId: No matches array in response")
                    }
                } else {
                    errorCount++
                    Log.w(TAG, "   ⚠️ League $leagueId: Error ${response.code}")

                    // Check if it's a rate limit error
                    if (response.code == 429 || body?.contains("request limit") == true) {
                        Log.e(TAG, "   🚨 Rate limit hit! Stopping multi-league query")
                        break  // Stop querying more leagues if we hit rate limit
                    }
                }

                // Small delay between requests to avoid rate limiting
                if (leagueId != leagues.last()) {
                    Thread.sleep(100)  // 100ms delay between requests
                }

            } catch (e: Exception) {
                errorCount++
                Log.w(TAG, "   ❌ League $leagueId error: ${e.message}")
            }
        }

        Log.d(TAG, "📊 Multi-league query complete:")
        Log.d(TAG, "   ✅ Success: $successCount leagues")
        Log.d(TAG, "   ❌ Errors: $errorCount leagues")
        Log.d(TAG, "   ⚽ Total matches: ${allFixtures.size}")

        return Result.success(allFixtures)
    }

    private fun getFootballDataFixtureById(
        config: ApiConfigManager.ApiConfig,
        fixtureId: Int
    ): Result<List<Fixture>> {
        val url = "${config.baseUrl}/matches/$fixtureId"

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", config.apiKey)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val fixture = parseFootballDataMatch(json)

            Result.success(listOf(fixture))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fixture", e)
            Result.failure(e)
        }
    }

    private fun getFootballDataH2H(
        config: ApiConfigManager.ApiConfig,
        team1Id: Int,
        team2Id: Int
    ): Result<List<Fixture>> {
        return Result.success(emptyList())
    }

    private fun getFootballDataStandings(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int
    ): Result<List<LeagueStanding>> {
        val mappedLeagueId = LEAGUE_MAPPINGS[leagueId] ?: leagueId
        val url = "${config.baseUrl}/competitions/$mappedLeagueId/standings"

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", config.apiKey)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val standingsArray = json.getJSONArray("standings")

            val standings = mutableListOf<LeagueStanding>()
            for (i in 0 until standingsArray.length()) {
                val standingJson = standingsArray.getJSONObject(i)
                standings.add(parseFootballDataStanding(standingJson, json.getJSONObject("competition")))
            }

            Log.d(TAG, "✅ Loaded standings from Football-Data")
            Result.success(standings)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading standings", e)
            Result.failure(e)
        }
    }

    private fun getFootballDataTopScorers(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int,
        season: Int
    ): Result<List<TopScorerEntry>> {
        val mappedLeagueId = LEAGUE_MAPPINGS[leagueId] ?: leagueId
        val url = "${config.baseUrl}/competitions/$mappedLeagueId/scorers"

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", config.apiKey)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code}"))
            }

            val json = JSONObject(body ?: "")
            val scorersArray = json.getJSONArray("scorers")

            val topScorers = mutableListOf<TopScorerEntry>()
            for (i in 0 until scorersArray.length()) {
                val scorerJson = scorersArray.getJSONObject(i)
                topScorers.add(parseFootballDataTopScorer(scorerJson))
            }

            Log.d(TAG, "✅ Loaded ${topScorers.size} top scorers from Football-Data")
            Result.success(topScorers)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading top scorers", e)
            Result.failure(e)
        }
    }

    private fun getFootballDataLeagueFixtures(
        config: ApiConfigManager.ApiConfig,
        leagueId: Int,
        from: String?,
        to: String?
    ): Result<List<Fixture>> {
        val mappedLeagueId = LEAGUE_MAPPINGS[leagueId] ?: leagueId

        val url = buildString {
            append("${config.baseUrl}/competitions/$mappedLeagueId/matches")

            val params = mutableListOf<String>()
            if (from != null) params.add("dateFrom=$from")
            if (to != null) params.add("dateTo=$to")

            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }

        Log.d(TAG, "🔗 Fetching fixtures: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", config.apiKey)
            .build()

        return executeFootballDataRequest(request)
    }

    /**
     * Execute a single Football-Data request
     * SINGLE VERSION - handles all Football-Data requests
     */
    private fun executeFootballDataRequest(request: Request): Result<List<Fixture>> {
        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d(TAG, "📥 Football-Data Response Code: ${response.code}")

            if (!response.isSuccessful) {
                // Parse error message if available
                if (body != null) {
                    try {
                        val errorJson = JSONObject(body)
                        if (errorJson.has("message")) {
                            val errorMsg = errorJson.getString("message")
                            Log.e(TAG, "❌ Football-Data Error: $errorMsg")
                            return Result.failure(Exception("Football-Data API Error: $errorMsg"))
                        }
                    } catch (e: Exception) {
                        // Body wasn't JSON
                    }
                }
                return Result.failure(Exception("API Error: ${response.code} - ${response.message}"))
            }

            val json = JSONObject(body ?: "{}")
            val matchesArray = json.optJSONArray("matches")

            if (matchesArray == null) {
                Log.e(TAG, "❌ No 'matches' array in response")
                return Result.failure(Exception("Invalid response format"))
            }

            val fixtures = mutableListOf<Fixture>()
            for (i in 0 until matchesArray.length()) {
                try {
                    val matchJson = matchesArray.getJSONObject(i)
                    fixtures.add(parseFootballDataMatch(matchJson))
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error parsing match $i: ${e.message}")
                }
            }

            Log.d(TAG, "✅ Parsed ${fixtures.size} fixtures")
            Result.success(fixtures)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Request failed", e)
            Result.failure(e)
        }
    }
    // ============================================================================
    // FOOTBALL-DATA.ORG PARSING METHODS
    // ============================================================================

    private fun parseFootballDataMatch(json: JSONObject): Fixture {
        val competitionObj = json.getJSONObject("competition")
        val homeTeamObj = json.getJSONObject("homeTeam")
        val awayTeamObj = json.getJSONObject("awayTeam")
        val scoreObj = json.getJSONObject("score")

        // FIX: Extract year from startDate string instead of trying to get it as int
        val seasonObj = json.getJSONObject("season")
        val startDate = seasonObj.getString("startDate")  // e.g., "2025-08-15"
        val seasonYear = startDate.substring(0, 4).toInt()  // Extract "2025" and convert to int

        return Fixture(
            fixture = FixtureDetails(
                id = json.getInt("id"),
                referee = json.optString("referee", null),
                timezone = "UTC",
                date = json.getString("utcDate"),
                timestamp = 0,
                venue = Venue(null, null, null),
                status = MatchStatus(
                    long = json.getString("status"),
                    short = mapFootballDataStatus(json.getString("status")),
                    elapsed = json.optInt("minute")
                )
            ),
            league = League(
                id = competitionObj.getInt("id"),
                name = competitionObj.getString("name"),
                country = "",
                logo = competitionObj.optString("emblem", ""),
                flag = null,
                season = seasonYear,  // Use the extracted year
                round = json.optString("matchday", null)
            ),
            teams = Teams(
                home = Team(
                    id = homeTeamObj.getInt("id"),
                    name = homeTeamObj.getString("name"),
                    logo = homeTeamObj.optString("crest", ""),
                    winner = null
                ),
                away = Team(
                    id = awayTeamObj.getInt("id"),
                    name = awayTeamObj.getString("name"),
                    logo = awayTeamObj.optString("crest", ""),
                    winner = null
                )
            ),
            goals = Goals(
                home = scoreObj.getJSONObject("fullTime").optInt("home"),
                away = scoreObj.getJSONObject("fullTime").optInt("away")
            ),
            score = Score(
                halftime = Goals(
                    home = scoreObj.getJSONObject("halfTime").optInt("home"),
                    away = scoreObj.getJSONObject("halfTime").optInt("away")
                ),
                fulltime = Goals(
                    home = scoreObj.getJSONObject("fullTime").optInt("home"),
                    away = scoreObj.getJSONObject("fullTime").optInt("away")
                ),
                extratime = scoreObj.optJSONObject("extraTime")?.let {
                    Goals(
                        home = it.optInt("home"),
                        away = it.optInt("away")
                    )
                },
                penalty = scoreObj.optJSONObject("penalties")?.let {
                    Goals(
                        home = it.optInt("home"),
                        away = it.optInt("away")
                    )
                }
            )
        )
    }

    private fun parseFootballDataStanding(json: JSONObject, competitionObj: JSONObject): LeagueStanding {
        val tableArray = json.getJSONArray("table")

        val standings = mutableListOf<Standing>()
        for (i in 0 until tableArray.length()) {
            val entryJson = tableArray.getJSONObject(i)
            val teamObj = entryJson.getJSONObject("team")

            standings.add(
                Standing(
                    rank = entryJson.getInt("position"),
                    team = Team(
                        id = teamObj.getInt("id"),
                        name = teamObj.getString("name"),
                        logo = teamObj.optString("crest", ""),
                        winner = null
                    ),
                    points = entryJson.getInt("points"),
                    goalsDiff = entryJson.getInt("goalDifference"),
                    group = json.optString("group", "Overall"),
                    form = entryJson.optString("form", ""),
                    status = "",
                    description = null,
                    all = StandingStats(
                        played = entryJson.getInt("playedGames"),
                        win = entryJson.getInt("won"),
                        draw = entryJson.getInt("draw"),
                        lose = entryJson.getInt("lost"),
                        goals = StandingGoals(
                            goalsFor = entryJson.getInt("goalsFor"),
                            against = entryJson.getInt("goalsAgainst")
                        )
                    ),
                    home = StandingStats(
                        played = 0, win = 0, draw = 0, lose = 0,
                        goals = StandingGoals(0, 0)
                    ),
                    away = StandingStats(
                        played = 0, win = 0, draw = 0, lose = 0,
                        goals = StandingGoals(0, 0)
                    ),
                    update = ""
                )
            )
        }

        return LeagueStanding(
            league = StandingLeague(
                id = competitionObj.getInt("id"),
                name = competitionObj.getString("name"),
                country = "",
                logo = competitionObj.optString("emblem", ""),
                flag = "",
                season = 2024,
                standings = listOf(standings)
            )
        )
    }

    private fun parseFootballDataTopScorer(json: JSONObject): TopScorerEntry {
        val playerObj = json.getJSONObject("player")
        val teamObj = json.getJSONObject("team")
        val goals = json.getInt("goals")
        val assists = json.optInt("assists", 0)
        val playedMatches = json.optInt("playedMatches", 0)

        return TopScorerEntry(
            player = TopScorerPlayer(
                id = playerObj.getInt("id"),
                name = playerObj.getString("name"),
                firstname = playerObj.optString("firstName", null),
                lastname = playerObj.optString("lastName", null),
                age = null,
                birth = playerObj.optJSONObject("dateOfBirth")?.let {
                    PlayerBirth(
                        date = it.optString("dateOfBirth", null),
                        place = null,
                        country = playerObj.optString("nationality", null)
                    )
                },
                nationality = playerObj.optString("nationality", null),
                height = null,
                weight = null,
                injured = false,
                photo = ""
            ),
            statistics = listOf(
                TopScorerStatistics(
                    team = Team(
                        id = teamObj.getInt("id"),
                        name = teamObj.getString("name"),
                        logo = teamObj.optString("crest", ""),
                        winner = null
                    ),
                    league = League(
                        id = 0,
                        name = "",
                        country = "",
                        logo = "",
                        flag = null,
                        season = 2024,
                        round = null
                    ),
                    games = TopScorerGames(
                        appearances = playedMatches,
                        lineups = playedMatches,
                        minutes = null,
                        number = null,
                        position = playerObj.optString("position", null),
                        rating = null,
                        captain = false
                    ),
                    goals = TopScorerGoals(
                        total = goals,
                        conceded = null,
                        assists = assists,
                        saves = null
                    ),
                    assists = TopScorerAssists(total = assists),
                    rating = null
                )
            )
        )
    }

    private fun mapFootballDataStatus(status: String): String {
        return when (status) {
            "SCHEDULED", "TIMED" -> "NS"
            "IN_PLAY", "LIVE" -> "LIVE"
            "PAUSED" -> "HT"
            "FINISHED" -> "FT"
            "POSTPONED" -> "PST"
            "CANCELLED" -> "CANC"
            "SUSPENDED" -> "ABD"
            else -> "NS"
        }
    }
}