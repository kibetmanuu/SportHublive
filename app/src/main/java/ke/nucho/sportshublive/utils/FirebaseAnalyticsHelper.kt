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

    // ==================== MATCH DETAIL SCREEN ====================

    /**
     * Log when user views match details
     */
    fun logMatchDetailViewed(
        sport: String,
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
            putString("home_team", homeTeam)
            putString("away_team", awayTeam)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("match_detail_viewed", bundle)
    }

    /**
     * Log when user switches tabs in match detail
     */
    fun logMatchDetailTabSelected(
        sport: String,
        fixtureId: Int,
        tabName: String
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
            putString("tab_name", tabName)
        }
        analytics.logEvent("match_detail_tab_selected", bundle)
    }

    /**
     * Log when user refreshes match details
     */
    fun logMatchDetailRefreshed(
        sport: String,
        fixtureId: Int
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
        }
        analytics.logEvent("match_detail_refreshed", bundle)
    }

    /**
     * Log when user shares match details
     */
    fun logMatchShared(
        sport: String,
        fixtureId: Int,
        shareMethod: String
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "match")
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
            putString(FirebaseAnalytics.Param.METHOD, shareMethod)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    /**
     * Log when user adds match to favorites
     */
    fun logMatchFavorited(
        sport: String,
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
            putString("home_team", homeTeam)
            putString("away_team", awayTeam)
        }
        analytics.logEvent("match_favorited", bundle)
    }

    /**
     * Log when user views statistics tab
     */
    fun logStatisticsViewed(
        sport: String,
        fixtureId: Int
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
        }
        analytics.logEvent("statistics_viewed", bundle)
    }

    /**
     * Log when user views lineups
     */
    fun logLineupsViewed(
        sport: String,
        fixtureId: Int
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putInt("fixture_id", fixtureId)
        }
        analytics.logEvent("lineups_viewed", bundle)
    }

    /**
     * Log when user views H2H data
     */
    fun logHeadToHeadViewed(
        sport: String,
        team1: String,
        team2: String
    ) {
        val bundle = Bundle().apply {
            putString("sport_name", sport)
            putString("team1", team1)
            putString("team2", team2)
        }
        analytics.logEvent("head_to_head_viewed", bundle)
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

    fun setFavoriteSport(sport: String) {
        SportsHubApplication.instance.setUserProperty("favorite_sport", sport)
    }

    fun setFavoriteTeam(teamName: String) {
        SportsHubApplication.instance.setUserProperty("favorite_team", teamName)
    }

    fun setUserProperty(propertyName: String, propertyValue: String) {
        SportsHubApplication.instance.setUserProperty(propertyName, propertyValue)
    }

    // ==================== ENGAGEMENT ====================

    fun logAppOpened() {
        analytics.logEvent("app_opened", null)
    }

    fun logSessionEnd(duration: Long) {
        val bundle = Bundle().apply {
            putLong("session_duration_seconds", duration / 1000)
        }
        analytics.logEvent("user_session_end", bundle)
    }

    /**
     * Log user screen engagement time
     * Changed from reserved "user_engagement" to "screen_engagement_time"
     */
    fun logUserEngagement(
        screenName: String,
        engagementTimeSeconds: Long
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putLong("engagement_time_seconds", engagementTimeSeconds)
        }
        analytics.logEvent("screen_engagement_time", bundle)
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
