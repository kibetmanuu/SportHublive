package ke.nucho.sportshublive.ui.livescores

import android.util.Log
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
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

class LiveScoresViewModel : ViewModel() {

    // ✅ USE CACHED REPOSITORY FROM APPLICATION
    private val repository = SportsHubApplication.cachedRepository
    private var currentLoadingJob: Job? = null

    // UI State
    private val _uiState = MutableStateFlow<LiveScoresUiState>(LiveScoresUiState.Loading)
    val uiState: StateFlow<LiveScoresUiState> = _uiState.asStateFlow()

    // Selected Date
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Selected League
    private val _selectedLeague = MutableStateFlow<Int?>(null)
    val selectedLeague: StateFlow<Int?> = _selectedLeague.asStateFlow()

    // Auto-refresh state
    private val _isAutoRefreshEnabled = MutableStateFlow(true)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()

    // Live view state
    private val _isLiveView = MutableStateFlow(true)
    val isLiveView: StateFlow<Boolean> = _isLiveView.asStateFlow()

    // API Provider info
    private val _apiProvider = MutableStateFlow<String>("Football API")
    val apiProvider: StateFlow<String> = _apiProvider.asStateFlow()

    companion object {
        private const val TAG = "LiveScoresViewModel"

        val FOOTBALL_LEAGUES = mapOf(
            null to LeagueInfo("All Leagues", "🌍", "World"),
            39 to LeagueInfo("Premier League", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "England"),
            140 to LeagueInfo("La Liga", "🇪🇸", "Spain"),
            78 to LeagueInfo("Bundesliga", "🇩🇪", "Germany"),
            135 to LeagueInfo("Serie A", "🇮🇹", "Italy"),
            61 to LeagueInfo("Ligue 1", "🇫🇷", "France"),
            2 to LeagueInfo("Champions League", "⚽", "UEFA"),
            3 to LeagueInfo("Europa League", "🏆", "UEFA"),
            848 to LeagueInfo("Conference League", "🎯", "UEFA"),
            4 to LeagueInfo("World Cup", "🌎", "FIFA"),
            5 to LeagueInfo("Euro Championship", "🇪🇺", "UEFA")
        )
    }

    data class LeagueInfo(val name: String, val flag: String, val country: String)

    init {
        Log.d(TAG, "⚽ Football-Only Live Scores initialized (WITH CACHE)")
        FirebaseAnalyticsHelper.logScreenView("FootballLiveScores")

        // Load initial data immediately
        loadLiveMatches()
    }

    fun selectLeague(leagueId: Int?) {
        if (_selectedLeague.value == leagueId) return

        _selectedLeague.value = leagueId

        leagueId?.let {
            val leagueInfo = FOOTBALL_LEAGUES[it]
            FirebaseAnalyticsHelper.logLeagueSelected(
                it,
                leagueInfo?.name ?: "League $it"
            )
        }

        if (_isLiveView.value) {
            loadLiveMatches()
        } else {
            loadFixturesByDate(_selectedDate.value)
        }
    }

    fun loadLiveMatches(forceRefresh: Boolean = false) {
        currentLoadingJob?.cancel()

        _isLiveView.value = true
        _selectedDate.value = getCurrentDate()

        currentLoadingJob = viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading

            Log.d(TAG, "🔴 Loading live matches (League: ${_selectedLeague.value}, forceRefresh: $forceRefresh)")

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_live_football")
            trace.start()

            val startTime = System.currentTimeMillis()

            try {
                // ✅ USE CACHED REPOSITORY METHOD
                val result = repository.getLiveMatches(
                    leagueId = _selectedLeague.value,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )
                val responseTime = System.currentTimeMillis() - startTime

                result.onSuccess { fixtures ->
                    Log.d(TAG, "✅ Loaded ${fixtures.size} live matches in ${responseTime}ms")

                    updateUiState(
                        fixtures = fixtures,
                        emptyMessage = getEmptyMessage(isLive = true)
                    )

                    trace.putMetric("response_time_ms", responseTime)
                    trace.putAttribute("status", "success")
                    trace.putAttribute("count", fixtures.size.toString())
                    trace.putAttribute("from_cache", (responseTime < 500).toString())

                }.onFailure { e ->
                    Log.e(TAG, "❌ Error loading live matches: ${e.message}", e)
                    handleError(e, trace, responseTime)
                }

            } catch (e: Exception) {
                val responseTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Exception loading live matches: ${e.message}", e)
                e.printStackTrace()
                handleError(e, trace, responseTime)
            } finally {
                trace.stop()
            }
        }
    }

    fun loadFixturesByDate(date: String, forceRefresh: Boolean = false) {
        currentLoadingJob?.cancel()

        _isLiveView.value = false
        _selectedDate.value = date

        currentLoadingJob = viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading
            FirebaseAnalyticsHelper.logDateSelected(date)

            val trace = FirebasePerformance.getInstance().newTrace("load_date_football")
            trace.start()
            trace.putAttribute("date", date)
            val startTime = System.currentTimeMillis()

            try {
                val currentDate = getCurrentDate()
                val selectedYear = date.substring(0, 4).toInt()

                val dateType = when {
                    date < currentDate -> "PAST"
                    date > currentDate -> "FUTURE"
                    else -> "TODAY"
                }

                Log.d(TAG, "🔵 Loading $dateType matches ($date, Year: $selectedYear, forceRefresh: $forceRefresh)")

                // Map API-Sports league ID to Football-Data league ID if needed
                val mappedLeagueId = _selectedLeague.value?.let { mapLeagueId(it) }

                // ✅ USE CACHED REPOSITORY METHOD
                val result = repository.getMatchesByDate(
                    date = date,
                    leagueId = mappedLeagueId,
                    forceRefresh = forceRefresh  // ✅ Cache-aware!
                )
                val responseTime = System.currentTimeMillis() - startTime

                result.onSuccess { fixtures ->
                    Log.d(TAG, "✅ Loaded ${fixtures.size} matches for $date in ${responseTime}ms")
                    updateUiState(fixtures, getEmptyMessage(isLive = false, date = date))

                    trace.putMetric("response_time_ms", responseTime)
                    trace.putAttribute("status", "success")
                    trace.putAttribute("year", selectedYear.toString())
                    trace.putAttribute("from_cache", (responseTime < 500).toString())

                }.onFailure { e ->
                    Log.e(TAG, "❌ Error loading matches for $date: ${e.message}", e)
                    handleError(e, trace, responseTime)
                }

            } catch (e: Exception) {
                val responseTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Exception loading matches for $date: ${e.message}", e)
                handleError(e, trace, responseTime)
            } finally {
                trace.stop()
            }
        }
    }

    // Helper function to map API-Sports league IDs to Football-Data league IDs
    private fun mapLeagueId(apiSportsId: Int): Int {
        return when (apiSportsId) {
            39 -> 2021    // Premier League
            140 -> 2014   // La Liga
            78 -> 2002    // Bundesliga
            135 -> 2019   // Serie A
            61 -> 2015    // Ligue 1
            2 -> 2001     // Champions League
            3 -> 2018     // Europa League
            else -> apiSportsId
        }
    }

    private fun updateUiState(fixtures: List<Fixture>, emptyMessage: String) {
        _uiState.value = if (fixtures.isEmpty()) {
            LiveScoresUiState.Empty(emptyMessage)
        } else {
            LiveScoresUiState.Success(fixtures)
        }
    }

    private fun getEmptyMessage(isLive: Boolean, date: String? = null): String {
        val leagueId = _selectedLeague.value
        val leagueName = leagueId?.let { FOOTBALL_LEAGUES[it]?.name }

        return when {
            leagueName != null && isLive ->
                "⚽ No Live $leagueName Matches\n\n" +
                        "Try:\n" +
                        "• Check back during match hours\n" +
                        "• View a specific date\n" +
                        "• Select 'All Leagues'"

            leagueName != null && date != null ->
                "⚽ No $leagueName Matches on $date\n\n" +
                        "Try:\n" +
                        "• Select a different date\n" +
                        "• Switch to Live view\n" +
                        "• View 'All Leagues'"

            isLive ->
                "⚽ No Live Matches Right Now\n\n" +
                        "Try:\n" +
                        "• Select a specific league\n" +
                        "• View yesterday's or tomorrow's matches\n" +
                        "• Check back during peak hours"

            date != null ->
                "⚽ No Matches Found on $date\n\n" +
                        "Try:\n" +
                        "• Select a different date\n" +
                        "• Switch to Live view\n" +
                        "• Select a specific league"

            else -> "No football matches available"
        }
    }

    private fun handleError(
        e: Throwable,
        trace: com.google.firebase.perf.metrics.Trace,
        responseTime: Long
    ) {
        val errorMessage = when {
            e.message?.contains("Free plans do not have access to this season") == true -> {
                "🔒 API-Sports Free Plan Limitation\n\n" +
                        "The free plan only supports seasons 2022-2024.\n" +
                        "Current season (2025/2026) requires a paid plan.\n\n" +
                        "Try:\n" +
                        "• Selecting a date from 2022-2024\n" +
                        "• Using Live view for current matches"
            }
            e.message?.contains("403") == true || e.message?.contains("Forbidden") == true -> {
                "🔒 Authentication Error\n\n" +
                        "Your API key is invalid or expired.\n\n" +
                        "Please check your API key in Firebase Remote Config."
            }
            e.message?.contains("429") == true -> {
                "⏱️ Rate Limit Reached\n\n" +
                        "Too many requests to the API.\n\n" +
                        "Please wait a moment and try again.\n\n" +
                        "💡 Tip: The app uses caching to reduce API calls!"
            }
            e.message?.contains("404") == true -> {
                "❓ Data Not Found\n\n" +
                        "No data available for this request.\n\n" +
                        "Try:\n" +
                        "• Select a different date\n" +
                        "• Choose a different league"
            }
            e.message?.contains("timeout") == true || e.message?.contains("SocketTimeout") == true -> {
                "⏳ Request Timeout\n\n" +
                        "The server took too long to respond.\n\n" +
                        "Please:\n" +
                        "• Check your internet connection\n" +
                        "• Try again in a moment"
            }
            e.message?.contains("UnknownHost") == true || e.message?.contains("Unable to resolve host") == true -> {
                "📡 No Internet Connection\n\n" +
                        "Cannot connect to the server.\n\n" +
                        "Please check your internet connection."
            }
            else -> {
                "❌ Unable to Load Matches\n\n" +
                        "Error: ${e.message ?: "Unknown error"}\n\n" +
                        "Please:\n" +
                        "• Check your internet connection\n" +
                        "• Tap 'Try Again' to retry"
            }
        }

        _uiState.value = LiveScoresUiState.Error(errorMessage)

        trace.putMetric("response_time_ms", responseTime)
        trace.putAttribute("status", "error")
        trace.putAttribute("error_type", e.javaClass.simpleName)
        trace.putAttribute("error_message", e.message ?: "unknown")
    }

    fun toggleAutoRefresh() {
        _isAutoRefreshEnabled.value = !_isAutoRefreshEnabled.value
        FirebaseAnalyticsHelper.logAutoRefreshToggled(_isAutoRefreshEnabled.value)
        Log.d(TAG, "🔄 Auto-refresh: ${_isAutoRefreshEnabled.value}")
    }

    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh (force refresh = true)")
        val viewType = if (_isLiveView.value) "live" else "date"
        FirebaseAnalyticsHelper.logMatchRefreshed(viewType)

        if (_isLiveView.value) {
            loadLiveMatches(forceRefresh = true)  // ✅ Force fresh data
        } else {
            loadFixturesByDate(_selectedDate.value, forceRefresh = true)  // ✅ Force fresh data
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        currentLoadingJob?.cancel()
        Log.d(TAG, "🔚 ViewModel cleared")
    }
}

sealed class LiveScoresUiState {
    object Loading : LiveScoresUiState()
    data class Success(val fixtures: List<Fixture>) : LiveScoresUiState()
    data class Error(val message: String) : LiveScoresUiState()
    data class Empty(val message: String) : LiveScoresUiState()
}