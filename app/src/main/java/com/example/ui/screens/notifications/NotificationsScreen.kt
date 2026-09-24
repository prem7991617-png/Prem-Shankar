package com.example.ui.screens.notifications

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: EarnGramRepository,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    val notifications by repository.getNotifications().collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadCount by repository.getUnreadNotificationsCount().collectAsStateWithLifecycle(initialValue = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = langState.t("Notifications", "सूचनाएं"),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.markNotificationsRead()
                                }
                            },
                            modifier = Modifier.testTag("mark_notifications_read_button")
                        ) {
                            Text(langState.t("Mark all read", "सभी पढ़ें"))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = langState.t("No notifications yet", "अभी कोई सूचना नहीं है"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = langState.t(
                            "Engagement alerts and reward updates will appear here.",
                            "जुड़ाव और रिवार्ड से संबंधित सभी अपडेट यहाँ दिखेंगे।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCard(notif = notif)
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification) {
    val (icon, iconColor) = when (notif.type) {
        NotificationType.REWARD_CREDITED -> Icons.Default.MonetizationOn to Color(0xFF10B981)
        NotificationType.WITHDRAWAL_PAID -> Icons.Default.CheckCircle to Color(0xFF10B981)
        NotificationType.WITHDRAWAL_APPROVED -> Icons.Default.ThumbUp to Color(0xFF3B82F6)
        NotificationType.WITHDRAWAL_REJECTED, NotificationType.POST_REJECTED -> Icons.Default.Cancel to Color(0xFFEF4444)
        NotificationType.POST_APPROVED -> Icons.Default.Verified to Color(0xFF6366F1)
        NotificationType.LIKE_RECEIVED -> Icons.Default.Favorite to Color(0xFFEF4444)
        NotificationType.COMMENT_RECEIVED -> Icons.Default.ChatBubble to Color(0xFF8B5CF6)
        NotificationType.WITHDRAWAL_SUBMITTED -> Icons.Default.Schedule to Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notif.isRead) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (!notif.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notif.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notif.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
