package com.example.ui.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun StandingsScreen(
    viewModel: BefccViewModel,
    modifier: Modifier = Modifier
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val rankedPlayers = remember(allUsers) {
        allUsers.filter { it.role == UserRole.PLAYER || it.matchesPlayed > 0 }
            .sortedWith(
                compareByDescending<UserEntity> { it.points }
                    .thenByDescending { it.wins }
                    .thenByDescending { it.goalsScored - it.goalsConceded }
                    .thenByDescending { it.goalsScored }
            )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Leaderboard Title & Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.8f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccNeonLime.copy(alpha = 0.6f), BefccNeonCyan.copy(alpha = 0.4f)))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BefccNeonLime.copy(alpha = 0.12f))
                            .border(1.dp, BefccNeonLime.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = BefccNeonLime,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NATIONAL LEADERBOARD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = BefccTextPrimary
                        )
                        Text(
                            text = "Official BEFCC Competitive Ranking",
                            style = MaterialTheme.typography.labelSmall,
                            color = BefccNeonLime
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (rankedPlayers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Leaderboard,
                        contentDescription = null,
                        tint = BefccTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Ranked Players Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BefccTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Official standings and rankings will automatically update as tournament and challenge match scores are verified.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BefccTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Table Header
            Surface(
                color = BefccSurfaceCardElevated.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, modifier = Modifier.width(26.dp))
                    Text("PLAYER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, modifier = Modifier.weight(1f))
                    Text("P", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                    Text("W", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                    Text("D", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                    Text("L", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                    Text("PTS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = BefccNeonLime, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Players Rankings List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
            itemsIndexed(rankedPlayers, key = { _, user -> user.id }) { index, user ->
                val isCurrentUser = currentUser?.id == user.id
                val rank = index + 1

                val rankColor = when (rank) {
                    1 -> BefccNeonLime
                    2 -> BefccNeonCyan
                    3 -> BefccGoldAccent
                    else -> BefccTextSecondary
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaderboard_player_$rank"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentUser) BefccSurfaceGlass else BefccSurfaceGlass.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = if (isCurrentUser) {
                            Brush.linearGradient(listOf(BefccNeonLime.copy(alpha = 0.7f), BefccNeonCyan.copy(alpha = 0.4f)))
                        } else {
                            Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.4f), BefccBorderDark))
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (rank <= 3) rankColor.copy(alpha = 0.15f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rank",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = rankColor
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Player Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCurrentUser) BefccNeonLime else BefccTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (rank == 1) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BefccNeonLime, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = "${user.playerId} • ${user.favoriteTeam}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BefccTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Stats
                        Text("${user.matchesPlayed}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                        Text("${user.wins}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                        Text("${user.draws}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                        Text("${user.losses}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                        Text(
                            text = "${user.points}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isCurrentUser || rank == 1) BefccNeonLime else BefccTextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }
        }
    }
}
}
