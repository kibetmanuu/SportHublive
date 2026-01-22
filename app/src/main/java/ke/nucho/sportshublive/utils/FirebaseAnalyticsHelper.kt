package ke.nucho.sportshublive.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import ke.nucho.sportshublive.SportsHubApplication

/**
 * Firebase Analytics Helper for Football Live Scores
 * Provides type-safe analytics logging for football-specific events
 */
object FirebaseAnalyticsHelper {

    private val analytics: FirebaseAnalytics
        get() = SportsHubApplication.analytics

    // ==================== SCREEN TRACKING ====================

    /**
     * Log screen views
     */
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // ==================== LEAGUE SELECTION ====================

    /**
     * Log when user selects a league filter
     */
    fun logLeagueSelected(leagueId: Int, leagueName: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("league_id", leagueId.toString())
            putString("league_name", leagueName)
        }
        analytics.logEvent("league_selected", bundle)
    }

    /**
     * Log when user views all leagues
     */
    fun logAllLeaguesSelected() {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("filter", "all_leagues")
        }
        analytics.logEvent("all_leagues_selected", bundle)
    }

    // ==================== MATCH INTERACTIONS ====================

    /**
     * Log when user clicks on a match
     */
    fun logMatchViewed(fixtureId: Int, leagueName: String, matchInfo: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("fixture_id", fixtureId)
            putString("league_name", leagueName)
            putString("match_info", matchInfo)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("match_viewed", bundle)
    }

    /**
     * Log when user refreshes match list
     */
    fun logMatchRefreshed(viewType: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("view_type", viewType) // "live" or "date"
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("match_refreshed", bundle)
    }

    // ==================== MATCH DETAIL SCREEN ====================

    /**
     * Log when user opens match detail screen
     */
    fun logMatchDetailOpened(fixtureId: Int, matchStatus: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("fixture_id", fixtureId)
            putString("match_status", matchStatus) // "live", "finished", "scheduled"
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("match_detail_opened", bundle)
    }

    /**
     * Log when user switches tabs in match detail
     */
    fun logTabSelected(tabName: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("tab_name", tabName) // "overview", "stats", "lineups", "events", "h2h"
        }
        analytics.logEvent("match_detail_tab_selected", bundle)
    }

    /**
     * Log when user refreshes match detail
     */
    fun logMatchDetailRefreshed(fixtureId: Int) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("fixture_id", fixtureId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("match_detail_refreshed", bundle)
    }

    /**
     * Log when user views statistics
     */
    fun logStatisticsViewed(fixtureId: Int, hasData: Boolean) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("fixture_id", fixtureId)
            putBoolean("has_data", hasData)
        }
        analytics.logEvent("statistics_viewed", bundle)
    }

    /**
     * Log when user views lineups
     */
    fun logLineupsViewed(fixtureId: Int, hasData: Boolean) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("fixture_id", fixtureId)
            putBoolean("has_data", hasData)
        }
        analytics.logEvent("lineups_viewed", bundle)
    }

    /**
     * Log when user views events (goals, cards, etc.)
     */
    fun logEventsViewed(fixtureId: Int, eventCount: Int) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("fixture_id", fixtureId)
            putInt("event_count", eventCount)
        }
        analytics.logEvent("match_events_viewed", bundle)
    }

    /**
     * Log when user views H2H data
     */
    fun logHeadToHeadViewed(team1: String, team2: String, matchCount: Int) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("team1", team1)
            putString("team2", team2)
            putInt("h2h_matches", matchCount)
        }
        analytics.logEvent("head_to_head_viewed", bundle)
    }

    // ==================== DATE & VIEW SELECTION ====================

    /**
     * Log when user selects a specific date
     */
    fun logDateSelected(date: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("selected_date", date)
        }
        analytics.logEvent("date_selected", bundle)
    }

    /**
     * Log when user switches to live view
     */
    fun logLiveViewSelected() {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("view_type", "live")
        }
        analytics.logEvent("live_view_selected", bundle)
    }

    /**
     * Log when user views live matches
     */
    fun logLiveMatchesViewed(matchCount: Int) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("view_type", "live")
            putInt("match_count", matchCount)
        }
        analytics.logEvent("live_matches_viewed", bundle)
    }

    // ==================== AUTO-REFRESH ====================

    /**
     * Log when user toggles auto-refresh
     */
    fun logAutoRefreshToggled(enabled: Boolean) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putBoolean("enabled", enabled)
        }
        analytics.logEvent("auto_refresh_toggled", bundle)
    }

    /**
     * Log auto-refresh cycle
     */
    fun logAutoRefreshCycle(matchCount: Int, isLive: Boolean) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("match_count", matchCount)
            putBoolean("is_live", isLive)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("auto_refresh_cycle", bundle)
    }

    // ==================== API PROVIDER ====================

    /**
     * Log API provider info
     */
    fun logApiProvider(provider: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("api_provider", provider) // "api_sports" or "football_data"
        }
        analytics.logEvent("api_provider_used", bundle)

        // Set as user property
        SportsHubApplication.instance.setUserProperty("api_provider", provider)
    }

    /**
     * Log successful API call
     */
    fun logApiCallSuccess(endpoint: String, responseTime: Long, resultCount: Int) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("endpoint", endpoint)
            putLong("response_time_ms", responseTime)
            putInt("result_count", resultCount)
        }
        analytics.logEvent("api_call_success", bundle)
    }

    /**
     * Log failed API call
     */
    fun logApiCallFailure(endpoint: String, errorCode: Int, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("endpoint", endpoint)
            putInt("error_code", errorCode)
            putString("error_message", errorMessage.take(100)) // Limit length
        }
        analytics.logEvent("api_call_failure", bundle)
    }

    // ==================== ERROR TRACKING ====================

    /**
     * Log general errors
     */
    fun logError(errorType: String, errorMessage: String, context: String? = null) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("error_type", errorType)
            putString("error_message", errorMessage.take(100))
            context?.let { putString("context", it) }
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("app_error", bundle)
    }

    /**
     * Log when no matches found
     */
    fun logNoMatchesFound(viewType: String, leagueId: Int?, date: String?) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("view_type", viewType) // "live" or "date"
            leagueId?.let { putInt("league_id", it) }
            date?.let { putString("date", it) }
        }
        analytics.logEvent("no_matches_found", bundle)
    }

    // ==================== USER ENGAGEMENT ====================

    /**
     * Log app opened
     */
    fun logAppOpened() {
        val bundle = Bundle().apply {
            putString("sport", "football")
        }
        analytics.logEvent("app_opened", bundle)
    }

    /**
     * Log session end
     */
    fun logSessionEnd(durationSeconds: Long) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putLong("session_duration_seconds", durationSeconds)
        }
        analytics.logEvent("session_end", bundle)
    }

    /**
     * Log screen engagement time
     */
    fun logScreenEngagement(screenName: String, engagementSeconds: Long) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putLong("engagement_seconds", engagementSeconds)
        }
        analytics.logEvent("screen_engagement", bundle)
    }

    // ==================== USER PREFERENCES ====================

    /**
     * Set favorite league as user property
     */
    fun setFavoriteLeague(leagueId: Int, leagueName: String) {
        SportsHubApplication.instance.setUserProperty("favorite_league_id", leagueId.toString())
        SportsHubApplication.instance.setUserProperty("favorite_league", leagueName)

        val bundle = Bundle().apply {
            putString("sport", "football")
            putInt("league_id", leagueId)
            putString("league_name", leagueName)
        }
        analytics.logEvent("favorite_league_set", bundle)
    }

    /**
     * Set favorite team as user property
     */
    fun setFavoriteTeam(teamName: String) {
        SportsHubApplication.instance.setUserProperty("favorite_team", teamName)

        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("team_name", teamName)
        }
        analytics.logEvent("favorite_team_set", bundle)
    }

    /**
     * Set custom user property
     */
    fun setUserProperty(propertyName: String, propertyValue: String) {
        SportsHubApplication.instance.setUserProperty(propertyName, propertyValue)
    }

    // ==================== CACHE & PERFORMANCE ====================

    /**
     * Log cache hit
     */
    fun logCacheHit(dataType: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("data_type", dataType)
        }
        analytics.logEvent("cache_hit", bundle)
    }

    /**
     * Log cache miss
     */
    fun logCacheMiss(dataType: String) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString("data_type", dataType)
        }
        analytics.logEvent("cache_miss", bundle)
    }

    /**
     * Log screen load time
     */
    fun logScreenLoadTime(screenName: String, loadTimeMs: Long) {
        val bundle = Bundle().apply {
            putString("sport", "football")
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putLong("load_time_ms", loadTimeMs)
        }
        analytics.logEvent("screen_load_time", bundle)
    }
}