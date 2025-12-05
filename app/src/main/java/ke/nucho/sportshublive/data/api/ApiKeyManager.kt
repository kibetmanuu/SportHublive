package ke.nucho.sportshublive.data.api

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * API Key Manager with Remote Config and Automatic Reset
 * Manages multiple API keys from Firebase Remote Config
 */
object ApiKeyManager {
    private const val TAG = "ApiKeyManager"

    // Remote Config keys
    private const val REMOTE_CONFIG_API_KEYS = "api_keys_json"
    private const val REMOTE_CONFIG_SELECTION_MODE = "api_key_selection_mode"
    private const val REMOTE_CONFIG_RESET_INTERVAL = "api_key_reset_interval_hours"

    // Default fallback keys
    private val DEFAULT_API_KEYS = listOf(
        "dfa5bc422e979517069be14236ec78e5"
    )

    // Current loaded keys from Remote Config
    private var apiKeys: List<String> = DEFAULT_API_KEYS

    // Selection mode: "random" or "round_robin"
    private var selectionMode: String = "random"

    // Reset interval in hours (default 0.25 = 15 minutes)
    private var resetIntervalHours: Double = 0.25

    // Track current key index for round-robin
    private val currentKeyIndex = AtomicInteger(0)

    // Track failed keys with timestamp
    private val failedKeys = mutableMapOf<String, Long>()

    // Track key usage count for analytics
    private val keyUsageCount = mutableMapOf<String, Int>()

    // Coroutine scope for automatic reset
    private var resetJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Initialize and load API keys from Firebase Remote Config
     */
    fun initialize(remoteConfig: FirebaseRemoteConfig) {
        try {
            // Load selection mode
            selectionMode = remoteConfig.getString(REMOTE_CONFIG_SELECTION_MODE)
                .takeIf { it.isNotEmpty() } ?: "random"

            // Load reset interval (in hours)
            resetIntervalHours = remoteConfig.getDouble(REMOTE_CONFIG_RESET_INTERVAL)
                .takeIf { it > 0 } ?: 0.25

            // Load API keys from Remote Config
            val keysJson = remoteConfig.getString(REMOTE_CONFIG_API_KEYS)

            if (keysJson.isNotEmpty()) {
                val parsedKeys = parseApiKeysJson(keysJson)

                if (parsedKeys.isNotEmpty()) {
                    apiKeys = parsedKeys
                    Log.d(TAG, "✓ Loaded ${apiKeys.size} API keys from Remote Config")
                    Log.d(TAG, "Selection mode: $selectionMode")
                    Log.d(TAG, "Reset interval: ${resetIntervalHours * 60} minutes")
                } else {
                    Log.w(TAG, "Failed to parse API keys, using defaults")
                    apiKeys = DEFAULT_API_KEYS
                }
            } else {
                Log.w(TAG, "No API keys in Remote Config, using defaults")
                apiKeys = DEFAULT_API_KEYS
            }

            // Start automatic reset timer
            startAutomaticReset()

        } catch (e: Exception) {
            Log.e(TAG, "Error loading API keys from Remote Config", e)
            apiKeys = DEFAULT_API_KEYS
        }
    }

    /**
     * Start automatic periodic reset of failed keys
     */
    private fun startAutomaticReset() {
        // Cancel existing job if any
        resetJob?.cancel()

        // Convert hours to milliseconds
        val intervalMillis = (resetIntervalHours * 60 * 60 * 1000).toLong()

        resetJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMillis)
                resetExpiredFailedKeys()
            }
        }

        Log.d(TAG, "Started automatic reset timer: every ${resetIntervalHours * 60} minutes")
    }

    /**
     * Reset failed keys that have expired
     */
    private fun resetExpiredFailedKeys() {
        val currentTime = System.currentTimeMillis()
        val intervalMillis = (resetIntervalHours * 60 * 60 * 1000).toLong()

        val expiredKeys = failedKeys.filter { (_, timestamp) ->
            currentTime - timestamp >= intervalMillis
        }

        expiredKeys.keys.forEach { key ->
            failedKeys.remove(key)
            Log.d(TAG, "✓ Auto-reset expired failed key: ${maskKey(key)}")
        }

        if (expiredKeys.isNotEmpty()) {
            Log.d(TAG, "Auto-reset ${expiredKeys.size} expired failed keys")
        }
    }

    /**
     * Parse JSON array of API keys
     */
    private fun parseApiKeysJson(json: String): List<String> {
        return try {
            json.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing API keys JSON", e)
            emptyList()
        }
    }

    /**
     * Get next available API key based on selection mode
     */
    fun getNextApiKey(): String {
        // Check for expired failed keys before getting next key
        resetExpiredFailedKeys()

        val availableKeys = apiKeys.filter { !failedKeys.containsKey(it) }

        if (availableKeys.isEmpty()) {
            Log.w(TAG, "All API keys failed, resetting and using fallback")
            failedKeys.clear()
            return apiKeys.firstOrNull() ?: DEFAULT_API_KEYS.first()
        }

        val selectedKey = when (selectionMode) {
            "random" -> getRandomKey(availableKeys)
            "round_robin" -> getRoundRobinKey(availableKeys)
            else -> getRandomKey(availableKeys)
        }

        // Track usage
        keyUsageCount[selectedKey] = (keyUsageCount[selectedKey] ?: 0) + 1

        Log.d(TAG, "Selected API key (mode: $selectionMode): ${maskKey(selectedKey)}")
        return selectedKey
    }

    /**
     * Get random API key
     */
    private fun getRandomKey(keys: List<String>): String {
        return keys[Random.nextInt(keys.size)]
    }

    /**
     * Get next key using round-robin
     */
    private fun getRoundRobinKey(keys: List<String>): String {
        val index = currentKeyIndex.getAndIncrement() % keys.size
        return keys[index]
    }

    /**
     * Mark a key as failed (rate limited or error)
     */
    fun markKeyAsFailed(apiKey: String) {
        failedKeys[apiKey] = System.currentTimeMillis()
        Log.w(TAG, "⚠️ Marked API key as failed: ${maskKey(apiKey)}")
        Log.d(TAG, "Total failed keys: ${failedKeys.size}/${apiKeys.size}")
        Log.d(TAG, "Will auto-reset in ${resetIntervalHours * 60} minutes")
    }

    /**
     * Mark a key as working again
     */
    fun markKeyAsWorking(apiKey: String) {
        if (failedKeys.remove(apiKey) != null) {
            Log.d(TAG, "✓ Marked API key as working again: ${maskKey(apiKey)}")
        }
    }

    /**
     * Reset all failed keys manually
     */
    fun resetFailedKeys() {
        val count = failedKeys.size
        failedKeys.clear()
        Log.d(TAG, "Manually reset $count failed API keys")
    }

    /**
     * Get current API key without rotation
     */
    fun getCurrentApiKey(): String {
        return apiKeys.firstOrNull { !failedKeys.containsKey(it) }
            ?: apiKeys.firstOrNull()
            ?: DEFAULT_API_KEYS.first()
    }

    /**
     * Get all available (non-failed) keys
     */
    fun getAvailableKeysCount(): Int {
        resetExpiredFailedKeys()
        return apiKeys.count { !failedKeys.containsKey(it) }
    }

    /**
     * Get total keys count
     */
    fun getTotalKeysCount(): Int {
        return apiKeys.size
    }

    /**
     * Get usage statistics
     */
    fun getUsageStats(): Map<String, Int> {
        return keyUsageCount.mapKeys { maskKey(it.key) }
    }

    /**
     * Mask API key for logging
     */
    private fun maskKey(key: String): String {
        return if (key.length > 8) {
            "${key.take(8)}...${key.takeLast(4)}"
        } else {
            "****"
        }
    }

    /**
     * Reload keys from Remote Config
     */
    fun reloadFromRemoteConfig(remoteConfig: FirebaseRemoteConfig) {
        initialize(remoteConfig)
    }

    /**
     * Get debug info
     */
    fun getDebugInfo(): String {
        return buildString {
            appendLine("API Key Manager Debug Info:")
            appendLine("- Total keys: ${apiKeys.size}")
            appendLine("- Available keys: ${getAvailableKeysCount()}")
            appendLine("- Failed keys: ${failedKeys.size}")
            appendLine("- Selection mode: $selectionMode")
            appendLine("- Reset interval: ${resetIntervalHours * 60} minutes")
            appendLine("- Usage stats: $keyUsageCount")
            if (failedKeys.isNotEmpty()) {
                appendLine("- Failed keys timestamps:")
                failedKeys.forEach { (key, timestamp) ->
                    val minutesAgo = (System.currentTimeMillis() - timestamp) / 60000
                    appendLine("  ${maskKey(key)}: $minutesAgo minutes ago")
                }
            }
        }
    }

    /**
     * Cleanup when no longer needed
     */
    fun cleanup() {
        resetJob?.cancel()
        coroutineScope.cancel()
    }
}