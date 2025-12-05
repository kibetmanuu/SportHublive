package ke.nucho.sportshublive.data.repository

import android.util.Log
import ke.nucho.sportshublive.data.api.RetrofitClient
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager
import ke.nucho.sportshublive.data.models.*
import retrofit2.Response

/**
 * Repository with Firestore caching
 * Checks cache first, then fetches from API if needed
 */
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

    // ==================== FOOTBALL ====================

    suspend fun getFootballLiveMatches(forceRefresh: Boolean = false): Result<List<Fixture>> {
        val cacheKey = cacheManager.generateCacheKey("football", "live")

        // Check cache first
        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, FootballResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached football live matches")
                return Result.success(cached.response ?: emptyList())
            }
        }

        // Fetch from API
        return try {
            val response = footballApi.getLiveMatches()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                // Cache the response
                val cacheDuration = cacheManager.getCacheDuration("live")
                cacheManager.cacheData(cacheKey, data, cacheDuration)

                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching football live matches", e)
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
                Log.d(TAG, "Returning cached football fixtures for date: $date")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = footballApi.getFixturesByDate(date)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("date")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching football fixtures by date", e)
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
                Log.d(TAG, "Returning cached football league fixtures")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = footballApi.getFixturesByLeague(leagueId, season)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("league")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching football league fixtures", e)
            Result.failure(e)
        }
    }

    suspend fun getFootballStandings(
        leagueId: Int,
        season: Int,
        forceRefresh: Boolean = false
    ): Result<StandingsResponse> {
        val cacheKey = cacheManager.generateCacheKey(
            "football",
            "standings",
            mapOf("league" to leagueId.toString(), "season" to season.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, StandingsResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached football standings")
                return Result.success(cached)
            }
        }

        return try {
            val response = footballApi.getStandings(leagueId, season)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("standings")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching football standings", e)
            Result.failure(e)
        }
    }

    // ==================== BASKETBALL ====================

    suspend fun getBasketballLiveGames(forceRefresh: Boolean = false): Result<List<BasketballGame>> {
        val cacheKey = cacheManager.generateCacheKey("basketball", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, BasketballResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached basketball live games")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = basketballApi.getLiveGames()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("live")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching basketball live games", e)
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
                Log.d(TAG, "Returning cached basketball games")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = basketballApi.getGamesByDate(date)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("date")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching basketball games by date", e)
            Result.failure(e)
        }
    }

    // ==================== HOCKEY ====================

    suspend fun getHockeyLiveGames(forceRefresh: Boolean = false): Result<List<HockeyGame>> {
        val cacheKey = cacheManager.generateCacheKey("hockey", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, HockeyResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached hockey live games")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = hockeyApi.getLiveGames()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("live")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hockey live games", e)
            Result.failure(e)
        }
    }

    // ==================== FORMULA 1 ====================

    suspend fun getFormula1Races(
        season: Int,
        forceRefresh: Boolean = false
    ): Result<List<F1Race>> {
        val cacheKey = cacheManager.generateCacheKey(
            "formula1",
            "races",
            mapOf("season" to season.toString())
        )

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, Formula1Response::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached F1 races")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = formula1Api.getRaces(season)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("date")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching F1 races", e)
            Result.failure(e)
        }
    }

    // ==================== VOLLEYBALL ====================

    suspend fun getVolleyballLiveGames(forceRefresh: Boolean = false): Result<List<VolleyballGame>> {
        val cacheKey = cacheManager.generateCacheKey("volleyball", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, VolleyballResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached volleyball live games")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = volleyballApi.getLiveGames()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("live")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching volleyball live games", e)
            Result.failure(e)
        }
    }

    // ==================== RUGBY ====================

    suspend fun getRugbyLiveGames(forceRefresh: Boolean = false): Result<List<RugbyGame>> {
        val cacheKey = cacheManager.generateCacheKey("rugby", "live")

        if (!forceRefresh) {
            val cached = cacheManager.getCachedData(cacheKey, RugbyResponse::class.java)
            if (cached != null) {
                Log.d(TAG, "Returning cached rugby live games")
                return Result.success(cached.response ?: emptyList())
            }
        }

        return try {
            val response = rugbyApi.getLiveGames()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val cacheDuration = cacheManager.getCacheDuration("live")
                cacheManager.cacheData(cacheKey, data, cacheDuration)
                Result.success(data.response ?: emptyList())
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching rugby live games", e)
            Result.failure(e)
        }
    }

    // ==================== CACHE MANAGEMENT ====================

    suspend fun clearExpiredCache() {
        cacheManager.clearExpiredCache()
    }

    suspend fun clearAllCache() {
        cacheManager.clearAllCache()
    }
}