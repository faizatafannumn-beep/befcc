package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SlotStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun BefccLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showBorder: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBorder) {
                    Modifier
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    BefccGoldAccent,
                                    BefccEmeraldPrimary,
                                    BefccCrimsonAccent,
                                    BefccGoldAccent
                                )
                            ),
                            CircleShape
                        )
                } else Modifier.clip(CircleShape)
            )
            .background(BefccSurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.befcc_logo),
            contentDescription = "Official BEFCC Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBorder) 2.dp else 0.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BefccTopAppBar(
    title: String,
    subtitle: String? = null,
    role: UserRole = UserRole.PLAYER,
    unreadNotifCount: Int = 0,
    onNotifClick: () -> Unit = {},
    onAdminDashboardClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        color = BefccSurfaceDark,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BefccBorderDark.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                // Sleek Gradient Squircle Logo Frame
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BefccNeonLime.copy(alpha = 0.2f), BefccNeonCyan.copy(alpha = 0.1f))
                            )
                        )
                        .border(1.dp, BefccNeonLime.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BefccLogo(size = 38.dp, showBorder = false)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = BefccTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle?.uppercase() ?: "COMPETITIVE COMMUNITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 9.sp
                        ),
                        color = BefccNeonLime,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Leader / Admin Dashboard Quick Button: ONLY for Leader / Admin team
                if (role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN) {
                    val roleLabel = if (role == UserRole.SUPER_ADMIN) "LEADER" else "ADMIN"
                    AssistChip(
                        onClick = onAdminDashboardClick,
                        label = {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = BefccGoldAccent
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Open $roleLabel Dashboard",
                                tint = BefccGoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = BefccSurfaceCardElevated.copy(alpha = 0.7f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = BefccGoldAccent.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("leader_dashboard_button")
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Notifications Icon Button with Sleek Glow Badge
                IconButton(
                    onClick = onNotifClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BefccSurfaceCardElevated.copy(alpha = 0.5f))
                        .border(1.dp, BefccBorderDark, RoundedCornerShape(10.dp))
                        .testTag("notification_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(BefccCrimsonAccent)
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = BefccTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = BefccNeonLime,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.65f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(BefccBorderSubtle.copy(alpha = 0.6f), BefccBorderDark)
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = BefccTextMuted
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.12f))
                        .border(1.dp, iconColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = BefccTextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = iconColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.7f)))
        ),
        modifier = modifier
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                fontSize = 9.sp
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
