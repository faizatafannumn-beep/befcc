package com.example.ui.tournaments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    viewModel: BefccViewModel,
    onBack: () -> Unit,
    onSubmitMatchScore: (MatchEntity) -> Unit,
    onAdminVerifyMatch: (MatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTournaments by viewModel.allTournaments.collectAsState()
    val tournament = remember(allTournaments, tournamentId) {
        allTournaments.find { it.id == tournamentId }
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.isAdminOrLeader == true

    val slotsFlow = remember(tournamentId) { viewModel.getSlotsForTournament(tournamentId) }
    val slots by slotsFlow.collectAsState(initial = emptyList())

    val standingsFlow = remember(tournamentId) { viewModel.getStandingsForTournament(tournamentId) }
    val standings by standingsFlow.collectAsState(initial = emptyList())

    val matchesFlow = remember(tournamentId) { viewModel.getMatchesForTournament(tournamentId) }
    val matches by matchesFlow.collectAsState(initial = emptyList())
    val officialContactSetting by viewModel.officialContactNumberSetting.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("SLOTS & TEAMS", "GROUP STAGE", "KNOCKOUT BRACKET", "RULES & INFO")

    // Booking Dialog state
    var selectedSlotForBooking by remember { mutableStateOf<SlotEntity?>(null) }

    if (tournament == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BefccEmeraldPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            Surface(
                color = BefccSurfaceDark,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BefccTextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tournament.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = BefccTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tournament.type} • Prize: ${tournament.prizePool}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BefccEmeraldPrimary
                            )
                        }
                        StatusBadge(
                            status = tournament.status.name,
                            color = when (tournament.status) {
                                TournamentStatus.REGISTRATION_OPEN -> StatusAvailable
                                TournamentStatus.GROUP_STAGE -> BefccEmeraldPrimary
                                TournamentStatus.KNOCKOUT_STAGE -> BefccGoldAccent
                                TournamentStatus.COMPLETED -> StatusSuccess
                                else -> BefccTextMuted
                            }
                        )
                    }

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = BefccSurfaceDark,
                        contentColor = BefccEmeraldPrimary
                    ) {
                        tabs.forEachIndexed { index, tabTitle ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = tabTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (selectedTabIndex == index) BefccEmeraldLight else BefccTextSecondary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = BefccBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Admin Action Banner
            if (isAdmin) {
                Surface(
                    color = BefccSurfaceCardElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BefccGoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin Controls", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccGoldAccent)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!tournament.groupsGenerated) {
                                OutlinedButton(
                                    onClick = { viewModel.generateGroupsAndFixtures(tournament.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccEmeraldLight)
                                ) {
                                    Text("Gen Groups", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            if (!tournament.knockoutBracketGenerated && tournament.groupsGenerated) {
                                OutlinedButton(
                                    onClick = { viewModel.generateKnockoutBracket(tournament.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccGoldAccent)
                                ) {
                                    Text("Gen Knockouts", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // --- TAB 1: SLOTS & TEAMS ---
                    val confirmedSlotsCount = slots.count { it.status == SlotStatus.CONFIRMED }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Slots Registration", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                                        Text("Entry Fee: ৳${tournament.entryFee.toInt()} BDT", style = MaterialTheme.typography.labelSmall, color = BefccEmeraldPrimary)
                                    }
                                    Text(
                                        "$confirmedSlotsCount / ${tournament.playerLimit} Booked",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BefccGoldAccent
                                    )
                                }
                            }
                        }

                        items(slots, key = { it.id }) { slot ->
                            SlotCard(
                                slot = slot,
                                onClick = {
                                    if (slot.status == SlotStatus.AVAILABLE) {
                                        selectedSlotForBooking = slot
                                    }
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // --- TAB 2: GROUP STAGE ---
                    val groupMatches = matches.filter { it.matchType == MatchType.TOURNAMENT_GROUP }
                    val standingsByGroup = standings.groupBy { it.groupName }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        if (!tournament.groupsGenerated) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Outlined.GroupWork, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("Groups not yet generated", style = MaterialTheme.typography.titleSmall, color = BefccTextSecondary)
                                        Text("Admin will generate groups once slots are filled.", style = MaterialTheme.typography.bodySmall, color = BefccTextMuted)
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    "Group Standings",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = BefccTextPrimary
                                )
                            }

                            standingsByGroup.forEach { (groupName, list) ->
                                item(key = groupName) {
                                    GroupTableView(groupName = groupName, standings = list)
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Group Match Fixtures",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = BefccTextPrimary
                                )
                            }

                            items(groupMatches, key = { it.id }) { match ->
                                MatchCard(
                                    match = match,
                                    currentUserId = currentUser?.id,
                                    onSubmitScoreClick = { onSubmitMatchScore(match) },
                                    onAdminVerifyClick = { onAdminVerifyMatch(match) },
                                    isAdmin = isAdmin
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // --- TAB 3: KNOCKOUT BRACKET ---
                    val knockoutMatches = matches.filter { it.isKnockout }

                    if (!tournament.knockoutBracketGenerated) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Knockout Bracket not yet generated", style = MaterialTheme.typography.titleSmall, color = BefccTextSecondary)
                                Text("Top 2 teams from each group will advance to Quarter Finals & Semi Finals.", style = MaterialTheme.typography.bodySmall, color = BefccTextMuted, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        KnockoutBracketView(
                            matches = knockoutMatches,
                            currentUserId = currentUser?.id,
                            onSubmitScore = { onSubmitMatchScore(it) },
                            onVerifyScore = { onAdminVerifyMatch(it) },
                            isAdmin = isAdmin
                        )
                    }
                }

                3 -> {
                    // --- TAB 4: RULES & INFO ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Tournament Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = BefccTextPrimary)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Format:", color = BefccTextSecondary)
                                    Text("${tournament.groupsCount} Groups of ${tournament.groupSize}", fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Team Restriction:", color = BefccTextSecondary)
                                    Text(if (tournament.teamType == TournamentTeamType.NATIONAL_TEAMS) "National Teams Only" else "Club Teams Only", fontWeight = FontWeight.Bold, color = BefccEmeraldLight)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Schedule:", color = BefccTextSecondary)
                                    Text("${tournament.startDate} to ${tournament.endDate}", fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Prize Pool:", color = BefccTextSecondary)
                                    Text(tournament.prizePool, fontWeight = FontWeight.Bold, color = BefccGoldAccent)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Official BEFCC Rules & Fair Play", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = BefccTextPrimary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = tournament.rules,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BefccTextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Available Teams List", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = BefccTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = tournament.availableTeams,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BefccEmeraldLight,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Booking Dialog Trigger
    selectedSlotForBooking?.let { slot ->
        SlotBookingDialog(
            tournament = tournament,
            slot = slot,
            existingSlots = slots,
            officialContactNumber = officialContactSetting?.value,
            onDismiss = { selectedSlotForBooking = null },
            onConfirm = { team, teamType, paymentMethod, trxId ->
                viewModel.bookTournamentSlot(
                    tournamentId = tournament.id,
                    slotNumber = slot.slotNumber,
                    selectedTeam = team,
                    teamType = teamType,
                    entryFee = tournament.entryFee,
                    paymentMethod = paymentMethod,
                    transactionNumber = trxId,
                    onSuccess = { selectedSlotForBooking = null }
                )
            }
        )
    }
}
