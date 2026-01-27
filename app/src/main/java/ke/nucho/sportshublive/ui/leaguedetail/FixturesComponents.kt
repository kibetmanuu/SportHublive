package ke.nucho.sportshublive.ui.leaguedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ke.nucho.sportshublive.data.models.Fixture
import java.text.SimpleDateFormat
import java.util.*

// Theme Colors
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val BluePrimary = Color(0xFF1565C0)
private val BlueLight = Color(0xFF42A5F5)
private val AccentGreen = Color(0xFF4CAF50)

enum class FixtureFilter {
    ALL, LIVE, UPCOMING, FINISHED
}

@Composable
fun EnhancedFixturesContent(
    fixtures: List<Fixture>,
    onMatchClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FixtureFilter.ALL) }

    if (fixtures.isEmpty()) {
        EmptyStateContent(
            emoji = "📅",
            message = "No fixtures available for this league"
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            EnhancedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                fixtures = fixtures
            )

            val filteredFixtures = filterFixtures(fixtures, searchQuery, selectedFilter)

            if (filteredFixtures.isEmpty()) {
                EmptySearchContent(searchQuery)
            } else {
                Column {
                    if (searchQuery.isNotBlank()) {
                        SearchResultsInfo(
                            query = searchQuery,
                            totalMatches = filteredFixtures.size
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredFixtures, key = { it.fixture.id }) { fixture ->
                            CompactMatchCard(
                                fixture = fixture,
                                onClick = { onMatchClick(fixture.fixture.id) },
                                highlightQuery = searchQuery
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = BlueLight,
                modifier = Modifier.size(20.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(BlueLight),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search teams, dates, or months...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                },
                singleLine = true
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsInfo(
    query: String,
    totalMatches: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = BluePrimary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Search Results",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"$query\"",
                    fontSize = 14.sp,
                    color = BlueLight,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$totalMatches",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (totalMatches == 1) "match" else "matches",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: FixtureFilter,
    onFilterSelected: (FixtureFilter) -> Unit,
    fixtures: List<Fixture>
) {
    val liveCount = fixtures.count { isLive(it.fixture.status.short) }
    val upcomingCount = fixtures.count { it.fixture.status.short == "NS" }
    val finishedCount = fixtures.count { it.fixture.status.short == "FT" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "All (${fixtures.size})",
            selected = selectedFilter == FixtureFilter.ALL,
            onClick = { onFilterSelected(FixtureFilter.ALL) }
        )
        if (liveCount > 0) {
            FilterChip(
                label = "🔴 Live ($liveCount)",
                selected = selectedFilter == FixtureFilter.LIVE,
                onClick = { onFilterSelected(FixtureFilter.LIVE) }
            )
        }
        FilterChip(
            label = "Upcoming ($upcomingCount)",
            selected = selectedFilter == FixtureFilter.UPCOMING,
            onClick = { onFilterSelected(FixtureFilter.UPCOMING) }
        )
        FilterChip(
            label = "Finished ($finishedCount)",
            selected = selectedFilter == FixtureFilter.FINISHED,
            onClick = { onFilterSelected(FixtureFilter.FINISHED) }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(32.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) BluePrimary else DarkSurface,
        border = if (!selected) {
            androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        } else null
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmptySearchContent(query: String) {
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
            Text(text = "🔍", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No matches found",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$query\"",
                fontSize = 14.sp,
                color = BlueLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Try searching by:\n• Team name (e.g., Arsenal, Chelsea)\n• Team initials (e.g., MU, MC)\n• Month (e.g., January, Feb)\n• Day (e.g., Thursday, Sat)\n• Date (e.g., Jan 23, 23/01)\n• Year (e.g., 2026)",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EmptyStateContent(emoji: String, message: String) {
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
            Text(text = emoji, fontSize = 56.sp)
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

@Composable
fun CompactMatchCard(
    fixture: Fixture,
    onClick: () -> Unit,
    highlightQuery: String = ""
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamInfo(
                    teamName = fixture.teams.home.name,
                    teamLogo = fixture.teams.home.logo,
                    score = fixture.goals.home,
                    modifier = Modifier.weight(1f),
                    isHighlighted = highlightQuery.isNotBlank() &&
                            (fixture.teams.home.name.contains(highlightQuery, ignoreCase = true) ||
                                    matchesTeamInitials(fixture.teams.home.name, highlightQuery))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    MatchStatus(fixture)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (fixture.goals.home != null && fixture.goals.away != null) {
                        Text(
                            text = "${fixture.goals.home} - ${fixture.goals.away}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        MatchTimeCompact(fixture)
                    }
                }

                TeamInfo(
                    teamName = fixture.teams.away.name,
                    teamLogo = fixture.teams.away.logo,
                    score = fixture.goals.away,
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                    isHighlighted = highlightQuery.isNotBlank() &&
                            (fixture.teams.away.name.contains(highlightQuery, ignoreCase = true) ||
                                    matchesTeamInitials(fixture.teams.away.name, highlightQuery))
                )
            }

            MatchDetailFooter(fixture)
        }
    }
}

@Composable
fun MatchDetailFooter(fixture: Fixture) {
    val (fullDate, time) = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val date = sdf.parse(fixture.fixture.date) ?: Date()

        val dateFmt = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        Pair(dateFmt.format(date), timeFmt.format(date))
    } catch (e: Exception) {
        Pair("Unknown Date", "TBD")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = BlueLight.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = fullDate,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "•",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.3f)
            )

            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = BlueLight.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }

        fixture.fixture.venue?.name?.let { venue ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = AccentGreen.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = venue,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                fixture.fixture.venue.city?.let { city ->
                    Text(
                        text = "• $city",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun TeamInfo(
    teamName: String,
    teamLogo: String,
    score: Int?,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    isHighlighted: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = teamLogo,
                contentDescription = teamName,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = teamName,
            fontSize = 13.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) AccentGreen else Color.White,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MatchStatus(fixture: Fixture) {
    val (text, color) = when (fixture.fixture.status.short) {
        "NS" -> "Upcoming" to Color.Gray
        "1H", "2H", "ET", "P", "LIVE" -> "LIVE" to AccentGreen
        "HT" -> "HT" to Color(0xFFFF9800)
        "FT" -> "FT" to Color.Gray
        else -> fixture.fixture.status.short to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MatchTimeCompact(fixture: Fixture) {
    val time = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val date = sdf.parse(fixture.fixture.date)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date ?: Date())
    } catch (e: Exception) {
        "TBD"
    }

    Text(
        text = time,
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.6f)
    )
}

// Helper Functions
private fun filterFixtures(
    fixtures: List<Fixture>,
    query: String,
    filter: FixtureFilter
): List<Fixture> {
    var filtered = fixtures

    filtered = when (filter) {
        FixtureFilter.LIVE -> filtered.filter { isLive(it.fixture.status.short) }
        FixtureFilter.UPCOMING -> filtered.filter { it.fixture.status.short == "NS" }
        FixtureFilter.FINISHED -> filtered.filter { it.fixture.status.short == "FT" }
        FixtureFilter.ALL -> filtered
    }

    if (query.isNotBlank()) {
        filtered = filtered.filter { fixture ->
            matchesSearchQuery(fixture, query)
        }
    }

    return filtered
}

private fun matchesSearchQuery(fixture: Fixture, query: String): Boolean {
    if (query.length < 2) return false

    val homeTeam = fixture.teams.home.name
    val awayTeam = fixture.teams.away.name

    val matchesTeam = homeTeam.contains(query, ignoreCase = true) ||
            awayTeam.contains(query, ignoreCase = true) ||
            matchesTeamInitials(homeTeam, query) ||
            matchesTeamInitials(awayTeam, query)

    val matchesDate = matchesDateQuery(fixture, query)

    return matchesTeam || matchesDate
}

private fun matchesTeamInitials(teamName: String, query: String): Boolean {
    if (query.length < 2) return false

    val words = teamName.split(" ", "-").filter { it.isNotEmpty() }
    if (words.isEmpty()) return false

    val initials = words.mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    return initials.startsWith(query.uppercase(), ignoreCase = true) ||
            initials.contains(query.uppercase())
}

private fun matchesDateQuery(fixture: Fixture, query: String): Boolean {
    if (query.length < 2) return false

    return try {
        val fixtureTime = fixture.fixture.timestamp * 1000
        val date = Date(fixtureTime)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val fullMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(date).lowercase()
        val shortMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(date).lowercase()
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date).lowercase()
        val shortDayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date).lowercase()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val year = calendar.get(Calendar.YEAR).toString()

        val q = query.lowercase().trim()

        val matchesMonth = fullMonth.contains(q) ||
                fullMonth.startsWith(q) ||
                shortMonth.contains(q) ||
                shortMonth.startsWith(q)

        val matchesDay = dayName.contains(q) ||
                dayName.startsWith(q) ||
                shortDayName.contains(q) ||
                shortDayName.startsWith(q)

        val matchesDayOfMonth = dayOfMonth == q ||
                dayOfMonth.padStart(2, '0') == q

        val matchesYear = year == q || year.endsWith(q)

        val fullDate = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date).lowercase()
        val shortDate = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date).lowercase()
        val numericDate1 = SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        val numericDate2 = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
        val numericDate3 = SimpleDateFormat("dd-MM", Locale.getDefault()).format(date)
        val numericDate4 = SimpleDateFormat("MM-dd", Locale.getDefault()).format(date)
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date).lowercase()
        val shortMonthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date).lowercase()

        val matchesCombined = fullDate.contains(q) ||
                shortDate.contains(q) ||
                numericDate1.contains(q) ||
                numericDate2.contains(q) ||
                numericDate3.contains(q) ||
                numericDate4.contains(q) ||
                monthYear.contains(q) ||
                shortMonthYear.contains(q)

        matchesMonth || matchesDay || matchesDayOfMonth || matchesYear || matchesCombined

    } catch (e: Exception) {
        false
    }
}

private fun isLive(status: String): Boolean {
    return status in listOf("1H", "2H", "ET", "P", "LIVE", "HT")
}