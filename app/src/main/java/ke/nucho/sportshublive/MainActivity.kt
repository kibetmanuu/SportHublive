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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ke.nucho.sportshublive.ui.livescores.LiveScoresScreen
import ke.nucho.sportshublive.ui.matchdetail.MatchDetailScreen
import ke.nucho.sportshublive.ui.theme.SportsHubLiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportsHubLiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SportsHubNavigation()
                }
            }
        }
    }
}

@Composable
fun SportsHubNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "live_scores"
    ) {
        // Live Scores Screen
        composable("live_scores") {
            LiveScoresScreen(
                onMatchClick = { fixtureId, sport ->
                    navController.navigate("match_detail/$fixtureId/$sport")
                }
            )
        }

        // Match Detail Screen
        composable(
            route = "match_detail/{fixtureId}/{sport}",
            arguments = listOf(
                navArgument("fixtureId") { type = NavType.IntType },
                navArgument("sport") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fixtureId = backStackEntry.arguments?.getInt("fixtureId") ?: 0
            val sport = backStackEntry.arguments?.getString("sport") ?: "Football"

            MatchDetailScreen(
                fixtureId = fixtureId,
                sport = sport,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}