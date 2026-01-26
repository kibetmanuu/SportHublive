package ke.nucho.sportshublive.ui.matchdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.perf.FirebasePerformance
import ke.nucho.sportshublive.data.api.ApiConfigManager
import ke.nucho.sportshublive.data.models.*
import ke.nucho.sportshublive.data.repository.UnifiedFootballRepository
import ke.nucho.sportshublive.utils.FirebaseAnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

/**
 * Professional Match Detail ViewModel
 * Uses API-Sports for comprehensive match information including:
 * - Live match updates
 * - Match statistics
 * - Events (goals, cards, substitutions)
 * - Team lineups
 * - Head-to-head history
 */
class MatchDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fixtureId: Int = checkNotNull(savedStateHandle["fixtureId"])

    private val apiConfigManager = ApiConfigManager()
    private var repository: UnifiedFootballRepository? = null
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
        Log.d(TAG, "⚽ Match Detail initialized for fixture: $fixtureId")
        FirebaseAnalyticsHelper.logScreenView("MatchDetail")
        initializeRepository()
    }

    /**
     * Initialize repository and load match data
     */
    private fun initializeRepository() {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading

            try {
                Log.d(TAG, "🔄 Fetching Firebase Remote Config...")
                val success = apiConfigManager.fetchAndActivate()

                if (success) {
                    Log.d(TAG, "✅ Remote Config activated")
                } else {
                    Log.w(TAG, "⚠️ Using default Remote Config values")
                }

                // Initialize repository
                repository = UnifiedFootballRepository(apiConfigManager)
                Log.d(TAG, "✅ Repository initialized")

                // Load match details
                loadMatchDetails()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Initialization error", e)
                _uiState.value = MatchDetailUiState.Error(
                    "Failed to initialize: ${e.message}\n\nPlease check:\n" +
                            "• Firebase Remote Config is set up\n" +
                            "• API-Sports key is configured\n" +
                            "• Internet connection"
                )
            }
        }
    }

    /**
     * Load complete match details using API-Sports
     */
    fun loadMatchDetails() {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading

            val repo = repository
            if (repo == null) {
                _uiState.value = MatchDetailUiState.Error("Repository not initialized")
                return@launch
            }

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_match_detail")
            trace.start()
            trace.putAttribute("fixture_id", fixtureId.toString())
            trace.putAttribute("provider", "API-Sports")

            try {
                Log.d(TAG, "🔴 Loading match details from API-Sports")
                Log.d(TAG, "   Fixture ID: $fixtureId")

                // Use API-Sports for match details
                val fixtureResult = repo.getFixtureByIdHybrid(fixtureId)

                fixtureResult.onSuccess { fixtures ->
                    if (fixtures.isNotEmpty()) {
                        val fixture = fixtures.first()
                        _fixture.value = fixture

                        Log.d(TAG, "✅ Fixture loaded successfully")
                        Log.d(TAG, "   Match: ${fixture.teams.home.name} vs ${fixture.teams.away.name}")
                        Log.d(TAG, "   Status: ${fixture.fixture.status.long}")
                        Log.d(TAG, "   League: ${fixture.league.name}")

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

                        // Load additional data
                        loadAdditionalData(fixture)

                        _uiState.value = MatchDetailUiState.Success

                        trace.putAttribute("status", "success")
                        trace.putAttribute("is_live", isLive.toString())
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
                    Log.e(TAG, "❌ Error loading fixture from API-Sports", e)
                    handleError(e)
                    trace.putAttribute("status", "error")
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
     * Load additional match data (statistics, events, lineups, h2h)
     * All using API-Sports
     */
    private suspend fun loadAdditionalData(fixture: Fixture) {
        val repo = repository ?: return

        Log.d(TAG, "🔄 Loading additional match data...")

        // Load statistics
        viewModelScope.launch {
            try {
                Log.d(TAG, "   📊 Loading statistics...")
                val statsResult = repo.getMatchStatisticsHybrid(fixtureId)
                statsResult.onSuccess { stats ->
                    _statistics.value = stats
                    Log.d(TAG, "   ✅ Loaded ${stats.size} team statistics")
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
                Log.d(TAG, "   📋 Loading events...")
                val eventsResult = repo.getMatchEventsHybrid(fixtureId)
                eventsResult.onSuccess { events ->
                    _events.value = events.sortedBy { it.time.elapsed }
                    Log.d(TAG, "   ✅ Loaded ${events.size} match events")

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
                    Log.d(TAG, "   👥 Loading lineups...")
                    val lineupsResult = repo.getMatchLineupsHybrid(fixtureId)
                    lineupsResult.onSuccess { lineups ->
                        _lineups.value = lineups
                        Log.d(TAG, "   ✅ Loaded ${lineups.size} team lineups")
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
                Log.d(TAG, "   🔄 Loading H2H...")
                val h2hResult = repo.getHeadToHeadHybrid(
                    team1Id = fixture.teams.home.id,
                    team2Id = fixture.teams.away.id
                )
                h2hResult.onSuccess { matches ->
                    _h2h.value = matches.take(5)
                    Log.d(TAG, "   ✅ Loaded ${matches.size} H2H matches (showing 5)")
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
     * Start auto-refresh for live matches
     */
    private fun startAutoRefresh() {
        if (_isAutoRefresh.value) {
            Log.d(TAG, "⏭️ Auto-refresh already running")
            return
        }

        _isAutoRefresh.value = true
        autoRefreshJob?.cancel()

        Log.d(TAG, "🔄 Starting auto-refresh (every ${AUTO_REFRESH_INTERVAL / 1000}s)")

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
     * Refresh match data (for live matches)
     */
    private suspend fun refreshMatchData() {
        val repo = repository ?: return

        try {
            Log.d(TAG, "🔄 Refreshing match data...")

            // Refresh fixture
            val fixtureResult = repo.getFixtureByIdHybrid(fixtureId)
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

            // Refresh events
            val eventsResult = repo.getMatchEventsHybrid(fixtureId)
            eventsResult.onSuccess { events ->
                val oldCount = _events.value.size
                val newEvents = events.sortedBy { it.time.elapsed }
                _events.value = newEvents

                if (newEvents.size > oldCount) {
                    Log.d(TAG, "   📋 New events: ${newEvents.size - oldCount}")
                }
            }

            // Refresh statistics
            val statsResult = repo.getMatchStatisticsHybrid(fixtureId)
            statsResult.onSuccess { stats ->
                _statistics.value = stats
            }

            Log.d(TAG, "   ✅ Refresh complete")

        } catch (e: Exception) {
            Log.e(TAG, "   ⚠️ Refresh error: ${e.message}")
        }
    }

    /**
     * Manual refresh
     */
    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh triggered")
        FirebaseAnalyticsHelper.logMatchRefreshed("MatchDetail")
        loadMatchDetails()
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