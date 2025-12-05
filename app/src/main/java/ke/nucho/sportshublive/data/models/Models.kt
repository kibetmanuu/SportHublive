package ke.nucho.sportshublive.data.models

import com.google.gson.annotations.SerializedName

// ==================== API-FOOTBALL MODELS ====================


data class FootballResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<Fixture>
)

data class Paging(
    val current: Int,
    val total: Int
)

data class Fixture(
    val fixture: FixtureInfo,
    val league: League,
    val teams: Teams,
    val goals: Goals,
    val score: Score
)

data class FixtureInfo(
    val id: Int,
    val referee: String?,
    val timezone: String,
    val date: String,
    val timestamp: Long,
    val periods: Periods,
    val venue: Venue,
    val status: Status
)

data class Periods(
    val first: Long?,
    val second: Long?
)

data class Venue(
    val id: Int?,
    val name: String?,
    val city: String?
)

data class Status(
    val long: String,
    val short: String,
    val elapsed: Int?
)

data class League(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?,
    val season: Int,
    val round: String?
)

data class Teams(
    val home: Team,
    val away: Team
)

data class Team(
    val id: Int,
    val name: String,
    val logo: String,
    val winner: Boolean?
)

data class Goals(
    val home: Int?,
    val away: Int?
)

data class Score(
    val halftime: Goals,
    val fulltime: Goals,
    val extratime: Goals,
    val penalty: Goals
)

data class StandingsResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<StandingsData>
)

data class StandingsData(
    val league: LeagueStandings
)

data class LeagueStandings(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?,
    val season: Int,
    val standings: List<List<Standing>>
)

data class Standing(
    val rank: Int,
    val team: Team,
    val points: Int,
    val goalsDiff: Int,
    val group: String,
    val form: String,
    val status: String,
    val description: String?,
    val all: MatchStats,
    val home: MatchStats,
    val away: MatchStats,
    val update: String
)

data class MatchStats(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goals: MatchGoals
)

data class MatchGoals(
    @SerializedName("for") val goalsFor: Int,
    @SerializedName("against") val goalsAgainst: Int
)

data class LeaguesResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<LeagueData>
)

data class LeagueData(
    val league: LeagueInfo,
    val country: Country,
    val seasons: List<Season>
)

data class LeagueInfo(
    val id: Int,
    val name: String,
    val type: String,
    val logo: String
)

data class Country(
    val name: String,
    val code: String?,
    val flag: String?
)

data class Season(
    val year: Int,
    val start: String,
    val end: String,
    val current: Boolean,
    val coverage: Coverage
)

data class Coverage(
    val fixtures: Fixtures,
    val standings: Boolean,
    val players: Boolean,
    val top_scorers: Boolean,
    val top_assists: Boolean,
    val top_cards: Boolean,
    val injuries: Boolean,
    val predictions: Boolean,
    val odds: Boolean
)

data class Fixtures(
    val events: Boolean,
    val lineups: Boolean,
    val statistics_fixtures: Boolean,
    val statistics_players: Boolean
)

data class TeamResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<TeamData>
)

data class TeamData(
    val team: TeamDetails,
    val venue: VenueDetails
)

data class TeamDetails(
    val id: Int,
    val name: String,
    val code: String?,
    val country: String,
    val founded: Int?,
    val national: Boolean,
    val logo: String
)

data class VenueDetails(
    val id: Int?,
    val name: String?,
    val address: String?,
    val city: String?,
    val capacity: Int?,
    val surface: String?,
    val image: String?
)

data class TeamStatsResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: TeamStatistics?
)

data class TeamStatistics(
    val league: League,
    val team: Team,
    val form: String
)

data class PredictionsResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<PredictionData>
)

data class PredictionData(
    val predictions: Predictions,
    val league: League,
    val teams: Teams,
    val comparison: Comparison,
    val h2h: List<Fixture>
)

data class Predictions(
    val winner: PredictionWinner?,
    val win_or_draw: Boolean,
    val under_over: String?,
    val goals: PredictionGoals,
    val advice: String,
    val percent: PredictionPercent
)

data class PredictionWinner(
    val id: Int,
    val name: String,
    val comment: String
)

data class PredictionGoals(
    val home: String,
    val away: String
)

data class PredictionPercent(
    val home: String,
    val draw: String,
    val away: String
)

data class Comparison(
    val form: ComparisonStats,
    val att: ComparisonStats,
    val def: ComparisonStats,
    val poisson_distribution: ComparisonStats,
    val h2h: ComparisonStats,
    val goals: ComparisonStats,
    val total: ComparisonStats
)

data class ComparisonStats(
    val home: String,
    val away: String
)

// ==================== BASKETBALL MODELS ====================

data class BasketballResponse(
    val get: String?,
    val parameters: Map<String, String>?,
    val errors: List<String>?,
    val results: Int?,
    val response: List<BasketballGame>?
)

data class BasketballGame(
    val id: Int,
    val date: String,
    val time: String,
    val timestamp: Long,
    val timezone: String,
    val stage: String?,
    val week: String?,
    val status: BasketballStatus,
    val league: BasketballLeague,
    val country: BasketballCountry,
    val teams: BasketballTeams,
    val scores: BasketballScores
)

data class BasketballStatus(
    val long: String,
    val short: String,
    val timer: String?
)

data class BasketballLeague(
    val id: Int,
    val name: String,
    val type: String,
    val season: String,
    val logo: String
)

data class BasketballCountry(
    val id: Int,
    val name: String,
    val code: String?,
    val flag: String?
)

data class BasketballTeams(
    val home: BasketballTeam,
    val away: BasketballTeam
)

data class BasketballTeam(
    val id: Int,
    val name: String,
    val logo: String
)

data class BasketballScores(
    val home: BasketballScore,
    val away: BasketballScore
)

data class BasketballScore(
    val quarter_1: Int?,
    val quarter_2: Int?,
    val quarter_3: Int?,
    val quarter_4: Int?,
    val over_time: Int?,
    val total: Int?
)

data class BasketballStandingsResponse(
    val response: List<BasketballStanding>?
)

data class BasketballStanding(
    val position: Int,
    val team: BasketballTeam,
    val games: BasketballGames
)

data class BasketballGames(
    val played: Int,
    val win: BasketballWinLoss,
    val lose: BasketballWinLoss
)

data class BasketballWinLoss(
    val total: Int?,
    val percentage: String?
)

// ==================== HOCKEY MODELS ====================

data class HockeyResponse(
    val get: String?,
    val parameters: Map<String, String>?,
    val errors: List<String>?,
    val results: Int?,
    val response: List<HockeyGame>?
)

data class HockeyGame(
    val id: Int,
    val date: String,
    val time: String,
    val timestamp: Long,
    val timezone: String,
    val stage: String?,
    val week: String?,
    val status: HockeyStatus,
    val league: HockeyLeague,
    val country: HockeyCountry,
    val teams: HockeyTeams,
    val scores: HockeyScores
)

data class HockeyStatus(
    val long: String,
    val short: String,
    val timer: String?
)

data class HockeyLeague(
    val id: Int,
    val name: String,
    val type: String,
    val season: Int,
    val logo: String
)

data class HockeyCountry(
    val id: Int,
    val name: String,
    val code: String?,
    val flag: String?
)

data class HockeyTeams(
    val home: HockeyTeam,
    val away: HockeyTeam
)

data class HockeyTeam(
    val id: Int,
    val name: String,
    val logo: String
)

data class HockeyScores(
    val home: Int?,
    val away: Int?
)

data class HockeyStandingsResponse(
    val response: List<HockeyStanding>?
)

data class HockeyStanding(
    val position: Int,
    val team: HockeyTeam,
    val games: HockeyGames
)

data class HockeyGames(
    val played: Int,
    val win: HockeyWinLoss,
    val lose: HockeyWinLoss
)

data class HockeyWinLoss(
    val total: Int?,
    val percentage: String?
)

// ==================== FORMULA 1 MODELS ====================

data class Formula1Response(
    val get: String?,
    val parameters: Map<String, String>?,
    val errors: List<String>?,
    val results: Int?,
    val response: List<F1Race>?
)

data class F1Race(
    val id: Int,
    val competition: F1Competition,
    val circuit: F1Circuit,
    val season: Int,
    val type: String,
    val laps: F1Laps,
    val fastest_lap: F1FastestLap?,
    val distance: String?,
    val timezone: String,
    val date: String,
    val weather: String?,
    val status: String
)

data class F1Competition(
    val id: Int,
    val name: String,
    val location: F1Location
)

data class F1Location(
    val country: String,
    val city: String
)

data class F1Circuit(
    val id: Int,
    val name: String,
    val image: String?
)

data class F1Laps(
    val current: Int?,
    val total: Int?
)

data class F1FastestLap(
    val driver: F1Driver,
    val time: String
)

data class F1Driver(
    val id: Int,
    val name: String
)

data class F1DriversResponse(
    val response: List<F1DriverStanding>?
)

data class F1DriverStanding(
    val position: Int,
    val driver: F1Driver,
    val team: F1Team,
    val points: Int,
    val wins: Int,
    val behind: Int?
)

data class F1Team(
    val id: Int,
    val name: String,
    val logo: String?
)

// ==================== VOLLEYBALL MODELS ====================

data class VolleyballResponse(
    val get: String?,
    val parameters: Map<String, String>?,
    val errors: List<String>?,
    val results: Int?,
    val response: List<VolleyballGame>?
)

data class VolleyballGame(
    val id: Int,
    val date: String,
    val time: String,
    val timestamp: Long,
    val timezone: String,
    val stage: String?,
    val week: String?,
    val status: VolleyballStatus,
    val league: VolleyballLeague,
    val country: VolleyballCountry,
    val teams: VolleyballTeams,
    val scores: VolleyballScores
)

data class VolleyballStatus(
    val long: String,
    val short: String,
    val timer: String?
)

data class VolleyballLeague(
    val id: Int,
    val name: String,
    val type: String,
    val season: Int,
    val logo: String
)

data class VolleyballCountry(
    val id: Int,
    val name: String,
    val code: String?,
    val flag: String?
)

data class VolleyballTeams(
    val home: VolleyballTeam,
    val away: VolleyballTeam
)

data class VolleyballTeam(
    val id: Int,
    val name: String,
    val logo: String
)

data class VolleyballScores(
    val home: VolleyballScore?,
    val away: VolleyballScore?
)

data class VolleyballScore(
    val set_1: Int?,
    val set_2: Int?,
    val set_3: Int?,
    val set_4: Int?,
    val set_5: Int?,
    val total: Int?
)

data class VolleyballStandingsResponse(
    val response: List<VolleyballStanding>?
)

data class VolleyballStanding(
    val position: Int,
    val team: VolleyballTeam,
    val games: VolleyballGames
)

data class VolleyballGames(
    val played: Int,
    val win: VolleyballWinLoss,
    val lose: VolleyballWinLoss
)

data class VolleyballWinLoss(
    val total: Int?,
    val percentage: String?
)

// ==================== RUGBY MODELS ====================

data class RugbyResponse(
    val get: String?,
    val parameters: Map<String, String>?,
    val errors: List<String>?,
    val results: Int?,
    val response: List<RugbyGame>?
)

data class RugbyGame(
    val id: Int,
    val date: String,
    val time: String,
    val timestamp: Long,
    val timezone: String,
    val stage: String?,
    val week: String?,
    val status: RugbyStatus,
    val league: RugbyLeague,
    val country: RugbyCountry,
    val teams: RugbyTeams,
    val scores: RugbyScores
)

data class RugbyStatus(
    val long: String,
    val short: String,
    val timer: String?
)

data class RugbyLeague(
    val id: Int,
    val name: String,
    val type: String,
    val season: Int,
    val logo: String
)

data class RugbyCountry(
    val id: Int,
    val name: String,
    val code: String?,
    val flag: String?
)

data class RugbyTeams(
    val home: RugbyTeam,
    val away: RugbyTeam
)

data class RugbyTeam(
    val id: Int,
    val name: String,
    val logo: String
)

data class RugbyScores(
    val home: Int?,
    val away: Int?
)

data class RugbyStandingsResponse(
    val response: List<RugbyStanding>?
)

data class RugbyStanding(
    val position: Int,
    val team: RugbyTeam,
    val games: RugbyGames
)

data class RugbyGames(
    val played: Int,
    val win: RugbyWinLoss,
    val lose: RugbyWinLoss
)

data class RugbyWinLoss(
    val total: Int?,
    val percentage: String?
)