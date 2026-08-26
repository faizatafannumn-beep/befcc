package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotBookingDialog(
    tournament: TournamentEntity,
    slot: SlotEntity,
    existingSlots: List<SlotEntity>,
    officialContactNumber: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (team: String, teamType: String, paymentMethod: String, trxId: String) -> Unit
) {
    val availableTeamsList = remember(tournament.availableTeams) {
        tournament.availableTeams.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    // Teams already taken by other players in this tournament
    val takenTeams = remember(existingSlots) {
        existingSlots.filter { it.status == SlotStatus.CONFIRMED || it.status == SlotStatus.PENDING }
            .mapNotNull { it.selectedTeam?.lowercase() }
    }

    var selectedTeam by remember {
        mutableStateOf(availableTeamsList.firstOrNull { it.lowercase() !in takenTeams } ?: "")
    }
    var selectedPaymentMethod by remember { mutableStateOf("bKash") }
    var transactionNumber by remember { mutableStateOf("") }
    var expandedTeamDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccEmeraldPrimary.copy(alpha = 0.6f), BefccEmeraldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Book Slot #${slot.slotNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = BefccTextPrimary
                        )
                        Text(
                            text = tournament.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = BefccEmeraldPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BefccTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Entry Fee banner
                Surface(
                    color = BefccSurfaceCard,
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
                        Text("Entry Fee:", style = MaterialTheme.typography.bodyMedium, color = BefccTextSecondary)
                        Text(
                            "৳${tournament.entryFee.toInt()} BDT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BefccGoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Select Team
                Text("1. Select Available Team", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedTeamDropdown,
                    onExpandedChange = { expandedTeamDropdown = !expandedTeamDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedTeam,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTeamDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("team_selector"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BefccEmeraldPrimary,
                            unfocusedBorderColor = BefccBorderDark
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTeamDropdown,
                        onDismissRequest = { expandedTeamDropdown = false },
                        modifier = Modifier.background(BefccSurfaceDark)
                    ) {
                        availableTeamsList.forEach { team ->
                            val isTaken = team.lowercase() in takenTeams
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            team,
                                            color = if (isTaken) BefccTextMuted else BefccTextPrimary,
                                            fontWeight = if (team == selectedTeam) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isTaken) {
                                            Text("Already Taken", color = StatusRejected, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                },
                                onClick = {
                                    if (!isTaken) {
                                        selectedTeam = team
                                        expandedTeamDropdown = false
                                    }
                                },
                                enabled = !isTaken
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Select Payment Method
                Text("2. Payment Method", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("bKash", "Nagad", "Rocket", "Bank").forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPaymentMethod = method },
                            color = if (isSelected) BefccEmeraldPrimary else BefccSurfaceCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(
                                        if (isSelected) BefccEmeraldLight else BefccBorderDark,
                                        if (isSelected) BefccEmeraldPrimary else BefccSurfaceCard
                                    )
                                )
                            )
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) BefccBackgroundDark else BefccTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BEFCC Official Merchant Number Display
                Surface(
                    color = BefccSurfaceCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "BEFCC Official $selectedPaymentMethod Merchant/Personal:",
                            style = MaterialTheme.typography.labelSmall,
                            color = BefccTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (!officialContactNumber.isNullOrBlank()) {
                            Text(
                                text = "$officialContactNumber (Reference: Slot #${slot.slotNumber})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BefccGoldAccent
                            )
                        } else {
                            Text(
                                text = "Official contact number has not been configured yet.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = BefccTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Transaction ID Input
                Text("3. Transaction Number (TrxID)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = transactionNumber,
                    onValueChange = { transactionNumber = it.uppercase() },
                    placeholder = { Text("e.g. TRX992140BK") },
                    leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BefccEmeraldPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trx_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BefccEmeraldPrimary,
                        unfocusedBorderColor = BefccBorderDark
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val teamType = if (tournament.teamType == TournamentTeamType.NATIONAL_TEAMS) "National Team" else "Club Team"
                        onConfirm(selectedTeam, teamType, selectedPaymentMethod, transactionNumber)
                    },
                    enabled = selectedTeam.isNotBlank() && transactionNumber.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_slot_booking_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = BefccBackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Submit for Verification",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BefccBackgroundDark
                    )
                }
            }
        }
    }
}

@Composable
fun SubmitResultDialog(
    match: MatchEntity,
    onDismiss: () -> Unit,
    onSubmit: (p1Score: Int, p2Score: Int, p1Pens: Int?, p2Pens: Int?, notes: String?) -> Unit
) {
    var p1ScoreText by remember { mutableStateOf(match.player1Score?.toString() ?: "0") }
    var p2ScoreText by remember { mutableStateOf(match.player2Score?.toString() ?: "0") }
    var isPensNeeded by remember { mutableStateOf(match.isKnockout) }
    var p1PensText by remember { mutableStateOf(match.player1Penalties?.toString() ?: "") }
    var p2PensText by remember { mutableStateOf(match.player2Penalties?.toString() ?: "") }
    var matchNotes by remember { mutableStateOf(match.matchNotes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccEmeraldPrimary.copy(alpha = 0.6f), BefccEmeraldDark))
            )
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
                    Column {
                        Text(
                            text = "Submit Match Result",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = BefccTextPrimary
                        )
                        Text(
                            text = match.roundStage,
                            style = MaterialTheme.typography.labelSmall,
                            color = BefccEmeraldPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BefccTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fixture Banner
                Surface(
                    color = BefccSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                            Text(match.player1Name, fontWeight = FontWeight.Bold, color = BefccTextPrimary, maxLines = 1)
                            Text(match.player1Team, style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary)
                        }

                        Text("VS", fontWeight = FontWeight.Black, color = BefccGoldAccent, modifier = Modifier.padding(horizontal = 8.dp))

                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                            Text(match.player2Name, fontWeight = FontWeight.Bold, color = BefccTextPrimary, textAlign = TextAlign.End, maxLines = 1)
                            Text(match.player2Team, style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary, textAlign = TextAlign.End)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Inputs
                Text("Final Score (Full Time / ET):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = p1ScoreText,
                        onValueChange = { p1ScoreText = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("p1_score_input"),
                        textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BefccEmeraldPrimary,
                            unfocusedBorderColor = BefccBorderDark
                        )
                    )

                    Text(" - ", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = BefccTextMuted, modifier = Modifier.padding(horizontal = 12.dp))

                    OutlinedTextField(
                        value = p2ScoreText,
                        onValueChange = { p2ScoreText = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("p2_score_input"),
                        textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BefccEmeraldPrimary,
                            unfocusedBorderColor = BefccBorderDark
                        )
                    )
                }

                // Penalties Section (if knockout or draw in knockout)
                if (match.isKnockout) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Penalty Shootout (Optional if decided on pens):", style = MaterialTheme.typography.labelMedium, color = BefccGoldAccent)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = p1PensText,
                            onValueChange = { p1PensText = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("Pens") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccGoldAccent, unfocusedBorderColor = BefccBorderDark)
                        )

                        Text(" (P) ", color = BefccTextMuted, modifier = Modifier.padding(horizontal = 12.dp))

                        OutlinedTextField(
                            value = p2PensText,
                            onValueChange = { p2PensText = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("Pens") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccGoldAccent, unfocusedBorderColor = BefccBorderDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes / Proof reference
                Text("Match Notes / Screenshot Reference:", style = MaterialTheme.typography.labelMedium, color = BefccTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = matchNotes,
                    onValueChange = { matchNotes = it },
                    placeholder = { Text("e.g. Screenshot posted in Discord or match played on 20:30") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BefccEmeraldPrimary,
                        unfocusedBorderColor = BefccBorderDark
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val p1 = p1ScoreText.toIntOrNull() ?: 0
                        val p2 = p2ScoreText.toIntOrNull() ?: 0
                        val pen1 = p1PensText.toIntOrNull()
                        val pen2 = p2PensText.toIntOrNull()
                        onSubmit(p1, p2, pen1, pen2, matchNotes.ifBlank { null })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_match_score_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = BefccBackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Submit for Admin Verification",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BefccBackgroundDark
                    )
                }
            }
        }
    }
}

@Composable
fun Create1v1Dialog(
    allUsers: List<UserEntity>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmit: (opponentId: String, opponentName: String, userTeam: String, opponentTeam: String) -> Unit
) {
    val eligibleOpponents = remember(allUsers) {
        allUsers.filter { it.id != currentUserId }
    }

    var selectedOpponent by remember { mutableStateOf(eligibleOpponents.firstOrNull()) }
    var userTeam by remember { mutableStateOf("Bangladesh") }
    var opponentTeam by remember { mutableStateOf("Real Madrid") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccEmeraldPrimary.copy(alpha = 0.6f), BefccEmeraldDark))
            )
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
                    Text(
                        text = "Create 1v1 Match Challenge",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = BefccTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BefccTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Opponent:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                eligibleOpponents.forEach { opponent ->
                    val isSelected = selectedOpponent?.id == opponent.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedOpponent = opponent },
                        color = if (isSelected) BefccEmeraldPrimary.copy(alpha = 0.15f) else BefccSurfaceCard,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BefccEmeraldPrimary, BefccEmeraldDark))) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(opponent.fullName, fontWeight = FontWeight.Bold, color = BefccTextPrimary)
                                Text("${opponent.playerId} • ${opponent.divisionRank}", style = MaterialTheme.typography.labelSmall, color = BefccTextSecondary)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BefccEmeraldPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Your Team:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = userTeam,
                    onValueChange = { userTeam = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Opponent Team:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = opponentTeam,
                    onValueChange = { opponentTeam = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        selectedOpponent?.let {
                            onSubmit(it.id, it.fullName, userTeam, opponentTeam)
                        }
                    },
                    enabled = selectedOpponent != null && userTeam.isNotBlank() && opponentTeam.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("create_1v1_confirm_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = BefccBackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Issue 1v1 Challenge", fontWeight = FontWeight.Bold, color = BefccBackgroundDark)
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSave: (fullName: String, inGameName: String, favoriteTeam: String, division: String, teams: String) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser.fullName) }
    var inGameName by remember { mutableStateOf(currentUser.inGameUsername) }
    var favoriteTeam by remember { mutableStateOf(currentUser.favoriteTeam) }
    var division by remember { mutableStateOf(currentUser.divisionRank) }
    var teams by remember { mutableStateOf(currentUser.selectedTeams) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BefccSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(BefccEmeraldPrimary.copy(alpha = 0.6f), BefccEmeraldDark))
            )
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
                    Text("Edit Player Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = BefccTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BefccTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inGameName,
                    onValueChange = { inGameName = it },
                    label = { Text("eFootball In-Game ID / Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = favoriteTeam,
                    onValueChange = { favoriteTeam = it },
                    label = { Text("Favorite Team") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = division,
                    onValueChange = { division = it },
                    label = { Text("Division Rank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = teams,
                    onValueChange = { teams = it },
                    label = { Text("Selected Teams (Comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BefccEmeraldPrimary, unfocusedBorderColor = BefccBorderDark)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onSave(fullName, inGameName, favoriteTeam, division, teams) },
                    enabled = fullName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BefccEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = BefccBackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = BefccBackgroundDark)
                }
            }
        }
    }
}
