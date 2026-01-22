package ke.nucho.sportshublive.ui.livescores

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ke.nucho.sportshublive.data.models.Fixture
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Theme Colors - Deep Blue Theme
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val BluePrimary = Color(0xFF1565C0)      // Deep Blue
private val BlueLight = Color(0xFF42A5F5)        // Light Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScoresScreen(
    viewModel: LiveScoresViewModel = viewModel(),
    onMatchClick: (fixtureId: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isAutoRefresh by viewModel.isAutoRefreshEnabled.collectAsStateWithLifecycle()
    val selectedLeague by viewModel.selectedLeague.collectAsStateWithLifecycle()
    val isLiveView by viewModel.isLiveView.collectAsStateWithLifecycle()
    val apiProvider by viewModel.apiProvider.collectAsStateWithLifecycle()

    // Auto-refresh effect
    LaunchedEffect(isAutoRefresh, isLiveView) {
        if (isAutoRefresh && isLiveView) {
            while (true) {
                delay(30000)
                viewModel.refresh()
            }
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                selectedLeague = selectedLeague,
                apiProvider = apiProvider,
                isLiveView = isLiveView,
                isAutoRefresh = isAutoRefresh,
                onAutoRefreshToggle = { viewModel.toggleAutoRefresh() },
                onRefresh = { viewModel.refresh() }
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // League Filter
            ModernLeagueFilter(
                selectedLeague = selectedLeague,
                onLeagueSelected = { viewModel.selectLeague(it) }
            )

            // Date Selector
            ModernDateSelector(
                selectedDate = selectedDate,
                isLiveView = isLiveView,
                onDateSelected = { viewModel.loadFixturesByDate(it) },
                onLiveClick = { viewModel.loadLiveMatches() }
            )

            // Content
            when (val state = uiState) {
                is LiveScoresUiState.Loading -> ModernLoadingContent()
                is LiveScoresUiState.Success -> {
                    ModernMatchesList(
                        fixtures = state.fixtures,
                        isLiveView = isLiveView,
                        onMatchClick = onMatchClick
                    )
                }
                is LiveScoresUiState.Error -> {
                    ModernErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }
                is LiveScoresUiState.Empty -> ModernEmptyContent(message = state.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    selectedLeague: Int?,
    apiProvider: String,
    isLiveView: Boolean,
    isAutoRefresh: Boolean,
    onAutoRefreshToggle: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚽ Football Matches",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (selectedLeague != null) {
                        val leagueInfo = LiveScoresViewModel.FOOTBALL_LEAGUES[selectedLeague]
                        Text(
                            text = "${leagueInfo?.flag} ${leagueInfo?.name}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    } else if (apiProvider.isNotEmpty()) {
                        Text(
                            text = apiProvider,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        actions = {
            // Only show auto-refresh toggle when in live view
            if (isLiveView) {
                IconButton(onClick = onAutoRefreshToggle) {
                    Icon(
                        imageVector = if (isAutoRefresh) Icons.Default.Autorenew else Icons.Default.Close,
                        contentDescription = if (isAutoRefresh) "Disable auto-refresh" else "Enable auto-refresh",
                        tint = if (isAutoRefresh) BlueLight else Color.Gray
                    )
                }
            }
            // Single refresh button
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
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
fun ModernLeagueFilter(
    selectedLeague: Int?,
    onLeagueSelected: (Int?) -> Unit
) {
    val leagues = listOf(
        null to "🌍 All",
        39 to "🏴󠁧󠁢󠁥󠁮󠁧󠁿 EPL",
        140 to "🇪🇸 LaLiga",
        78 to "🇩🇪 Bundesliga",
        135 to "🇮🇹 SerieA",
        61 to "🇫🇷 Ligue1",
        2 to "⚽ UCL",
        3 to "🏆 UEL"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(leagues) { (leagueId, leagueName) ->
            ModernLeagueChip(
                label = leagueName,
                isSelected = selectedLeague == leagueId,
                onClick = { onLeagueSelected(leagueId) }
            )
        }
    }
}

@Composable
fun ModernLeagueChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) BluePrimary else Color(0xFF2C2C2C),
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ModernDateSelector(
    selectedDate: String,
    isLiveView: Boolean,
    onDateSelected: (String) -> Unit,
    onLiveClick: () -> Unit
) {
    val datesList = remember {
        buildList {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val shortSdf = SimpleDateFormat("EEE dd", Locale.getDefault())
            val today = sdf.format(Date())

            for (offset in -3..3) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, offset)
                val date = sdf.format(cal.time)
                val label = if (date == today) "Today" else shortSdf.format(cal.time)
                add(Triple(date, label, offset))
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            ModernDateChip(
                label = "🔴 LIVE",
                isSelected = isLiveView,
                onClick = onLiveClick,
                isLive = true
            )
        }

        items(datesList) { (date, label, offset) ->
            ModernDateChip(
                label = label,
                isSelected = !isLiveView && date == selectedDate,
                onClick = { onDateSelected(date) },
                isToday = offset == 0
            )
        }
    }
}

@Composable
fun ModernDateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLive: Boolean = false,
    isToday: Boolean = false
) {
    var pulse by remember { mutableStateOf(false) }

    LaunchedEffect(isSelected && isLive) {
        if (isSelected && isLive) {
            while (true) {
                pulse = !pulse
                delay(1000)
            }
        }
    }

    val backgroundColor = when {
        isSelected && isLive -> Color(0xFFD32F2F)
        isSelected -> BluePrimary
        isToday -> Color(0xFF37474F)
        else -> Color(0xFF2C2C2C)
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isLive && isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (pulse) Color.White else Color.White.copy(0.5f))
                )
            }
            Text(
                text = label,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ModernMatchesList(
    fixtures: List<Fixture>,
    isLiveView: Boolean,
    onMatchClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val groupedFixtures = fixtures.groupBy { it.league.name }

        groupedFixtures.forEach { (leagueName, leagueFixtures) ->
            item {
                ModernLeagueHeader(
                    leagueName = leagueName,
                    leagueLogo = leagueFixtures.first().league.logo,
                    matchCount = leagueFixtures.size
                )
            }

            items(
                items = leagueFixtures,
                key = { it.fixture.id }
            ) { fixture ->
                ModernMatchCard(
                    fixture = fixture,
                    isLive = isLiveView,
                    onClick = { onMatchClick(fixture.fixture.id) }
                )
            }
        }
    }
}

@Composable
fun ModernLeagueHeader(
    leagueName: String,
    leagueLogo: String,
    matchCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = leagueLogo,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = leagueName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$matchCount",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ModernMatchCard(
    fixture: Fixture,
    isLive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernMatchTime(fixture)
                ModernStatusBadge(fixture)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernTeamColumn(
                    teamName = fixture.teams.home.name,
                    teamLogo = fixture.teams.home.logo,
                    score = fixture.goals.home,
                    isWinner = (fixture.goals.home ?: 0) > (fixture.goals.away ?: 0),
                    modifier = Modifier.weight(1f)
                )

                ModernScore(
                    homeScore = fixture.goals.home,
                    awayScore = fixture.goals.away,
                    status = fixture.fixture.status.short
                )

                ModernTeamColumn(
                    teamName = fixture.teams.away.name,
                    teamLogo = fixture.teams.away.logo,
                    score = fixture.goals.away,
                    isWinner = (fixture.goals.away ?: 0) > (fixture.goals.home ?: 0),
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
fun ModernMatchTime(fixture: Fixture) {
    val time = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val date = sdf.parse(fixture.fixture.date)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date ?: Date())
    } catch (e: Exception) {
        "00:00"
    }

    Text(
        text = time,
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.6f),
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun ModernStatusBadge(fixture: Fixture) {
    val (text, color) = when (fixture.fixture.status.short) {
        "NS" -> "Scheduled" to Color(0xFF757575)
        "1H", "2H", "ET", "P", "LIVE" -> "${fixture.fixture.status.elapsed}'" to BlueLight
        "HT" -> "HT" to Color(0xFFFF9800)
        "FT" -> "FT" to Color(0xFF9E9E9E)
        "PST" -> "Postponed" to Color(0xFFE53935)
        "CANC" -> "Cancelled" to Color(0xFFE53935)
        else -> fixture.fixture.status.short to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModernTeamColumn(
    teamName: String,
    teamLogo: String,
    score: Int?,
    isWinner: Boolean,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        AsyncImage(
            model = teamLogo,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = teamName,
            fontSize = 14.sp,
            fontWeight = if (isWinner && score != null) FontWeight.Bold else FontWeight.Normal,
            color = if (isWinner && score != null) BlueLight else Color.White,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ModernScore(
    homeScore: Int?,
    awayScore: Int?,
    status: String
) {
    if (status != "NS" && homeScore != null && awayScore != null) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeScore.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (homeScore > awayScore) BlueLight else Color.White
            )
            Text(
                text = " : ",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Text(
                text = awayScore.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (awayScore > homeScore) BlueLight else Color.White
            )
        }
    } else {
        Text(
            text = "VS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun ModernLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = BlueLight,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Loading matches...",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ModernErrorContent(
    message: String,
    onRetry: () -> Unit
) {
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
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFE53935)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ModernEmptyContent(message: String) {
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
                text = "⚽",
                fontSize = 56.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}