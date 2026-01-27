package ke.nucho.sportshublive.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.nucho.sportshublive.ui.components.SportsBottomNavigationBar
import ke.nucho.sportshublive.ui.leaguedetail.LeagueDetailScreen
import ke.nucho.sportshublive.ui.leagues.LeaguesScreen
import ke.nucho.sportshublive.ui.livescores.LiveScoresScreen

@Composable
fun MainScreen(
    onMatchClick: (fixtureId: Int) -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf("matches") }
    var selectedLeague by remember { mutableStateOf<LeagueSelection?>(null) }

    // If a league is selected, show league detail screen
    if (selectedLeague != null) {
        LeagueDetailScreen(
            leagueId = selectedLeague!!.id,
            leagueName = selectedLeague!!.name,
            leagueLogo = selectedLeague!!.logo,
            onBackClick = {
                selectedLeague = null
                currentRoute = "leagues"
            },
            onMatchClick = onMatchClick
        )
    } else {
        Scaffold(
            bottomBar = {
                SportsBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> currentRoute = route }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentRoute) {
                    "predictions" -> PlaceholderScreen(
                        title = "Predictions",
                        message = "Coming Soon",
                        description = "Get AI-powered match predictions\nand betting tips"
                    )
                    "matches" -> LiveScoresScreen(onMatchClick = onMatchClick)
                    "leagues" -> LeaguesScreen(
                        onLeagueClick = { leagueId, leagueName, leagueLogo ->
                            selectedLeague = LeagueSelection(leagueId, leagueName, leagueLogo)
                        }
                    )
                    "favorites" -> PlaceholderScreen(
                        title = "Favorites",
                        message = "Your Saved Content",
                        description = "Save your favorite teams,\nmatches, and leagues here"
                    )
                }
            }
        }
    }
}

data class LeagueSelection(
    val id: Int,
    val name: String,
    val logo: String
)

@Composable
fun PlaceholderScreen(
    title: String,
    message: String,
    description: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon based on screen
            Text(
                text = when (title) {
                    "Predictions" -> "🔮"
                    "Favorites" -> "⭐"
                    else -> "🏆"
                },
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                color = Color(0xFF42A5F5), // Blue accent
                fontWeight = FontWeight.SemiBold
            )
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = description,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}