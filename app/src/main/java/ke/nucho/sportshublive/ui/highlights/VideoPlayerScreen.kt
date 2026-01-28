package ke.nucho.sportshublive.ui.highlights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun VideoScreen(
    videoHighlight: VideoHighlight,
    onBackClick: () -> Unit = {},
    onVideoClick: (VideoHighlight) -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }

    // Sample related videos
    val relatedVideos = remember {
        getSampleHighlights().filter { it.id != videoHighlight.id }.take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Video Player Section (Placeholder until ExoPlayer is added)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            // Thumbnail as placeholder
            AsyncImage(
                model = videoHighlight.thumbnailUrl,
                contentDescription = videoHighlight.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play button overlay
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .background(
                        color = Color(0xFF42A5F5).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(16.dp),
                tint = Color.White
            )

            // Back button overlay
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Note overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = Color(0xFF42A5F5).copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Add ExoPlayer dependency to enable video playback",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Video Details and Related Videos
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Video Title and Info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = videoHighlight.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category badge
                    Surface(
                        color = Color(0xFF42A5F5).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = videoHighlight.category,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Match Info
                    Text(
                        text = videoHighlight.matchInfo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Views and date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${videoHighlight.views} views",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = videoHighlight.uploadDate,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Like Button
                        VideoActionButton(
                            icon = Icons.Default.ThumbUp,
                            label = "Like",
                            onClick = { }
                        )

                        // Dislike Button
                        VideoActionButton(
                            icon = Icons.Default.ThumbDown,
                            label = "Dislike",
                            onClick = { }
                        )

                        // Share Button
                        VideoActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = { }
                        )

                        // Save Button
                        VideoActionButton(
                            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            label = "Save",
                            tint = if (isFavorite) Color(0xFFE91E63) else Color.White.copy(alpha = 0.7f),
                            onClick = { isFavorite = !isFavorite }
                        )

                        // Download Button
                        VideoActionButton(
                            icon = Icons.Default.Download,
                            label = "Download",
                            onClick = { }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Related Videos Section
            item {
                Text(
                    text = "Related Highlights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Related Videos List
            items(relatedVideos) { highlight ->
                RelatedVideoCard(
                    highlight = highlight,
                    onClick = { onVideoClick(highlight) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun VideoActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White.copy(alpha = 0.7f),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun RelatedVideoCard(
    highlight: VideoHighlight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(90.dp)
        ) {
            AsyncImage(
                model = highlight.thumbnailUrl,
                contentDescription = highlight.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Duration badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = highlight.duration,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        // Video Info
        Column(
            modifier = Modifier
                .weight(1f)
                .height(90.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = highlight.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = highlight.matchInfo,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Views and date
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${highlight.views} views",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = highlight.uploadDate,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}