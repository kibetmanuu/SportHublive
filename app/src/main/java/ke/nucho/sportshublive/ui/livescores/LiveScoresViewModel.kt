package ke.nucho.sportshublive.ui.livescores

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.perf.FirebasePerformance
import ke.nucho.sportshublive.data.models.*
import ke.nucho.sportshublive.data.repository.CachedSportsRepository
import ke.nucho.sportshublive.utils.FirebaseAnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel with Firestore caching support
 */
class LiveScoresViewModel : ViewModel() {

    // USE CACHED REPOSITORY INSTEAD OF DIRECT API
    private val repository = CachedSportsRepository()

    // UI State
    private val _uiState = MutableStateFlow<LiveScoresUiState>(LiveScoresUiState.Loading)
    val uiState: StateFlow<LiveScoresUiState> = _uiState.asStateFlow()

    // Selected Date
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Selected Sport
    private val _selectedSport = MutableStateFlow("Football")
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    // Selected League
    private val _selectedLeague = MutableStateFlow<Int?>(null)
    val selectedLeague: StateFlow<Int?> = _selectedLeague.asStateFlow()

    // Auto-refresh state
    private val _isAutoRefreshEnabled = MutableStateFlow(true)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()

    companion object {
        private const val TAG = "LiveScoresViewModel"
    }

    init {
        Log.d(TAG, "🎯 ViewModel initialized with CachedSportsRepository")
        FirebaseAnalyticsHelper.logScreenView("LiveScoresScreen")
        loadLiveMatches()
    }

    fun selectSport(sport: String) {
        _selectedSport.value = sport
        _selectedLeague.value = null
        FirebaseAnalyticsHelper.logSportSelected(sport)

        Log.d(TAG, "Sport selected: $sport")

        if (_selectedDate.value == getCurrentDate()) {
            loadLiveMatches()
        } else {
            loadFixturesByDate(_selectedDate.value)
        }
    }

    fun selectLeague(leagueId: Int?) {
        _selectedLeague.value = leagueId
        leagueId?.let {
            FirebaseAnalyticsHelper.logLeagueSelected(_selectedSport.value, it, getLeagueName(it))
        }

        if (leagueId != null) {
            loadFixturesByLeague(leagueId)
        } else {
            loadLiveMatches()
        }
    }

    /**
     * Load live matches with caching
     */
    fun loadLiveMatches(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading

            Log.d(TAG, "📥 Loading live matches for ${_selectedSport.value} (forceRefresh: $forceRefresh)")

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_live_matches_${_selectedSport.value.lowercase()}")
            trace.start()
            trace.putAttribute("cached", (!forceRefresh).toString())

            val startTime = System.currentTimeMillis()

            try {
                val result = when (_selectedSport.value) {
                    "Football" -> {
                        Log.d(TAG, "Fetching football live matches...")
                        repository.getFootballLiveMatches(forceRefresh).map { fixtures ->
                            Log.d(TAG, "✓ Received ${fixtures.size} football fixtures")
                            handleFootballFixtures(fixtures)
                        }
                    }
                    "Basketball" -> {
                        Log.d(TAG, "Fetching basketball live games...")
                        repository.getBasketballLiveGames(forceRefresh).map { games ->
                            Log.d(TAG, "✓ Received ${games.size} basketball games")
                            handleBasketballGames(games)
                        }
                    }
                    "Hockey" -> {
                        Log.d(TAG, "Fetching hockey live games...")
                        repository.getHockeyLiveGames(forceRefresh).map { games ->
                            Log.d(TAG, "✓ Received ${games.size} hockey games")
                            handleHockeyGames(games)
                        }
                    }
                    "Formula 1" -> {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        Log.d(TAG, "Fetching F1 races for season $currentYear...")
                        repository.getFormula1Races(currentYear, forceRefresh).map { races ->
                            Log.d(TAG, "✓ Received ${races.size} F1 races")
                            handleF1Races(races)
                        }
                    }
                    "Volleyball" -> {
                        Log.d(TAG, "Fetching volleyball live games...")
                        repository.getVolleyballLiveGames(forceRefresh).map { games ->
                            Log.d(TAG, "✓ Received ${games.size} volleyball games")
                            handleVolleyballGames(games)
                        }
                    }
                    "Rugby" -> {
                        Log.d(TAG, "Fetching rugby live games...")
                        repository.getRugbyLiveGames(forceRefresh).map { games ->
                            Log.d(TAG, "✓ Received ${games.size} rugby games")
                            handleRugbyGames(games)
                        }
                    }
                    else -> {
                        Log.w(TAG, "Unknown sport: ${_selectedSport.value}")
                        Result.failure(Exception("Unknown sport"))
                    }
                }

                val responseTime = System.currentTimeMillis() - startTime

                result.onSuccess {
                    Log.d(TAG, "✅ Success! Response time: ${responseTime}ms")
                    FirebaseAnalyticsHelper.logApiCallSuccess("live_matches", _selectedSport.value, responseTime)
                    FirebaseAnalyticsHelper.logLiveMatchesViewed(_selectedSport.value)
                    trace.putMetric("response_time_ms", responseTime)
                    trace.putAttribute("status", "success")
                    trace.putAttribute("from_cache", (!forceRefresh).toString())
                }.onFailure { e ->
                    Log.e(TAG, "❌ Failed! Error: ${e.message}")
                    FirebaseAnalyticsHelper.logApiCallFailure("live_matches", _selectedSport.value, e.message ?: "Unknown")
                    _uiState.value = LiveScoresUiState.Error("Error: ${e.message}")
                    trace.putMetric("response_time_ms", responseTime)
                    trace.putAttribute("status", "error")
                }

            } catch (e: Exception) {
                val responseTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Exception caught: ${e.message}", e)
                FirebaseAnalyticsHelper.logApiCallFailure("live_matches", _selectedSport.value, e.message ?: "Unknown")
                _uiState.value = LiveScoresUiState.Error("Network error: ${e.message}")
                trace.putMetric("response_time_ms", responseTime)
                trace.putAttribute("status", "error")
            } finally {
                trace.stop()
            }
        }
    }

    /**
     * Load fixtures by date with caching
     */
    fun loadFixturesByDate(date: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading
            _selectedDate.value = date
            FirebaseAnalyticsHelper.logDateSelected(date, _selectedSport.value)

            Log.d(TAG, "📅 Loading fixtures for date: $date")

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_date_fixtures_${_selectedSport.value.lowercase()}")
            trace.start()
            trace.putAttribute("date", date)

            try {
                val result = when (_selectedSport.value) {
                    "Football" -> {
                        repository.getFootballFixturesByDate(date, forceRefresh).map { fixtures ->
                            handleFootballFixtures(fixtures)
                        }
                    }
                    "Basketball" -> {
                        repository.getBasketballGamesByDate(date, forceRefresh).map { games ->
                            handleBasketballGames(games)
                        }
                    }
                    else -> {
                        Log.w(TAG, "Date fixtures not implemented for ${_selectedSport.value}")
                        Result.failure(Exception("Not implemented for this sport"))
                    }
                }

                result.onSuccess {
                    trace.putAttribute("status", "success")
                }.onFailure { e ->
                    _uiState.value = LiveScoresUiState.Error("Error: ${e.message}")
                    trace.putAttribute("status", "error")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading fixtures by date", e)
                _uiState.value = LiveScoresUiState.Error("Network error: ${e.message}")
                trace.putAttribute("status", "error")
            } finally {
                trace.stop()
            }
        }
    }

    /**
     * Load fixtures by league with caching
     */
    private fun loadFixturesByLeague(leagueId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading

            Log.d(TAG, "🏆 Loading fixtures for league: $leagueId")

            try {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val result = repository.getFootballFixturesByLeague(leagueId, currentYear, forceRefresh)

                result.onSuccess { fixtures ->
                    handleFootballFixtures(fixtures)
                }.onFailure { e ->
                    _uiState.value = LiveScoresUiState.Error("Error: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading fixtures by league", e)
                _uiState.value = LiveScoresUiState.Error("Network error: ${e.message}")
            }
        }
    }

    // ==================== HANDLERS ====================

    private fun handleFootballFixtures(fixtures: List<Fixture>) {
        if (fixtures.isEmpty()) {
            _uiState.value = LiveScoresUiState.Empty("No matches found")
        } else {
            val filteredFixtures = _selectedLeague.value?.let { leagueId ->
                fixtures.filter { it.league.id == leagueId }
            } ?: fixtures

            _uiState.value = if (filteredFixtures.isEmpty()) {
                LiveScoresUiState.Empty("No matches found")
            } else {
                LiveScoresUiState.Success(filteredFixtures)
            }
        }
    }

    private fun handleBasketballGames(games: List<BasketballGame>) {
        if (games.isEmpty()) {
            _uiState.value = LiveScoresUiState.Empty("No games found")
        } else {
            val fixtures = games.mapNotNull { convertBasketballToFixture(it) }
            _uiState.value = if (fixtures.isEmpty()) {
                LiveScoresUiState.Empty("No games found")
            } else {
                LiveScoresUiState.Success(fixtures)
            }
        }
    }

    private fun handleHockeyGames(games: List<HockeyGame>) {
        if (games.isEmpty()) {
            _uiState.value = LiveScoresUiState.Empty("No games found")
        } else {
            val fixtures = games.mapNotNull { convertHockeyToFixture(it) }
            _uiState.value = if (fixtures.isEmpty()) {
                LiveScoresUiState.Empty("No games found")
            } else {
                LiveScoresUiState.Success(fixtures)
            }
        }
    }

    private fun handleF1Races(races: List<F1Race>) {
        if (races.isEmpty()) {
            _uiState.value = LiveScoresUiState.Empty("No races found")
        } else {
            val fixtures = races.mapNotNull { convertF1ToFixture(it) }
            _uiState.value = if (fixtures.isEmpty()) {
                LiveScoresUiState.Empty("No races found")
            } else {
                LiveScoresUiState.Success(fixtures)
            }
        }
    }

    private fun handleVolleyballGames(games: List<VolleyballGame>) {
        if (games.isEmpty()) {
            _uiState.value = LiveScoresUiState.Empty("No games found")
        } else {
            val fixtures = games.mapNotNull { convertVolleyballToFixture(it) }
            _uiState.value = if (fixtures.isEmpty()) {
                LiveScoresUiState.Empty("No games found")
            } else {
                LiveScoresUiState.Success(fixtures)
            }
        }
    }

    private fun handleRugbyGames(games: List<RugbyGame>) {
        if (games.isEmpty()) {
            _uiState.value = LiveScoresUiState.Empty("No games found")
        } else {
            val fixtures = games.mapNotNull { convertRugbyToFixture(it) }
            _uiState.value = if (fixtures.isEmpty()) {
                LiveScoresUiState.Empty("No games found")
            } else {
                LiveScoresUiState.Success(fixtures)
            }
        }
    }

    // ==================== CONVERTERS - Keep your existing implementation ====================

    private fun convertBasketballToFixture(game: BasketballGame): Fixture? {
        // TODO: Use your existing converter implementation
        return null
    }

    private fun convertHockeyToFixture(game: HockeyGame): Fixture? {
        // TODO: Use your existing converter implementation
        return null
    }

    private fun convertF1ToFixture(race: F1Race): Fixture? {
        // TODO: Use your existing converter implementation
        return null
    }

    private fun convertVolleyballToFixture(game: VolleyballGame): Fixture? {
        // TODO: Use your existing converter implementation
        return null
    }

    private fun convertRugbyToFixture(game: RugbyGame): Fixture? {
        // TODO: Use your existing converter implementation
        return null
    }

    // ==================== UTILITY ====================

    fun toggleAutoRefresh() {
        _isAutoRefreshEnabled.value = !_isAutoRefreshEnabled.value
        FirebaseAnalyticsHelper.logAutoRefreshToggled(_isAutoRefreshEnabled.value)
    }

    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh triggered")
        FirebaseAnalyticsHelper.logMatchRefreshed(_selectedSport.value)
        loadLiveMatches(forceRefresh = true)
    }

    fun clearCache() {
        viewModelScope.launch {
            Log.d(TAG, "🗑️ Clearing all cache")
            repository.clearAllCache()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getLeagueName(leagueId: Int): String {
        return when (leagueId) {
            39 -> "Premier League"
            140 -> "La Liga"
            78 -> "Bundesliga"
            135 -> "Serie A"
            61 -> "Ligue 1"
            2 -> "Champions League"
            3 -> "Europa League"
            else -> "League $leagueId"
        }
    }
}

sealed class LiveScoresUiState {
    object Loading : LiveScoresUiState()
    data class Success(val fixtures: List<Fixture>) : LiveScoresUiState()
    data class Error(val message: String) : LiveScoresUiState()
    data class Empty(val message: String) : LiveScoresUiState()
}