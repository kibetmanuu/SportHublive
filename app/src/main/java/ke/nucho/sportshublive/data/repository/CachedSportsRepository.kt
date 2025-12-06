package ke.nucho.sportshublive.data.repository

import android.util.Log
import com.google.gson.JsonSyntaxException
import ke.nucho.sportshublive.data.api.RetrofitClient
import ke.nucho.sportshublive.data.api.ApiKeyManager
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager
import ke.nucho.sportshublive.data.models.*
import ke.nucho.sportshublive.utils.SportDataConverters
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Repository with Proper Error Handling
 * - Checks for API errors before parsing
 * - Handles account suspension gracefully
 * - Auto-adjusts F1 season for free tier
 * - Fixed Volleyball API parameters
 */
@Suppress("unused")
class CachedSportsRepository {

    private val cacheManager = FirestoreCacheManager()

    private val footballApi = RetrofitClient.footballApiService
    private val basketballApi = RetrofitClient.basketballApiService
    private val hockeyApi = RetrofitClient.hockeyApiService
    private val formula1Api = RetrofitClient.formula1ApiService
    private val volleyballApi = RetrofitClient.volleyballApiService
    private val rugbyApi = RetrofitClient.rugbyApiService

    companion object {
        private const val TAG = "CachedSportsRepository"
    }

    // ==================== CUSTOM EXCEPTIONS ====================

    class ApiAccountSuspendedException(message: String) : Exception(message)
    class ApiPlanLimitationException(message: String) : Exception(message)
    class ApiInvalidParameterException(message: String) : Exception(message)
    class ApiErrorException(message: String) : Exception(message)

    // ==================== BASKETBALL ====================

    suspend fun getBasketballLiveGames(forceRefresh: Boolean = false): Result<List<BasketballGame>> {
        val cacheKey = cacheManager.generateCacheKey("basketball", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, BasketballResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached basketball: ${cached.response?.size ?: 0}")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            Log.d(TAG, "📡 Calling Basketball API...")
            val response = basketballApi.getLiveGames()

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("live"),
                sportName = "Basketball",
                onSuccess = { data ->
                    Log.d(TAG, "✓ Basketball parsed: ${data.response?.size ?: 0} games")
                    data.response ?: emptyList()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Basketball): ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getBasketballGamesByDate(
        date: String,
        forceRefresh: Boolean = false
    ): Result<List<BasketballGame>> {
        val cacheKey = cacheManager.generateCacheKey(
            "basketball",
            "games_by_date",
            mapOf("date" to date)
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, BasketballResponse::class.java)
            if (cached != null) {
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = basketballApi.getGamesByDate(date)
            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("date"),
                sportName = "Basketball",
                onSuccess = { it.response ?: emptyList() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBasketballGameById(
        gameId: Int,
        forceRefresh: Boolean = false
    ): Result<Fixture> {
        val cacheKey = cacheManager.generateCacheKey(
            "basketball",
            "game_by_id",
            mapOf("id" to gameId.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Fixture::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached basketball game: $gameId")
                return Result.success(cached)
            }
        }

        return try {
            val response = basketballApi.getLiveGames()

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = 30_000L,
                sportName = "Basketball",
                onSuccess = { data ->
                    val game = data.response?.find { it.id == gameId }
                    if (game != null) {
                        SportDataConverters.convertBasketballToFixture(game)
                    } else {
                        throw Exception("Basketball game not found: $gameId")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Basketball by ID): ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==================== VOLLEYBALL ====================

    suspend fun getVolleyballLiveGames(forceRefresh: Boolean = false): Result<List<VolleyballGame>> {
        val cacheKey = cacheManager.generateCacheKey("volleyball", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, VolleyballResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached volleyball: ${cached.response?.size ?: 0}")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            Log.d(TAG, "📡 Calling Volleyball API (using date instead of live parameter)...")
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val response = volleyballApi.getGamesByDate(currentDate)

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("live"),
                sportName = "Volleyball",
                onSuccess = { data ->
                    Log.d(TAG, "✓ Volleyball parsed: ${data.response?.size ?: 0} games")
                    data.response ?: emptyList()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Volleyball): ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getVolleyballGameById(
        gameId: Int,
        forceRefresh: Boolean = false
    ): Result<Fixture> {
        val cacheKey = cacheManager.generateCacheKey(
            "volleyball",
            "game_by_id",
            mapOf("id" to gameId.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Fixture::class.java)
            if (cached != null) {
                return Result.success(cached)
            }
        }

        return try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val response = volleyballApi.getGamesByDate(currentDate)

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = 30_000L,
                sportName = "Volleyball",
                onSuccess = { data ->
                    val game = data.response?.find { it.id == gameId }
                    if (game != null) {
                        SportDataConverters.convertVolleyballToFixture(game)
                    } else {
                        throw Exception("Volleyball game not found: $gameId")
                    }
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== HOCKEY ====================

    suspend fun getHockeyLiveGames(forceRefresh: Boolean = false): Result<List<HockeyGame>> {
        val cacheKey = cacheManager.generateCacheKey("hockey", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, HockeyResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached hockey: ${cached.response?.size ?: 0}")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            Log.d(TAG, "📡 Calling Hockey API...")
            val response = hockeyApi.getLiveGames()

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("live"),
                sportName = "Hockey",
                onSuccess = { data ->
                    Log.d(TAG, "✓ Hockey parsed: ${data.response?.size ?: 0} games")
                    data.response ?: emptyList()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Hockey): ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getHockeyGameById(
        gameId: Int,
        forceRefresh: Boolean = false
    ): Result<Fixture> {
        val cacheKey = cacheManager.generateCacheKey(
            "hockey",
            "game_by_id",
            mapOf("id" to gameId.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Fixture::class.java)
            if (cached != null) {
                return Result.success(cached)
            }
        }

        return try {
            val response = hockeyApi.getLiveGames()

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = 30_000L,
                sportName = "Hockey",
                onSuccess = { data ->
                    val game = data.response?.find { it.id == gameId }
                    if (game != null) {
                        SportDataConverters.convertHockeyToFixture(game)
                    } else {
                        throw Exception("Hockey game not found: $gameId")
                    }
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== RUGBY ====================

    suspend fun getRugbyLiveGames(forceRefresh: Boolean = false): Result<List<RugbyGame>> {
        val cacheKey = cacheManager.generateCacheKey("rugby", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, RugbyResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached rugby: ${cached.response?.size ?: 0}")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            Log.d(TAG, "📡 Calling Rugby API...")
            val response = rugbyApi.getLiveGames()

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("live"),
                sportName = "Rugby",
                onSuccess = { data ->
                    Log.d(TAG, "✓ Rugby parsed: ${data.response?.size ?: 0} games")
                    data.response ?: emptyList()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Rugby): ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getRugbyGameById(
        gameId: Int,
        forceRefresh: Boolean = false
    ): Result<Fixture> {
        val cacheKey = cacheManager.generateCacheKey(
            "rugby",
            "game_by_id",
            mapOf("id" to gameId.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Fixture::class.java)
            if (cached != null) {
                return Result.success(cached)
            }
        }

        return try {
            val response = rugbyApi.getLiveGames()

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = 30_000L,
                sportName = "Rugby",
                onSuccess = { data ->
                    val game = data.response?.find { it.id == gameId }
                    if (game != null) {
                        SportDataConverters.convertRugbyToFixture(game)
                    } else {
                        throw Exception("Rugby game not found: $gameId")
                    }
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FORMULA 1 ====================

    suspend fun getFormula1Races(
        season: Int,
        forceRefresh: Boolean = false
    ): Result<List<F1Race>> {
        val adjustedSeason = if (season >= 2024) {
            Log.w(TAG, "⚠️ Season $season not available on free plan, using 2023 instead")
            2023
        } else {
            season
        }

        val cacheKey = cacheManager.generateCacheKey(
            "formula1",
            "races",
            mapOf("season" to adjustedSeason.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Formula1Response::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached F1: ${cached.response?.size ?: 0} races")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            Log.d(TAG, "📡 Calling Formula 1 API (season: $adjustedSeason)...")
            val response = formula1Api.getRaces(adjustedSeason)

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("date"),
                sportName = "Formula 1",
                onSuccess = { data ->
                    Log.d(TAG, "✓ F1 parsed: ${data.response?.size ?: 0} races")
                    data.response ?: emptyList()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Formula 1): ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getFormula1RaceById(
        raceId: Int,
        forceRefresh: Boolean = false
    ): Result<Fixture> {
        val cacheKey = cacheManager.generateCacheKey(
            "formula1",
            "race_by_id",
            mapOf("id" to raceId.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Fixture::class.java)
            if (cached != null) {
                return Result.success(cached)
            }
        }

        return try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val adjustedYear = if (currentYear >= 2024) 2023 else currentYear
            val response = formula1Api.getRaces(adjustedYear)

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = 300_000L,
                sportName = "Formula 1",
                onSuccess = { data ->
                    val race = data.response?.find { it.id == raceId }
                    if (race != null) {
                        SportDataConverters.convertF1ToFixture(race)
                    } else {
                        throw Exception("F1 race not found: $raceId")
                    }
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FOOTBALL ====================

    suspend fun getFootballLiveMatches(forceRefresh: Boolean = false): Result<List<Fixture>> {
        val cacheKey = cacheManager.generateCacheKey("football", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FootballResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "✓ Returning cached football: ${cached.response.size}")
                return Result.success(cached.response)
            }
        }

        return try {
            Log.d(TAG, "📡 Calling Football API...")
            val response = footballApi.getLiveMatches()

            // Check for JSON parsing errors first
            if (response.isSuccessful && response.body() == null) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Failed to parse response body for Football")
                Log.e(TAG, "Raw error body: $errorBody")
                return Result.failure(Exception("Failed to parse API response. The API may have returned an error in an unexpected format."))
            }

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("live"),
                sportName = "Football",
                onSuccess = { data ->
                    Log.d(TAG, "✓ Football parsed: ${data.response.size} fixtures")
                    data.response
                }
            )
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "❌ JSON Parsing Exception (Football): ${e.message}", e)
            Result.failure(Exception("API returned data in an unexpected format. This usually means there's an API error. Please try again later or check your API subscription."))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception (Football): ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getFootballFixturesByDate(
        date: String,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        val cacheKey = cacheManager.generateCacheKey(
            "football",
            "fixtures_by_date",
            mapOf("date" to date)
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FootballResponse::class.java)
            if (cached != null) {
                return Result.success(cached.response)
            }
        }

        return try {
            val response = footballApi.getFixturesByDate(date)
            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("date"),
                sportName = "Football",
                onSuccess = { it.response }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballFixturesByLeague(
        leagueId: Int,
        season: Int,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        val cacheKey = cacheManager.generateCacheKey(
            "football",
            "fixtures_by_league",
            mapOf("league" to leagueId.toString(), "season" to season.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FootballResponse::class.java)
            if (cached != null) {
                return Result.success(cached.response)
            }
        }

        return try {
            val response = footballApi.getFixturesByLeague(leagueId, season)
            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = cacheManager.getCacheDuration("league"),
                sportName = "Football",
                onSuccess = { it.response }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballFixtureById(fixtureId: Int, forceRefresh: Boolean = false): Result<Fixture> {
        val cacheKey = cacheManager.generateCacheKey("football", "fixture_by_id", mapOf("id" to fixtureId.toString()))

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FootballResponse::class.java)
            if (cached != null && cached.response.isNotEmpty()) {
                return Result.success(cached.response.first())
            }
        }

        return try {
            val response = footballApi.getFixtureById(fixtureId)

            handleApiResponseWithErrorCheck(
                response = response,
                cacheKey = cacheKey,
                cacheDuration = if (response.body()?.response?.firstOrNull()?.let { isMatchLive(it) } == true) 30_000L else 300_000L,
                sportName = "Football",
                onSuccess = { data ->
                    if (data.response.isNotEmpty()) {
                        data.response.first()
                    } else {
                        throw Exception("Fixture not found")
                    }
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballStandings(leagueId: Int, season: Int, forceRefresh: Boolean = false): Result<StandingsResponse> {
        val cacheKey = cacheManager.generateCacheKey("football", "standings", mapOf("league" to leagueId.toString(), "season" to season.toString()))

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, StandingsResponse::class.java)
            if (cached != null) return Result.success(cached)
        }

        return try {
            val response = footballApi.getStandings(leagueId, season)
            handleApiResponseWithErrorCheck(response, cacheKey, cacheManager.getCacheDuration("standings"), "Football") { it }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballFixtureStats(fixtureId: Int, forceRefresh: Boolean = false): Result<List<FixtureStatistics>> {
        val cacheKey = cacheManager.generateCacheKey("football", "fixture_stats", mapOf("id" to fixtureId.toString()))

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FixtureStatsResponse::class.java)
            if (cached != null) return Result.success(cached.response ?: emptyList())
        }

        return try {
            val response = footballApi.getFixtureStatistics(fixtureId)
            handleApiResponseWithErrorCheck(response, cacheKey, 30_000L, "Football") { it.response ?: emptyList() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballFixtureEvents(fixtureId: Int, forceRefresh: Boolean = false): Result<List<FixtureEvent>> {
        val cacheKey = cacheManager.generateCacheKey("football", "fixture_events", mapOf("id" to fixtureId.toString()))

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FixtureEventsResponse::class.java)
            if (cached != null) return Result.success(cached.response ?: emptyList())
        }

        return try {
            val response = footballApi.getFixtureEvents(fixtureId)
            handleApiResponseWithErrorCheck(response, cacheKey, 30_000L, "Football") { it.response ?: emptyList() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballFixtureLineups(fixtureId: Int, forceRefresh: Boolean = false): Result<List<FixtureLineup>> {
        val cacheKey = cacheManager.generateCacheKey("football", "fixture_lineups", mapOf("id" to fixtureId.toString()))

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FixtureLineupsResponse::class.java)
            if (cached != null) return Result.success(cached.response ?: emptyList())
        }

        return try {
            val response = footballApi.getFixtureLineups(fixtureId)
            handleApiResponseWithErrorCheck(response, cacheKey, 300_000L, "Football") { it.response ?: emptyList() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFootballHeadToHead(team1Id: Int, team2Id: Int, forceRefresh: Boolean = false): Result<List<Fixture>> {
        val cacheKey = cacheManager.generateCacheKey("football", "h2h", mapOf("teams" to "${team1Id}_${team2Id}"))

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FootballResponse::class.java)
            if (cached != null) return Result.success(cached.response)
        }

        return try {
            val response = footballApi.getHeadToHead("$team1Id-$team2Id")
            handleApiResponseWithErrorCheck(response, cacheKey, 3600_000L, "Football") { it.response }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ERROR CHECKING HANDLER ====================

    private suspend fun <T, R> handleApiResponseWithErrorCheck(
        response: Response<T>,
        cacheKey: String,
        cacheDuration: Long,
        sportName: String,
        onSuccess: (T) -> R
    ): Result<R> {
        return when {
            // Handle successful response with body
            response.isSuccessful && response.body() != null -> {
                val data = response.body()!!

                val errors = extractErrors(data)
                if (errors.isNotEmpty()) {
                    val errorMsg = errors.joinToString(", ")
                    Log.e(TAG, "❌ API returned errors for $sportName: $errorMsg")

                    return when {
                        errorMsg.contains("suspended", ignoreCase = true) -> {
                            Result.failure(ApiAccountSuspendedException(
                                "Your API account is suspended. Please check your subscription at https://dashboard.api-football.com"
                            ))
                        }
                        errorMsg.contains("Free plans do not have access", ignoreCase = true) -> {
                            Result.failure(ApiPlanLimitationException(
                                "This feature requires a paid plan. $errorMsg"
                            ))
                        }
                        errorMsg.contains("do not exist", ignoreCase = true) ||
                                errorMsg.contains("does not exist", ignoreCase = true) -> {
                            Result.failure(ApiInvalidParameterException(
                                "Invalid API parameter. $errorMsg"
                            ))
                        }
                        else -> {
                            Result.failure(ApiErrorException("API Error: $errorMsg"))
                        }
                    }
                }

                cacheManager.cacheData(cacheKey, data, cacheDuration)
                ApiKeyManager.markKeyAsWorking(ApiKeyManager.getCurrentApiKey())
                Result.success(onSuccess(data))
            }

            // Handle successful response but null body (parsing error)
            response.isSuccessful && response.body() == null -> {
                val errorBody = response.errorBody()?.string() ?: response.raw().toString()
                Log.e(TAG, "❌ Failed to parse response for $sportName")
                Log.e(TAG, "Raw response: $errorBody")

                // Try to extract error message from raw response
                val errorMessage = when {
                    errorBody.contains("suspended", ignoreCase = true) ->
                        "Your API account is suspended. Please check your subscription."
                    errorBody.contains("requests limit", ignoreCase = true) ||
                            errorBody.contains("quota", ignoreCase = true) ->
                        "API request limit reached. Please upgrade your plan or wait for quota reset."
                    errorBody.contains("invalid", ignoreCase = true) ->
                        "Invalid API request. Please check your parameters."
                    else ->
                        "Failed to parse API response. The API may be returning an error. Please try again later."
                }

                Result.failure(ApiErrorException(errorMessage))
            }

            response.code() == 429 -> {
                Log.w(TAG, "⚠️ Rate limit (429) for $sportName")
                ApiKeyManager.markKeyAsFailed(ApiKeyManager.getCurrentApiKey())
                Result.failure(Exception("Rate limit exceeded. Please try again later."))
            }

            response.code() == 401 || response.code() == 403 -> {
                Log.e(TAG, "❌ Auth error (${response.code()}) for $sportName")
                ApiKeyManager.markKeyAsFailed(ApiKeyManager.getCurrentApiKey())
                Result.failure(Exception("API authentication failed. Please check your API key."))
            }

            response.code() >= 500 -> {
                Log.e(TAG, "❌ Server error (${response.code()}) for $sportName")
                Result.failure(Exception("Server error. Please try again later."))
            }

            else -> {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ API error ${response.code()} for $sportName")
                Log.e(TAG, "Error body: $errorBody")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        }
    }

    private fun <T> extractErrors(data: T): List<String> {
        return try {
            val errorsField = data!!::class.java.getDeclaredField("errors")
            errorsField.isAccessible = true
            val errors = errorsField.get(data)

            when (errors) {
                // Handle array of error strings
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    errors.filterIsInstance<String>()
                }
                // Handle error object (Map)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val errorMap = errors as? Map<String, Any>
                    errorMap?.values?.mapNotNull { it.toString() } ?: emptyList()
                }
                // Handle single error string
                is String -> listOf(errors)
                // Handle null or other types
                else -> emptyList()
            }
        } catch (e: NoSuchFieldException) {
            emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract errors from response: ${e.message}")
            emptyList()
        }
    }

    // ==================== UTILITY ====================

    private fun isMatchLive(fixture: Fixture): Boolean {
        return when (fixture.fixture.status.short) {
            "1H", "2H", "HT", "ET", "P", "PEN" -> true
            else -> false
        }
    }

    suspend fun clearExpiredCache() = cacheManager.clearExpiredCache()
    suspend fun clearAllCache() = cacheManager.clearAllCache()
}