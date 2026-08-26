package com.example.ui.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.ui.components.Create1v1Dialog
import com.example.ui.components.MatchCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun MatchesScreen(
    viewModel: BefccViewModel,
    onSubmitScore: (MatchEntity) -> Unit,
    onAdminVerifyScore: (MatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isAdmin = currentUser?.isAdminOrLeader == true

    var selectedTab by remember { mutableStateOf("MY MATCHES") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var showCreate1v1Dialog by remember { mutableStateOf(false) }

    val tabs = listOf("MY MATCHES", "ALL FIXTURES", "1v1 MATCHES")
    val statusFilters = listOf("ALL", "SCHEDULED", "PENDING REVIEW", "VERIFIED")

    val filteredMatches = remember(allMatches, currentUser, selectedTab, selectedStatusFilter) {
        allMatches.filter { match ->
            val matchesTab = when (selectedTab) {
                "MY MATCHES" -> currentUser?.id != null && (match.player1Id == currentUser?.id || match.player2Id == currentUser?.id)
                "1v1 MATCHES" -> match.matchType == MatchType.ONE_VS_ONE
                else -> true
            }
            val matchesStatus = when (selectedStatusFilter) {
                "SCHEDULED" -> match.status == MatchStatus.SCHEDULED
                "PENDING REVIEW" -> match.status == MatchStatus.PENDING_VERIFICATION
                "VERIFIED" -> match.status == MatchStatus.VERIFIED
                else -> true
            }
            matchesTab && matchesStatus
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = { Text(tab, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BefccNeonLime,
                            selectedLabelColor = BefccTextDarkOnNeon,
                            containerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                            labelColor = BefccTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) BefccNeonLime else BefccBorderDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-status Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters) { filter ->
                    val isSelected = selectedStatusFilter == filter
                    SuggestionChip(
                        onClick = { selectedStatusFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) BefccSurfaceCardElevated else BefccSurfaceGlass.copy(alpha = 0.4f),
                            labelColor = if (isSelected) BefccNeonCyan else BefccTextMuted
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isSelected) BefccNeonCyan.copy(alpha = 0.5f) else BefccBorderDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredMatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SportsSoccer, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matches found in this category", color = BefccTextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMatches, key = { it.id }) { match ->
                        MatchCard(
                            match = match,
                            currentUserId = currentUser?.id,
                            onSubmitScoreClick = { onSubmitScore(match) },
                            onAdminVerifyClick = { onAdminVerifyScore(match) },
                            isAdmin = isAdmin,
                            modifier = Modifier.testTag("match_item_${match.id}")
                        )
                    }
                }
            }
        }

        // Floating Action Button to create 1v1 match
        FloatingActionButton(
            onClick = { showCreate1v1Dialog = true },
            containerColor = BefccNeonLime,
            contentColor = BefccTextDarkOnNeon,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("create_1v1_fab")
        ) {
            Icon(Icons.Default.SportsEsports, contentDescription = "Create 1v1 Match")
        }
    }

    if (showCreate1v1Dialog && currentUser != null) {
        Create1v1Dialog(
            allUsers = allUsers,
            currentUserId = currentUser!!.id,
            onDismiss = { showCreate1v1Dialog = false },
            onSubmit = { opponentId, opponentName, userTeam, opponentTeam ->
                viewModel.create1v1Challenge(
                    opponentId = opponentId,
                    opponentName = opponentName,
                    userTeam = userTeam,
                    opponentTeam = opponentTeam,
                    onSuccess = { showCreate1v1Dialog = false }
                )
            }
        )
    }
}
