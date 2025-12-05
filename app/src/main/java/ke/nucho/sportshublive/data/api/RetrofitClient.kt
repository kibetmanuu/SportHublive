package ke.nucho.sportshublive.data.api

import android.util.Log
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
import java.util.concurrent.TimeUnit

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
}

/**
 * API-Basketball Service Interface
 * Base URL: https://v1.basketball.api-sports.io/
 */
interface BasketballApiService {
    @GET("games")
    suspend fun getLiveGames(@Query("live") live: String = "all"): retrofit2.Response<BasketballResponse>

    @GET("games")
    suspend fun getGamesByDate(@Query("date") date: String): retrofit2.Response<BasketballResponse>

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
    suspend fun getGamesByDate(@Query("date") date: String): retrofit2.Response<HockeyResponse>

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
    suspend fun getRacesByDate(@Query("date") date: String): retrofit2.Response<Formula1Response>

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
    suspend fun getGamesByDate(@Query("date") date: String): retrofit2.Response<VolleyballResponse>

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
    suspend fun getGamesByDate(@Query("date") date: String): retrofit2.Response<RugbyResponse>

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
     * API Key Interceptor with automatic retry on rate limit
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

                    when (response.code) {
                        429 -> {
                            // Rate limit exceeded
                            Log.w(TAG, "Rate limit exceeded (429) on attempt $attempt for host: $host")
                            ApiKeyManager.markKeyAsFailed(currentApiKey)

                            if (attempt < MAX_RETRIES) {
                                response.close()
                                Thread.sleep(RETRY_DELAY_MS * attempt) // Exponential backoff
                                currentApiKey = ApiKeyManager.getNextApiKey() // Get next key
                                continue
                            }
                        }
                        401, 403 -> {
                            // Invalid API key
                            Log.e(TAG, "Invalid API key (${response.code}) for host: $host")
                            ApiKeyManager.markKeyAsFailed(currentApiKey)

                            if (attempt < MAX_RETRIES) {
                                response.close()
                                currentApiKey = ApiKeyManager.getNextApiKey()
                                continue
                            }
                        }
                        in 200..299 -> {
                            // Success - mark key as working
                            ApiKeyManager.markKeyAsWorking(currentApiKey)
                            return response
                        }
                        else -> {
                            // Other errors - don't retry
                            return response
                        }
                    }

                    return response

                } catch (e: IOException) {
                    lastException = e
                    Log.e(TAG, "Network error on attempt $attempt: ${e.message}")

                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS * attempt)
                        currentApiKey = ApiKeyManager.getNextApiKey()
                    }
                }
            }

            // All retries failed
            response?.let { return it }
            throw lastException ?: IOException("Request failed after $MAX_RETRIES attempts")
        }
    }

    /**
     * Create OkHttp Client for API-Sports with specific host
     */
    private fun createApiSportsClient(host: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(host))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Create Retrofit instance
     */
    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
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