package ke.nucho.sportshublive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ke.nucho.sportshublive.ui.leaguedetail.LeagueDetailScreen
import ke.nucho.sportshublive.ui.main.MainScreen
import ke.nucho.sportshublive.ui.matchdetail.MatchDetailScreen
import ke.nucho.sportshublive.ui.theme.SportsHubLiveTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            SportsHubLiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FootballNavigation()
                }
            }
        }
    }
}

@Composable
fun FootballNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main Screen with Bottom Navigation
        // Contains: Predictions, Matches (Live Scores), Leagues, Favorites
        composable("main") {
            MainScreen(
                onMatchClick = { fixtureId ->
                    navController.navigate("match_detail/$fixtureId")
                }
            )
        }

        // Match Detail Screen
        composable(
            route = "match_detail/{fixtureId}",
            arguments = listOf(
                navArgument("fixtureId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fixtureId = backStackEntry.arguments?.getInt("fixtureId") ?: 0

            MatchDetailScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}

