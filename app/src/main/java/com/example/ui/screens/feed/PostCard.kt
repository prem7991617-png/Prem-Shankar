package com.example.ui.screens.feed

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaType
import com.example.data.model.Post
import com.example.util.LocalLanguageState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val langState = LocalLanguageState.current
    var isPlayingVideo by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Creator details & Report option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = if (post.userAvatar.isNotBlank()) post.userAvatar else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&q=80",
                        contentDescription = post.userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.userName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Creator",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = formatTime(post.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onReportClick,
                    modifier = Modifier.testTag("report_post_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Report post",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Media Container (Photo or Video)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (post.mediaType == MediaType.VIDEO) {
                    // Video duration badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "0:${if (post.videoDurationSec < 10) "0" else ""}${post.videoDurationSec}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Play / Pause overlay
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { isPlayingVideo = !isPlayingVideo }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlayingVideo) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingVideo) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    if (isPlayingVideo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Playing Preview",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Action Buttons Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Like button
                    val heartColor by animateColorAsState(
                        targetValue = if (post.isLikedByCurrentUser) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                        label = "heartColor"
                    )

                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.testTag("like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = heartColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Comment button
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.testTag("comment_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${post.commentsCount}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Share button
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this post on EarnGram by ${post.userName}: \"${post.caption}\" https://earngram.app/p/${post.id}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                        },
                        modifier = Modifier.testTag("share_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Eligible Reward Score Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Score",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${"%.1f".format(post.eligibleRewardScore)} pts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Caption & Tags
            if (post.caption.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
