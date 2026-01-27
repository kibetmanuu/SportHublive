package ke.nucho.sportshublive.ui.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class League(
    val id: Int,
    val name: String,
    val logo: String,
    val country: String,
    val countryFlag: String,
    val season: String,
    val category: String = "Top Leagues"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesScreen(
    onLeagueClick: (Int, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val leagues = remember {
        listOf(
            // Top European Leagues
            League(
                id = 39,
                name = "Premier League",
                logo = "https://media.api-sports.io/football/leagues/39.png",
                country = "England",
                countryFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                season = "2024/2025",
                category = "Top Leagues"
            ),
            League(
                id = 140,
                name = "La Liga",
                logo = "https://media.api-sports.io/football/leagues/140.png",
                country = "Spain",
                countryFlag = "🇪🇸",
                season = "2024/2025",
                category = "Top Leagues"
            ),
            League(
                id = 78,
                name = "Bundesliga",
                logo = "https://media.api-sports.io/football/leagues/78.png",
                country = "Germany",
                countryFlag = "🇩🇪",
                season = "2024/2025",
                category = "Top Leagues"
            ),
            League(
                id = 135,
                name = "Serie A",
                logo = "https://media.api-sports.io/football/leagues/135.png",
                country = "Italy",
                countryFlag = "🇮🇹",
                season = "2024/2025",
                category = "Top Leagues"
            ),
            League(
                id = 61,
                name = "Ligue 1",
                logo = "https://media.api-sports.io/football/leagues/61.png",
                country = "France",
                countryFlag = "🇫🇷",
                season = "2024/2025",
                category = "Top Leagues"
            ),

            // UEFA Competitions
            League(
                id = 2,
                name = "UEFA Champions League",
                logo = "https://media.api-sports.io/football/leagues/2.png",
                country = "Europe",
                countryFlag = "🇪🇺",
                season = "2024/2025",
                category = "UEFA"
            ),
            League(
                id = 3,
                name = "UEFA Europa League",
                logo = "https://media.api-sports.io/football/leagues/3.png",
                country = "Europe",
                countryFlag = "🇪🇺",
                season = "2024/2025",
                category = "UEFA"
            ),
            League(
                id = 848,
                name = "UEFA Conference League",
                logo = "https://media.api-sports.io/football/leagues/848.png",
                country = "Europe",
                countryFlag = "🇪🇺",
                season = "2024/2025",
                category = "UEFA"
            ),

            // International
            League(
                id = 4,
                name = "World Cup",
                logo = "https://media.api-sports.io/football/leagues/4.png",
                country = "International",
                countryFlag = "🌍",
                season = "2026",
                category = "International"
            ),
            League(
                id = 5,
                name = "Euro Championship",
                logo = "https://media.api-sports.io/football/leagues/5.png",
                country = "Europe",
                countryFlag = "🇪🇺",
                season = "2024",
                category = "International"
            ),

            // Other European Leagues
            League(
                id = 94,
                name = "Primeira Liga",
                logo = "https://media.api-sports.io/football/leagues/94.png",
                country = "Portugal",
                countryFlag = "🇵🇹",
                season = "2024/2025",
                category = "Other European"
            ),
            League(
                id = 88,
                name = "Eredivisie",
                logo = "https://media.api-sports.io/football/leagues/88.png",
                country = "Netherlands",
                countryFlag = "🇳🇱",
                season = "2024/2025",
                category = "Other European"
            ),
            League(
                id = 203,
                name = "Süper Lig",
                logo = "https://media.api-sports.io/football/leagues/203.png",
                country = "Turkey",
                countryFlag = "🇹🇷",
                season = "2024/2025",
                category = "Other European"
            ),
            League(
                id = 144,
                name = "Jupiler Pro League",
                logo = "https://media.api-sports.io/football/leagues/144.png",
                country = "Belgium",
                countryFlag = "🇧🇪",
                season = "2024/2025",
                category = "Other European"
            ),

            // English Leagues
            League(
                id = 40,
                name = "Championship",
                logo = "https://media.api-sports.io/football/leagues/40.png",
                country = "England",
                countryFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                season = "2024/2025",
                category = "England"
            ),
            League(
                id = 41,
                name = "League One",
                logo = "https://media.api-sports.io/football/leagues/41.png",
                country = "England",
                countryFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                season = "2024/2025",
                category = "England"
            ),

            // South America
            League(
                id = 71,
                name = "Brasileirão Serie A",
                logo = "https://media.api-sports.io/football/leagues/71.png",
                country = "Brazil",
                countryFlag = "🇧🇷",
                season = "2025",
                category = "South America"
            ),
            League(
                id = 128,
                name = "Liga Argentina",
                logo = "https://media.api-sports.io/football/leagues/128.png",
                country = "Argentina",
                countryFlag = "🇦🇷",
                season = "2024",
                category = "South America"
            ),
            League(
                id = 13,
                name = "Copa Libertadores",
                logo = "https://media.api-sports.io/football/leagues/13.png",
                country = "South America",
                countryFlag = "🌎",
                season = "2025",
                category = "South America"
            ),

            // Other Regions
            League(
                id = 253,
                name = "MLS",
                logo = "https://media.api-sports.io/football/leagues/253.png",
                country = "USA",
                countryFlag = "🇺🇸",
                season = "2025",
                category = "Americas"
            ),
            League(
                id = 262,
                name = "Liga MX",
                logo = "https://media.api-sports.io/football/leagues/262.png",
                country = "Mexico",
                countryFlag = "🇲🇽",
                season = "2024/2025",
                category = "Americas"
            ),
            League(
                id = 188,
                name = "Ligue 1",
                logo = "https://media.api-sports.io/football/leagues/188.png",
                country = "Morocco",
                countryFlag = "🇲🇦",
                season = "2024/2025",
                category = "Africa"
            ),
            League(
                id = 207,
                name = "Super League",
                logo = "https://media.api-sports.io/football/leagues/207.png",
                country = "Switzerland",
                countryFlag = "🇨🇭",
                season = "2024/2025",
                category = "Other European"
            )
        )
    }

    val groupedLeagues = leagues.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leagues",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${leagues.size} competitions",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0) // Blue theme
                )
            )
        },
        containerColor = Color(0xFF121212) // Dark background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121212)),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            groupedLeagues.forEach { (category, categoryLeagues) ->
                // Category Header
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    CategoryHeader(category)
                }

                // League Cards
                items(categoryLeagues) { league ->
                    LeagueCard(
                        league = league,
                        onClick = {
                            onLeagueClick(league.id, league.name, league.logo)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(category: String) {
    Text(
        text = category,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF42A5F5), // Blue accent
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun LeagueCard(
    league: League,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E) // Dark card background
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // League Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = league.logo,
                    contentDescription = league.name,
                    modifier = Modifier
                        .size(85.dp)
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // League Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = league.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = league.countryFlag,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = league.country,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = league.season,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}