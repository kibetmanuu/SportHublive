package ke.nucho.sportshublive.ui.matchdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.perf.FirebasePerformance
import ke.nucho.sportshublive.SportsHubApplication
import ke.nucho.sportshublive.data.models.*
import ke.nucho.sportshublive.utils.FirebaseAnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

/**
 * Professional Match Detail ViewModel with Caching
 * Uses CachedFootballRepository for improved performance:
 * - Live match updates
 * - Match statistics
 * - Events (goals, cards, substitutions)
 * - Team lineups
 * - Head-to-head history
 *
 * ✅ CACHED: All data is cached with smart refresh strategy
 */
class MatchDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fixtureId: Int = checkNotNull(savedStateHandle["fixtureId"])

    // ✅ USE CACHED REPOSITORY FROM APPLICATION
    private val repository = SportsHubApplication.cachedRepository
    private var autoRefreshJob: Job? = null

    // UI States
    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(MatchDetailTab.OVERVIEW)
    val selectedTab: StateFlow<MatchDetailTab> = _selectedTab.asStateFlow()

    // Match Data
    private val _fixture = MutableStateFlow<Fixture?>(null)
    val fixture: StateFlow<Fixture?> = _fixture.asStateFlow()

    private val _statistics = MutableStateFlow<List<TeamStatistics>>(emptyList())
    val statistics: StateFlow<List<TeamStatistics>> = _statistics.asStateFlow()

    private val _events = MutableStateFlow<List<MatchEvent>>(emptyList())
    val events: StateFlow<List<MatchEvent>> = _events.asStateFlow()

    private val _lineups = MutableStateFlow<List<TeamLineup>>(emptyList())
    val lineups: StateFlow<List<TeamLineup>> = _lineups.asStateFlow()

    private val _h2h = MutableStateFlow<List<Fixture>>(emptyList())
    val h2h: StateFlow<List<Fixture>> = _h2h.asStateFlow()

    // Auto-refresh
    private val _isAutoRefresh = MutableStateFlow(false)
    val isAutoRefresh: StateFlow<Boolean> = _isAutoRefresh.asStateFlow()

    companion object {
        private const val TAG = "MatchDetailViewModel"
        private const val AUTO_REFRESH_INTERVAL = 30000L // 30 seconds
    }

    init {
        Log.d(TAG, "⚽ Match Detail initialized for fixture: $fixtureId (WITH CACHE)")
        FirebaseAnalyticsHelper.logScreenView("MatchDetail")
        loadMatchDetails(forceRefresh = false)
    }

    /**
     * ✅ CACHED: Load complete match details
     * @param forceRefresh If true, bypasses cache and fetches fresh data
     */
    fun loadMatchDetails(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_match_detail")
            trace.start()
            trace.putAttribute("fixture_id", fixtureId.toString())
            trace.putAttribute("provider", "API-Sports (Cached)")
            trace.putAttribute("force_refresh", forceRefresh.toString())

            try {
                val startTime = System.currentTimeMillis()
                Log.d(TAG, "🔴 Loading match details from API-Sports (forceRefresh: $forceRefresh)")
                Log.d(TAG, "   Fixture ID: $fixtureId")

                // ✅ USE CACHED METHOD - getFixtureById
                val fixtureResult = repository.getFixtureById(
                    fixtureId = fixtureId,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )

                val responseTime = System.currentTimeMillis() - startTime

                fixtureResult.onSuccess { fixtures ->
                    if (fixtures.isNotEmpty()) {
                        val fixture = fixtures.first()
                        _fixture.value = fixture

                        Log.d(TAG, "✅ Fixture loaded successfully in ${responseTime}ms")
                        Log.d(TAG, "   Match: ${fixture.teams.home.name} vs ${fixture.teams.away.name}")
                        Log.d(TAG, "   Status: ${fixture.fixture.status.long}")
                        Log.d(TAG, "   League: ${fixture.league.name}")
                        Log.d(TAG, "   📦 Cache: ${if (forceRefresh) "BYPASSED" else "USED"}")

                        // Log match viewed
                        FirebaseAnalyticsHelper.logMatchViewed(
                            fixtureId,
                            fixture.league.name,
                            "${fixture.teams.home.name} vs ${fixture.teams.away.name}"
                        )

                        val isLive = isMatchLive(fixture.fixture.status.short)
                        Log.d(TAG, "   Is Live: $isLive")

                        if (isLive) {
                            startAutoRefresh()
                        }

                        // Load additional data (with cache support)
                        loadAdditionalData(fixture, forceRefresh)

                        _uiState.value = MatchDetailUiState.Success

                        trace.putAttribute("status", "success")
                        trace.putAttribute("is_live", isLive.toString())
                        trace.putAttribute("response_time_ms", responseTime.toString())
                    } else {
                        Log.w(TAG, "⚠️ No fixture found for ID: $fixtureId")
                        _uiState.value = MatchDetailUiState.Error(
                            "Match not found\n\n" +
                                    "Fixture ID: $fixtureId\n\n" +
                                    "This match may have been:\n" +
                                    "• Postponed or cancelled\n" +
                                    "• Not yet scheduled\n" +
                                    "• Removed from the database"
                        )
                        trace.putAttribute("status", "not_found")
                    }
                }.onFailure { e ->
                    Log.e(TAG, "❌ Error loading fixture from API-Sports (${responseTime}ms)", e)
                    handleError(e)
                    trace.putAttribute("status", "error")
                    trace.putAttribute("response_time_ms", responseTime.toString())
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading match", e)
                e.printStackTrace()
                handleError(e)
                trace.putAttribute("status", "exception")
            } finally {
                trace.stop()
            }
        }
    }

    /**
     * ✅ CACHED: Load additional match data (statistics, events, lineups, h2h)
     * All using cached repository methods
     * @param forceRefresh If true, bypasses cache for all additional data
     */
    private suspend fun loadAdditionalData(fixture: Fixture, forceRefresh: Boolean = false) {
        Log.d(TAG, "🔄 Loading additional match data (forceRefresh: $forceRefresh)...")

        // Load statistics
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                Log.d(TAG, "   📊 Loading statistics...")

                // ✅ USE CACHED METHOD - getMatchStatistics
                val statsResult = repository.getMatchStatistics(
                    fixtureId = fixtureId,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )

                val responseTime = System.currentTimeMillis() - startTime

                statsResult.onSuccess { stats ->
                    _statistics.value = stats
                    Log.d(TAG, "   ✅ Loaded ${stats.size} team statistics (${responseTime}ms)")
                    Log.d(TAG, "      📦 Cache: ${if (forceRefresh) "BYPASSED" else "USED"}")
                }.onFailure { e ->
                    Log.w(TAG, "   ⚠️ Could not load statistics: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "   ⚠️ Statistics error: ${e.message}")
            }
        }

        // Load events
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                Log.d(TAG, "   📋 Loading events...")

                // ✅ USE CACHED METHOD - getMatchEvents
                val eventsResult = repository.getMatchEvents(
                    fixtureId = fixtureId,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )

                val responseTime = System.currentTimeMillis() - startTime

                eventsResult.onSuccess { events ->
                    _events.value = events.sortedBy { it.time.elapsed }
                    Log.d(TAG, "   ✅ Loaded ${events.size} match events (${responseTime}ms)")
                    Log.d(TAG, "      📦 Cache: ${if (forceRefresh) "BYPASSED" else "USED"}")

                    // Log event types
                    val eventTypes = events.groupBy { it.type }
                    eventTypes.forEach { (type, list) ->
                        Log.d(TAG, "      • $type: ${list.size}")
                    }
                }.onFailure { e ->
                    Log.w(TAG, "   ⚠️ Could not load events: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "   ⚠️ Events error: ${e.message}")
            }
        }

        // Load lineups (only if match has started or finished)
        if (fixture.fixture.status.short !in listOf("NS", "PST", "CANC", "ABD")) {
            viewModelScope.launch {
                try {
                    val startTime = System.currentTimeMillis()
                    Log.d(TAG, "   👥 Loading lineups...")

                    // ✅ USE CACHED METHOD - getMatchLineups
                    val lineupsResult = repository.getMatchLineups(
                        fixtureId = fixtureId,
                        forceRefresh = forceRefresh  // ✅ Cache-aware!
                    )

                    val responseTime = System.currentTimeMillis() - startTime

                    lineupsResult.onSuccess { lineups ->
                        _lineups.value = lineups
                        Log.d(TAG, "   ✅ Loaded ${lineups.size} team lineups (${responseTime}ms)")
                        Log.d(TAG, "      📦 Cache: ${if (forceRefresh) "BYPASSED" else "USED"}")
                        lineups.forEach { lineup ->
                            Log.d(TAG, "      • ${lineup.team.name}: ${lineup.formation}")
                        }
                    }.onFailure { e ->
                        Log.w(TAG, "   ⚠️ Could not load lineups: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "   ⚠️ Lineups error: ${e.message}")
                }
            }
        } else {
            Log.d(TAG, "   ⏭️ Skipping lineups (match not started)")
        }

        // Load head-to-head
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                Log.d(TAG, "   🔄 Loading H2H...")

                // ✅ USE CACHED METHOD - getHeadToHead
                val h2hResult = repository.getHeadToHead(
                    team1Id = fixture.teams.home.id,
                    team2Id = fixture.teams.away.id,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )

                val responseTime = System.currentTimeMillis() - startTime

                h2hResult.onSuccess { matches ->
                    _h2h.value = matches.take(5)
                    Log.d(TAG, "   ✅ Loaded ${matches.size} H2H matches (showing 5) (${responseTime}ms)")
                    Log.d(TAG, "      📦 Cache: ${if (forceRefresh) "BYPASSED" else "USED"}")
                }.onFailure { e ->
                    Log.w(TAG, "   ⚠️ Could not load H2H: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "   ⚠️ H2H error: ${e.message}")
            }
        }

        Log.d(TAG, "✅ Additional data loading initiated")
    }

    /**
     * ✅ IMPROVED: Start auto-refresh for live matches
     * Auto-refresh always forces fresh data (bypasses cache)
     */
    private fun startAutoRefresh() {
        if (_isAutoRefresh.value) {
            Log.d(TAG, "⏭️ Auto-refresh already running")
            return
        }

        _isAutoRefresh.value = true
        autoRefreshJob?.cancel()

        Log.d(TAG, "🔄 Starting auto-refresh (every ${AUTO_REFRESH_INTERVAL / 1000}s)")
        Log.d(TAG, "   📦 Auto-refresh will BYPASS cache for live data")

        autoRefreshJob = viewModelScope.launch {
            while (_isAutoRefresh.value) {
                delay(AUTO_REFRESH_INTERVAL)

                val currentFixture = _fixture.value
                if (currentFixture != null && isMatchLive(currentFixture.fixture.status.short)) {
                    Log.d(TAG, "🔄 Auto-refreshing live match...")
                    refreshMatchData()
                } else {
                    Log.d(TAG, "⏹️ Match no longer live, stopping auto-refresh")
                    stopAutoRefresh()
                }
            }
        }
    }

    /**
     * Stop auto-refresh
     */
    private fun stopAutoRefresh() {
        Log.d(TAG, "⏹️ Stopping auto-refresh")
        _isAutoRefresh.value = false
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    /**
     * ✅ IMPROVED: Refresh match data (for live matches)
     * Always bypasses cache to get the freshest data
     */
    private suspend fun refreshMatchData() {
        try {
            Log.d(TAG, "🔄 Refreshing match data (bypassing cache)...")

            // ✅ Refresh fixture (force refresh)
            val fixtureResult = repository.getFixtureById(
                fixtureId = fixtureId,
                forceRefresh = true  // ✅ Always bypass cache for live updates
            )

            fixtureResult.onSuccess { fixtures ->
                if (fixtures.isNotEmpty()) {
                    val newFixture = fixtures.first()
                    val oldFixture = _fixture.value

                    _fixture.value = newFixture

                    // Log changes
                    if (oldFixture != null) {
                        if (oldFixture.goals.home != newFixture.goals.home ||
                            oldFixture.goals.away != newFixture.goals.away) {
                            Log.d(TAG, "   ⚽ Score updated: ${newFixture.goals.home} - ${newFixture.goals.away}")
                        }
                        if (oldFixture.fixture.status.elapsed != newFixture.fixture.status.elapsed) {
                            Log.d(TAG, "   ⏱️ Time: ${newFixture.fixture.status.elapsed}'")
                        }
                    }
                }
            }

            // ✅ Refresh events (force refresh)
            val eventsResult = repository.getMatchEvents(
                fixtureId = fixtureId,
                forceRefresh = true  // ✅ Always bypass cache for live updates
            )

            eventsResult.onSuccess { events ->
                val oldCount = _events.value.size
                val newEvents = events.sortedBy { it.time.elapsed }
                _events.value = newEvents

                if (newEvents.size > oldCount) {
                    Log.d(TAG, "   📋 New events: ${newEvents.size - oldCount}")
                }
            }

            // ✅ Refresh statistics (force refresh)
            val statsResult = repository.getMatchStatistics(
                fixtureId = fixtureId,
                forceRefresh = true  // ✅ Always bypass cache for live updates
            )

            statsResult.onSuccess { stats ->
                _statistics.value = stats
            }

            Log.d(TAG, "   ✅ Refresh complete")

        } catch (e: Exception) {
            Log.e(TAG, "   ⚠️ Refresh error: ${e.message}")
        }
    }

    /**
     * ✅ IMPROVED: Manual refresh
     * Forces fresh data from API (bypasses cache)
     */
    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh triggered (force refresh = true)")
        FirebaseAnalyticsHelper.logMatchRefreshed("MatchDetail")
        loadMatchDetails(forceRefresh = true)  // ✅ Bypass cache
    }

    /**
     * Select tab
     */
    fun selectTab(tab: MatchDetailTab) {
        _selectedTab.value = tab
        FirebaseAnalyticsHelper.logTabSelected(tab.name)
        Log.d(TAG, "📑 Tab selected: ${tab.name}")
    }

    /**
     * Check if match is live
     */
    private fun isMatchLive(status: String): Boolean {
        return status in listOf("1H", "2H", "HT", "ET", "P", "LIVE")
    }

    /**
     * Handle errors with detailed messages
     */
    private fun handleError(e: Throwable) {
        val errorMessage = when {
            e.message?.contains("404") == true || e.message?.contains("not found") == true ->
                "⚠️ Match Not Found\n\n" +
                        "Fixture ID: $fixtureId\n\n" +
                        "This match may have been:\n" +
                        "• Postponed or cancelled\n" +
                        "• Not yet scheduled\n" +
                        "• Removed from the database\n\n" +
                        "Please try:\n" +
                        "• Going back and selecting the match again\n" +
                        "• Checking if the match date is correct"

            e.message?.contains("403") == true ->
                "🔒 Access Denied\n\n" +
                        "Your API-Sports key is invalid or expired.\n\n" +
                        "Please:\n" +
                        "1. Go to Firebase Console\n" +
                        "2. Update your API-Sports key in Remote Config\n" +
                        "3. Restart the app"

            e.message?.contains("429") == true ->
                "⏱️ Rate Limit Exceeded\n\n" +
                        "API-Sports rate limit reached.\n\n" +
                        "Free tier limits:\n" +
                        "• 100 requests per day\n" +
                        "• 10 requests per minute\n\n" +
                        "💡 Tip: The app uses caching to reduce API calls!\n\n" +
                        "Please wait a few minutes and try again."

            e.message?.contains("timeout") == true || e.message?.contains("Unable to resolve host") == true ->
                "🌐 Connection Error\n\n" +
                        "Unable to connect to API-Sports.\n\n" +
                        "Please check:\n" +
                        "• Your internet connection\n" +
                        "• WiFi or mobile data is enabled\n" +
                        "• Try again in a moment"

            else ->
                "❌ Unable to Load Match\n\n" +
                        "Error: ${e.message}\n\n" +
                        "Fixture ID: $fixtureId\n\n" +
                        "Please try:\n" +
                        "• Pulling down to refresh\n" +
                        "• Going back and selecting the match again\n" +
                        "• Checking your internet connection"
        }

        _uiState.value = MatchDetailUiState.Error(errorMessage)
        Log.e(TAG, "❌ Error handled: $errorMessage")
    }

    /**
     * Clean up
     */
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
        Log.d(TAG, "🔚 ViewModel cleared")
    }
}

/**
 * Match Detail Tabs
 */
enum class MatchDetailTab {
    OVERVIEW,
    STATS,
    LINEUPS,
    EVENTS,
    H2H
}

/**
 * UI States
 */
sealed class MatchDetailUiState {
    object Loading : MatchDetailUiState()
    object Success : MatchDetailUiState()
    data class Error(val message: String) : MatchDetailUiState()
}