package ke.nucho.sportshublive.utils

import ke.nucho.sportshublive.data.models.*

/**
 * Utility class to convert different sport models to unified Fixture format
 */
object SportDataConverters {

    /**
     * Convert Basketball game to Fixture
     */
    fun convertBasketballToFixture(game: BasketballGame): Fixture {
        return Fixture(
            fixture = FixtureInfo(
                id = game.id,
                referee = null,
                timezone = game.timezone,
                date = "${game.date}T${game.time}:00Z",
                timestamp = game.timestamp,
                periods = Periods(first = null, second = null),
                venue = Venue(id = null, name = null, city = null),
                status = Status(
                    long = game.status.long,
                    short = game.status.short,
                    elapsed = game.status.timer?.toIntOrNull()
                )
            ),
            league = League(
                id = game.league.id,
                name = game.league.name,
                country = game.country.name,
                logo = game.league.logo,
                flag = game.country.flag,
                season = game.league.season.toIntOrNull() ?: 2024,
                round = game.stage
            ),
            teams = Teams(
                home = Team(
                    id = game.teams.home.id,
                    name = game.teams.home.name,
                    logo = game.teams.home.logo,
                    winner = determineBasketballWinner(game.scores.home.total, game.scores.away.total, true)
                ),
                away = Team(
                    id = game.teams.away.id,
                    name = game.teams.away.name,
                    logo = game.teams.away.logo,
                    winner = determineBasketballWinner(game.scores.home.total, game.scores.away.total, false)
                )
            ),
            goals = Goals(
                home = game.scores.home.total,
                away = game.scores.away.total
            ),
            score = Score(
                halftime = Goals(
                    home = game.scores.home.quarter_1?.plus(game.scores.home.quarter_2 ?: 0),
                    away = game.scores.away.quarter_1?.plus(game.scores.away.quarter_2 ?: 0)
                ),
                fulltime = Goals(
                    home = game.scores.home.total,
                    away = game.scores.away.total
                ),
                extratime = Goals(home = game.scores.home.over_time, away = game.scores.away.over_time),
                penalty = Goals(home = null, away = null)
            )
        )
    }

    /**
     * Convert Hockey game to Fixture
     */
    fun convertHockeyToFixture(game: HockeyGame): Fixture {
        return Fixture(
            fixture = FixtureInfo(
                id = game.id,
                referee = null,
                timezone = game.timezone,
                date = "${game.date}T${game.time}:00Z",
                timestamp = game.timestamp,
                periods = Periods(first = null, second = null),
                venue = Venue(id = null, name = null, city = null),
                status = Status(
                    long = game.status.long,
                    short = game.status.short,
                    elapsed = game.status.timer?.toIntOrNull()
                )
            ),
            league = League(
                id = game.league.id,
                name = game.league.name,
                country = game.country.name,
                logo = game.league.logo,
                flag = game.country.flag,
                season = game.league.season,
                round = game.stage
            ),
            teams = Teams(
                home = Team(
                    id = game.teams.home.id,
                    name = game.teams.home.name,
                    logo = game.teams.home.logo,
                    winner = determineWinner(game.scores.home, game.scores.away, true)
                ),
                away = Team(
                    id = game.teams.away.id,
                    name = game.teams.away.name,
                    logo = game.teams.away.logo,
                    winner = determineWinner(game.scores.home, game.scores.away, false)
                )
            ),
            goals = Goals(
                home = game.scores.home,
                away = game.scores.away
            ),
            score = Score(
                halftime = Goals(home = null, away = null),
                fulltime = Goals(home = game.scores.home, away = game.scores.away),
                extratime = Goals(home = null, away = null),
                penalty = Goals(home = null, away = null)
            )
        )
    }

    /**
     * Convert F1 race to Fixture
     */
    fun convertF1ToFixture(race: F1Race): Fixture {
        return Fixture(
            fixture = FixtureInfo(
                id = race.id,
                referee = null,
                timezone = race.timezone,
                date = race.date,
                timestamp = 0L,
                periods = Periods(first = null, second = null),
                venue = Venue(
                    id = race.circuit.id,
                    name = race.circuit.name,
                    city = race.competition.location.city
                ),
                status = Status(
                    long = race.status,
                    short = getF1StatusShort(race.status),
                    elapsed = race.laps.current
                )
            ),
            league = League(
                id = race.competition.id,
                name = race.competition.name,
                country = race.competition.location.country,
                logo = race.circuit.image ?: "",
                flag = null,
                season = race.season,
                round = race.type
            ),
            teams = Teams(
                home = Team(
                    id = 0,
                    name = race.circuit.name,
                    logo = race.circuit.image ?: "",
                    winner = null
                ),
                away = Team(
                    id = 0,
                    name = "${race.laps.total ?: 0} Laps",
                    logo = "",
                    winner = null
                )
            ),
            goals = Goals(
                home = race.laps.current,
                away = race.laps.total
            ),
            score = Score(
                halftime = Goals(home = null, away = null),
                fulltime = Goals(home = race.laps.current, away = race.laps.total),
                extratime = Goals(home = null, away = null),
                penalty = Goals(home = null, away = null)
            )
        )
    }

    /**
     * Convert Volleyball game to Fixture
     */
    fun convertVolleyballToFixture(game: VolleyballGame): Fixture {
        return Fixture(
            fixture = FixtureInfo(
                id = game.id,
                referee = null,
                timezone = game.timezone,
                date = "${game.date}T${game.time}:00Z",
                timestamp = game.timestamp,
                periods = Periods(first = null, second = null),
                venue = Venue(id = null, name = null, city = null),
                status = Status(
                    long = game.status.long,
                    short = game.status.short,
                    elapsed = game.status.timer?.toIntOrNull()
                )
            ),
            league = League(
                id = game.league.id,
                name = game.league.name,
                country = game.country.name,
                logo = game.league.logo,
                flag = game.country.flag,
                season = game.league.season,
                round = game.stage
            ),
            teams = Teams(
                home = Team(
                    id = game.teams.home.id,
                    name = game.teams.home.name,
                    logo = game.teams.home.logo,
                    winner = determineVolleyballWinner(game.scores.home?.total, game.scores.away?.total, true)
                ),
                away = Team(
                    id = game.teams.away.id,
                    name = game.teams.away.name,
                    logo = game.teams.away.logo,
                    winner = determineVolleyballWinner(game.scores.home?.total, game.scores.away?.total, false)
                )
            ),
            goals = Goals(
                home = game.scores.home?.total,
                away = game.scores.away?.total
            ),
            score = Score(
                halftime = Goals(home = null, away = null),
                fulltime = Goals(
                    home = game.scores.home?.total,
                    away = game.scores.away?.total
                ),
                extratime = Goals(home = null, away = null),
                penalty = Goals(home = null, away = null)
            )
        )
    }

    /**
     * Convert Rugby game to Fixture
     */
    fun convertRugbyToFixture(game: RugbyGame): Fixture {
        return Fixture(
            fixture = FixtureInfo(
                id = game.id,
                referee = null,
                timezone = game.timezone,
                date = "${game.date}T${game.time}:00Z",
                timestamp = game.timestamp,
                periods = Periods(first = null, second = null),
                venue = Venue(id = null, name = null, city = null),
                status = Status(
                    long = game.status.long,
                    short = game.status.short,
                    elapsed = game.status.timer?.toIntOrNull()
                )
            ),
            league = League(
                id = game.league.id,
                name = game.league.name,
                country = game.country.name,
                logo = game.league.logo,
                flag = game.country.flag,
                season = game.league.season,
                round = game.stage
            ),
            teams = Teams(
                home = Team(
                    id = game.teams.home.id,
                    name = game.teams.home.name,
                    logo = game.teams.home.logo,
                    winner = determineWinner(game.scores.home, game.scores.away, true)
                ),
                away = Team(
                    id = game.teams.away.id,
                    name = game.teams.away.name,
                    logo = game.teams.away.logo,
                    winner = determineWinner(game.scores.home, game.scores.away, false)
                )
            ),
            goals = Goals(
                home = game.scores.home,
                away = game.scores.away
            ),
            score = Score(
                halftime = Goals(home = null, away = null),
                fulltime = Goals(home = game.scores.home, away = game.scores.away),
                extratime = Goals(home = null, away = null),
                penalty = Goals(home = null, away = null)
            )
        )
    }

    // Helper functions
    private fun determineWinner(homeScore: Int?, awayScore: Int?, isHome: Boolean): Boolean? {
        if (homeScore == null || awayScore == null) return null
        return if (isHome) homeScore > awayScore else awayScore > homeScore
    }

    private fun determineBasketballWinner(homeScore: Int?, awayScore: Int?, isHome: Boolean): Boolean? {
        if (homeScore == null || awayScore == null) return null
        return if (isHome) homeScore > awayScore else awayScore > homeScore
    }

    private fun determineVolleyballWinner(homeScore: Int?, awayScore: Int?, isHome: Boolean): Boolean? {
        if (homeScore == null || awayScore == null) return null
        return if (isHome) homeScore > awayScore else awayScore > homeScore
    }

    private fun getF1StatusShort(status: String): String {
        return when (status.lowercase()) {
            "scheduled" -> "NS"
            "completed" -> "FT"
            "cancelled" -> "CANC"
            else -> status.take(3).uppercase()
        }
    }
}