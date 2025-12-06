package ke.nucho.sportshublive.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import ke.nucho.sportshublive.data.models.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

// ==================== CUSTOM DESERIALIZERS ====================

/**
 * Custom deserializer to handle API errors that can be arrays or objects
 */
class ErrorsDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String> {
        if (json == null || json.isJsonNull) return emptyList()

        return when {
            json.isJsonArray -> {
                json.asJsonArray.mapNotNull {
                    if (it.isJsonPrimitive) it.asString else null
                }
            }
            json.isJsonObject -> {
                // If errors is an object, convert to list of error messages
                json.asJsonObject.entrySet().map { "${it.key}: ${it.value}" }
            }
            json.isJsonPrimitive -> listOf(json.asString)
            else -> emptyList()
        }
    }
}

// ==================== API INTERFACES ====================

/**
 * API-Football Service Interface
 * Base URL: https://v3.football.api-sports.io/
 */
interface FootballApiService {
    @GET("fixtures")
    suspend fun getLiveMatches(@Query("live") live: String = "all"): retrofit2.Response<FootballResponse>

    @GET("fixtures")
    suspend fun getFixturesByDate(@Query("date") date: String): retrofit2.Response<FootballResponse>

    @GET("fixtures")
    suspend fun getFixturesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<FootballResponse>

    @GET("fixtures")
    suspend fun getFixtureById(@Query("id") fixtureId: Int): retrofit2.Response<FootballResponse>

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<StandingsResponse>

    @GET("leagues")
    suspend fun getLeagues(
        @Query("country") country: String? = null,
        @Query("season") season: Int? = null
    ): retrofit2.Response<LeaguesResponse>

    @GET("teams/statistics")
    suspend fun getTeamStatistics(
        @Query("team") teamId: Int,
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<TeamStatsResponse>

    @GET("fixtures/headtohead")
    suspend fun getHeadToHead(@Query("h2h") teams: String): retrofit2.Response<FootballResponse>

    @GET("predictions")
    suspend fun getPredictions(@Query("fixture") fixtureId: Int): retrofit2.Response<PredictionsResponse>

    // ==================== MATCH DETAIL ENDPOINTS ====================

    /**
     * Get fixture statistics
     */
    @GET("fixtures/statistics")
    suspend fun getFixtureStatistics(
        @Query("fixture") fixtureId: Int
    ): retrofit2.Response<FixtureStatsResponse>

    /**
     * Get fixture events (goals, cards, substitutions)
     */
    @GET("fixtures/events")
    suspend fun getFixtureEvents(
        @Query("fixture") fixtureId: Int
    ): retrofit2.Response<FixtureEventsResponse>

    /**
     * Get fixture lineups
     */
    @GET("fixtures/lineups")
    suspend fun getFixtureLineups(
        @Query("fixture") fixtureId: Int
    ): retrofit2.Response<FixtureLineupsResponse>
}

/**
 * API-Basketball Service Interface
 * Base URL: https://v1.basketball.api-sports.io/
 */
interface BasketballApiService {
    @GET("games")
    suspend fun getLiveGames(@Query("live") live: String = "all"): retrofit2.Response<BasketballResponse>

    @GET("games")
    suspend fun getGamesByDate(
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Africa/Nairobi"
    ): retrofit2.Response<BasketballResponse>

    @GET("games")
    suspend fun getGamesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: String
    ): retrofit2.Response<BasketballResponse>

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: String
    ): retrofit2.Response<BasketballStandingsResponse>
}

/**
 * API-Hockey Service Interface
 * Base URL: https://v1.hockey.api-sports.io/
 */
interface HockeyApiService {
    @GET("games")
    suspend fun getLiveGames(@Query("live") live: String = "all"): retrofit2.Response<HockeyResponse>

    @GET("games")
    suspend fun getGamesByDate(
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Africa/Nairobi"
    ): retrofit2.Response<HockeyResponse>

    @GET("games")
    suspend fun getGamesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<HockeyResponse>

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<HockeyStandingsResponse>
}

/**
 * API-Formula1 Service Interface
 * Base URL: https://v1.formula-1.api-sports.io/
 */
interface Formula1ApiService {
    @GET("races")
    suspend fun getRaces(
        @Query("season") season: Int,
        @Query("type") type: String? = null
    ): retrofit2.Response<Formula1Response>

    @GET("races")
    suspend fun getRacesBySeason(
        @Query("season") season: Int
    ): retrofit2.Response<Formula1Response>

    @GET("rankings/drivers")
    suspend fun getDriverStandings(@Query("season") season: Int): retrofit2.Response<F1DriversResponse>
}

/**
 * API-Volleyball Service Interface
 * Base URL: https://v1.volleyball.api-sports.io/
 */
interface VolleyballApiService {
    @GET("games")
    suspend fun getLiveGames(@Query("live") live: String = "all"): retrofit2.Response<VolleyballResponse>

    @GET("games")
    suspend fun getGamesByDate(
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Africa/Nairobi"
    ): retrofit2.Response<VolleyballResponse>

    @GET("games")
    suspend fun getGamesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<VolleyballResponse>

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<VolleyballStandingsResponse>
}

/**
 * API-Rugby Service Interface
 * Base URL: https://v1.rugby.api-sports.io/
 */
interface RugbyApiService {
    @GET("games")
    suspend fun getLiveGames(@Query("live") live: String = "all"): retrofit2.Response<RugbyResponse>

    @GET("games")
    suspend fun getGamesByDate(
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Africa/Nairobi"
    ): retrofit2.Response<RugbyResponse>

    @GET("games")
    suspend fun getGamesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<RugbyResponse>

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): retrofit2.Response<RugbyStandingsResponse>
}

// ==================== RETROFIT CLIENT ====================

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val DEBUG = true

    // Base URLs for different sports
    private const val FOOTBALL_BASE_URL = "https://v3.football.api-sports.io/"
    private const val BASKETBALL_BASE_URL = "https://v1.basketball.api-sports.io/"
    private const val HOCKEY_BASE_URL = "https://v1.hockey.api-sports.io/"
    private const val FORMULA1_BASE_URL = "https://v1.formula-1.api-sports.io/"
    private const val VOLLEYBALL_BASE_URL = "https://v1.volleyball.api-sports.io/"
    private const val RUGBY_BASE_URL = "https://v1.rugby.api-sports.io/"

    // Timeouts
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // Rate limit retry settings
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L

    // Logging Interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    /**
     * Header Logging Interceptor for debugging
     */
    private val headerLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        if (DEBUG) {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "🔑 REQUEST DETAILS")
            Log.d(TAG, "URL: ${request.url}")
            Log.d(TAG, "Host Header: ${request.header("x-rapidapi-host")}")
            Log.d(TAG, "API Key: ${request.header("x-rapidapi-key")?.take(12)}...")
            Log.d(TAG, "═══════════════════════════════════════")
        }
        chain.proceed(request)
    }

    /**
     * API Key Interceptor with automatic retry on rate limit
     * FIXED VERSION - Proper retry logic
     */
    private class ApiKeyInterceptor(private val host: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var response: Response? = null
            var lastException: IOException? = null
            var currentApiKey = ApiKeyManager.getNextApiKey()

            for (attempt in 1..MAX_RETRIES) {
                try {
                    // Add headers with current API key
                    request = request.newBuilder()
                        .removeHeader("x-rapidapi-key")
                        .removeHeader("x-rapidapi-host")
                        .addHeader("x-rapidapi-key", currentApiKey)
                        .addHeader("x-rapidapi-host", host)
                        .build()

                    response?.close() // Close previous response if exists
                    response = chain.proceed(request)

                    // Log response details for debugging
                    if (DEBUG && response.code !in 200..299) {
                        try {
                            val bodyString = response.peekBody(2048).string()
                            Log.d(TAG, "📥 Response ${response.code} for $host")
                            Log.d(TAG, "Body preview: ${bodyString.take(500)}")
                        } catch (e: Exception) {
                            Log.d(TAG, "Could not peek response body: ${e.message}")
                        }
                    }

                    when (response.code) {
                        in 200..299 -> {
                            // ✅ SUCCESS - Return immediately
                            ApiKeyManager.markKeyAsWorking(currentApiKey)
                            Log.d(TAG, "✅ Success (${response.code}) for $host with key ${maskKey(currentApiKey)}")
                            return response
                        }
                        429 -> {
                            // ⚠️ RATE LIMIT
                            Log.w(TAG, "⚠️ Rate limit (429) attempt $attempt/$MAX_RETRIES for $host")
                            Log.w(TAG, "   Key: ${maskKey(currentApiKey)}")
                            ApiKeyManager.markKeyAsFailed(currentApiKey)

                            if (attempt < MAX_RETRIES) {
                                response.close()
                                val delayMs = RETRY_DELAY_MS * attempt
                                Log.d(TAG, "   Waiting ${delayMs}ms before retry...")
                                Thread.sleep(delayMs)
                                currentApiKey = ApiKeyManager.getNextApiKey()
                                Log.d(TAG, "   Retrying with key: ${maskKey(currentApiKey)}")
                                // Continue to next iteration
                            } else {
                                // Last attempt failed
                                Log.e(TAG, "❌ All retries exhausted for rate limit")
                                return response
                            }
                        }
                        401, 403 -> {
                            // ❌ AUTHENTICATION ERROR
                            Log.e(TAG, "❌ Auth error (${response.code}) attempt $attempt/$MAX_RETRIES")
                            Log.e(TAG, "   Host: $host")
                            Log.e(TAG, "   Key: ${maskKey(currentApiKey)}")

                            ApiKeyManager.markKeyAsFailed(currentApiKey)

                            if (attempt < MAX_RETRIES) {
                                response.close()
                                currentApiKey = ApiKeyManager.getNextApiKey()
                                Log.d(TAG, "   Switching to next key: ${maskKey(currentApiKey)}")
                                // Continue to next iteration
                            } else {
                                // Last attempt failed
                                Log.e(TAG, "❌ All retries exhausted for auth errors")
                                Log.e(TAG, "❌ Check your API subscription and endpoint permissions!")
                                return response
                            }
                        }
                        else -> {
                            // Other HTTP errors - don't retry
                            Log.w(TAG, "⚠️ HTTP ${response.code} for $host: ${response.message}")
                            return response
                        }
                    }

                } catch (e: IOException) {
                    lastException = e
                    Log.e(TAG, "❌ Network error attempt $attempt/$MAX_RETRIES: ${e.message}", e)

                    response?.close()

                    if (attempt < MAX_RETRIES) {
                        val delayMs = RETRY_DELAY_MS * attempt
                        Log.d(TAG, "   Waiting ${delayMs}ms before retry...")
                        Thread.sleep(delayMs)
                        currentApiKey = ApiKeyManager.getNextApiKey()
                        Log.d(TAG, "   Retrying with key: ${maskKey(currentApiKey)}")
                    } else {
                        // All retries exhausted
                        Log.e(TAG, "❌ Network request failed after $MAX_RETRIES attempts")
                        throw lastException
                    }
                }
            }

            // This should never be reached due to returns/throws above
            response?.let {
                Log.e(TAG, "⚠️ Unexpected code path - returning last response")
                return it
            }
            throw lastException ?: IOException("Request failed after $MAX_RETRIES attempts")
        }

        private fun maskKey(key: String): String {
            return if (key.length > 8) {
                "${key.take(8)}...${key.takeLast(4)}"
            } else {
                "****"
            }
        }
    }

    /**
     * Create OkHttp Client for API-Sports with specific host
     */
    private fun createApiSportsClient(host: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(headerLoggingInterceptor) // Add header logging first
            .addInterceptor(ApiKeyInterceptor(host))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Create Retrofit instance with custom Gson for error handling
     */
    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(
                object : TypeToken<List<String>>() {}.type,
                ErrorsDeserializer()
            )
            .setLenient() // Allow lenient JSON parsing
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ==================== API SERVICE INSTANCES ====================

    // Football
    val footballApiService: FootballApiService by lazy {
        val client = createApiSportsClient("v3.football.api-sports.io")
        createRetrofit(FOOTBALL_BASE_URL, client).create(FootballApiService::class.java)
    }

    // Basketball
    val basketballApiService: BasketballApiService by lazy {
        val client = createApiSportsClient("v1.basketball.api-sports.io")
        createRetrofit(BASKETBALL_BASE_URL, client).create(BasketballApiService::class.java)
    }

    // Hockey
    val hockeyApiService: HockeyApiService by lazy {
        val client = createApiSportsClient("v1.hockey.api-sports.io")
        createRetrofit(HOCKEY_BASE_URL, client).create(HockeyApiService::class.java)
    }

    // Formula 1
    val formula1ApiService: Formula1ApiService by lazy {
        val client = createApiSportsClient("v1.formula-1.api-sports.io")
        createRetrofit(FORMULA1_BASE_URL, client).create(Formula1ApiService::class.java)
    }

    // Volleyball
    val volleyballApiService: VolleyballApiService by lazy {
        val client = createApiSportsClient("v1.volleyball.api-sports.io")
        createRetrofit(VOLLEYBALL_BASE_URL, client).create(VolleyballApiService::class.java)
    }

    // Rugby
    val rugbyApiService: RugbyApiService by lazy {
        val client = createApiSportsClient("v1.rugby.api-sports.io")
        createRetrofit(RUGBY_BASE_URL, client).create(RugbyApiService::class.java)
    }
}