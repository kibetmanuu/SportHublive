package ke.nucho.sportshublive.ui.livescores

import android.util.Log
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
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

class LiveScoresViewModel : ViewModel() {

    private val apiConfigManager = ApiConfigManager()
    private var repository: UnifiedFootballRepository? = null
    private var currentLoadingJob: Job? = null
    private var initializationAttempted = false

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
    private val _apiProvider = MutableStateFlow<String>("")
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
        Log.d(TAG, "⚽ Football-Only Live Scores initialized")
        FirebaseAnalyticsHelper.logScreenView("FootballLiveScores")
        initializeRepository()
    }

    private fun initializeRepository() {
        viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading
            initializationAttempted = true

            try {
                Log.d(TAG, "🔧 Starting repository initialization...")

                // Fetch remote config with timeout
                val success = apiConfigManager.fetchAndActivate()
                Log.d(TAG, "📡 Remote config fetch result: $success")

                if (success) {
                    // Get API configuration
                    val configResult = apiConfigManager.getApiConfig()

                    configResult.onSuccess { config ->
                        Log.d(TAG, "✅ Config loaded successfully")
                        Log.d(TAG, "🔑 Provider: ${config.provider}")
                        Log.d(TAG, "🌐 Base URL: ${config.baseUrl}")
                        Log.d(TAG, "🔐 API Key length: ${config.apiKey.length}")

                        // Validate API key
                        if (config.apiKey.isEmpty() || config.apiKey.contains("YOUR_")) {
                            Log.e(TAG, "❌ Invalid API key detected")
                            _uiState.value = LiveScoresUiState.Error(
                                "⚠️ API Key Not Configured\n\n" +
                                        "Please set your API key in Firebase Remote Config:\n\n" +
                                        "1. Go to Firebase Console\n" +
                                        "2. Navigate to Remote Config\n" +
                                        "3. Update 'api_config_json' with your API key\n" +
                                        "4. Publish changes\n\n" +
                                        "Current provider: ${config.provider}"
                            )
                            return@onSuccess
                        }

                        _apiProvider.value = when (config.provider) {
                            ApiConfigManager.ApiProvider.API_SPORTS -> "API-Sports"
                            ApiConfigManager.ApiProvider.FOOTBALL_DATA -> "Football-Data.org"
                        }

                        // Initialize repository
                        repository = UnifiedFootballRepository(apiConfigManager)
                        Log.d(TAG, "✅ Repository initialized successfully")

                        // Load initial data
                        loadLiveMatches()

                    }.onFailure { e ->
                        Log.e(TAG, "❌ Failed to get API config", e)
                        _uiState.value = LiveScoresUiState.Error(
                            "⚠️ Configuration Error\n\n" +
                                    "Failed to load API configuration from Firebase.\n\n" +
                                    "Error: ${e.message}\n\n" +
                                    "Please check:\n" +
                                    "• Firebase Remote Config is set up\n" +
                                    "• 'api_config_json' parameter exists\n" +
                                    "• Your API key is valid"
                        )
                    }
                } else {
                    Log.e(TAG, "❌ Failed to fetch remote config")
                    _uiState.value = LiveScoresUiState.Error(
                        "⚠️ Cannot Load Configuration\n\n" +
                                "Unable to fetch configuration from Firebase Remote Config.\n\n" +
                                "Please check:\n" +
                                "• Your internet connection\n" +
                                "• Firebase is properly initialized\n" +
                                "• Remote Config has required parameters\n\n" +
                                "Tap 'Try Again' to retry"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Initialization error", e)
                e.printStackTrace()
                _uiState.value = LiveScoresUiState.Error(
                    "❌ Initialization Failed\n\n" +
                            "An error occurred while setting up the app.\n\n" +
                            "Error: ${e.message}\n\n" +
                            "Stack trace logged to console.\n\n" +
                            "Tap 'Try Again' to retry"
                )
            }
        }
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
            // Check if repository is initialized
            if (repository == null) {
                Log.w(TAG, "⚠️ Repository not initialized, attempting to initialize...")

                if (!initializationAttempted) {
                    initializeRepository()
                } else {
                    _uiState.value = LiveScoresUiState.Error(
                        "⚠️ Repository Not Ready\n\n" +
                                "The app is still initializing or failed to initialize.\n\n" +
                                "Please:\n" +
                                "• Wait a moment and try again\n" +
                                "• Check your internet connection\n" +
                                "• Restart the app if the issue persists\n\n" +
                                "Tap 'Try Again' to retry initialization"
                    )
                }
                return@launch
            }

            _uiState.value = LiveScoresUiState.Loading

            Log.d(TAG, "🔴 Loading live matches (League: ${_selectedLeague.value})")

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_live_football")
            trace.start()

            val startTime = System.currentTimeMillis()

            try {
                val result = repository!!.getLiveMatches(_selectedLeague.value)
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
                    trace.putAttribute("provider", _apiProvider.value)

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
            // Check if repository is initialized
            if (repository == null) {
                Log.w(TAG, "⚠️ Repository not initialized, attempting to initialize...")

                if (!initializationAttempted) {
                    initializeRepository()
                } else {
                    _uiState.value = LiveScoresUiState.Error(
                        "⚠️ Repository Not Ready\n\n" +
                                "The app is still initializing or failed to initialize.\n\n" +
                                "Please:\n" +
                                "• Wait a moment and try again\n" +
                                "• Check your internet connection\n" +
                                "• Restart the app if the issue persists\n\n" +
                                "Tap 'Try Again' to retry initialization"
                    )
                }
                return@launch
            }

            _uiState.value = LiveScoresUiState.Loading

            FirebaseAnalyticsHelper.logDateSelected(date)
            Log.d(TAG, "📅 Loading matches for $date (League: ${_selectedLeague.value})")

            val trace = FirebasePerformance.getInstance()
                .newTrace("load_date_football")
            trace.start()
            trace.putAttribute("date", date)

            val startTime = System.currentTimeMillis()

            try {
                val result = repository!!.getMatchesByDate(date, _selectedLeague.value)
                val responseTime = System.currentTimeMillis() - startTime

                result.onSuccess { fixtures ->
                    Log.d(TAG, "✅ Loaded ${fixtures.size} matches for $date in ${responseTime}ms")

                    updateUiState(
                        fixtures = fixtures,
                        emptyMessage = getEmptyMessage(isLive = false, date = date)
                    )

                    trace.putMetric("response_time_ms", responseTime)
                    trace.putAttribute("status", "success")
                    trace.putAttribute("provider", _apiProvider.value)

                }.onFailure { e ->
                    Log.e(TAG, "❌ Error loading matches for $date: ${e.message}", e)
                    handleError(e, trace, responseTime)
                }

            } catch (e: Exception) {
                val responseTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Exception loading matches for $date: ${e.message}", e)
                e.printStackTrace()
                handleError(e, trace, responseTime)
            } finally {
                trace.stop()
            }
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
            e.message?.contains("403") == true || e.message?.contains("Forbidden") == true -> {
                "🔒 Authentication Error\n\n" +
                        "Your API key is invalid or expired.\n\n" +
                        "Please:\n" +
                        "• Check your API key in Firebase Remote Config\n" +
                        "• Verify your ${_apiProvider.value} subscription is active\n" +
                        "• Ensure the API key has the correct permissions\n\n" +
                        "Provider: ${_apiProvider.value}"
            }
            e.message?.contains("429") == true -> {
                "⏱️ Rate Limit Reached\n\n" +
                        "Too many requests to the ${_apiProvider.value} API.\n\n" +
                        "Please wait a moment and try again.\n\n" +
                        "Tip: Enable auto-refresh to avoid manual requests."
            }
            e.message?.contains("404") == true -> {
                "❓ Data Not Found\n\n" +
                        "The requested data is not available.\n\n" +
                        "Try:\n" +
                        "• Select a different league\n" +
                        "• Use Live view\n" +
                        "• Choose a different date"
            }
            e.message?.contains("timeout") == true || e.message?.contains("SocketTimeout") == true -> {
                "⏳ Request Timeout\n\n" +
                        "The ${_apiProvider.value} server took too long to respond.\n\n" +
                        "Please:\n" +
                        "• Check your internet connection\n" +
                        "• Try again in a moment"
            }
            e.message?.contains("UnknownHost") == true || e.message?.contains("Unable to resolve host") == true -> {
                "📡 No Internet Connection\n\n" +
                        "Cannot connect to ${_apiProvider.value}.\n\n" +
                        "Please:\n" +
                        "• Check your internet connection\n" +
                        "• Verify you can access the internet\n" +
                        "• Try again when connected"
            }
            else -> {
                "❌ Unable to Load Matches\n\n" +
                        "Provider: ${_apiProvider.value}\n" +
                        "Error: ${e.message ?: "Unknown error"}\n\n" +
                        "Please:\n" +
                        "• Check your internet connection\n" +
                        "• Tap 'Try Again' to retry\n" +
                        "• Check logcat for detailed error\n\n" +
                        "If issue persists, verify your API configuration."
            }
        }

        _uiState.value = LiveScoresUiState.Error(errorMessage)

        trace.putMetric("response_time_ms", responseTime)
        trace.putAttribute("status", "error")
        trace.putAttribute("error_type", e.javaClass.simpleName)
        trace.putAttribute("error_message", e.message ?: "unknown")
        trace.putAttribute("provider", _apiProvider.value)
    }

    fun toggleAutoRefresh() {
        _isAutoRefreshEnabled.value = !_isAutoRefreshEnabled.value
        FirebaseAnalyticsHelper.logAutoRefreshToggled(_isAutoRefreshEnabled.value)
        Log.d(TAG, "🔄 Auto-refresh: ${_isAutoRefreshEnabled.value}")
    }

    fun refresh() {
        Log.d(TAG, "🔄 Manual refresh")
        val viewType = if (_isLiveView.value) "live" else "date"
        FirebaseAnalyticsHelper.logMatchRefreshed(viewType)

        // If repository is null, retry initialization
        if (repository == null) {
            Log.d(TAG, "🔧 Repository null, retrying initialization...")
            initializeRepository()
            return
        }

        if (_isLiveView.value) {
            loadLiveMatches(forceRefresh = true)
        } else {
            loadFixturesByDate(_selectedDate.value, forceRefresh = true)
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