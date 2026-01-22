package ke.nucho.sportshublive.data.models

import com.google.gson.annotations.SerializedName

/**
 * Main Fixture Response
 */
data class FixtureResponse(
    @SerializedName("response")
    val response: List<Fixture>
)

/**
 * Complete Fixture Model
 */
data class Fixture(
    @SerializedName("fixture")
    val fixture: FixtureDetails,

    @SerializedName("league")
    val league: League,

    @SerializedName("teams")
    val teams: Teams,

    @SerializedName("goals")
    val goals: Goals,

    @SerializedName("score")
    val score: Score? = null
)

/**
 * Fixture Details
 */
data class FixtureDetails(
    @SerializedName("id")
    val id: Int,

    @SerializedName("referee")
    val referee: String? = null,

    @SerializedName("timezone")
    val timezone: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("venue")
    val venue: Venue,

    @SerializedName("status")
    val status: MatchStatus
)

/**
 * Match Status
 */
data class MatchStatus(
    @SerializedName("long")
    val long: String,

    @SerializedName("short")
    val short: String,

    @SerializedName("elapsed")
    val elapsed: Int? = null
)

/**
 * Venue Information
 */
data class Venue(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("city")
    val city: String? = null
)

/**
 * League Information
 */
data class League(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("logo")
    val logo: String,

    @SerializedName("flag")
    val flag: String? = null,

    @SerializedName("season")
    val season: Int,

    @SerializedName("round")
    val round: String? = null
)

/**
 * Teams (Home and Away)
 */
data class Teams(
    @SerializedName("home")
    val home: Team,

    @SerializedName("away")
    val away: Team
)

/**
 * Team Information
 */
data class Team(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String,

    @SerializedName("winner")
    val winner: Boolean? = null
)

/**
 * Goals
 */
data class Goals(
    @SerializedName("home")
    val home: Int? = null,

    @SerializedName("away")
    val away: Int? = null
)

/**
 * Score (includes halftime, fulltime, extra time, penalty)
 */
data class Score(
    @SerializedName("halftime")
    val halftime: Goals? = null,

    @SerializedName("fulltime")
    val fulltime: Goals? = null,

    @SerializedName("extratime")
    val extratime: Goals? = null,

    @SerializedName("penalty")
    val penalty: Goals? = null
)

/**
 * Match Statistics Response
 */
data class StatisticsResponse(
    @SerializedName("response")
    val response: List<TeamStatistics>
)

/**
 * Team Statistics
 */
data class TeamStatistics(
    @SerializedName("team")
    val team: Team,

    @SerializedName("statistics")
    val statistics: List<Statistic>
)

/**
 * Individual Statistic
 */
data class Statistic(
    @SerializedName("type")
    val type: String,

    @SerializedName("value")
    val value: Any? // Can be String, Int, or null
)

/**
 * Match Events Response
 */
data class EventsResponse(
    @SerializedName("response")
    val response: List<MatchEvent>
)

/**
 * Match Event (Goals, Cards, Substitutions, etc.)
 */
data class MatchEvent(
    @SerializedName("time")
    val time: EventTime,

    @SerializedName("team")
    val team: Team,

    @SerializedName("player")
    val player: Player,

    @SerializedName("assist")
    val assist: Player? = null,

    @SerializedName("type")
    val type: String, // "Goal", "Card", "subst", etc.

    @SerializedName("detail")
    val detail: String, // "Normal Goal", "Yellow Card", etc.

    @SerializedName("comments")
    val comments: String? = null
)

/**
 * Event Time
 */
data class EventTime(
    @SerializedName("elapsed")
    val elapsed: Int,

    @SerializedName("extra")
    val extra: Int? = null
)

/**
 * Player Information
 */
data class Player(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

/**
 * Lineups Response
 */
data class LineupsResponse(
    @SerializedName("response")
    val response: List<TeamLineup>
)

/**
 * Team Lineup
 */
data class TeamLineup(
    @SerializedName("team")
    val team: Team,

    @SerializedName("formation")
    val formation: String,

    @SerializedName("startXI")
    val startXI: List<LineupPlayer>,

    @SerializedName("substitutes")
    val substitutes: List<LineupPlayer>,

    @SerializedName("coach")
    val coach: Coach
)

/**
 * Lineup Player
 */
data class LineupPlayer(
    @SerializedName("player")
    val player: PlayerDetails
)

/**
 * Player Details
 */
data class PlayerDetails(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("number")
    val number: Int,

    @SerializedName("pos")
    val pos: String, // "G", "D", "M", "F"

    @SerializedName("grid")
    val grid: String? = null
)

/**
 * Coach Information
 */
data class Coach(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("photo")
    val photo: String
)

/**
 * Predictions Response (for future features)
 */
data class PredictionsResponse(
    @SerializedName("response")
    val response: List<Prediction>
)

/**
 * Prediction Data
 */
data class Prediction(
    @SerializedName("predictions")
    val predictions: PredictionDetails,

    @SerializedName("league")
    val league: League,

    @SerializedName("teams")
    val teams: Teams
)

/**
 * Prediction Details
 */
data class PredictionDetails(
    @SerializedName("winner")
    val winner: Winner? = null,

    @SerializedName("win_or_draw")
    val winOrDraw: Boolean? = null,

    @SerializedName("under_over")
    val underOver: String? = null,

    @SerializedName("goals")
    val goals: GoalsPrediction? = null,

    @SerializedName("advice")
    val advice: String? = null,

    @SerializedName("percent")
    val percent: PredictionPercent? = null
)

/**
 * Winner Prediction
 */
data class Winner(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("comment")
    val comment: String
)

/**
 * Goals Prediction
 */
data class GoalsPrediction(
    @SerializedName("home")
    val home: String,

    @SerializedName("away")
    val away: String
)

/**
 * Prediction Percentages
 */
data class PredictionPercent(
    @SerializedName("home")
    val home: String,

    @SerializedName("draw")
    val draw: String,

    @SerializedName("away")
    val away: String
)

/**
 * Head to Head Response
 */
data class H2HResponse(
    @SerializedName("response")
    val response: List<Fixture>
)

/**
 * Standings Response
 */
data class StandingsResponse(
    @SerializedName("response")
    val response: List<LeagueStanding>
)

/**
 * League Standing
 */
data class LeagueStanding(
    @SerializedName("league")
    val league: StandingLeague
)

/**
 * Standing League
 */
data class StandingLeague(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("logo")
    val logo: String,

    @SerializedName("flag")
    val flag: String,

    @SerializedName("season")
    val season: Int,

    @SerializedName("standings")
    val standings: List<List<Standing>>
)

/**
 * Standing Entry
 */
data class Standing(
    @SerializedName("rank")
    val rank: Int,

    @SerializedName("team")
    val team: Team,

    @SerializedName("points")
    val points: Int,

    @SerializedName("goalsDiff")
    val goalsDiff: Int,

    @SerializedName("group")
    val group: String,

    @SerializedName("form")
    val form: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("all")
    val all: StandingStats,

    @SerializedName("home")
    val home: StandingStats,

    @SerializedName("away")
    val away: StandingStats,

    @SerializedName("update")
    val update: String
)

/**
 * Standing Statistics
 */
data class StandingStats(
    @SerializedName("played")
    val played: Int,

    @SerializedName("win")
    val win: Int,

    @SerializedName("draw")
    val draw: Int,

    @SerializedName("lose")
    val lose: Int,

    @SerializedName("goals")
    val goals: StandingGoals
)

/**
 * Standing Goals
 */
data class StandingGoals(
    @SerializedName("for")
    val goalsFor: Int,

    @SerializedName("against")
    val against: Int
)
// ============================================================================
// TOP SCORERS MODELS - Add these to your Models.kt file
// ============================================================================

/**
 * Top Scorers Response
 */
data class TopScorersResponse(
    @SerializedName("response")
    val response: List<TopScorerEntry>
)

/**
 * Top Scorer Entry
 */
data class TopScorerEntry(
    @SerializedName("player")
    val player: TopScorerPlayer,

    @SerializedName("statistics")
    val statistics: List<TopScorerStatistics>
)

/**
 * Top Scorer Player
 */
data class TopScorerPlayer(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("firstname")
    val firstname: String? = null,

    @SerializedName("lastname")
    val lastname: String? = null,

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("birth")
    val birth: PlayerBirth? = null,

    @SerializedName("nationality")
    val nationality: String? = null,

    @SerializedName("height")
    val height: String? = null,

    @SerializedName("weight")
    val weight: String? = null,

    @SerializedName("injured")
    val injured: Boolean? = null,

    @SerializedName("photo")
    val photo: String
)

/**
 * Player Birth Info
 */
data class PlayerBirth(
    @SerializedName("date")
    val date: String? = null,

    @SerializedName("place")
    val place: String? = null,

    @SerializedName("country")
    val country: String? = null
)

/**
 * Top Scorer Statistics
 */
data class TopScorerStatistics(
    @SerializedName("team")
    val team: Team,

    @SerializedName("league")
    val league: League,

    @SerializedName("games")
    val games: TopScorerGames,

    @SerializedName("goals")
    val goals: TopScorerGoals,

    @SerializedName("assists")
    val assists: TopScorerAssists? = null,

    @SerializedName("rating")
    val rating: String? = null
)

/**
 * Top Scorer Games
 */
data class TopScorerGames(
    @SerializedName("appearences")
    val appearances: Int? = null,

    @SerializedName("lineups")
    val lineups: Int? = null,

    @SerializedName("minutes")
    val minutes: Int? = null,

    @SerializedName("number")
    val number: Int? = null,

    @SerializedName("position")
    val position: String? = null,

    @SerializedName("rating")
    val rating: String? = null,

    @SerializedName("captain")
    val captain: Boolean? = null
)

/**
 * Top Scorer Goals
 */
data class TopScorerGoals(
    @SerializedName("total")
    val total: Int? = null,

    @SerializedName("conceded")
    val conceded: Int? = null,

    @SerializedName("assists")
    val assists: Int? = null,

    @SerializedName("saves")
    val saves: Int? = null
)

/**
 * Top Scorer Assists (alternative structure)
 */
data class TopScorerAssists(
    @SerializedName("total")
    val total: Int? = null
)