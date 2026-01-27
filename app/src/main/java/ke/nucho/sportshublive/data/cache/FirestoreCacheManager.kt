package ke.nucho.sportshublive.data.cache

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await


class FirestoreCacheManager {
    private val firestore: FirebaseFirestore
    private val gson = Gson()
    private val cacheCollection = "api_cache"

    companion object {
        private const val TAG = "FirestoreCache"

        // ============================================================================
        // CACHE DURATIONS - Optimized per data type
        // ============================================================================

        const val LIVE_MATCHES_CACHE = 30 * 1000L           // 30 seconds (live updates)
        const val TODAY_MATCHES_CACHE = 5 * 60 * 1000L      // 5 minutes (may have updates)
        const val DATE_FIXTURES_CACHE = 60 * 60 * 1000L     // 1 hour (historical data)
        const val LEAGUE_FIXTURES_CACHE = 60 * 60 * 1000L   // 1 hour (schedules stable)
        const val STANDINGS_CACHE = 30 * 60 * 1000L         // 30 minutes (daily updates)
        const val TOP_SCORERS_CACHE = 30 * 60 * 1000L       // 30 minutes (match day updates)
        const val STATISTICS_CACHE = 60 * 60 * 1000L        // 1 hour (post-match data)
        const val TEAM_INFO_CACHE = 24 * 60 * 60 * 1000L    // 24 hours (static data)
        const val DEFAULT_CACHE_DURATION = 5 * 60 * 1000L   // 5 minutes (default)
    }

    init {
        // Initialize Firestore with offline persistence
        firestore = FirebaseFirestore.getInstance().apply {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)  // ✅ Enable offline support
                .setCacheSizeBytes(100 * 1024 * 1024)  // 100MB cache size
                .build()

            firestoreSettings = settings

            Log.d(TAG, "✅ Firestore Cache initialized with offline persistence")
            Log.d(TAG, "   📦 Cache size: 100 MB")
            Log.d(TAG, "   📶 Offline mode: ENABLED")
        }
    }

    /**
     * Cache data in Firestore with automatic expiration
     *
     * @param key Unique cache key
     * @param data Data to cache (will be serialized to JSON)
     * @param cacheDuration How long to keep the cache (milliseconds)
     * @return true if cached successfully
     */
    suspend fun <T> cacheData(
        key: String,
        data: T,
        cacheDuration: Long = DEFAULT_CACHE_DURATION
    ): Boolean {
        return try {
            val now = System.currentTimeMillis()
            val expiresAt = now + cacheDuration

            val cacheEntry = hashMapOf(
                "key" to key,
                "data" to gson.toJson(data),
                "timestamp" to now,
                "expiresAt" to expiresAt,
                "version" to 1  // For future schema migrations
            )

            firestore.collection(cacheCollection)
                .document(key)
                .set(cacheEntry)
                .await()

            val expiresInSeconds = cacheDuration / 1000
            Log.d(TAG, "✅ Cached: $key (expires in ${expiresInSeconds}s)")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Cache write failed: $key - ${e.message}", e)
            false  // Don't crash app if caching fails
        }
    }

    /**
     * Get cached data with cache-first strategy
     *
     * Strategy:
     * 1. Try cache first (fast, works offline)
     * 2. Check if cache is valid (not expired)
     * 3. Return data or null if expired/missing
     *
     * @param key Cache key
     * @param clazz Class type for deserialization
     * @param source Cache source (CACHE for offline-first, SERVER for fresh data)
     * @return Cached data or null if expired/missing
     */
    suspend fun <T> getCachedData(
        key: String,
        clazz: Class<T>,
        source: Source = Source.CACHE  // ✅ Cache-first by default (works offline!)
    ): T? {
        return try {
            // Try to get from cache (works offline!)
            val document = firestore.collection(cacheCollection)
                .document(key)
                .get(source)  // CACHE = offline-first, SERVER = fresh from cloud
                .await()

            if (!document.exists()) {
                Log.d(TAG, "⚪ Cache miss: $key")
                return null
            }

            // Check expiration
            val expiresAt = document.getLong("expiresAt") ?: 0L
            val now = System.currentTimeMillis()

            if (expiresAt < now) {
                val expiredSeconds = (now - expiresAt) / 1000
                Log.d(TAG, "⏰ Cache expired: $key (${expiredSeconds}s ago)")

                // Delete expired cache in background
                deleteCachedData(key)
                return null
            }

            // Valid cache found!
            val dataString = document.getString("data")
            if (dataString.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ Invalid cache (empty data): $key")
                return null
            }

            val timeLeft = (expiresAt - now) / 1000
            val sourceType = if (source == Source.CACHE) "offline" else "online"
            Log.d(TAG, "✅ Cache hit ($sourceType): $key (${timeLeft}s remaining)")

            gson.fromJson(dataString, clazz)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Cache read failed: $key - ${e.message}")
            null  // Return null on error, let repository fetch from API
        }
    }

    /**
     * Try to get from cache first, then from server if not available
     * This is useful when you want offline-first but also want fresh data when online
     */
    suspend fun <T> getCachedDataWithFallback(
        key: String,
        clazz: Class<T>
    ): T? {
        // Try cache first (offline-first)
        val cached = getCachedData(key, clazz, Source.CACHE)
        if (cached != null) {
            return cached
        }

        // If cache miss and online, try server
        Log.d(TAG, "🔄 Cache miss, trying server: $key")
        return getCachedData(key, clazz, Source.SERVER)
    }

    /**
     * Delete cached data
     */
    suspend fun deleteCachedData(key: String) {
        try {
            firestore.collection(cacheCollection)
                .document(key)
                .delete()
                .await()
            Log.d(TAG, "🗑️ Deleted cache: $key")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Cache delete failed: $key - ${e.message}")
        }
    }

    /**
     * Clear all expired cache entries (run this periodically)
     */
    suspend fun clearExpiredCache(): Int {
        return try {
            val now = System.currentTimeMillis()
            val expiredDocs = firestore.collection(cacheCollection)
                .whereLessThan("expiresAt", now)
                .get()
                .await()

            if (expiredDocs.isEmpty) {
                Log.d(TAG, "✨ No expired cache to clear")
                return 0
            }

            // Delete in batches (Firestore limit: 500 per batch)
            val batch = firestore.batch()
            var count = 0

            expiredDocs.documents.forEach { doc ->
                if (count < 500) {  // Firestore batch limit
                    batch.delete(doc.reference)
                    count++
                }
            }

            batch.commit().await()
            Log.d(TAG, "🧹 Cleared $count expired cache entries")
            count

        } catch (e: Exception) {
            Log.e(TAG, "❌ Clear expired cache failed: ${e.message}")
            0
        }
    }

    /**
     * Clear ALL cache (use for logout or reset)
     */
    suspend fun clearAllCache(): Int {
        return try {
            val allDocs = firestore.collection(cacheCollection)
                .get()
                .await()

            if (allDocs.isEmpty) {
                Log.d(TAG, "✨ No cache to clear")
                return 0
            }

            val batch = firestore.batch()
            var count = 0

            allDocs.documents.forEach { doc ->
                if (count < 500) {
                    batch.delete(doc.reference)
                    count++
                }
            }

            batch.commit().await()
            Log.d(TAG, "🧹 Cleared ALL cache ($count entries)")
            count

        } catch (e: Exception) {
            Log.e(TAG, "❌ Clear all cache failed: ${e.message}")
            0
        }
    }

    /**
     * Get appropriate cache duration based on endpoint type
     */
    fun getCacheDuration(endpoint: String): Long {
        return when {
            endpoint.contains("live", ignoreCase = true) -> LIVE_MATCHES_CACHE
            endpoint.contains("today", ignoreCase = true) -> TODAY_MATCHES_CACHE
            endpoint.contains("date", ignoreCase = true) -> DATE_FIXTURES_CACHE
            endpoint.contains("fixtures", ignoreCase = true) -> LEAGUE_FIXTURES_CACHE
            endpoint.contains("standings", ignoreCase = true) -> STANDINGS_CACHE
            endpoint.contains("scorers", ignoreCase = true) -> TOP_SCORERS_CACHE
            endpoint.contains("statistics", ignoreCase = true) -> STATISTICS_CACHE
            endpoint.contains("team", ignoreCase = true) -> TEAM_INFO_CACHE
            else -> DEFAULT_CACHE_DURATION
        }
    }

    /**
     * Generate unique cache key from parameters
     * Format: sport_endpoint_param1=value1_param2=value2
     */
    fun generateCacheKey(
        sport: String,
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): String {
        val sortedParams = params.toSortedMap()
        val paramsString = if (sortedParams.isNotEmpty()) {
            "_" + sortedParams.entries.joinToString("_") { "${it.key}=${it.value}" }
        } else {
            ""
        }
        return "${sport}_${endpoint}${paramsString}"
            .replace(" ", "_")
            .replace("/", "_")
            .lowercase()
    }

    /**
     * Get cache statistics for monitoring
     */
    suspend fun getCacheStats(): CacheStats {
        return try {
            val allDocs = firestore.collection(cacheCollection)
                .get(Source.CACHE)  // Get from local cache
                .await()

            val now = System.currentTimeMillis()
            var validCount = 0
            var expiredCount = 0
            var totalSize = 0L

            allDocs.documents.forEach { doc ->
                val expiresAt = doc.getLong("expiresAt") ?: 0L
                val dataString = doc.getString("data") ?: ""

                totalSize += dataString.length

                if (expiresAt >= now) {
                    validCount++
                } else {
                    expiredCount++
                }
            }

            val stats = CacheStats(
                totalEntries = allDocs.size(),
                validEntries = validCount,
                expiredEntries = expiredCount,
                totalSizeBytes = totalSize,
                lastChecked = now
            )

            Log.d(TAG, "📊 Cache Stats:")
            Log.d(TAG, "   Total: ${stats.totalEntries}")
            Log.d(TAG, "   Valid: ${stats.validEntries}")
            Log.d(TAG, "   Expired: ${stats.expiredEntries}")
            Log.d(TAG, "   Size: ${stats.totalSizeBytes / 1024} KB")

            stats

        } catch (e: Exception) {
            Log.e(TAG, "❌ Get cache stats failed: ${e.message}")
            CacheStats(0, 0, 0, 0, System.currentTimeMillis())
        }
    }

    /**
     * Invalidate cache for a specific key pattern
     * Example: Invalidate all "football_live_*" caches
     */
    suspend fun invalidateCachePattern(pattern: String): Int {
        return try {
            val allDocs = firestore.collection(cacheCollection)
                .get()
                .await()

            val batch = firestore.batch()
            var count = 0

            allDocs.documents.forEach { doc ->
                val key = doc.getString("key") ?: ""
                if (key.contains(pattern, ignoreCase = true) && count < 500) {
                    batch.delete(doc.reference)
                    count++
                }
            }

            if (count > 0) {
                batch.commit().await()
                Log.d(TAG, "🔄 Invalidated $count caches matching: $pattern")
            }

            count
        } catch (e: Exception) {
            Log.e(TAG, "❌ Invalidate pattern failed: ${e.message}")
            0
        }
    }
}

/**
 * Cache statistics data class
 */
data class CacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val expiredEntries: Int,
    val totalSizeBytes: Long,
    val lastChecked: Long
) {
    val cacheSizeKB: Long get() = totalSizeBytes / 1024
    val cacheSizeMB: Double get() = totalSizeBytes / (1024.0 * 1024.0)
    val hitRate: Float get() = if (totalEntries > 0) {
        (validEntries.toFloat() / totalEntries.toFloat()) * 100f
    } else 0f
}