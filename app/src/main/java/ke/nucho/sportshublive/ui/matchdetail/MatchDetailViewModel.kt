package ke.nucho.sportshublive.ui.matchdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ke.nucho.sportshublive.data.models.Fixture
import ke.nucho.sportshublive.data.repository.CachedSportsRepository
import ke.nucho.sportshublive.utils.FirebaseAnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Match Detail Screen
 * Handles loading match details for different sports with proper error handling
 */
class MatchDetailViewModel(
    private val fixtureId: Int,
    private val sport: String
) : ViewModel() {

    private val repository = CachedSportsRepository()

    // UI State
    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    // Selected Tab
    private val _selectedTab = MutableStateFlow(MatchDetailTab.OVERVIEW)
    val selectedTab: StateFlow<MatchDetailTab> = _selectedTab.asStateFlow()

    // Live Match State
    private val _isLive = MutableStateFlow(false)
    val isLive: StateFlow<Boolean> = _isLive.asStateFlow()

    companion object {
        private const val TAG = "MatchDetailViewModel"
    }

    init {
        Log.d(TAG, "🎯 ViewModel initialized - Sport: $sport, ID: $fixtureId")
        FirebaseAnalyticsHelper.logScreenView("MatchDetailScreen_$sport")
        loadMatchDetails()
    }

    /**
     * Load match details based on sport
     */
    private fun loadMatchDetails(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading

            Log.d(TAG, "📥 Loading match details for $sport (ID: $fixtureId)")

            try {
                val result = when (sport) {
                    "Football" -> {
                        Log.d(TAG, "Fetching football fixture...")
                        repository.getFootballFixtureById(fixtureId, forceRefresh)
                    }
                    "Basketball" -> {
                        Log.d(TAG, "Fetching basketball game...")
                        repository.getBasketballGameById(fixtureId, forceRefresh)
                    }
                    "Hockey" -> {
                        Log.d(TAG, "Fetching hockey game...")
                        repository.getHockeyGameById(fixtureId, forceRefresh)
                    }
                    "Volleyball" -> {
                        Log.d(TAG, "Fetching volleyball game...")
                        repository.getVolleyballGameById(fixtureId, forceRefresh)
                    }
                    "Rugby" -> {
                        Log.d(TAG, "Fetching rugby game...")
                        repository.getRugbyGameById(fixtureId, forceRefresh)
                    }
                    "Formula 1" -> {
                        Log.d(TAG, "Fetching F1 race...")
                        repository.getFormula1RaceById(fixtureId, forceRefresh)
                    }
                    else -> {
                        Log.w(TAG, "Unknown sport: $sport")
                        Result.failure(Exception("Unknown sport: $sport"))
                    }
                }

                result.onSuccess { fixture ->
                    Log.d(TAG, "✅ Successfully loaded match: ${fixture.teams.home.name} vs ${fixture.teams.away.name}")
                    _uiState.value = MatchDetailUiState.Success(fixture)
                    _isLive.value = isMatchLive(fixture)

                    // Log match detail viewed event
                    FirebaseAnalyticsHelper.logMatchDetailViewed(
                        sport = sport,
                        fixtureId = fixtureId,
                        homeTeam = fixture.teams.home.name,
                        awayTeam = fixture.teams.away.name
                    )
                }.onFailure { e ->
                    Log.e(TAG, "❌ Failed to load match: ${e.message}", e)

                    val errorMessage = when (e) {
                        is CachedSportsRepository.ApiAccountSuspendedException -> {
                            "⚠️ API Account Suspended\n\n" +
                                    "Your API subscription is suspended.\n\n" +
                                    "Please visit:\nhttps://dashboard.api-football.com\n\n" +
                                    "to resolve this issue."
                        }
                        is CachedSportsRepository.ApiPlanLimitationException -> {
                            "⚠️ Plan Limitation\n\n" +
                                    "${e.message}\n\n" +
                                    "Options:\n" +
                                    "• Upgrade your API plan\n" +
                                    "• Try a different match"
                        }
                        is CachedSportsRepository.ApiInvalidParameterException -> {
                            "⚠️ Match Not Found\n\n" +
                                    "This match may no longer be available.\n\n" +
                                    "Please try:\n" +
                                    "• Going back and selecting another match\n" +
                                    "• Refreshing the main screen"
                        }
                        is CachedSportsRepository.ApiErrorException -> {
                            "⚠️ API Error\n\n" +
                                    "${e.message}\n\n" +
                                    "This is usually temporary.\n" +
                                    "Please try again in a few moments."
                        }
                        else -> {
                            "Unable to load match details\n\n" +
                                    "Error: ${e.message}\n\n" +
                                    "Please check your internet connection\n" +
                                    "and try again."
                        }
                    }

                    _uiState.value = MatchDetailUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading match: ${e.message}", e)

                val errorMessage = "Network Error\n\n" +
                        "${e.message}\n\n" +
                        "Please check your internet connection\n" +
                        "and try again."

                _uiState.value = MatchDetailUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Select a tab
     */
    fun selectTab(tab: MatchDetailTab) {
        _selectedTab.value = tab
        Log.d(TAG, "Tab selected: ${tab.title}")

        // Log tab selection
        FirebaseAnalyticsHelper.logMatchDetailTabSelected(
            sport = sport,
            fixtureId = fixtureId,
            tabName = tab.title
        )

        // Load additional data based on tab if needed (for Football)
        if (sport == "Football") {
            when (tab) {
                MatchDetailTab.STATS -> {
                    loadFootballStats()
                    FirebaseAnalyticsHelper.logStatisticsViewed(sport, fixtureId)
                }
                MatchDetailTab.EVENTS -> loadFootballEvents()
                MatchDetailTab.LINEUPS -> {
                    loadFootballLineups()
                    FirebaseAnalyticsHelper.logLineupsViewed(sport, fixtureId)
                }
                MatchDetailTab.H2H -> {
                    loadFootballH2H()
                }
                MatchDetailTab.TABLE -> loadFootballStandings()
                else -> {} // No additional loading needed
            }
        }
    }

    /**
     * Refresh match details
     */
    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh triggered")
        FirebaseAnalyticsHelper.logMatchDetailRefreshed(
            sport = sport,
            fixtureId = fixtureId
        )
        loadMatchDetails(forceRefresh = true)
    }

    /**
     * Check if match is currently live
     */
    private fun isMatchLive(fixture: Fixture): Boolean {
        return when (fixture.fixture.status.short) {
            "1H", "2H", "HT", "ET", "P", "PEN" -> true
            else -> false
        }
    }

    // ==================== FOOTBALL-SPECIFIC DATA LOADING ====================
    // These will be implemented when you need detailed football stats, events, etc.

    @Suppress("UNUSED_VARIABLE")
    private fun loadFootballStats() {
        // TODO: Implement when needed
        Log.d(TAG, "Loading football stats for fixture $fixtureId")
        // repository.getFootballFixtureStats(fixtureId)
    }

    @Suppress("UNUSED_VARIABLE")
    private fun loadFootballEvents() {
        // TODO: Implement when needed
        Log.d(TAG, "Loading football events for fixture $fixtureId")
        // repository.getFootballFixtureEvents(fixtureId)
    }

    @Suppress("UNUSED_VARIABLE")
    private fun loadFootballLineups() {
        // TODO: Implement when needed
        Log.d(TAG, "Loading football lineups for fixture $fixtureId")
        // repository.getFootballFixtureLineups(fixtureId)
    }

    @Suppress("UNUSED_VARIABLE")
    private fun loadFootballH2H() {
        // TODO: Implement when needed
        Log.d(TAG, "Loading football H2H for fixture $fixtureId")
        val currentState = _uiState.value
        if (currentState is MatchDetailUiState.Success) {
            val homeTeamId = currentState.fixture.teams.home.id
            val awayTeamId = currentState.fixture.teams.away.id
            val homeTeam = currentState.fixture.teams.home.name
            val awayTeam = currentState.fixture.teams.away.name

            // Log H2H viewed
            FirebaseAnalyticsHelper.logHeadToHeadViewed(sport, homeTeam, awayTeam)

            // repository.getFootballHeadToHead(homeTeamId, awayTeamId)
        }
    }

    @Suppress("UNUSED_VARIABLE")
    private fun loadFootballStandings() {
        // TODO: Implement when needed
        Log.d(TAG, "Loading football standings for fixture $fixtureId")
        val currentState = _uiState.value
        if (currentState is MatchDetailUiState.Success) {
            val leagueId = currentState.fixture.league.id
            val season = currentState.fixture.league.season
            // repository.getFootballStandings(leagueId, season)
        }
    }
}

/**
 * Factory for creating MatchDetailViewModel with parameters
 */
class MatchDetailViewModelFactory(
    private val fixtureId: Int,
    private val sport: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchDetailViewModel::class.java)) {
            return MatchDetailViewModel(fixtureId, sport) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}