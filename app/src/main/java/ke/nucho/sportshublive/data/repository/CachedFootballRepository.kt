package ke.nucho.sportshublive.data.repository

import android.util.Log
import com.google.firebase.firestore.Source
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ke.nucho.sportshublive.data.api.ApiConfigManager
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager.Companion.DATE_FIXTURES_CACHE
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager.Companion.LEAGUE_FIXTURES_CACHE
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager.Companion.LIVE_MATCHES_CACHE
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager.Companion.STANDINGS_CACHE
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager.Companion.STATISTICS_CACHE
import ke.nucho.sportshublive.data.cache.FirestoreCacheManager.Companion.TOP_SCORERS_CACHE
import ke.nucho.sportshublive.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CachedFootballRepository(
    private val apiConfigManager: ApiConfigManager
) {
    private val unifiedRepo = UnifiedFootballRepository(apiConfigManager)
    private val cacheManager = FirestoreCacheManager()
    private val gson = Gson()

    companion object {
        private const val TAG = "CachedFootballRepo"
        private const val SPORT = "football"
    }

    // ============================================================================
    // CACHED API METHODS
    // ============================================================================

    /**
     * Get live matches with smart caching (30 second cache)
     */
    suspend fun getLiveMatches(
        leagueId: Int? = null,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "live",
                params = if (leagueId != null) mapOf("league" to leagueId.toString()) else emptyMap()
            ),
            cacheDuration = LIVE_MATCHES_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<Fixture>>() {}.type,
            fetchBlock = {
                unifiedRepo.getLiveMatchesHybrid(leagueId)
            }
        )
    }

    /**
     * Get matches by date with 1-hour caching (historical data)
     */
    suspend fun getMatchesByDate(
        date: String,
        leagueId: Int? = null,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "matches_date",
                params = buildMap {
                    put("date", date)
                    if (leagueId != null) put("league", leagueId.toString())
                }
            ),
            cacheDuration = DATE_FIXTURES_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<Fixture>>() {}.type,
            fetchBlock = {
                unifiedRepo.getMatchesByDateFootballData(date, leagueId)
            }
        )
    }

    /**
     * Get fixture by ID with caching
     */
    suspend fun getFixtureById(
        fixtureId: Int,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "fixture",
                params = mapOf("id" to fixtureId.toString())
            ),
            cacheDuration = STATISTICS_CACHE,  // 1 hour for match details
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<Fixture>>() {}.type,
            fetchBlock = {
                unifiedRepo.getFixtureByIdHybrid(fixtureId)
            }
        )
    }

    /**
     * Get league fixtures with caching
     */
    suspend fun getLeagueFixtures(
        leagueId: Int,
        season: Int,
        from: String? = null,
        to: String? = null,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "league_fixtures",
                params = buildMap {
                    put("league", leagueId.toString())
                    put("season", season.toString())
                    if (from != null) put("from", from)
                    if (to != null) put("to", to)
                }
            ),
            cacheDuration = LEAGUE_FIXTURES_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<Fixture>>() {}.type,
            fetchBlock = {
                unifiedRepo.getLeagueFixturesHybrid(leagueId, season, from, to)
            }
        )
    }

    /**
     * Get standings with caching (30 minutes)
     */
    suspend fun getStandings(
        leagueId: Int,
        season: Int,
        forceRefresh: Boolean = false
    ): Result<List<LeagueStanding>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "standings",
                params = mapOf(
                    "league" to leagueId.toString(),
                    "season" to season.toString()
                )
            ),
            cacheDuration = STANDINGS_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<LeagueStanding>>() {}.type,
            fetchBlock = {
                unifiedRepo.getStandingsHybrid(leagueId, season)
            }
        )
    }

    /**
     * Get top scorers with caching (30 minutes)
     */
    suspend fun getTopScorers(
        leagueId: Int,
        season: Int,
        forceRefresh: Boolean = false
    ): Result<List<TopScorerEntry>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "top_scorers",
                params = mapOf(
                    "league" to leagueId.toString(),
                    "season" to season.toString()
                )
            ),
            cacheDuration = TOP_SCORERS_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<TopScorerEntry>>() {}.type,
            fetchBlock = {
                unifiedRepo.getTopScorersHybrid(leagueId, season)
            }
        )
    }

    /**
     * Get match statistics with caching
     */
    suspend fun getMatchStatistics(
        fixtureId: Int,
        forceRefresh: Boolean = false
    ): Result<List<TeamStatistics>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "statistics",
                params = mapOf("fixture" to fixtureId.toString())
            ),
            cacheDuration = STATISTICS_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<TeamStatistics>>() {}.type,
            fetchBlock = {
                unifiedRepo.getMatchStatisticsHybrid(fixtureId)
            }
        )
    }

    /**
     * Get match events with caching
     */
    suspend fun getMatchEvents(
        fixtureId: Int,
        forceRefresh: Boolean = false
    ): Result<List<MatchEvent>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "events",
                params = mapOf("fixture" to fixtureId.toString())
            ),
            cacheDuration = STATISTICS_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<MatchEvent>>() {}.type,
            fetchBlock = {
                unifiedRepo.getMatchEventsHybrid(fixtureId)
            }
        )
    }

    /**
     * Get match lineups with caching
     */
    suspend fun getMatchLineups(
        fixtureId: Int,
        forceRefresh: Boolean = false
    ): Result<List<TeamLineup>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "lineups",
                params = mapOf("fixture" to fixtureId.toString())
            ),
            cacheDuration = STATISTICS_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<TeamLineup>>() {}.type,
            fetchBlock = {
                unifiedRepo.getMatchLineupsHybrid(fixtureId)
            }
        )
    }

    /**
     * Get head-to-head matches with caching
     */
    suspend fun getHeadToHead(
        team1Id: Int,
        team2Id: Int,
        forceRefresh: Boolean = false
    ): Result<List<Fixture>> {
        return getCachedOrFetch(
            cacheKey = cacheManager.generateCacheKey(
                sport = SPORT,
                endpoint = "h2h",
                params = mapOf(
                    "team1" to team1Id.toString(),
                    "team2" to team2Id.toString()
                )
            ),
            cacheDuration = STATISTICS_CACHE,
            forceRefresh = forceRefresh,
            typeToken = object : TypeToken<List<Fixture>>() {}.type,
            fetchBlock = {
                unifiedRepo.getHeadToHeadHybrid(team1Id, team2Id)
            }
        )
    }

    // ============================================================================
    // CACHE MANAGEMENT METHODS
    // ============================================================================

    /**
     * Clear all expired cache entries
     */
    suspend fun clearExpiredCache(): Int {
        return withContext(Dispatchers.IO) {
            cacheManager.clearExpiredCache()
        }
    }

    /**
     * Clear all cache (for logout/reset)
     */
    suspend fun clearAllCache(): Int {
        return withContext(Dispatchers.IO) {
            cacheManager.clearAllCache()
        }
    }

    /**
     * Invalidate specific cache pattern
     * Example: invalidateCache("live") clears all live match caches
     */
    suspend fun invalidateCache(pattern: String): Int {
        return withContext(Dispatchers.IO) {
            cacheManager.invalidateCachePattern(pattern)
        }
    }

    /**
     * Get cache statistics
     */
    suspend fun getCacheStats() = cacheManager.getCacheStats()

    // ============================================================================
    // CORE CACHE LOGIC (Generic Method)
    // ============================================================================

    /**
     * Generic cache-or-fetch method
     *
     * Flow:
     * 1. Check cache (works offline!)
     * 2. Return if valid cache found
     * 3. Fetch from API if cache miss
     * 4. Save to cache
     * 5. Return data
     */
    private suspend fun <T> getCachedOrFetch(
        cacheKey: String,
        cacheDuration: Long,
        forceRefresh: Boolean,
        typeToken: java.lang.reflect.Type,
        fetchBlock: suspend () -> Result<T>
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                // If force refresh, skip cache and fetch directly
                if (forceRefresh) {
                    Log.d(TAG, "🔄 Force refresh: $cacheKey")
                    return@withContext fetchAndCache(cacheKey, cacheDuration, typeToken, fetchBlock)
                }

                // Try to get from cache first (offline-first!)
                val cachedData = try {
                    val jsonString = cacheManager.getCachedData(
                        key = cacheKey,
                        clazz = String::class.java,
                        source = Source.CACHE  // Offline-first
                    )

                    if (jsonString != null) {
                        gson.fromJson<T>(jsonString, typeToken)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Cache read error: ${e.message}")
                    null
                }

                if (cachedData != null) {
                    Log.d(TAG, "✅ Using cached data: $cacheKey")
                    return@withContext Result.success(cachedData)
                }

                // Cache miss - fetch from API
                Log.d(TAG, "⚪ Cache miss, fetching: $cacheKey")
                fetchAndCache(cacheKey, cacheDuration, typeToken, fetchBlock)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in getCachedOrFetch: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch from API and save to cache
     */
    private suspend fun <T> fetchAndCache(
        cacheKey: String,
        cacheDuration: Long,
        typeToken: java.lang.reflect.Type,
        fetchBlock: suspend () -> Result<T>
    ): Result<T> {
        return try {
            // Fetch from API
            val result = fetchBlock()

            if (result.isSuccess) {
                val data = result.getOrNull()

                if (data != null) {
                    // Save to cache (async, don't block on cache failure)
                    try {
                        val jsonString = gson.toJson(data, typeToken)
                        cacheManager.cacheData(cacheKey, jsonString, cacheDuration)
                        Log.d(TAG, "💾 Saved to cache: $cacheKey")
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Cache save failed (continuing anyway): ${e.message}")
                        // Don't fail the whole operation if caching fails
                    }
                }
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Fetch failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}