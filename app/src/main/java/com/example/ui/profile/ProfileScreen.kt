package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.BefccLogo
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun ProfileScreen(
    viewModel: BefccViewModel,
    onOpenAdminDashboard: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user logged in", color = BefccTextMuted)
        }
        return
    }

    val user = currentUser!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.85f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(BefccNeonLime.copy(alpha = 0.8f), BefccNeonCyan.copy(alpha = 0.6f))
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with BEFCC border
                BefccLogo(size = 80.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = BefccTextPrimary
                )

                Text(
                    text = "@${user.username} • In-Game: ${user.inGameUsername}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BefccNeonLime
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        status = user.playerId,
                        color = BefccGoldAccent
                    )
                    StatusBadge(
                        status = user.divisionRank,
                        color = BefccNeonCyan
                    )
                    val roleTitle = when (user.role) {
                        UserRole.SUPER_ADMIN -> "LEADER"
                        UserRole.ADMIN -> "ADMINISTRATOR"
                        UserRole.PLAYER -> "VERIFIED PLAYER"
                    }
                    val roleColor = when (user.role) {
                        UserRole.SUPER_ADMIN -> BefccGoldAccent
                        UserRole.ADMIN -> BefccNeonCyan
                        UserRole.PLAYER -> BefccNeonLime
                    }
                    StatusBadge(
                        status = roleTitle,
                        color = roleColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showEditDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccTextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(BefccBorderSubtle, BefccNeonLime.copy(alpha = 0.6f)))
                    ),
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = BefccNeonLime, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Player Profile", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Win Rate & Performance Progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.7f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.5f), BefccBorderDark))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "COMPETITIVE PERFORMANCE",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = BefccTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Win Rate:", style = MaterialTheme.typography.bodyMedium, color = BefccTextSecondary)
                    Text(
                        "${String.format("%.1f", user.winRate)}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = BefccNeonLime
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (user.winRate / 100f).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BefccNeonLime,
                    trackColor = BefccSurfaceCardElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${user.wins}", fontWeight = FontWeight.Black, color = BefccNeonLime)
                        Text("Wins", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${user.draws}", fontWeight = FontWeight.Black, color = BefccGoldAccent)
                        Text("Draws", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${user.losses}", fontWeight = FontWeight.Black, color = BefccCrimsonAccent)
                        Text("Losses", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${user.points}", fontWeight = FontWeight.Black, color = BefccNeonCyan)
                        Text("Points", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricCard(
                title = "Total Matches",
                value = "${user.matchesPlayed}",
                icon = Icons.Default.SportsSoccer,
                iconColor = BefccNeonLime,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Goals Scored",
                value = "${user.goalsScored}",
                icon = Icons.Default.AddCircleOutline,
                iconColor = BefccNeonCyan,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Goals Conceded",
                value = "${user.goalsConceded}",
                icon = Icons.Default.RemoveCircleOutline,
                iconColor = BefccCrimsonAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Teams & Registration Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.7f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.5f), BefccBorderDark))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PLAYER INFORMATION & TEAMS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = BefccTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Official Player ID:", color = BefccTextSecondary)
                    Text(user.playerId, fontWeight = FontWeight.Bold, color = BefccGoldAccent)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Email:", color = BefccTextSecondary)
                    Text(user.email, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Favorite Team:", color = BefccTextSecondary)
                    Text(user.favoriteTeam, fontWeight = FontWeight.Bold, color = BefccNeonLime)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Registered Selected Teams:", color = BefccTextSecondary)
                    Text(user.selectedTeams, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Leader & Admin Dashboard Access (Only for Super Admin / Admin accounts)
        if (user.isAdminOrLeader) {
            Button(
                onClick = onOpenAdminDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_open_admin_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BefccGoldAccent,
                    contentColor = BefccTextDarkOnNeon
                )
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = BefccTextDarkOnNeon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "OPEN ${if (user.role == UserRole.SUPER_ADMIN) "LEADER" else "ADMIN"} DASHBOARD",
                    fontWeight = FontWeight.Black,
                    color = BefccTextDarkOnNeon
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedButton(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccCrimsonAccent),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(BefccCrimsonAccent.copy(alpha = 0.5f), BefccCrimsonDark))
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = BefccCrimsonAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", fontWeight = FontWeight.Bold, color = BefccCrimsonAccent)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentUser = user,
            onDismiss = { showEditDialog = false },
            onSave = { name, ign, favTeam, div, teams ->
                viewModel.updateProfile(name, ign, favTeam, div, teams)
                showEditDialog = false
            }
        )
    }
}
