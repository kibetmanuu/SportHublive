package ke.nucho.sportshublive.ui.leaguedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

// Theme Colors
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val BluePrimary = Color(0xFF1565C0)
private val BlueLight = Color(0xFF42A5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueDetailScreen(
    leagueId: Int,
    leagueName: String,
    leagueLogo: String,
    onBackClick: () -> Unit,
    onMatchClick: (Int) -> Unit = {},
    viewModel: LeagueDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    LaunchedEffect(leagueId) {
        viewModel.loadLeagueDetails(leagueId)
    }

    Scaffold(
        topBar = {
            LeagueDetailTopBar(
                leagueName = leagueName,
                leagueLogo = leagueLogo,
                onBackClick = onBackClick
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            LeagueDetailTabs(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            // Content
            when (val state = uiState) {
                is LeagueDetailUiState.Loading -> LoadingContent()
                is LeagueDetailUiState.FixturesSuccess -> {
                    EnhancedFixturesContent(
                        fixtures = state.fixtures,
                        onMatchClick = onMatchClick
                    )
                }
                is LeagueDetailUiState.StandingsSuccess -> {
                    StandingsContent(standings = state.standings)
                }
                is LeagueDetailUiState.TopScorersSuccess -> {
                    TopScorersContent(scorers = state.scorers)
                }
                is LeagueDetailUiState.Error -> {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueDetailTopBar(
    leagueName: String,
    leagueLogo: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = leagueLogo,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = leagueName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BluePrimary
        )
    )
}

@Composable
fun LeagueDetailTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = DarkSurface,
        contentColor = Color.White
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = {
                Text(
                    text = "Fixtures",
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = {
                Text(
                    text = "Table",
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = {
                Text(
                    text = "Top Scorers",
                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

@Composable
fun StandingsContent(standings: List<StandingItem>) {
    if (standings.isEmpty()) {
        EmptyStateContent(
            emoji = "🏆",
            message = "No standings available\n\nStandings may not be available for:\n• Ongoing tournaments\n• Cup competitions\n• Off-season periods"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item {
                StandingsHeader()
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(standings) { standing ->
                StandingRow(standing)
            }
        }
    }
}

@Composable
fun StandingsHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp)
            )
            Text(
                "Team",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "P",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "W",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "D",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "L",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "GD",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(35.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "Pts",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(35.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StandingRow(standing: StandingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(30.dp),
                contentAlignment = Alignment.Center
            ) {
                val positionColor = when (standing.position) {
                    in 1..4 -> Color(0xFF4CAF50)
                    5 -> Color(0xFFFF9800)
                    in 18..20 -> Color(0xFFE53935)
                    else -> Color.White
                }
                Text(
                    text = "${standing.position}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = positionColor
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = standing.teamLogo,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = standing.teamName,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${standing.played}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${standing.won}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${standing.drawn}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${standing.lost}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = if (standing.goalDifference > 0) "+${standing.goalDifference}" else "${standing.goalDifference}",
                fontSize = 12.sp,
                color = when {
                    standing.goalDifference > 0 -> Color(0xFF4CAF50)
                    standing.goalDifference < 0 -> Color(0xFFE53935)
                    else -> Color.White.copy(alpha = 0.8f)
                },
                modifier = Modifier.width(35.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${standing.points}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueLight,
                modifier = Modifier.width(35.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TopScorersContent(scorers: List<TopScorer>) {
    if (scorers.isEmpty()) {
        EmptyStateContent(
            emoji = "⚽",
            message = "No top scorers data available\n\nTop scorers may not be available for:\n• Newly started seasons\n• Cup competitions\n• International tournaments"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scorers) { scorer ->
                TopScorerCard(scorer)
            }
        }
    }
}

@Composable
fun TopScorerCard(scorer: TopScorer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (scorer.playerPhoto.isNotEmpty()) {
                AsyncImage(
                    model = scorer.playerPhoto,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(BluePrimary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scorer.playerName.take(1),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = scorer.playerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AsyncImage(
                        model = scorer.teamLogo,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = scorer.teamName,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (scorer.position.isNotEmpty()) {
                    Text(
                        text = scorer.position,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚽",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${scorer.goals}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueLight
                    )
                }
                if (scorer.assists > 0) {
                    Text(
                        text = "${scorer.assists} assists",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${scorer.appearances} apps",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BlueLight)
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "❌",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// EmptyStateContent is now defined only in FixturesComponents.kt
// This file uses it from there since they're in the same package