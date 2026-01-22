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
 * Handles comprehensive match information including:
 * - Live match updates
 * - Match statistics
 * - Events (goals, cards, substitutions)
 * - Team lineups
 * - Head-to-head history
 * - Match predictions
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
                val success = apiConfigManager.fetchAndActivate()

                if (success) {
                    val configResult = apiConfigManager.getApiConfig()

                    configResult.onSuccess { config ->
                        Log.d(TAG, "✅ Using ${config.provider} API")
                        repository = UnifiedFootballRepository(apiConfigManager)
                        loadMatchDetails()
                    }.onFailure { e ->
                        Log.e(TAG, "❌ Failed to get API config", e)
                        _uiState.value = MatchDetailUiState.Error(
                            "Configuration Error: ${e.message}"
                        )
                    }
                } else {
                    _uiState.value = MatchDetailUiState.Error(
                        "Unable to load configuration"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Initialization error", e)
                _uiState.value = MatchDetailUiState.Error(
                    "Initialization failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Load complete match details
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

            try {
                // Load fixture details
                val fixtureResult = repo.getFixtureById(fixtureId)

                fixtureResult.onSuccess { fixtures ->
                    if (fixtures.isNotEmpty()) {
                        val fixture = fixtures.first()
                        _fixture.value = fixture

                        FirebaseAnalyticsHelper.logMatchViewed(
                            fixtureId,
                            fixture.league.name,
                            "${fixture.teams.home.name} vs ${fixture.teams.away.name}"
                        )

                        // Determine if match is live
                        val isLive = isMatchLive(fixture.fixture.status.short)

                        if (isLive) {
                            startAutoRefresh()
                        }

                        // Load additional data based on match status
                        loadAdditionalData(fixture)

                        _uiState.value = MatchDetailUiState.Success

                        trace.putAttribute("status", "success")
                        trace.putAttribute("is_live", isLive.toString())
                    } else {
                        _uiState.value = MatchDetailUiState.Error("Match not found")
                        trace.putAttribute("status", "not_found")
                    }
                }.onFailure { e ->
                    Log.e(TAG, "❌ Error loading fixture", e)
                    handleError(e)
                    trace.putAttribute("status", "error")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading match", e)
                handleError(e)
                trace.putAttribute("status", "exception")
            } finally {
                trace.stop()
            }
        }
    }

    /**
     * Load additional match data (statistics, events, lineups, h2h)
     */
    private suspend fun loadAdditionalData(fixture: Fixture) {
        val repo = repository ?: return

        // Load match statistics
        viewModelScope.launch {
            try {
                val statsResult = repo.getMatchStatistics(fixtureId)
                statsResult.onSuccess { stats ->
                    _statistics.value = stats
                    Log.d(TAG, "✅ Loaded statistics")
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Could not load statistics: ${e.message}")
            }
        }

        // Load match events
        viewModelScope.launch {
            try {
                val eventsResult = repo.getMatchEvents(fixtureId)
                eventsResult.onSuccess { events ->
                    _events.value = events.sortedBy { it.time.elapsed }
                    Log.d(TAG, "✅ Loaded ${events.size} events")
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Could not load events: ${e.message}")
            }
        }

        // Load lineups (only for started matches)
        if (fixture.fixture.status.short !in listOf("NS", "PST", "CANC")) {
            viewModelScope.launch {
                try {
                    val lineupsResult = repo.getMatchLineups(fixtureId)
                    lineupsResult.onSuccess { lineups ->
                        _lineups.value = lineups
                        Log.d(TAG, "✅ Loaded lineups")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Could not load lineups: ${e.message}")
                }
            }
        }

        // Load head-to-head
        viewModelScope.launch {
            try {
                val h2hResult = repo.getHeadToHead(
                    team1Id = fixture.teams.home.id,
                    team2Id = fixture.teams.away.id
                )
                h2hResult.onSuccess { matches ->
                    _h2h.value = matches.take(5) // Last 5 matches
                    Log.d(TAG, "✅ Loaded ${matches.size} H2H matches")
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Could not load H2H: ${e.message}")
            }
        }
    }

    /**
     * Start auto-refresh for live matches
     */
    private fun startAutoRefresh() {
        if (_isAutoRefresh.value) return

        _isAutoRefresh.value = true
        autoRefreshJob?.cancel()

        autoRefreshJob = viewModelScope.launch {
            while (_isAutoRefresh.value) {
                delay(AUTO_REFRESH_INTERVAL)

                val currentFixture = _fixture.value
                if (currentFixture != null && isMatchLive(currentFixture.fixture.status.short)) {
                    Log.d(TAG, "🔄 Auto-refreshing live match")
                    refreshMatchData()
                } else {
                    // Stop auto-refresh if match is no longer live
                    stopAutoRefresh()
                }
            }
        }
    }

    /**
     * Stop auto-refresh
     */
    private fun stopAutoRefresh() {
        _isAutoRefresh.value = false
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    /**
     * Refresh match data
     */
    private suspend fun refreshMatchData() {
        val repo = repository ?: return

        try {
            // Refresh fixture
            val fixtureResult = repo.getFixtureById(fixtureId)
            fixtureResult.onSuccess { fixtures ->
                if (fixtures.isNotEmpty()) {
                    _fixture.value = fixtures.first()
                }
            }

            // Refresh events
            val eventsResult = repo.getMatchEvents(fixtureId)
            eventsResult.onSuccess { events ->
                _events.value = events.sortedBy { it.time.elapsed }
            }

            // Refresh statistics
            val statsResult = repo.getMatchStatistics(fixtureId)
            statsResult.onSuccess { stats ->
                _statistics.value = stats
            }

        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Refresh error: ${e.message}")
        }
    }

    /**
     * Manual refresh
     */
    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh")
        FirebaseAnalyticsHelper.logMatchRefreshed("MatchDetail")
        loadMatchDetails()
    }

    /**
     * Select tab
     */
    fun selectTab(tab: MatchDetailTab) {
        _selectedTab.value = tab
        FirebaseAnalyticsHelper.logTabSelected(tab.name)
    }

    /**
     * Check if match is live
     */
    private fun isMatchLive(status: String): Boolean {
        return status in listOf("1H", "2H", "HT", "ET", "P", "LIVE")
    }

    /**
     * Handle errors
     */
    private fun handleError(e: Throwable) {
        val errorMessage = when {
            e.message?.contains("404") == true ->
                "Match not found. It may have been postponed or cancelled."
            e.message?.contains("403") == true ->
                "Authentication error. Please check your API configuration."
            e.message?.contains("429") == true ->
                "Rate limit reached. Please wait a moment."
            else ->
                "Unable to load match details: ${e.message}"
        }

        _uiState.value = MatchDetailUiState.Error(errorMessage)
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