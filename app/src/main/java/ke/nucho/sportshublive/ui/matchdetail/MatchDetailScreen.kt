package ke.nucho.sportshublive.ui.matchdetail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ke.nucho.sportshublive.data.models.Fixture
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    fixtureId: Int,
    sport: String,
    onBackClick: () -> Unit,
    viewModel: MatchDetailViewModel = viewModel(
        factory = MatchDetailViewModelFactory(fixtureId, sport)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isLive by viewModel.isLive.collectAsStateWithLifecycle()

    // Auto-refresh for live matches
    LaunchedEffect(isLive) {
        if (isLive) {
            while (true) {
                kotlinx.coroutines.delay(30000) // 30 seconds
                viewModel.refresh()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isLive) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                Icons.Default.Refresh,
                                "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is MatchDetailUiState.Loading -> {
                LoadingContent()
            }
            is MatchDetailUiState.Success -> {
                MatchDetailContent(
                    fixture = state.fixture,
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    sport = sport,
                    modifier = Modifier.padding(padding)
                )
            }
            is MatchDetailUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
fun MatchDetailContent(
    fixture: Fixture,
    selectedTab: MatchDetailTab,
    onTabSelected: (MatchDetailTab) -> Unit,
    sport: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Match Header
        MatchHeader(fixture = fixture)

        // Tab Row
        MatchDetailTabRow(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            sport = sport
        )

        // Tab Content
        when (selectedTab) {
            MatchDetailTab.OVERVIEW -> OverviewTab(fixture)
            MatchDetailTab.STATS -> StatsTab(fixture, sport)
            MatchDetailTab.EVENTS -> EventsTab(fixture, sport)
            MatchDetailTab.LINEUPS -> LineupsTab(fixture, sport)
            MatchDetailTab.H2H -> HeadToHeadTab(fixture, sport)
            MatchDetailTab.TABLE -> StandingsTab(fixture, sport)
        }
    }
}

@Composable
fun MatchHeader(fixture: Fixture) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // League Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = fixture.league.logo,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${fixture.league.name} - ${fixture.league.round}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = fixture.teams.home.logo,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fixture.teams.home.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (fixture.fixture.status.short != "NS") {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${fixture.goals.home ?: 0}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = " - ",
                                style = MaterialTheme.typography.displayMedium,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "${fixture.goals.away ?: 0}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Badge
                    MatchStatusBadge(fixture)
                }

                // Away Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = fixture.teams.away.logo,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fixture.teams.away.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Match Date/Time or Venue
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fixture.fixture.venue?.name ?: "Venue TBA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MatchStatusBadge(fixture: Fixture) {
    val (text, color, isLive) = when (fixture.fixture.status.short) {
        "NS" -> Triple("Not Started", Color.Gray, false)
        "1H", "2H", "ET", "P" -> Triple("${fixture.fixture.status.elapsed}'", Color(0xFF4CAF50), true)
        "HT" -> Triple("Half Time", Color(0xFFFF9800), true)
        "FT" -> Triple("Full Time", Color.Gray, false)
        "AET" -> Triple("After Extra Time", Color.Gray, false)
        "PEN" -> Triple("Penalties", Color(0xFFFF9800), true)
        else -> Triple(fixture.fixture.status.long, Color.Gray, false)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MatchDetailTabRow(
    selectedTab: MatchDetailTab,
    onTabSelected: (MatchDetailTab) -> Unit,
    sport: String
) {
    val tabs = getAvailableTabs(sport)

    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun OverviewTab(fixture: Fixture) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats
        item {
            QuickStatsCard(fixture)
        }

        // Last Matches (if available)
        item {
            Text(
                text = "Recent Form",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Placeholder for recent form
        item {
            RecentFormPlaceholder()
        }
    }
}

@Composable
fun QuickStatsCard(fixture: Fixture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder stats - will be populated from API
            QuickStatItem("Shots on Target", "5", "3")
            QuickStatItem("Possession", "58%", "42%")
            QuickStatItem("Corners", "7", "4")
        }
    }
}

@Composable
fun QuickStatItem(label: String, homeValue: String, awayValue: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = awayValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun StatsTab(fixture: Fixture, sport: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Match Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Sample statistics with visual bars
        item { StatItemWithBar("Ball Possession", 58, 42) }
        item { StatItemWithBar("Shots on Goal", 12, 8) }
        item { StatItemWithBar("Shots off Goal", 5, 7) }
        item { StatItemWithBar("Total Shots", 17, 15) }
        item { StatItemWithBar("Blocked Shots", 3, 2) }
        item { StatItemWithBar("Corner Kicks", 7, 4) }
        item { StatItemWithBar("Offsides", 2, 3) }
        item { StatItemWithBar("Fouls", 11, 14) }
        item { StatItemWithBar("Yellow Cards", 2, 3) }
    }
}

@Composable
fun StatItemWithBar(
    label: String,
    homeValue: Int,
    awayValue: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = homeValue.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = awayValue.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val total = homeValue + awayValue
        val homePercentage = if (total > 0) homeValue.toFloat() / total else 0.5f

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(homePercentage)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

@Composable
fun EventsTab(fixture: Fixture, sport: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Match Events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sample events - replace with actual data
        items(listOf(
            EventItem("12'", "⚽ Goal", "John Doe", true),
            EventItem("25'", "🟨 Yellow Card", "Jane Smith", false),
            EventItem("45'", "⚽ Goal", "Mike Johnson", true),
            EventItem("67'", "🔄 Substitution", "Player Out → Player In", false)
        )) { event ->
            MatchEventCard(event)
        }
    }
}

@Composable
fun MatchEventCard(event: EventItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = if (event.isHome) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (event.isHome) {
            EventContent(event)
        } else {
            EventContent(event, alignEnd = true)
        }
    }
}

@Composable
fun EventContent(event: EventItem, alignEnd: Boolean = false) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = event.time,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = event.type,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = event.player,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LineupsTab(fixture: Fixture, sport: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Lineups feature coming soon!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HeadToHeadTab(fixture: Fixture, sport: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Head to Head",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Sample H2H matches
        items(3) {
            H2HMatchCard()
        }
    }
}

@Composable
fun H2HMatchCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Team A", modifier = Modifier.weight(1f))
            Text("2 - 1", fontWeight = FontWeight.Bold)
            Text("Team B", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun StandingsTab(fixture: Fixture, sport: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "League Standings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Table header
        item {
            StandingsHeader()
        }

        // Sample standings
        items(10) { index ->
            StandingsRow(position = index + 1, team = "Team ${index + 1}")
        }
    }
}

@Composable
fun StandingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("Team", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("P", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("W", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("D", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("L", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("Pts", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StandingsRow(position: Int, team: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$position", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodySmall)
        Text(team, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text("10", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodySmall)
        Text("7", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodySmall)
        Text("2", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodySmall)
        Text("1", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodySmall)
        Text("23", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RecentFormPlaceholder() {
    Text(
        text = "Recent form data will appear here",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Helper functions
fun getAvailableTabs(sport: String): List<MatchDetailTab> {
    return when (sport) {
        "Football" -> listOf(
            MatchDetailTab.OVERVIEW,
            MatchDetailTab.STATS,
            MatchDetailTab.EVENTS,
            MatchDetailTab.LINEUPS,
            MatchDetailTab.H2H,
            MatchDetailTab.TABLE
        )
        else -> listOf(
            MatchDetailTab.OVERVIEW,
            MatchDetailTab.STATS,
            MatchDetailTab.EVENTS
        )
    }
}

// Data classes
enum class MatchDetailTab(val title: String) {
    OVERVIEW("Overview"),
    STATS("Stats"),
    EVENTS("Events"),
    LINEUPS("Lineups"),
    H2H("H2H"),
    TABLE("Table")
}

data class EventItem(
    val time: String,
    val type: String,
    val player: String,
    val isHome: Boolean
)

sealed class MatchDetailUiState {
    object Loading : MatchDetailUiState()
    data class Success(val fixture: Fixture) : MatchDetailUiState()
    data class Error(val message: String) : MatchDetailUiState()
}