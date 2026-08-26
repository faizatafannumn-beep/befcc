package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun UserHomeScreen(
    viewModel: BefccViewModel,
    onNavigateToTournament: (TournamentEntity) -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToSlots: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenAdminPortal: () -> Unit,
    onSubmitMatchScore: (MatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allTournaments by viewModel.allTournaments.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allSlots by viewModel.allSlots.collectAsState()

    val myMatches = remember(allMatches, currentUser) {
        allMatches.filter { m ->
            currentUser?.id != null && (m.player1Id == currentUser?.id || m.player2Id == currentUser?.id)
        }
    }

    val upcomingMatch = remember(myMatches) {
        myMatches.find { it.status == MatchStatus.SCHEDULED }
    }

    val activeTournaments = remember(allTournaments) {
        allTournaments.filter { it.status != TournamentStatus.COMPLETED }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Hero BEFCC Featured Match / Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            BefccNeonLime.copy(alpha = 0.4f),
                            BefccBorderSubtle,
                            BefccNeonCyan.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header tag row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = BefccNeonLime.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(BefccNeonLime.copy(alpha = 0.5f), BefccNeonLime))
                            )
                        ) {
                            Text(
                                text = if (upcomingMatch != null) "NEXT MATCH" else "BEFCC OFFICIAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 9.sp
                                ),
                                color = BefccNeonLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = BefccSurfaceCardElevated.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(BefccBorderDark, BefccBorderDark))
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = BefccNeonCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (upcomingMatch != null) upcomingMatch.scheduledTime.take(12) else "SEASON 2026",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = BefccNeonCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (upcomingMatch != null) {
                        // Match Showcase with VS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Player 1
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BefccSurfaceCardElevated)
                                        .border(1.dp, BefccBorderSubtle, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = BefccNeonLime,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = upcomingMatch.player1Name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BefccTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = upcomingMatch.player1Team,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BefccTextMuted,
                                    maxLines = 1
                                )
                            }

                            // VS Badge
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .background(BefccSurfaceCardElevated.copy(alpha = 0.8f))
                                    .border(1.dp, BefccBorderDark, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        fontSize = 10.sp
                                    ),
                                    color = BefccTextMuted
                                )
                            }

                            // Player 2
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BefccSurfaceCardElevated)
                                        .border(1.dp, BefccBorderSubtle, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = BefccNeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = upcomingMatch.player2Name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BefccTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = upcomingMatch.player2Team,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BefccTextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${upcomingMatch.matchType} • ${upcomingMatch.roundStage}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BefccTextSecondary
                            )
                            Button(
                                onClick = { onSubmitMatchScore(upcomingMatch) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BefccNeonLime,
                                    contentColor = BefccTextDarkOnNeon
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "SUBMIT SCORE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }
                    } else {
                        // Community Welcome Header when no match scheduled
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BefccLogo(size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "BANGLADESH eFOOTBALL",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = BefccTextPrimary
                                )
                                Text(
                                    text = "Official National Esports Circuit & Tournaments",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BefccTextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Register for open slots, challenge rivals in 1v1 rooms, submit match screenshots, and climb the BEFCC national ranking ladder.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BefccTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 2-Column Sleek Metric Cards (Global Rank & Win Rate)
        item {
            currentUser?.let { user ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Metric 1: Global Rank
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.65f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.6f), BefccBorderDark))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "GLOBAL RANK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = BefccTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (user.matchesPlayed > 0) "#${user.points / 10 + 1}" else "UNRANKED",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                    fontSize = if (user.matchesPlayed > 0) 28.sp else 20.sp
                                ),
                                color = BefccTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (user.matchesPlayed > 0) "▲ Active" else "New Player",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = BefccNeonLime
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${user.points} pts)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = BefccTextMuted
                                )
                            }
                        }
                    }

                    // Metric 2: Win Rate
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.65f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.6f), BefccBorderDark))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "WIN RATE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = BefccTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${String.format("%.1f", user.winRate)}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = BefccTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${user.wins}W - ${user.losses}L",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = BefccNeonCyan
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${user.matchesPlayed} matches)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = BefccTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Next Upcoming Match Section
        if (upcomingMatch != null) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Upcoming Fixture",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BefccTextPrimary
                        )
                        TextButton(onClick = onNavigateToMatches) {
                            Text("View All Matches", color = BefccEmeraldPrimary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    MatchCard(
                        match = upcomingMatch,
                        currentUserId = currentUser?.id,
                        onSubmitScoreClick = { onSubmitMatchScore(upcomingMatch) }
                    )
                }
            }
        }

        // Active Tournaments Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE TOURNAMENTS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = BefccTextPrimary
                    )
                    if (activeTournaments.isNotEmpty()) {
                        TextButton(onClick = onNavigateToMatches) {
                            Text(
                                text = "VIEW ALL (${activeTournaments.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BefccNeonLime
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeTournaments.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.5f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.4f), BefccBorderDark))
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = BefccTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Active Tournaments",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BefccTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Official BEFCC tournaments will appear here when registrations open.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BefccTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    activeTournaments.forEach { tournament ->
                        val tournSlots = allSlots.filter { it.tournamentId == tournament.id }
                        val bookedCount = tournSlots.count { it.status == SlotStatus.CONFIRMED }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { onNavigateToTournament(tournament) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.7f)),
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(BefccSurfaceCardElevated)
                                                .border(1.dp, BefccNeonLime.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = BefccGoldAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = tournament.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                                color = BefccTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${tournament.type} • ${tournament.playerLimit} Slots",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BefccTextMuted
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onNavigateToTournament(tournament) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BefccNeonLime,
                                            contentColor = BefccTextDarkOnNeon
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (tournament.status == TournamentStatus.REGISTRATION_OPEN) "JOIN" else "VIEW",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Prize Pool: ", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                        Text(tournament.prizePool, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccGoldAccent)
                                    }

                                    Text(
                                        text = "$bookedCount / ${tournament.playerLimit} Registered",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = BefccNeonCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { if (tournament.playerLimit > 0) bookedCount.toFloat() / tournament.playerLimit.toFloat() else 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = BefccNeonLime,
                                    trackColor = BefccSurfaceCardElevated
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation Hub
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.65f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.5f), BefccBorderDark))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "QUICK ACCESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = BefccTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToSlots,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(BefccBorderSubtle, BefccBorderDark))),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BefccSurfaceCardElevated.copy(alpha = 0.4f),
                                contentColor = BefccTextPrimary
                            )
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BefccNeonLime, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("My Slots", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(BefccBorderSubtle, BefccBorderDark))),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BefccSurfaceCardElevated.copy(alpha = 0.4f),
                                contentColor = BefccTextPrimary
                            )
                        ) {
                            Icon(Icons.Default.AccountBox, contentDescription = null, tint = BefccNeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Profile", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    if (currentUser?.isAdminOrLeader == true) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenAdminPortal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BefccGoldAccent,
                                contentColor = BefccTextDarkOnNeon
                            )
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BefccTextDarkOnNeon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPEN BEFCC ADMIN DASHBOARD", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
