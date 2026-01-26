package ke.nucho.sportshublive.ui.matchdetail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ke.nucho.sportshublive.data.models.*
import java.text.SimpleDateFormat
import java.util.*

// Theme Colors
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val BluePrimary = Color(0xFF1565C0)
private val BlueLight = Color(0xFF42A5F5)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    viewModel: MatchDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val fixture by viewModel.fixture.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val lineups by viewModel.lineups.collectAsStateWithLifecycle()
    val h2h by viewModel.h2h.collectAsStateWithLifecycle()
    val isAutoRefresh by viewModel.isAutoRefresh.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = fixture?.league?.name ?: "Match Details",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        fixture?.league?.round?.let { round ->
                            Text(
                                text = round,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isAutoRefresh) {
                        LiveIndicator()
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            "Refresh",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        when (uiState) {
            is MatchDetailUiState.Loading -> LoadingContent(paddingValues)
            is MatchDetailUiState.Success -> {
                fixture?.let { match ->
                    SuccessContent(
                        fixture = match,
                        selectedTab = selectedTab,
                        statistics = statistics,
                        events = events,
                        lineups = lineups,
                        h2h = h2h,
                        onTabSelected = { tab -> viewModel.selectTab(tab) },
                        paddingValues = paddingValues
                    )
                }
            }
            is MatchDetailUiState.Error -> {
                ErrorContent(
                    message = (uiState as MatchDetailUiState.Error).message,
                    onRetry = { viewModel.refresh() },
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
fun LiveIndicator() {
    var pulse by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            pulse = !pulse
            kotlinx.coroutines.delay(1000)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (pulse) AccentRed else AccentRed.copy(alpha = 0.5f)
                )
        )
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SuccessContent(
    fixture: Fixture,
    selectedTab: MatchDetailTab,
    statistics: List<TeamStatistics>,
    events: List<MatchEvent>,
    lineups: List<TeamLineup>,
    h2h: List<Fixture>,
    onTabSelected: (MatchDetailTab) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Match Header
        MatchHeader(fixture)

        // Tabs
        TabRow(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        // Tab Content
        when (selectedTab) {
            MatchDetailTab.OVERVIEW -> OverviewTab(fixture, statistics, events)
            MatchDetailTab.STATS -> StatisticsTab(statistics)
            MatchDetailTab.LINEUPS -> LineupsTab(lineups)
            MatchDetailTab.EVENTS -> EventsTab(events)
            MatchDetailTab.H2H -> HeadToHeadTab(h2h, fixture)
        }
    }
}

@Composable
fun MatchHeader(fixture: Fixture) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Venue and Date
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = BlueLight.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fixture.fixture.venue.name ?: "Unknown Venue",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatMatchDate(fixture.fixture.date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                TeamColumn(
                    team = fixture.teams.home,
                    modifier = Modifier.weight(1f)
                )

                // Score
                ScoreColumn(
                    fixture = fixture,
                    modifier = Modifier.weight(1f)
                )

                // Away Team
                TeamColumn(
                    team = fixture.teams.away,
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            // Referee
            fixture.fixture.referee?.let { referee ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BlueLight.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Referee: $referee",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TeamColumn(
    team: Team,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        AsyncImage(
            model = team.logo,
            contentDescription = team.name,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = team.name,
            fontSize = MaterialTheme.typography.titleSmall.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )
    }
}

@Composable
fun ScoreColumn(
    fixture: Fixture,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Match Status Badge
        val (statusText, statusColor) = getMatchStatusInfo(fixture)

        Surface(
            color = statusColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = statusText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Score or VS
        if (fixture.fixture.status.short != "NS" &&
            fixture.goals.home != null &&
            fixture.goals.away != null) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fixture.goals.home.toString(),
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if ((fixture.goals.home ?: 0) > (fixture.goals.away ?: 0))
                        AccentGreen else Color.White
                )
                Text(
                    text = " : ",
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = fixture.goals.away.toString(),
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if ((fixture.goals.away ?: 0) > (fixture.goals.home ?: 0))
                        AccentGreen else Color.White
                )
            }
        } else {
            Text(
                text = "VS",
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatMatchTime(fixture.fixture.date),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TabRow(
    selectedTab: MatchDetailTab,
    onTabSelected: (MatchDetailTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = DarkSurface,
        contentColor = BlueLight,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            if (selectedTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = BlueLight
                )
            }
        }
    ) {
        MatchDetailTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = when (tab) {
                            MatchDetailTab.OVERVIEW -> "Overview"
                            MatchDetailTab.STATS -> "Stats"
                            MatchDetailTab.LINEUPS -> "Lineups"
                            MatchDetailTab.EVENTS -> "Events"
                            MatchDetailTab.H2H -> "H2H"
                        },
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == tab) BlueLight else Color.White.copy(alpha = 0.7f)
                    )
                }
            )
        }
    }
}

// TAB CONTENT FUNCTIONS would go here - keeping them from the original file
// (OverviewTab, StatisticsTab, LineupsTab, EventsTab, HeadToHeadTab)
// These remain the same as in your original document

@Composable
fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = BlueLight)
            Text(
                text = "Loading match details...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = AccentRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

// Helper Functions

fun getMatchStatusInfo(fixture: Fixture): Pair<String, Color> {
    return when (fixture.fixture.status.short) {
        "NS" -> "Not Started" to Color.Gray
        "1H" -> "1st Half ${fixture.fixture.status.elapsed}'" to AccentGreen
        "2H" -> "2nd Half ${fixture.fixture.status.elapsed}'" to AccentGreen
        "HT" -> "Half Time" to Color(0xFFFF9800)
        "FT" -> "Full Time" to Color.Gray
        "ET" -> "Extra Time ${fixture.fixture.status.elapsed}'" to Color(0xFFFF9800)
        "P" -> "Penalties" to Color(0xFFFF9800)
        "AET" -> "After Extra Time" to Color.Gray
        "PEN" -> "After Penalties" to Color.Gray
        "PST" -> "Postponed" to AccentRed
        "CANC" -> "Cancelled" to AccentRed
        "ABD" -> "Abandoned" to AccentRed
        else -> fixture.fixture.status.long to Color.Gray
    }
}

fun formatMatchDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun formatMatchTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
@Composable
fun OverviewTab(
    fixture: Fixture,
    statistics: List<TeamStatistics>,
    events: List<MatchEvent>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Key Statistics Preview
        if (statistics.isNotEmpty()) {
            item {
                Text(
                    text = "Key Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                KeyStatisticsPreview(statistics)
            }
        }

        // Recent Events
        if (events.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(events.take(5).size) { index ->
                EventItem(events[index])
            }
        }

        // Match Info
        item {
            MatchInfoCard(fixture)
        }
    }
}

@Composable
fun KeyStatisticsPreview(statistics: List<TeamStatistics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statistics.firstOrNull()?.statistics?.take(3)?.forEach { stat ->
                StatisticRow(
                    label = stat.type,
                    homeValue = stat.value.toString(),
                    awayValue = if (statistics.size > 1) {
                        statistics[1].statistics.find { it.type == stat.type }?.value.toString()
                    } else "0"
                )
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, homeValue: String, awayValue: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = awayValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun EventItem(event: MatchEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${event.time.elapsed}'",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = BlueLight,
            modifier = Modifier.width(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.player.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = event.detail ?: event.type,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MatchInfoCard(fixture: Fixture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Match Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            InfoRow("League", fixture.league.name)
            fixture.league.round?.let { InfoRow("Round", it) }
            InfoRow("Venue", fixture.fixture.venue.name ?: "Unknown")
            InfoRow("City", fixture.fixture.venue.city ?: "Unknown")
            fixture.fixture.referee?.let { InfoRow("Referee", it) }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun StatisticsTab(statistics: List<TeamStatistics>) {
    if (statistics.isEmpty()) {
        EmptyStateMessage("No statistics available")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val homeStats = statistics.firstOrNull()
        val awayStats = if (statistics.size > 1) statistics[1] else null

        homeStats?.statistics?.forEach { stat ->
            item {
                val awayValue = awayStats?.statistics?.find { it.type == stat.type }
                DetailedStatisticItem(
                    label = stat.type,
                    homeValue = stat.value,
                    awayValue = awayValue?.value
                )
            }
        }
    }
}

@Composable
fun DetailedStatisticItem(label: String, homeValue: Any?, awayValue: Any?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = homeValue?.toString() ?: "0",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = awayValue?.toString() ?: "0",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun LineupsTab(lineups: List<TeamLineup>) {
    if (lineups.isEmpty()) {
        EmptyStateMessage("No lineup information available")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lineups.forEach { lineup ->
            item {
                TeamLineupCard(lineup)
            }
        }
    }
}

@Composable
fun TeamLineupCard(lineup: TeamLineup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Team Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = lineup.team.logo,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = lineup.team.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Formation: ${lineup.formation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Starting XI
            Text(
                text = "Starting XI",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BlueLight
            )
            Spacer(modifier = Modifier.height(8.dp))

            lineup.startXI.forEach { lineupPlayer ->
                PlayerItem(lineupPlayer)
            }

            // Substitutes
            if (lineup.substitutes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Substitutes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BlueLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                lineup.substitutes.forEach { lineupPlayer ->
                    PlayerItem(lineupPlayer)
                }
            }
        }
    }
}

@Composable
fun PlayerItem(lineupPlayer: LineupPlayer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lineupPlayer.player.number?.toString() ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BlueLight,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = lineupPlayer.player.name ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = lineupPlayer.player.pos ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
@Composable
fun EventsTab(events: List<MatchEvent>) {
    if (events.isEmpty()) {
        EmptyStateMessage("No events recorded yet")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events.size) { index ->
            DetailedEventItem(events[index])
        }
    }
}

@Composable
fun DetailedEventItem(event: MatchEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Surface(
                color = BlueLight.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${event.time.elapsed}'",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BlueLight
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.player.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = event.detail ?: event.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                event.assist?.name?.let { assist ->
                    Text(
                        text = "Assist: $assist",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Event Type Icon
            Icon(
                imageVector = when (event.type.lowercase()) {
                    "goal" -> Icons.Default.Check
                    "card" -> Icons.Default.Warning
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when {
                    event.detail?.contains("Yellow") == true -> Color(0xFFFFEB3B)
                    event.detail?.contains("Red") == true -> AccentRed
                    event.type.lowercase() == "goal" -> AccentGreen
                    else -> Color.White.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HeadToHeadTab(h2h: List<Fixture>, currentFixture: Fixture) {
    if (h2h.isEmpty()) {
        EmptyStateMessage("No head-to-head history available")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(h2h.size) { index ->
            H2HMatchCard(h2h[index])
        }
    }
}

@Composable
fun H2HMatchCard(fixture: Fixture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date and League
            Text(
                text = formatMatchDate(fixture.fixture.date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = fixture.league.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = fixture.teams.home.logo,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fixture.teams.home.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = fixture.teams.away.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = fixture.teams.away.logo,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}