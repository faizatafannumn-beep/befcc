package com.example.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationType
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun NotificationsScreen(
    viewModel: BefccViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.userNotifications.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Notification Center",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = BefccTextPrimary
                )
                Text(
                    text = "${notifications.size} Updates & Announcements",
                    style = MaterialTheme.typography.labelSmall,
                    color = BefccEmeraldPrimary
                )
            }

            if (notifications.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.markNotificationsRead() },
                    colors = ButtonDefaults.textButtonColors(contentColor = BefccEmeraldLight)
                ) {
                    Text("Mark Read", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No notifications yet", color = BefccTextMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val (icon, iconColor) = when (notif.type) {
                        NotificationType.SLOT_APPROVED, NotificationType.RESULT_APPROVED -> Pair(Icons.Default.CheckCircle, StatusSuccess)
                        NotificationType.SLOT_REJECTED, NotificationType.RESULT_REJECTED -> Pair(Icons.Default.Cancel, StatusRejected)
                        NotificationType.MATCH_ASSIGNED, NotificationType.KNOCKOUT_ADVANCED -> Pair(Icons.Default.EmojiEvents, BefccGoldAccent)
                        NotificationType.SLOT_SUBMITTED, NotificationType.RESULT_SUBMITTED -> Pair(Icons.Default.Pending, StatusPending)
                        else -> Pair(Icons.Default.Notifications, BefccEmeraldPrimary)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notification_item_${notif.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) BefccSurfaceDark else BefccSurfaceCard
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    if (notif.isRead) BefccBorderDark else iconColor.copy(alpha = 0.5f),
                                    BefccSurfaceDark
                                )
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                color = iconColor.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (notif.isRead) BefccTextSecondary else BefccTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (notif.isRead) BefccTextMuted else BefccTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
