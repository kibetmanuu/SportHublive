package ke.nucho.sportshublive.ui.highlights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun MatchHighlightsScreen(
    onVideoClick: (VideoHighlight) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Premier League", "La Liga", "Champions League", "Goals", "Skills")

    // Sample data - replace with actual data from your repository
    val highlights = remember {
        getSampleHighlights()
    }

    val filteredHighlights = remember(selectedFilter, highlights) {
        if (selectedFilter == "All") {
            highlights
        } else {
            highlights.filter { it.category == selectedFilter }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Match Highlights",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Watch the best moments",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(filters.size) { index ->
                val filter = filters[index]
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF42A5F5),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF2A2A2A),
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Video list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredHighlights) { highlight ->
                VideoHighlightCard(
                    highlight = highlight,
                    onClick = { onVideoClick(highlight) }
                )
            }

            // Empty state
            if (filteredHighlights.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎬",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No highlights found",
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoHighlightCard(
    highlight: VideoHighlight,
    onClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail with play button overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = highlight.thumbnailUrl,
                    contentDescription = highlight.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Play button
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                        .background(
                            color = Color(0xFF42A5F5).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(12.dp),
                    tint = Color.White
                )

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = highlight.duration,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Category badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = Color(0xFF42A5F5).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = highlight.category,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = highlight.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Match info
                Text(
                    text = highlight.matchInfo,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: views, date, actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Views
                        Text(
                            text = "${highlight.views} views",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        // Date
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Text(
                            text = highlight.uploadDate,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFE91E63) else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class VideoHighlight(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: String,
    val category: String,
    val matchInfo: String,
    val views: String,
    val uploadDate: String
)

// Sample data - replace with actual API data
fun getSampleHighlights(): List<VideoHighlight> {
    return listOf(
        VideoHighlight(
            id = "1",
            title = "Manchester United vs Liverpool - All Goals & Highlights",
            thumbnailUrl = "https://via.placeholder.com/640x360/FF0000/FFFFFF?text=Man+Utd+vs+Liverpool",
            videoUrl = "",
            duration = "5:24",
            category = "Premier League",
            matchInfo = "Man United 2-3 Liverpool",
            views = "1.2M",
            uploadDate = "2 days ago"
        ),
        VideoHighlight(
            id = "2",
            title = "Real Madrid vs Barcelona - El Clasico Extended Highlights",
            thumbnailUrl = "https://via.placeholder.com/640x360/0000FF/FFFFFF?text=Real+Madrid+vs+Barca",
            videoUrl = "",
            duration = "8:15",
            category = "La Liga",
            matchInfo = "Real Madrid 3-1 Barcelona",
            views = "2.5M",
            uploadDate = "1 day ago"
        ),
        VideoHighlight(
            id = "3",
            title = "Bayern Munich Amazing Goals Compilation",
            thumbnailUrl = "https://via.placeholder.com/640x360/FF0000/FFFFFF?text=Bayern+Goals",
            videoUrl = "",
            duration = "6:45",
            category = "Goals",
            matchInfo = "Bayern Munich 4-0 Dortmund",
            views = "850K",
            uploadDate = "3 days ago"
        ),
        VideoHighlight(
            id = "4",
            title = "PSG vs Manchester City - Champions League Thriller",
            thumbnailUrl = "https://via.placeholder.com/640x360/000080/FFFFFF?text=PSG+vs+Man+City",
            videoUrl = "",
            duration = "7:30",
            category = "Champions League",
            matchInfo = "PSG 2-2 Manchester City",
            views = "3.1M",
            uploadDate = "5 hours ago"
        ),
        VideoHighlight(
            id = "5",
            title = "Top 10 Skills & Tricks of the Week",
            thumbnailUrl = "https://via.placeholder.com/640x360/FFD700/000000?text=Top+Skills",
            videoUrl = "",
            duration = "4:20",
            category = "Skills",
            matchInfo = "Weekly Compilation",
            views = "620K",
            uploadDate = "1 day ago"
        ),
        VideoHighlight(
            id = "6",
            title = "Arsenal vs Chelsea - North London Derby",
            thumbnailUrl = "https://via.placeholder.com/640x360/FF0000/FFFFFF?text=Arsenal+vs+Chelsea",
            videoUrl = "",
            duration = "6:12",
            category = "Premier League",
            matchInfo = "Arsenal 1-1 Chelsea",
            views = "980K",
            uploadDate = "4 days ago"
        ),
        VideoHighlight(
            id = "7",
            title = "Atletico Madrid vs Sevilla - Full Match Highlights",
            thumbnailUrl = "https://via.placeholder.com/640x360/FF0000/FFFFFF?text=Atletico+vs+Sevilla",
            videoUrl = "",
            duration = "5:55",
            category = "La Liga",
            matchInfo = "Atletico 2-1 Sevilla",
            views = "450K",
            uploadDate = "2 days ago"
        ),
        VideoHighlight(
            id = "8",
            title = "Inter Milan vs AC Milan - Derby della Madonnina",
            thumbnailUrl = "https://via.placeholder.com/640x360/0000FF/FFFFFF?text=Milan+Derby",
            videoUrl = "",
            duration = "7:05",
            category = "All",
            matchInfo = "Inter 3-2 AC Milan",
            views = "1.5M",
            uploadDate = "6 hours ago"
        )
    )
}