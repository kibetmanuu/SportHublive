package ke.nucho.sportshublive.ui.leaguedetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.nucho.sportshublive.SportsHubApplication
import ke.nucho.sportshublive.data.models.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LeagueDetailViewModel : ViewModel() {

    // ✅ USE CACHED REPOSITORY FROM APPLICATION
    private val repository = SportsHubApplication.cachedRepository

    private val _uiState = MutableStateFlow<LeagueDetailUiState>(LeagueDetailUiState.Loading)
    val uiState: StateFlow<LeagueDetailUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _leagueId = MutableStateFlow<Int?>(null)
    val leagueId: StateFlow<Int?> = _leagueId.asStateFlow()

    private var currentJob: Job? = null

    companion object {
        private const val TAG = "LeagueDetailViewModel"
    }

    init {
        Log.d(TAG, "⚽ League Detail initialized (WITH CACHE)")
    }

    fun loadLeagueDetails(leagueId: Int, forceRefresh: Boolean = false) {
        _leagueId.value = leagueId
        _selectedTab.value = 0
        viewModelScope.launch {
            loadFixtures(leagueId, forceRefresh)
        }
    }

    fun selectTab(tabIndex: Int) {
        currentJob?.cancel()
        _selectedTab.value = tabIndex

        _leagueId.value?.let { leagueId ->
            currentJob = viewModelScope.launch {
                when (tabIndex) {
                    0 -> loadFixtures(leagueId, forceRefresh = false)
                    1 -> loadStandings(leagueId, forceRefresh = false)
                    2 -> loadTopScorers(leagueId, forceRefresh = false)
                }
            }
        }
    }

    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh (force refresh = true)")
        _leagueId.value?.let { leagueId ->
            currentJob = viewModelScope.launch {
                when (_selectedTab.value) {
                    0 -> loadFixtures(leagueId, forceRefresh = true)
                    1 -> loadStandings(leagueId, forceRefresh = true)
                    2 -> loadTopScorers(leagueId, forceRefresh = true)
                }
            }
        }
    }

    // ✅ CACHED: Load fixtures with cache support
    private suspend fun loadFixtures(leagueId: Int, forceRefresh: Boolean = false) {
        executeApiCall(
            logTag = "📅 Loading fixtures",
            provider = "Football-Data.org (Cached)",
            call = {
                val season = getCurrentSeason()
                Log.d(TAG, "📅 Loading FULL SEASON fixtures for league $leagueId, season $season (forceRefresh: $forceRefresh)")

                // Calculate FULL SEASON date range
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Season start: August 1st of the season year
                calendar.set(Calendar.YEAR, season)
                calendar.set(Calendar.MONTH, Calendar.AUGUST)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val fromDate = dateFormat.format(calendar.time)

                // Season end: July 31st of the next year
                calendar.set(Calendar.YEAR, season + 1)
                calendar.set(Calendar.MONTH, Calendar.JULY)
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                val toDate = dateFormat.format(calendar.time)

                Log.d(TAG, "   📆 Full Season Range: $fromDate to $toDate")
                Log.d(TAG, "   🏆 Season: $season/${season + 1}")

                // ✅ USE CACHED METHOD
                repository.getLeagueFixtures(
                    leagueId = leagueId,
                    season = season,
                    from = fromDate,
                    to = toDate,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )
            },
            onSuccess = { fixtures ->
                Log.d(TAG, "✅ Loaded ${fixtures.size} fixtures for FULL SEASON")

                if (fixtures.isEmpty()) {
                    LeagueDetailUiState.FixturesSuccess(emptyList())
                } else {
                    val sortedFixtures = sortFixturesByTime(fixtures)
                    Log.d(TAG, "📊 Fixtures breakdown:")
                    Log.d(TAG, "   • Total: ${fixtures.size}")
                    Log.d(TAG, "   • Finished: ${fixtures.count { it.fixture.status.short == "FT" }}")
                    Log.d(TAG, "   • Upcoming: ${fixtures.count { it.fixture.status.short == "NS" }}")
                    Log.d(TAG, "   • Live: ${fixtures.count { isLive(it.fixture.status.short) }}")
                    LeagueDetailUiState.FixturesSuccess(sortedFixtures)
                }
            }
        )
    }

    // ✅ CACHED: Load standings with cache support
    private suspend fun loadStandings(leagueId: Int, forceRefresh: Boolean = false) {
        executeApiCall(
            logTag = "🏆 Loading standings",
            provider = "Football-Data.org (Cached)",
            call = {
                val season = getCurrentSeason()
                Log.d(TAG, "🏆 Loading standings for league $leagueId, season $season (forceRefresh: $forceRefresh)")

                // ✅ USE CACHED METHOD
                repository.getStandings(
                    leagueId = leagueId,
                    season = season,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )
            },
            onSuccess = { leagueStandings ->
                val standings = leagueStandings
                    .firstOrNull()
                    ?.league
                    ?.standings
                    ?.firstOrNull()
                    ?.map { it.toStandingItem() }
                    .orEmpty()

                Log.d(TAG, "✅ Converted ${standings.size} standing items")
                LeagueDetailUiState.StandingsSuccess(standings)
            }
        )
    }

    // ✅ CACHED: Load top scorers with cache support
    private suspend fun loadTopScorers(leagueId: Int, forceRefresh: Boolean = false) {
        executeApiCall(
            logTag = "⚽ Loading top scorers",
            provider = "Football-Data.org (Cached)",
            call = {
                val season = getCurrentSeason()
                Log.d(TAG, "⚽ Loading top scorers for league $leagueId, season $season (forceRefresh: $forceRefresh)")

                // ✅ USE CACHED METHOD
                repository.getTopScorers(
                    leagueId = leagueId,
                    season = season,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )
            },
            onSuccess = { scorerEntries ->
                val scorers = scorerEntries
                    .mapNotNull { it.toTopScorer() }
                    .sortedByDescending { it.goals }

                Log.d(TAG, "✅ Converted ${scorers.size} top scorers")
                LeagueDetailUiState.TopScorersSuccess(scorers)
            }
        )
    }

    private suspend fun <T> executeApiCall(
        logTag: String,
        provider: String,
        call: suspend () -> Result<T>,
        onSuccess: (T) -> LeagueDetailUiState
    ) {
        _uiState.value = LeagueDetailUiState.Loading

        try {
            val startTime = System.currentTimeMillis()

            call()
                .onSuccess { data ->
                    val responseTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "✅ $logTag completed in ${responseTime}ms")
                    _uiState.value = onSuccess(data)
                }
                .onFailure { e ->
                    val responseTime = System.currentTimeMillis() - startTime
                    Log.e(TAG, "❌ Error: $logTag (${responseTime}ms)", e)

                    val errorMessage = when {
                        e.message?.contains("403") == true || e.message?.contains("Forbidden") == true ->
                            "🔒 Access Denied\n\n" +
                                    "Your API key is invalid or expired.\n\n" +
                                    "Please check Firebase Remote Config.\n\n" +
                                    "Provider: $provider"
                        e.message?.contains("429") == true ->
                            "⏱️ Rate Limit Exceeded\n\n" +
                                    "Too many requests.\n" +
                                    "Please wait a moment.\n\n" +
                                    "💡 Tip: The app uses caching to reduce API calls!\n\n" +
                                    "Provider: $provider"
                        e.message?.contains("404") == true ->
                            "❓ Competition Not Found\n\n" +
                                    "This league may not be available.\n\n" +
                                    "Available leagues:\n" +
                                    "• Premier League (39)\n" +
                                    "• La Liga (140)\n" +
                                    "• Bundesliga (78)\n" +
                                    "• Serie A (135)\n" +
                                    "• Ligue 1 (61)\n" +
                                    "• Champions League (2)\n" +
                                    "• Europa League (3)\n\n" +
                                    "Provider: $provider"
                        e.message?.contains("timeout") == true || e.message?.contains("SocketTimeout") == true ->
                            "⏳ Request Timeout\n\n" +
                                    "The server took too long to respond.\n\n" +
                                    "Please:\n" +
                                    "• Check your internet connection\n" +
                                    "• Try again in a moment"
                        e.message?.contains("UnknownHost") == true || e.message?.contains("Unable to resolve host") == true ->
                            "📡 No Internet Connection\n\n" +
                                    "Cannot connect to the server.\n\n" +
                                    "Please check your internet connection."
                        else ->
                            "❌ Failed to Load Data\n\n" +
                                    "Error: ${e.message}\n\n" +
                                    "Provider: $provider\n\n" +
                                    "Please:\n" +
                                    "• Check your internet connection\n" +
                                    "• Try again later"
                    }

                    _uiState.value = LeagueDetailUiState.Error(errorMessage)
                }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: $logTag", e)
            e.printStackTrace()
            _uiState.value = LeagueDetailUiState.Error(
                "Error: ${e.message ?: "Unknown error"}\n\nProvider: $provider"
            )
        }
    }

    private fun sortFixturesByTime(fixtures: List<Fixture>): List<Fixture> {
        val now = System.currentTimeMillis()
        val (past, future) = fixtures.partition {
            it.fixture.timestamp * 1000 < now
        }
        // Sort past matches newest first, future matches oldest first
        return past.sortedByDescending { it.fixture.timestamp } +
                future.sortedBy { it.fixture.timestamp }
    }

    private fun getCurrentSeason(): Int {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        // Season transitions in August
        return if (month >= Calendar.AUGUST) year else year - 1
    }

    private fun isLive(status: String): Boolean {
        return status in listOf("1H", "2H", "ET", "P", "LIVE", "HT")
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
        Log.d(TAG, "🔚 ViewModel cleared")
    }
}

// Sealed class for UI states
sealed class LeagueDetailUiState {
    object Loading : LeagueDetailUiState()
    data class FixturesSuccess(val fixtures: List<Fixture>) : LeagueDetailUiState()
    data class StandingsSuccess(val standings: List<StandingItem>) : LeagueDetailUiState()
    data class TopScorersSuccess(val scorers: List<TopScorer>) : LeagueDetailUiState()
    data class Error(val message: String) : LeagueDetailUiState()
}

// UI Data models
data class StandingItem(
    val position: Int,
    val teamName: String,
    val teamLogo: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val form: String = ""
)

data class TopScorer(
    val playerName: String,
    val playerPhoto: String,
    val teamName: String,
    val teamLogo: String,
    val goals: Int,
    val assists: Int,
    val appearances: Int,
    val position: String = ""
)

// Extension functions for cleaner mapping
private fun Standing.toStandingItem() = StandingItem(
    position = rank,
    teamName = team.name,
    teamLogo = team.logo,
    played = all.played,
    won = all.win,
    drawn = all.draw,
    lost = all.lose,
    goalsFor = all.goals.goalsFor,
    goalsAgainst = all.goals.against,
    goalDifference = goalsDiff,
    points = points,
    form = form ?: ""
)

private fun TopScorerEntry.toTopScorer(): TopScorer? {
    return statistics.firstOrNull()?.let { stats ->
        val goals = stats.goals.total ?: 0
        if (goals > 0) {
            TopScorer(
                playerName = player.name,
                playerPhoto = player.photo ?: "",
                teamName = stats.team.name,
                teamLogo = stats.team.logo,
                goals = goals,
                assists = stats.goals.assists ?: stats.assists?.total ?: 0,
                appearances = stats.games.appearances ?: 0,
                position = stats.games.position ?: ""
            )
        } else null
    }
}