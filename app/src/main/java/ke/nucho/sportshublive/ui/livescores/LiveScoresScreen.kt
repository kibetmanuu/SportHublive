package ke.nucho.sportshublive.ui.livescores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun LiveScoresScreen(
    viewModel: LiveScoresViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isAutoRefresh by viewModel.isAutoRefreshEnabled.collectAsStateWithLifecycle()
    val selectedSport by viewModel.selectedSport.collectAsStateWithLifecycle()
    val selectedLeague by viewModel.selectedLeague.collectAsStateWithLifecycle()

    // Auto-refresh effect
    LaunchedEffect(isAutoRefresh) {
        if (isAutoRefresh) {
            kotlinx.coroutines.delay(30000) // Refresh every 30 seconds
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Live Scores",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleAutoRefresh() }) {
                        Icon(
                            imageVector = if (isAutoRefresh) Icons.Default.Refresh else Icons.Default.Close,
                            contentDescription = if (isAutoRefresh) "Auto-refresh ON" else "Auto-refresh OFF",
                            tint = if (isAutoRefresh) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sports Selector
            SportsFilterRow(
                selectedSport = selectedSport,
                onSportSelected = { sport ->
                    viewModel.selectSport(sport)
                }
            )

            // Competition/League Selector (only for Football)
            if (selectedSport == "Football") {
                LeagueFilterRow(
                    selectedLeague = selectedLeague,
                    onLeagueSelected = { leagueId ->
                        viewModel.selectLeague(leagueId)
                    }
                )
            }

            // Date Selector
            DateSelectorRow(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.loadFixturesByDate(date)
                },
                onLiveClick = {
                    viewModel.loadLiveMatches()
                }
            )

            // Content
            when (val state = uiState) {
                is LiveScoresUiState.Loading -> {
                    LoadingContent()
                }
                is LiveScoresUiState.Success -> {
                    MatchesList(fixtures = state.fixtures)
                }
                is LiveScoresUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }
                is LiveScoresUiState.Empty -> {
                    EmptyContent(message = state.message)
                }
            }
        }
    }
}

@Composable
fun DateSelectorRow(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onLiveClick: () -> Unit
) {
    val dates = remember {
        (-2..2).map { offset ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, offset)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displaySdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
            sdf.format(calendar.time) to displaySdf.format(calendar.time)
        }
    }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Live button
        item {
            DateChip(
                label = "LIVE",
                isSelected = selectedDate == today,
                onClick = onLiveClick
            )
        }

        // Date chips
        items(dates) { (date, displayDate) ->
            DateChip(
                label = displayDate,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun SportsFilterRow(
    selectedSport: String,
    onSportSelected: (String) -> Unit
) {
    val sports = listOf(
        "⚽ Football",
        "🏀 Basketball",
        "🏒 Hockey",
        "🏎️ Formula 1",
        "🏐 Volleyball",
        "🏉 Rugby"
    )

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(sports) { sport ->
            val sportName = sport.substringAfter(" ")
            SportChip(
                label = sport,
                isSelected = selectedSport == sportName,
                onClick = { onSportSelected(sportName) }
            )
        }
    }
}

@Composable
fun LeagueFilterRow(
    selectedLeague: Int?,
    onLeagueSelected: (Int?) -> Unit
) {
    // Popular football leagues
    val leagues = listOf(
        null to "🌍 All",
        39 to "🏴󠁧󠁢󠁥󠁮󠁧󠁿 Premier League",
        140 to "🇪🇸 La Liga",
        78 to "🇩🇪 Bundesliga",
        135 to "🇮🇹 Serie A",
        61 to "🇫🇷 Ligue 1",
        2 to "⚽ Champions League",
        3 to "🏆 Europa League"
    )

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(leagues) { (leagueId, leagueName) ->
            LeagueChip(
                label = leagueName,
                isSelected = selectedLeague == leagueId,
                onClick = { onLeagueSelected(leagueId) }
            )
        }
    }
}

@Composable
fun SportChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun LeagueChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MatchesList(fixtures: List<Fixture>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(fixtures) { fixture ->
            MatchCard(fixture = fixture)
        }
    }
}

@Composable
fun MatchCard(fixture: Fixture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // League info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = fixture.league.logo,
                        contentDescription = fixture.league.name,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fixture.league.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Match status
                MatchStatusBadge(fixture = fixture)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teams and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home team
                TeamSection(
                    teamName = fixture.teams.home.name,
                    teamLogo = fixture.teams.home.logo,
                    modifier = Modifier.weight(1f)
                )

                // Score
                ScoreSection(
                    homeScore = fixture.goals.home,
                    awayScore = fixture.goals.away,
                    status = fixture.fixture.status.short
                )

                // Away team
                TeamSection(
                    teamName = fixture.teams.away.name,
                    teamLogo = fixture.teams.away.logo,
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            // Match time/date
            if (fixture.fixture.status.short == "NS") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatMatchTime(fixture.fixture.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun TeamSection(
    teamName: String,
    teamLogo: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        AsyncImage(
            model = teamLogo,
            contentDescription = teamName,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
    }
}

@Composable
fun ScoreSection(
    homeScore: Int?,
    awayScore: Int?,
    status: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (status != "NS" && homeScore != null && awayScore != null) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = homeScore.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (homeScore > awayScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " - ",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = awayScore.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (awayScore > homeScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Text(
                text = "VS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MatchStatusBadge(fixture: Fixture) {
    val (text, color) = when (fixture.fixture.status.short) {
        "NS" -> "Scheduled" to Color.Gray
        "1H", "2H", "ET", "P" -> "${fixture.fixture.status.elapsed}'" to Color(0xFF4CAF50)
        "HT" -> "Half Time" to Color(0xFFFF9800)
        "FT" -> "Full Time" to Color.Gray
        "AET" -> "Extra Time Ended" to Color.Gray
        "PEN" -> "Penalties" to Color(0xFFFF9800)
        "PST" -> "Postponed" to Color(0xFFF44336)
        "CANC" -> "Cancelled" to Color(0xFFF44336)
        "ABD" -> "Abandoned" to Color(0xFFF44336)
        else -> fixture.fixture.status.long to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading matches...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Empty",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Format match date/time
 */
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