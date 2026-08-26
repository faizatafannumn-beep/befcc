package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun GroupTableView(
    groupName: String,
    standings: List<StandingEntity>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.7f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.6f), BefccBorderDark))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BefccNeonLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = groupName.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = BefccTextPrimary
                    )
                }
                Text(
                    text = "TOP 2 ADVANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 9.sp
                    ),
                    color = BefccNeonLime
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Header
            Surface(
                color = BefccSurfaceCardElevated.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, modifier = Modifier.width(22.dp))
                    Text("PLAYER / TEAM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, modifier = Modifier.weight(1f))
                    Text("P", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                    Text("W", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                    Text("D", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                    Text("L", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                    Text("GD", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(32.dp))
                    Text("PTS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = BefccNeonLime, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (standings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No teams assigned yet", style = MaterialTheme.typography.bodySmall, color = BefccTextMuted)
                }
            } else {
                standings.sortedWith(
                    compareByDescending<StandingEntity> { it.points }
                        .thenByDescending { it.goalDifference }
                        .thenByDescending { it.goalsFor }
                ).forEachIndexed { index, s ->
                    val isTop2 = index < 2
                    Surface(
                        color = if (isTop2) BefccNeonLime.copy(alpha = 0.05f) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isTop2) BefccNeonLime else BefccTextSecondary,
                                modifier = Modifier.width(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.playerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BefccTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = s.playerTeam,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BefccTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text("${s.played}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                            Text("${s.won}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                            Text("${s.drawn}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                            Text("${s.lost}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                            Text("${if (s.goalDifference > 0) "+${s.goalDifference}" else s.goalDifference}", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(32.dp))
                            Text(
                                text = "${s.points}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = if (isTop2) BefccNeonLime else BefccTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                    if (index == 1 && standings.size > 2) {
                        Divider(
                            color = BefccNeonLime.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: MatchEntity,
    currentUserId: String? = null,
    onSubmitScoreClick: () -> Unit = {},
    onAdminVerifyClick: () -> Unit = {},
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isMyMatch = currentUserId != null && (match.player1Id == currentUserId || match.player2Id == currentUserId)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMyMatch) BefccSurfaceGlass else BefccSurfaceGlass.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isMyMatch) {
                Brush.linearGradient(listOf(BefccNeonLime.copy(alpha = 0.6f), BefccNeonCyan.copy(alpha = 0.3f)))
            } else {
                Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.5f), BefccBorderDark))
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Stage & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (match.isKnockout) Icons.Default.EmojiEvents else Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = if (match.isKnockout) BefccGoldAccent else BefccNeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.roundStage.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 10.sp
                        ),
                        color = BefccTextSecondary
                    )
                }

                when (match.status) {
                    MatchStatus.VERIFIED -> StatusBadge("VERIFIED", BefccNeonLime)
                    MatchStatus.PENDING_VERIFICATION -> StatusBadge("REVIEW", StatusPending)
                    MatchStatus.SCHEDULED -> StatusBadge("SCHEDULED", BefccNeonCyan)
                    else -> StatusBadge(match.status.name, BefccTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scoreboard Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Player 1
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = match.player1Name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (match.winnerId == match.player1Id) BefccNeonLime else BefccTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = match.player1Team,
                        style = MaterialTheme.typography.labelSmall,
                        color = BefccTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Center Score Box
                Surface(
                    color = BefccSurfaceCardElevated.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.6f), BefccBorderDark))
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (match.player1Score != null && match.player2Score != null) {
                            Text(
                                text = "${match.player1Score}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = if (match.player1Score > match.player2Score) BefccNeonLime else BefccTextPrimary
                            )
                            Text(
                                text = " - ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = BefccTextMuted
                            )
                            Text(
                                text = "${match.player2Score}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = if (match.player2Score > match.player1Score) BefccNeonLime else BefccTextPrimary
                            )
                            if (match.player1Penalties != null && match.player2Penalties != null) {
                                Text(
                                    text = " (${match.player1Penalties}-${match.player2Penalties}p)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BefccGoldAccent
                                )
                            }
                        } else {
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = BefccTextMuted
                            )
                        }
                    }
                }

                // Player 2
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.player2Name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (match.winnerId == match.player2Id) BefccNeonLime else BefccTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = match.player2Team,
                        style = MaterialTheme.typography.labelSmall,
                        color = BefccTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = BefccTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.scheduledTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = BefccTextMuted
                    )
                }

                Row {
                    if (isMyMatch && match.status != MatchStatus.VERIFIED && match.status != MatchStatus.PENDING_VERIFICATION) {
                        Button(
                            onClick = onSubmitScoreClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BefccNeonLime,
                                contentColor = BefccTextDarkOnNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("submit_score_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp), tint = BefccTextDarkOnNeon)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Submit Score", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                        }
                    }

                    if (isAdmin && match.status == MatchStatus.PENDING_VERIFICATION) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAdminVerifyClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BefccGoldAccent,
                                contentColor = BefccTextDarkOnNeon
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("admin_verify_score_button")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = BefccTextDarkOnNeon)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verify Score", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KnockoutBracketView(
    matches: List<MatchEntity>,
    currentUserId: String? = null,
    onSubmitScore: (MatchEntity) -> Unit = {},
    onVerifyScore: (MatchEntity) -> Unit = {},
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val qfMatches = matches.filter { it.bracketNodeId?.startsWith("QF") == true }
    val sfMatches = matches.filter { it.bracketNodeId?.startsWith("SF") == true }
    val finalMatch = matches.find { it.bracketNodeId == "FINAL" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Quarter Finals Column
            if (qfMatches.isNotEmpty()) {
                Column(modifier = Modifier.width(260.dp)) {
                    Text(
                        text = "QUARTER FINALS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = BefccGoldAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    qfMatches.forEach { match ->
                        MatchCard(
                            match = match,
                            currentUserId = currentUserId,
                            onSubmitScoreClick = { onSubmitScore(match) },
                            onAdminVerifyClick = { onVerifyScore(match) },
                            isAdmin = isAdmin,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }

            // Semi Finals Column
            if (sfMatches.isNotEmpty()) {
                Column(modifier = Modifier.width(260.dp)) {
                    Text(
                        text = "SEMI FINALS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = BefccGoldAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    sfMatches.forEach { match ->
                        MatchCard(
                            match = match,
                            currentUserId = currentUserId,
                            onSubmitScoreClick = { onSubmitScore(match) },
                            onAdminVerifyClick = { onVerifyScore(match) },
                            isAdmin = isAdmin,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }

            // Grand Final Column
            if (finalMatch != null) {
                Column(modifier = Modifier.width(280.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BefccGoldAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GRAND FINAL",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = BefccGoldAccent
                        )
                    }
                    MatchCard(
                        match = finalMatch,
                        currentUserId = currentUserId,
                        onSubmitScoreClick = { onSubmitScore(finalMatch) },
                        onAdminVerifyClick = { onVerifyScore(finalMatch) },
                        isAdmin = isAdmin
                    )
                }
            }
        }
    }
}

@Composable
fun SlotCard(
    slot: SlotEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (slot.status) {
        SlotStatus.AVAILABLE -> StatusAvailable
        SlotStatus.PENDING -> StatusPending
        SlotStatus.CONFIRMED -> StatusSuccess
        SlotStatus.REJECTED -> StatusRejected
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = slot.status == SlotStatus.AVAILABLE) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (slot.status) {
                SlotStatus.AVAILABLE -> BefccSurfaceCard
                SlotStatus.PENDING -> BefccSurfaceCardElevated
                SlotStatus.CONFIRMED -> BefccSurfaceDark
                SlotStatus.REJECTED -> BefccSurfaceDark
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(statusColor.copy(alpha = 0.5f), statusColor))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${slot.slotNumber}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = statusColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (slot.status == SlotStatus.AVAILABLE) "Slot Available" else (slot.playerName ?: "Player Slot"),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (slot.status == SlotStatus.AVAILABLE) BefccEmeraldLight else BefccTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (slot.status == SlotStatus.AVAILABLE) "Tap to book slot" else (slot.selectedTeam ?: "No Team"),
                        style = MaterialTheme.typography.labelSmall,
                        color = BefccTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            StatusBadge(slot.status.name, statusColor)
        }
    }
}
