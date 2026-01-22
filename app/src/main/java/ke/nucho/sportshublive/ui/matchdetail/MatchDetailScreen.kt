package ke.nucho.sportshublive.ui.matchdetail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ke.nucho.sportshublive.data.models.*
import java.text.SimpleDateFormat
import java.util.*

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
                            overflow = TextOverflow.Ellipsis
                        )
                        fixture?.league?.round?.let { round ->
                            Text(
                                text = round,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isAutoRefresh) {
                        LiveIndicator()
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
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
                    if (pulse) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
        )
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Match Header
        item {
            MatchHeader(fixture)
        }

        // Tabs
        item {
            TabRow(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }

        // Tab Content
        item {
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                MatchDetailTab.OVERVIEW -> OverviewTab(fixture, statistics, events)
                MatchDetailTab.STATS -> StatisticsTab(statistics)
                MatchDetailTab.LINEUPS -> LineupsTab(lineups)
                MatchDetailTab.EVENTS -> EventsTab(events)
                MatchDetailTab.H2H -> HeadToHeadTab(h2h)
            }
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
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Venue and Date
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fixture.fixture.venue.name ?: "Unknown Venue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatMatchDate(fixture.fixture.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Referee: $referee",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = team.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = statusText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Score
        if (fixture.fixture.status.short != "NS" &&
            fixture.goals.home != null &&
            fixture.goals.away != null) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fixture.goals.home.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if ((fixture.goals.home ?: 0) > (fixture.goals.away ?: 0))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = " : ",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = fixture.goals.away.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if ((fixture.goals.away ?: 0) > (fixture.goals.home ?: 0))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            Text(
                text = "VS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatMatchTime(fixture.fixture.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp
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
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun OverviewTab(
    fixture: Fixture,
    statistics: List<TeamStatistics>,
    events: List<MatchEvent>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Key Events
        if (events.isNotEmpty()) {
            SectionTitle("Key Events")
            KeyEventsList(events.take(5))
        }

        // Quick Stats
        if (statistics.isNotEmpty()) {
            SectionTitle("Quick Stats")
            QuickStatsCard(statistics)
        }

        // Match Info
        SectionTitle("Match Information")
        MatchInfoCard(fixture)
    }
}

@Composable
fun StatisticsTab(statistics: List<TeamStatistics>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (statistics.isEmpty()) {
            EmptyStateMessage("No statistics available yet")
        } else {
            statistics.forEach { teamStat ->
                teamStat.statistics.forEach { stat ->
                    StatisticItem(
                        statType = stat.type,
                        homeValue = if (statistics.indexOf(teamStat) == 0) stat.value else null,
                        awayValue = if (statistics.indexOf(teamStat) == 1) stat.value else null,
                        homeTeam = statistics.getOrNull(0)?.team,
                        awayTeam = statistics.getOrNull(1)?.team
                    )
                }
            }
        }
    }
}

@Composable
fun LineupsTab(lineups: List<TeamLineup>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (lineups.isEmpty()) {
            EmptyStateMessage("Lineups not available yet")
        } else {
            lineups.forEach { lineup ->
                LineupCard(lineup)
            }
        }
    }
}

@Composable
fun EventsTab(events: List<MatchEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (events.isEmpty()) {
            EmptyStateMessage("No events recorded yet")
        } else {
            events.forEach { event ->
                EventCard(event)
            }
        }
    }
}

@Composable
fun HeadToHeadTab(h2h: List<Fixture>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (h2h.isEmpty()) {
            EmptyStateMessage("No head-to-head data available")
        } else {
            SectionTitle("Last 5 Meetings")
            h2h.forEach { match ->
                H2HMatchCard(match)
            }
        }
    }
}

// Helper Composables

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun KeyEventsList(events: List<MatchEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            events.forEach { event ->
                EventRow(event)
                if (event != events.last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun EventRow(event: MatchEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when (event.type) {
                "Goal" -> "⚽"
                "Card" -> if (event.detail.contains("Yellow")) "🟨" else "🟥"
                "subst" -> "🔄"
                else -> "•"
            }
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = event.player.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = event.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "${event.time.elapsed}'",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun QuickStatsCard(statistics: List<TeamStatistics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val keyStats = listOf("Shots on Goal", "Possession", "Passes")
            keyStats.forEach { statName ->
                val homeStat = statistics.getOrNull(0)?.statistics?.find { it.type == statName }
                val awayStat = statistics.getOrNull(1)?.statistics?.find { it.type == statName }

                if (homeStat != null && awayStat != null) {
                    StatisticItem(
                        statType = statName,
                        homeValue = homeStat.value,
                        awayValue = awayStat.value,
                        homeTeam = statistics[0].team,
                        awayTeam = statistics[1].team
                    )
                    if (statName != keyStats.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticItem(
    statType: String,
    homeValue: Any?,
    awayValue: Any?,
    homeTeam: Team?,
    awayTeam: Team?
) {
    Column {
        Text(
            text = statType,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeValue?.toString() ?: "0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Progress bar for percentage stats
            if (statType.contains("Possession") || statType.contains("%")) {
                val homePercent = homeValue?.toString()?.replace("%", "")?.toFloatOrNull() ?: 0f
                val awayPercent = awayValue?.toString()?.replace("%", "")?.toFloatOrNull() ?: 0f
                val total = homePercent + awayPercent

                if (total > 0) {
                    LinearProgressIndicator(
                        progress = homePercent / total,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = awayValue?.toString() ?: "0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MatchInfoCard(fixture: Fixture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow("Competition", fixture.league.name)
            InfoRow("Season", fixture.league.season.toString())
            fixture.league.round?.let { InfoRow("Round", it) }
            fixture.fixture.venue.city?.let { InfoRow("City", it) }
            InfoRow("Date", formatMatchDate(fixture.fixture.date))
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LineupCard(lineup: TeamLineup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = lineup.team.logo,
                    contentDescription = lineup.team.name,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = lineup.team.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Formation: ${lineup.formation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Starting XI",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            lineup.startXI.forEach { player ->
                PlayerRow(player.player)
            }
        }
    }
}

@Composable
fun PlayerRow(player: PlayerDetails) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = player.number.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = player.pos,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EventCard(event: MatchEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${event.time.elapsed}'",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (event.type) {
                        "Goal" -> "⚽"
                        "Card" -> if (event.detail.contains("Yellow")) "🟨" else "🟥"
                        "subst" -> "🔄"
                        else -> "•"
                    }
                    Text(text = icon, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.detail,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.player.name,
                    style = MaterialTheme.typography.bodyMedium
                )

                event.assist?.let { assist ->
                    Text(
                        text = "Assist: ${assist.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = event.team.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun H2HMatchCard(fixture: Fixture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatMatchDate(fixture.fixture.date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fixture.teams.home.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${fixture.goals.home} - ${fixture.goals.away}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = fixture.teams.away.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading match details...",
                style = MaterialTheme.typography.bodyMedium
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
            .padding(paddingValues),
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
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
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
        "1H" -> "1st Half ${fixture.fixture.status.elapsed}'" to Color(0xFF4CAF50)
        "2H" -> "2nd Half ${fixture.fixture.status.elapsed}'" to Color(0xFF4CAF50)
        "HT" -> "Half Time" to Color(0xFFFF9800)
        "FT" -> "Full Time" to Color.Gray
        "ET" -> "Extra Time ${fixture.fixture.status.elapsed}'" to Color(0xFFFF9800)
        "P" -> "Penalties" to Color(0xFFFF9800)
        "AET" -> "After Extra Time" to Color.Gray
        "PEN" -> "After Penalties" to Color.Gray
        "PST" -> "Postponed" to Color(0xFFF44336)
        "CANC" -> "Cancelled" to Color(0xFFF44336)
        "ABD" -> "Abandoned" to Color(0xFFF44336)
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