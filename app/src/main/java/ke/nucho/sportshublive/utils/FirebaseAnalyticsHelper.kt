package ke.nucho.sportshublive.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import ke.nucho.sportshublive.SportsHubApplication

/**
 * Helper class for Firebase Analytics events
 * Provides type-safe analytics logging
 */
object FirebaseAnalyticsHelper {

    private val analytics: FirebaseAnalytics
        get() = SportsHubApplication.analytics

    // ==================== SCREEN TRACKING ====================

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // ==================== SPORT SELECTION ====================

    fun logSportSelected(sport: String) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
        }
        analytics.logEvent("sport_selected", bundle)
    }

    fun logLeagueSelected(sport: String, leagueId: Int, leagueName: String) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("league_id", leagueId)
            putString("league_name", leagueName)
        }
        analytics.logEvent("league_selected", bundle)
    }

    // ==================== MATCH INTERACTIONS ====================

    fun logMatchViewed(
        fixtureId: Int,
        sport: String,
        leagueName: String,
        homeTeam: String,
        awayTeam: String
    ) {
        val bundle = Bundle().apply {
            putInt("fixture_id", fixtureId)
            putString("sport_name", sport)
            putString("league_name", leagueName)
            putString("home_team", homeTeam)
            putString("away_team", awayTeam)
        }
        analytics.logEvent("match_viewed", bundle)
    }

    fun logMatchRefreshed(sport: String) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
        }
        analytics.logEvent("match_refreshed", bundle)
    }

    // ==================== DATE SELECTION ====================

    fun logDateSelected(date: String, sport: String) {
        val bundle = Bundle().apply {
            putString("selected_date", date)
            putString("sport_name", sport)
        }
        analytics.logEvent("date_selected", bundle)
    }

    fun logLiveMatchesViewed(sport: String) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
        }
        analytics.logEvent("live_matches_viewed", bundle)
    }

    // ==================== AUTO-REFRESH ====================

    fun logAutoRefreshToggled(enabled: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("auto_refresh_enabled", enabled)
        }
        analytics.logEvent("auto_refresh_toggled", bundle)
    }

    // ==================== API CALLS ====================

    fun logApiCallSuccess(endpoint: String, sport: String, responseTime: Long) {
        val bundle = Bundle().apply {
            putString("endpoint", endpoint)
            putString("sport_name", sport)
            putLong("response_time_ms", responseTime)
        }
        analytics.logEvent("api_call_success", bundle)
    }

    fun logApiCallFailure(endpoint: String, sport: String, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("endpoint", endpoint)
            putString("sport_name", sport)
            putString("error_message", errorMessage)
        }
        analytics.logEvent("api_call_failure", bundle)
    }

    // ==================== ERROR TRACKING ====================

    fun logError(errorType: String, errorMessage: String, context: String? = null) {
        val bundle = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
            context?.let { putString("context", it) }
        }
        analytics.logEvent("app_error", bundle)
    }

    // ==================== USER PREFERENCES ====================

    fun logFavoriteSportSet(sport: String) {
        val bundle = Bundle().apply {
            putString("favorite_sport", sport)
        }
        analytics.logEvent("favorite_sport_set", bundle)

        // Also set as user property
        SportsHubApplication.instance.setUserProperty("favorite_sport", sport)
    }

    // ==================== ENGAGEMENT ====================

    // Note: session_start is automatically tracked by Firebase
    // No need to log it manually

    fun logAppOpened() {
        // Custom event for when user opens the app
        analytics.logEvent("app_opened", null)
    }

    fun logSessionEnd(duration: Long) {
        val bundle = Bundle().apply {
            putLong("session_duration_seconds", duration / 1000)
        }
        analytics.logEvent("user_session_end", bundle)  // Changed to avoid reserved name
    }

    // ==================== SEARCH & DISCOVERY ====================

    fun logSearch(query: String, resultCount: Int) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            putInt("result_count", resultCount)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    // ==================== SHARING ====================

    fun logShare(contentType: String, contentId: String, method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            putString(FirebaseAnalytics.Param.ITEM_ID, contentId)
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }
}