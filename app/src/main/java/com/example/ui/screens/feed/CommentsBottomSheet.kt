package com.example.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    post: Post,
    repository: EarnGramRepository,
    onDismiss: () -> Unit
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    val comments by repository.getComments(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var newCommentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${langState.t("Comments", "टिप्पणियाँ")} (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_comments_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = langState.t(
                            "No comments yet. Be the first to share your thoughts!",
                            "अभी तक कोई टिप्पणी नहीं है। सबसे पहले अपनी बात कहें!"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AsyncImage(
                                model = if (comment.userAvatar.isNotBlank()) comment.userAvatar else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&q=80",
                                contentDescription = comment.userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = comment.userName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = formatTime(comment.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = {
                            Text(
                                text = langState.t("Add a comment...", "एक टिप्पणी लिखें..."),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                isPosting = true
                                coroutineScope.launch {
                                    repository.addComment(post.id, newCommentText)
                                    newCommentText = ""
                                    isPosting = false
                                }
                            }
                        },
                        enabled = !isPosting && newCommentText.isNotBlank(),
                        modifier = Modifier.testTag("submit_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Post Comment",
                            tint = if (newCommentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
