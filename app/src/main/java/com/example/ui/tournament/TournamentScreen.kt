package com.example.ui.tournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BefccLogo
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun TournamentsScreen(
    viewModel: BefccViewModel,
    onTournamentClick: (TournamentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val tournaments by viewModel.allTournaments.collectAsState()
    val allSlots by viewModel.allSlots.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "OPEN", "GROUP STAGE", "KNOCKOUT", "COMPLETED")

    val filteredTournaments = remember(tournaments, searchQuery, selectedFilter) {
        tournaments.filter { t ->
            val matchesSearch = t.name.contains(searchQuery, ignoreCase = true) ||
                    t.type.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "OPEN" -> t.status == TournamentStatus.REGISTRATION_OPEN
                "GROUP STAGE" -> t.status == TournamentStatus.GROUP_STAGE
                "KNOCKOUT" -> t.status == TournamentStatus.KNOCKOUT_STAGE
                "COMPLETED" -> t.status == TournamentStatus.COMPLETED
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search BEFCC Tournaments...", color = BefccTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BefccNeonLime) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BefccTextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tournament_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BefccNeonLime,
                unfocusedBorderColor = BefccBorderDark,
                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f),
                focusedTextColor = BefccTextPrimary,
                unfocusedTextColor = BefccTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Tournament List
        if (filteredTournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tournaments found", color = BefccTextMuted)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTournaments, key = { it.id }) { tournament ->
                    val tournSlots = allSlots.filter { it.tournamentId == tournament.id }
                    val confirmedCount = tournSlots.count { it.status == SlotStatus.CONFIRMED }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTournamentClick(tournament) }
                            .testTag("tournament_item_${tournament.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.75f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(BefccBorderSubtle.copy(alpha = 0.6f), BefccBorderDark))
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BefccSurfaceCardElevated)
                                            .border(1.dp, BefccBorderSubtle, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = if (tournament.status == TournamentStatus.COMPLETED) BefccGoldAccent else BefccNeonLime,
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

                                StatusBadge(
                                    status = when (tournament.status) {
                                        TournamentStatus.REGISTRATION_OPEN -> "OPEN"
                                        TournamentStatus.GROUP_STAGE -> "GROUPS"
                                        TournamentStatus.KNOCKOUT_STAGE -> "KNOCKOUT"
                                        TournamentStatus.COMPLETED -> "COMPLETED"
                                        else -> "CLOSED"
                                    },
                                    color = when (tournament.status) {
                                        TournamentStatus.REGISTRATION_OPEN -> BefccNeonLime
                                        TournamentStatus.GROUP_STAGE -> BefccNeonCyan
                                        TournamentStatus.KNOCKOUT_STAGE -> BefccGoldAccent
                                        TournamentStatus.COMPLETED -> StatusSuccess
                                        else -> BefccTextMuted
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ENTRY FEE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = BefccTextMuted)
                                    Text("৳${tournament.entryFee.toInt()} BDT", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                                }

                                Column {
                                    Text("PRIZE POOL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = BefccTextMuted)
                                    Text(tournament.prizePool, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = BefccGoldAccent)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("SLOTS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = BefccTextMuted)
                                    Text("$confirmedCount / ${tournament.playerLimit}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = BefccNeonCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { if (tournament.playerLimit > 0) confirmedCount.toFloat() / tournament.playerLimit.toFloat() else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = BefccNeonLime,
                                trackColor = BefccSurfaceCardElevated
                            )

                            if (tournament.status == TournamentStatus.COMPLETED && tournament.championName != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BefccGoldAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Champion: ${tournament.championName}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccGoldAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
