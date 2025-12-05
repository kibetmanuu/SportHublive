package ke.nucho.sportshublive.data.cache

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

/**
 * Firestore Cache Manager
 * Caches API responses in Firestore with expiration
 */
class FirestoreCacheManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()
    private val cacheCollection = "api_cache"

    companion object {
        private const val TAG = "FirestoreCacheManager"

        // Cache durations for different endpoints (in milliseconds)
        private const val LIVE_MATCHES_CACHE = 30 * 1000L // 30 seconds
        private const val DATE_FIXTURES_CACHE = 5 * 60 * 1000L // 5 minutes
        private const val LEAGUE_FIXTURES_CACHE = 10 * 60 * 1000L // 10 minutes
        private const val STANDINGS_CACHE = 30 * 60 * 1000L // 30 minutes
        private const val STATISTICS_CACHE = 60 * 60 * 1000L // 1 hour
        private const val DEFAULT_CACHE_DURATION = 5 * 60 * 1000L // 5 minutes
    }

    /**
     * Cache data in Firestore
     */
    suspend fun <T> cacheData(
        key: String,
        data: T,
        cacheDuration: Long = DEFAULT_CACHE_DURATION
    ): Boolean {
        return try {
            val cacheEntry = mapOf(
                "key" to key,
                "data" to gson.toJson(data),
                "timestamp" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + cacheDuration)
            )

            firestore.collection(cacheCollection)
                .document(key)
                .set(cacheEntry)
                .await()

            Log.d(TAG, "✓ Cached data for key: $key (expires in ${cacheDuration / 1000}s)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to cache data for key: $key - ${e.message}", e)
            false
        }
    }

    /**
     * Get cached data from Firestore
     */
    suspend fun <T> getCachedData(key: String, clazz: Class<T>): T? {
        return try {
            val document = firestore.collection(cacheCollection)
                .document(key)
                .get()
                .await()

            if (!document.exists()) {
                Log.d(TAG, "○ Cache miss for key: $key (not found)")
                return null
            }

            val expiresAt = document.getLong("expiresAt") ?: 0L
            val currentTime = System.currentTimeMillis()

            // Check if cache is expired
            if (expiresAt < currentTime) {
                Log.d(TAG, "○ Cache miss for key: $key (expired ${(currentTime - expiresAt) / 1000}s ago)")
                deleteCachedData(key)
                return null
            }

            val dataString = document.getString("data")
            if (dataString.isNullOrEmpty()) {
                Log.w(TAG, "✗ Invalid cache entry for key: $key (empty data)")
                return null
            }

            val timeLeft = (expiresAt - currentTime) / 1000
            Log.d(TAG, "✓ Cache hit for key: $key (expires in ${timeLeft}s)")
            gson.fromJson(dataString, clazz)
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to get cached data for key: $key - ${e.message}", e)
            null
        }
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
            Log.d(TAG, "✓ Deleted cache for key: $key")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to delete cache for key: $key - ${e.message}", e)
        }
    }

    /**
     * Clear all expired cache entries
     */
    suspend fun clearExpiredCache() {
        try {
            val currentTime = System.currentTimeMillis()
            val expiredDocs = firestore.collection(cacheCollection)
                .whereLessThan("expiresAt", currentTime)
                .get()
                .await()

            if (expiredDocs.isEmpty) {
                Log.d(TAG, "○ No expired cache to clear")
                return
            }

            val batch = firestore.batch()
            expiredDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Log.d(TAG, "✓ Cleared ${expiredDocs.size()} expired cache entries")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to clear expired cache: ${e.message}", e)
        }
    }

    /**
     * Clear all cache
     */
    suspend fun clearAllCache() {
        try {
            val allDocs = firestore.collection(cacheCollection)
                .get()
                .await()

            if (allDocs.isEmpty) {
                Log.d(TAG, "○ No cache to clear")
                return
            }

            val batch = firestore.batch()
            allDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Log.d(TAG, "✓ Cleared all cache (${allDocs.size()} entries)")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to clear all cache: ${e.message}", e)
        }
    }

    /**
     * Get cache duration based on endpoint type
     */
    fun getCacheDuration(endpoint: String): Long {
        return when {
            endpoint.contains("live", ignoreCase = true) -> LIVE_MATCHES_CACHE
            endpoint.contains("date", ignoreCase = true) -> DATE_FIXTURES_CACHE
            endpoint.contains("league", ignoreCase = true) -> LEAGUE_FIXTURES_CACHE
            endpoint.contains("standings", ignoreCase = true) -> STANDINGS_CACHE
            endpoint.contains("statistics", ignoreCase = true) -> STATISTICS_CACHE
            else -> DEFAULT_CACHE_DURATION
        }
    }

    /**
     * Generate cache key from sport, endpoint, and parameters
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
            .lowercase()
    }

    /**
     * Get cache statistics (for monitoring/debugging)
     */
    suspend fun getCacheStats(): CacheStats {
        return try {
            val allDocs = firestore.collection(cacheCollection).get().await()
            val currentTime = System.currentTimeMillis()

            var validCount = 0
            var expiredCount = 0

            allDocs.documents.forEach { doc ->
                val expiresAt = doc.getLong("expiresAt") ?: 0L
                if (expiresAt >= currentTime) {
                    validCount++
                } else {
                    expiredCount++
                }
            }

            CacheStats(
                totalEntries = allDocs.size(),
                validEntries = validCount,
                expiredEntries = expiredCount,
                lastChecked = currentTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cache stats: ${e.message}", e)
            CacheStats(0, 0, 0, System.currentTimeMillis())
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
    val lastChecked: Long
)