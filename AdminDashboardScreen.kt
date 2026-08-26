package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: BefccViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTournaments by viewModel.allTournaments.collectAsState()
    val allSlots by viewModel.allSlots.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val officialContactSetting by viewModel.officialContactNumberSetting.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "TOURNAMENTS", "SLOT VERIFY", "MATCH RESULTS", "PLAYERS", "SETTINGS")

    val pendingSlots = remember(allSlots) { allSlots.filter { it.status == SlotStatus.PENDING } }
    val pendingMatches = remember(allMatches) { allMatches.filter { it.status == MatchStatus.PENDING_VERIFICATION } }

    var showCreateTournamentDialog by remember { mutableStateOf(false) }
    var selectedMatchForCorrection by remember { mutableStateOf<MatchEntity?>(null) }
    var selectedSlotForRejectDialog by remember { mutableStateOf<SlotEntity?>(null) }

    Scaffold(
        topBar = {
            Surface(
                color = BefccSurfaceDark.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ADMIN PORTAL",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = BefccTextPrimary
                                )
                            }
                            Text(
                                text = "BEFCC Tournament Control & Verification",
                                style = MaterialTheme.typography.labelSmall,
                                color = BefccNeonLime
                            )
                        }
                        StatusBadge("ADMIN", BefccNeonLime)
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = BefccSurfaceDark,
                        contentColor = BefccNeonLime,
                        edgePadding = 12.dp
                    ) {
                        tabs.forEachIndexed { index, tabTitle ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tabTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Bold
                                            ),
                                            color = if (selectedTab == index) BefccNeonLime else BefccTextMuted
                                        )
                                        if (tabTitle == "SLOT VERIFY" && pendingSlots.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Badge(containerColor = BefccCrimsonAccent) { Text("${pendingSlots.size}") }
                                        } else if (tabTitle == "MATCH RESULTS" && pendingMatches.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Badge(containerColor = BefccCrimsonAccent) { Text("${pendingMatches.size}") }
                                        }
                                    }
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
            when (selectedTab) {
                0 -> {
                    // --- OVERVIEW TAB ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatMetricCard(
                                    title = "Pending Slots",
                                    value = "${pendingSlots.size}",
                                    subtitle = "Require Verification",
                                    icon = Icons.Default.PendingActions,
                                    iconColor = if (pendingSlots.isNotEmpty()) BefccGoldAccent else BefccEmeraldPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricCard(
                                    title = "Pending Matches",
                                    value = "${pendingMatches.size}",
                                    subtitle = "Score Approvals",
                                    icon = Icons.Default.SportsScore,
                                    iconColor = if (pendingMatches.isNotEmpty()) BefccCrimsonAccent else BefccEmeraldPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatMetricCard(
                                    title = "Tournaments",
                                    value = "${allTournaments.size}",
                                    subtitle = "${allTournaments.count { it.status != TournamentStatus.COMPLETED }} Live",
                                    icon = Icons.Default.EmojiEvents,
                                    iconColor = BefccEmeraldLight,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricCard(
                                    title = "Total Players",
                                    value = "${allUsers.size}",
                                    subtitle = "Registered Community",
                                    icon = Icons.Default.People,
                                    iconColor = StatusAvailable,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Admin Quick Controls", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = BefccTextPrimary)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { showCreateTournamentDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("admin_create_tournament_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = BefccBackgroundDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Create New BEFCC Tournament", fontWeight = FontWeight.Bold, color = BefccBackgroundDark)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedTab = 2 },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccTextPrimary)
                                        ) {
                                            Text("Verify Slots (${pendingSlots.size})", style = MaterialTheme.typography.labelSmall)
                                        }

                                        OutlinedButton(
                                            onClick = { selectedTab = 3 },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccTextPrimary)
                                        ) {
                                            Text("Verify Scores (${pendingMatches.size})", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // --- TOURNAMENTS MANAGEMENT TAB ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showCreateTournamentDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BefccBackgroundDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Tournament", fontWeight = FontWeight.Bold, color = BefccBackgroundDark)
                            }
                        }

                        items(allTournaments, key = { it.id }) { tourn ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tourn.name, fontWeight = FontWeight.Black, color = BefccTextPrimary, style = MaterialTheme.typography.titleSmall)
                                            Text("${tourn.playerLimit} Players • Fee: ৳${tourn.entryFee.toInt()} • Prize: ${tourn.prizePool}", style = MaterialTheme.typography.labelSmall, color = BefccEmeraldPrimary)
                                        }
                                        StatusBadge(tourn.status.name, BefccGoldAccent)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons for tournament lifecycle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!tourn.groupsGenerated) {
                                            Button(
                                                onClick = { viewModel.generateGroupsAndFixtures(tourn.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Gen Groups", style = MaterialTheme.typography.labelSmall, color = BefccBackgroundDark)
                                            }
                                        }

                                        if (!tourn.knockoutBracketGenerated && tourn.groupsGenerated) {
                                            Button(
                                                onClick = { viewModel.generateKnockoutBracket(tourn.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BefccGoldAccent),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Gen Bracket", style = MaterialTheme.typography.labelSmall, color = BefccBackgroundDark)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.deleteTournament(tourn.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccCrimsonAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // --- SLOT & TRANSACTION VERIFICATION TAB ---
                    val displaySlots = remember(allSlots) {
                        allSlots.filter { it.status == SlotStatus.PENDING || it.status == SlotStatus.CONFIRMED || it.status == SlotStatus.REJECTED }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            Text(
                                text = "Player Slot & Payment Approvals (${pendingSlots.size} Pending)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = BefccTextPrimary
                            )
                        }

                        if (displaySlots.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No slot booking submissions found", color = BefccTextMuted)
                                }
                            }
                        } else {
                            items(displaySlots, key = { it.id }) { slot ->
                                val tourn = allTournaments.find { it.id == slot.tournamentId }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard),
                                    border = if (slot.status == SlotStatus.PENDING) {
                                        CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BefccGoldAccent, BefccSurfaceCardElevated)))
                                    } else null
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${slot.playerName} (Slot #${slot.slotNumber})",
                                                    fontWeight = FontWeight.Black,
                                                    color = BefccTextPrimary,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Text(
                                                    text = "${tourn?.name} • Team: ${slot.selectedTeam}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BefccEmeraldPrimary
                                                )
                                            }

                                            StatusBadge(
                                                status = slot.status.name,
                                                color = when (slot.status) {
                                                    SlotStatus.CONFIRMED -> StatusSuccess
                                                    SlotStatus.PENDING -> StatusPending
                                                    SlotStatus.REJECTED -> StatusRejected
                                                    else -> BefccTextMuted
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Payment verification info
                                        Surface(
                                            color = BefccBackgroundDark,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("Method", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                                    Text(slot.paymentMethod ?: "N/A", fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                                                }
                                                Column {
                                                    Text("TrxID", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                                    Text(slot.transactionNumber ?: "N/A", fontWeight = FontWeight.Black, color = BefccGoldAccent)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Amount", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                                    Text("৳${slot.entryFee.toInt()} BDT", fontWeight = FontWeight.Bold, color = BefccEmeraldLight)
                                                }
                                            }
                                        }

                                        if (slot.status == SlotStatus.PENDING) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { viewModel.verifySlot(slot.id, approve = true) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = BefccBackgroundDark, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Approve & Confirm", fontWeight = FontWeight.Bold, color = BefccBackgroundDark, style = MaterialTheme.typography.labelSmall)
                                                }

                                                OutlinedButton(
                                                    onClick = { selectedSlotForRejectDialog = slot },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccCrimsonAccent),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, tint = BefccCrimsonAccent, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reject Slot", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // --- MATCH RESULT VERIFICATION TAB ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            Text(
                                text = "Pending Match Result Verifications (${pendingMatches.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = BefccTextPrimary
                            )
                        }

                        if (pendingMatches.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No match results awaiting verification", color = BefccTextMuted)
                                }
                            }
                        } else {
                            items(pendingMatches, key = { it.id }) { match ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = BefccSurfaceCard),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(listOf(BefccGoldAccent, BefccSurfaceDark))
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(match.roundStage, fontWeight = FontWeight.Bold, color = BefccGoldAccent, style = MaterialTheme.typography.labelMedium)
                                            StatusBadge("REVIEW", StatusPending)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Match Score Preview
                                        Surface(
                                            color = BefccBackgroundDark,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(match.player1Name, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                                                    Text(match.player1Team, style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary)
                                                }

                                                Text(
                                                    "${match.player1Score ?: 0} - ${match.player2Score ?: 0}",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                                    color = BefccEmeraldLight,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )

                                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                                    Text(match.player2Name, fontWeight = FontWeight.Bold, color = BefccTextPrimary, textAlign = TextAlign.End)
                                                    Text(match.player2Team, style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary, textAlign = TextAlign.End)
                                                }
                                            }
                                        }

                                        if (match.matchNotes != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Notes: ${match.matchNotes}", style = MaterialTheme.typography.bodySmall, color = BefccTextMuted)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.verifyMatchResult(match.id, approve = true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = BefccBackgroundDark, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve", fontWeight = FontWeight.Bold, color = BefccBackgroundDark, style = MaterialTheme.typography.labelSmall)
                                            }

                                            OutlinedButton(
                                                onClick = { selectedMatchForCorrection = match },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccGoldAccent),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Correct Score", style = MaterialTheme.typography.labelSmall)
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.verifyMatchResult(match.id, approve = false) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccCrimsonAccent),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reject", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // --- PLAYERS MANAGEMENT TAB ---
                    val currentUser by viewModel.currentUser.collectAsState()
                    var userToManage by remember { mutableStateOf<UserEntity?>(null) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BEFCC Users & Roster (${allUsers.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = BefccTextPrimary
                                )
                                Text(
                                    text = "${allUsers.count { it.isAdminOrLeader }} Admins • ${allUsers.count { !it.isAdminOrLeader }} Players",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BefccNeonLime
                                )
                            }
                        }

                        items(allUsers, key = { it.id }) { player ->
                            val isLeader = player.isSuperAdmin
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLeader) BefccSurfaceCardElevated else BefccSurfaceCard
                                ),
                                border = if (isLeader) {
                                    CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(listOf(BefccGoldAccent, BefccBorderDark))
                                    )
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(player.fullName, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                                            if (isLeader) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = "Leader",
                                                    tint = BefccGoldAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "${player.playerId} • @${player.username} • ${player.divisionRank}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BefccTextSecondary
                                        )
                                        Text(
                                            "Team: ${player.favoriteTeam} • Record: ${player.wins}W/${player.draws}D/${player.losses}L (${player.points} Pts)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BefccNeonLime
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val (badgeTitle, badgeColor) = when (player.role) {
                                            UserRole.SUPER_ADMIN -> "LEADER" to BefccGoldAccent
                                            UserRole.ADMIN -> "ADMIN" to BefccNeonCyan
                                            UserRole.PLAYER -> "PLAYER" to BefccNeonLime
                                        }
                                        StatusBadge(status = badgeTitle, color = badgeColor)

                                        if (currentUser?.isAdminOrLeader == true && !isLeader) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (currentUser?.isSuperAdmin == true) {
                                                    if (player.role == UserRole.PLAYER) {
                                                        IconButton(
                                                            onClick = { viewModel.updateUserRole(player.id, UserRole.ADMIN) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ArrowUpward,
                                                                contentDescription = "Promote to Admin",
                                                                tint = BefccGoldAccent,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    } else if (player.role == UserRole.ADMIN) {
                                                        IconButton(
                                                            onClick = { viewModel.updateUserRole(player.id, UserRole.PLAYER) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ArrowDownward,
                                                                contentDescription = "Demote to Player",
                                                                tint = BefccCrimsonAccent,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (player.role == UserRole.PLAYER || currentUser?.isSuperAdmin == true) {
                                                    IconButton(
                                                        onClick = { viewModel.deleteUser(player.id) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.DeleteOutline,
                                                            contentDescription = "Delete User",
                                                            tint = BefccTextMuted,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // --- SETTINGS TAB ---
                    AdminSettingsContent(
                        officialContactSetting = officialContactSetting,
                        currentUser = currentUser,
                        onSaveOfficialContactNumber = { newNumber ->
                            viewModel.updateOfficialContactNumber(newNumber)
                        }
                    )
                }
            }
        }
    }

    // Create Tournament Dialog
    if (showCreateTournamentDialog) {
        CreateTournamentAdminDialog(
            onDismiss = { showCreateTournamentDialog = false },
            onCreate = { name, type, playerLimit, fee, prize, teamType, teams, rules, sDate, eDate ->
                viewModel.createTournament(
                    name, type, playerLimit, fee, prize, teamType, teams, rules, sDate, eDate,
                    onSuccess = { showCreateTournamentDialog = false }
                )
            }
        )
    }

    // Match Correction Dialog
    selectedMatchForCorrection?.let { match ->
        var p1Score by remember { mutableIntStateOf(match.player1Score ?: 0) }
        var p2Score by remember { mutableIntStateOf(match.player2Score ?: 0) }

        Dialog(onDismissRequest = { selectedMatchForCorrection = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Admin Score Correction", fontWeight = FontWeight.Black, color = BefccTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(match.player1Name, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                            OutlinedTextField(
                                value = p1Score.toString(),
                                onValueChange = { p1Score = it.toIntOrNull() ?: 0 },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(70.dp)
                            )
                        }

                        Text("VS", fontWeight = FontWeight.Bold, color = BefccGoldAccent, modifier = Modifier.padding(horizontal = 8.dp))

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(match.player2Name, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                            OutlinedTextField(
                                value = p2Score.toString(),
                                onValueChange = { p2Score = it.toIntOrNull() ?: 0 },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(70.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.verifyMatchResult(match.id, approve = true, p1Score = p1Score, p2Score = p2Score)
                            selectedMatchForCorrection = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                    ) {
                        Text("Save Corrected Score & Approve", color = BefccBackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Slot Reject Dialog
    selectedSlotForRejectDialog?.let { slot ->
        var rejectReason by remember { mutableStateOf("Transaction ID not verified or invalid.") }

        Dialog(onDismissRequest = { selectedSlotForRejectDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reject Slot #${slot.slotNumber}", fontWeight = FontWeight.Black, color = BefccCrimsonAccent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reason for Rejection:", style = MaterialTheme.typography.bodySmall, color = BefccTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.verifySlot(slot.id, approve = false, notes = rejectReason)
                            selectedSlotForRejectDialog = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BefccCrimsonAccent)
                    ) {
                        Text("Confirm Rejection", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentAdminDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, limit: Int, fee: Double, prize: String, teamType: TournamentTeamType, teams: String, rules: String, sDate: String, eDate: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("National eFootball Championship") }
    var playerLimit by remember { mutableIntStateOf(16) }
    var entryFeeText by remember { mutableStateOf("0") }
    var prizePool by remember { mutableStateOf("Trophy & Certificate") }
    var teamType by remember { mutableStateOf(TournamentTeamType.NATIONAL_TEAMS) }
    var availableTeams by remember {
        mutableStateOf("Bangladesh, Argentina, France, England, Brazil, Portugal, Spain, Germany, Netherlands, Italy, Belgium, Japan, South Korea, Croatia, Morocco, Uruguay")
    }
    var rules by remember {
        mutableStateOf("1. 10 Minutes Match Duration.\n2. Normal Form condition.\n3. Disconnect without proof results in forfeit.\n4. Top 2 from each group qualify for Knockouts.")
    }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val limitOptions = listOf(12, 16, 20, 24, 28, 32, 36, 40, 44, 48)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Create BEFCC Tournament", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = BefccTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BefccTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tournament Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Category / Type") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Player Limit (12 to 48):", style = MaterialTheme.typography.labelMedium, color = BefccTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    limitOptions.take(5).forEach { opt ->
                        FilterChip(
                            selected = playerLimit == opt,
                            onClick = { playerLimit = opt },
                            label = { Text("$opt", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BefccEmeraldPrimary,
                                selectedLabelColor = BefccBackgroundDark
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    limitOptions.drop(5).forEach { opt ->
                        FilterChip(
                            selected = playerLimit == opt,
                            onClick = { playerLimit = opt },
                            label = { Text("$opt", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BefccEmeraldPrimary,
                                selectedLabelColor = BefccBackgroundDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = entryFeeText,
                        onValueChange = { entryFeeText = it },
                        label = { Text("Fee (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prizePool,
                        onValueChange = { prizePool = it },
                        label = { Text("Prize Pool") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Team Restriction:", style = MaterialTheme.typography.labelMedium, color = BefccTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = teamType == TournamentTeamType.NATIONAL_TEAMS,
                        onClick = {
                            teamType = TournamentTeamType.NATIONAL_TEAMS
                            availableTeams = "Bangladesh, Argentina, France, England, Brazil, Portugal, Spain, Germany, Netherlands, Italy, Belgium, Japan, South Korea, Croatia, Morocco, Uruguay"
                        },
                        label = { Text("National Teams Only") }
                    )
                    FilterChip(
                        selected = teamType == TournamentTeamType.CLUB_TEAMS,
                        onClick = {
                            teamType = TournamentTeamType.CLUB_TEAMS
                            availableTeams = "Real Madrid, Manchester City, Arsenal, Barcelona, Bayern Munich, Inter Milan, PSG, Liverpool, Chelsea, Juventus, Atletico Madrid, Borussia Dortmund, AC Milan, Tottenham, Napoli, Bayer Leverkusen"
                        },
                        label = { Text("Club Teams Only") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = availableTeams,
                    onValueChange = { availableTeams = it },
                    label = { Text("Available Teams (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rules,
                    onValueChange = { rules = it },
                    label = { Text("Tournament Rules & Guidelines") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val fee = entryFeeText.toDoubleOrNull() ?: 0.0
                        onCreate(name, type, playerLimit, fee, prizePool, teamType, availableTeams, rules, startDate, endDate)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                ) {
                    Text("Publish Tournament", fontWeight = FontWeight.Bold, color = BefccBackgroundDark)
                }
            }
        }
    }
}

@Composable
fun AdminSettingsContent(
    officialContactSetting: SystemSettingEntity?,
    currentUser: UserEntity?,
    onSaveOfficialContactNumber: (String) -> Unit
) {
    var contactInput by remember(officialContactSetting) {
        mutableStateOf(officialContactSetting?.value ?: "")
    }
    var isEditing by remember { mutableStateOf(false) }

    val isConfigured = !officialContactSetting?.value.isNullOrBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 40.dp)
    ) {
        // Section Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(BefccEmeraldPrimary.copy(alpha = 0.5f), BefccBorderDark))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BefccEmeraldPrimary.copy(alpha = 0.15f))
                            .border(1.dp, BefccEmeraldPrimary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = BefccEmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SYSTEM SETTINGS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = BefccTextPrimary
                        )
                        Text(
                            text = "Configure dynamic operational parameters for BEFCC.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BefccTextSecondary
                        )
                    }
                }
            }
        }

        // Section 1: Official Contact Number (Tournament Registrations & Payments)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(BefccGoldAccent.copy(alpha = 0.4f), BefccBorderDark))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = BefccGoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Official Contact Number",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BefccTextPrimary
                            )
                        }

                        if (isConfigured) {
                            StatusBadge("CONFIGURED", BefccEmeraldPrimary)
                        } else {
                            StatusBadge("NOT CONFIGURED", BefccGoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This official merchant/support number is dynamically displayed to players when booking tournament slots and submitting fee transaction proofs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BefccTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Current Display State
                    Surface(
                        color = BefccSurfaceCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CURRENT ACTIVE NUMBER:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BefccTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isConfigured) {
                                Text(
                                    text = officialContactSetting?.value.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = BefccNeonLime
                                )
                            } else {
                                Text(
                                    text = "Official contact number has not been configured yet.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = BefccCrimsonAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit / Update Form
                    Text(
                        text = if (isConfigured) "Update Contact Number:" else "Set Official Contact Number:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = BefccTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = contactInput,
                        onValueChange = { contactInput = it },
                        placeholder = { Text("e.g. +8801712345678 or 01712-XXXXXX") },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = BefccEmeraldPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("official_contact_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BefccEmeraldPrimary,
                            unfocusedBorderColor = BefccBorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (contactInput.isNotBlank()) {
                                    onSaveOfficialContactNumber(contactInput.trim())
                                }
                            },
                            enabled = contactInput.isNotBlank() && contactInput.trim() != (officialContactSetting?.value ?: ""),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BefccEmeraldPrimary,
                                contentColor = BefccBackgroundDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_official_contact_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Official Number",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        if (isConfigured) {
                            OutlinedButton(
                                onClick = {
                                    onSaveOfficialContactNumber("")
                                    contactInput = ""
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccCrimsonAccent),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(BefccCrimsonAccent.copy(alpha = 0.5f), BefccBorderDark))
                                ),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Future Configurable System Parameters (Extensible architecture)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(BefccNeonCyan.copy(alpha = 0.3f), BefccBorderDark))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = BefccNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Admin Access & Security Information",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = BefccTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = BefccSurfaceCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Super Admin / Leader:", style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary)
                                Text("Maruf Hossain", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccGoldAccent)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Active Admins:", style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary)
                                Text("Jabir, Mahi, Jon", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccNeonCyan)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Your Logged Session:", style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary)
                                Text("${currentUser?.fullName} (${currentUser?.role?.name})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BefccEmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
