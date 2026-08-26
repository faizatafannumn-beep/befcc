package com.example.ui.slots

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SlotEntity
import com.example.data.model.SlotStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@Composable
fun MySlotsScreen(
    viewModel: BefccViewModel,
    modifier: Modifier = Modifier
) {
    val mySlotsFlow = remember { viewModel.getMySlots() }
    val mySlots by mySlotsFlow.collectAsState(initial = emptyList())
    val allTournaments by viewModel.allTournaments.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

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
                Column {
                    Text(
                        text = "MY TOURNAMENT SLOTS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = BefccTextPrimary
                    )
                    Text(
                        text = "Track your slot bookings, team selection & payment status",
                        style = MaterialTheme.typography.labelSmall,
                        color = BefccNeonLime
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mySlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tournament slots booked yet", color = BefccTextMuted, style = MaterialTheme.typography.bodyMedium)
                    Text("Browse Tournaments tab to reserve your slot!", color = BefccNeonLime, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mySlots, key = { it.id }) { slot ->
                    val tourn = allTournaments.find { it.id == slot.tournamentId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("my_slot_card_${slot.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.7f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    when (slot.status) {
                                        SlotStatus.CONFIRMED -> BefccNeonLime.copy(alpha = 0.7f)
                                        SlotStatus.PENDING -> BefccGoldAccent.copy(alpha = 0.7f)
                                        SlotStatus.REJECTED -> BefccCrimsonAccent.copy(alpha = 0.7f)
                                        else -> BefccBorderSubtle
                                    },
                                    BefccBorderDark
                                )
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tourn?.name ?: "Tournament Slot",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                        color = BefccTextPrimary
                                    )
                                    Text(
                                        text = "Slot #${slot.slotNumber} • Team: ${slot.selectedTeam ?: "Not Selected"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BefccNeonLime
                                    )
                                }

                                StatusBadge(
                                    status = slot.status.name,
                                    color = when (slot.status) {
                                        SlotStatus.CONFIRMED -> BefccNeonLime
                                        SlotStatus.PENDING -> BefccGoldAccent
                                        SlotStatus.REJECTED -> BefccCrimsonAccent
                                        else -> BefccNeonCyan
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Divider(color = BefccBorderDark)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Payment Method", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                    Text(slot.paymentMethod ?: "N/A", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = BefccTextPrimary)
                                }

                                Column {
                                    Text("Transaction TrxID", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                    Text(slot.transactionNumber ?: "N/A", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = BefccGoldAccent)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Entry Fee", style = MaterialTheme.typography.labelSmall, color = BefccTextMuted)
                                    Text("৳${slot.entryFee.toInt()} BDT", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = BefccEmeraldLight)
                                }
                            }

                            if (slot.adminNotes != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = BefccSurfaceDark,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = BefccTextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = slot.adminNotes,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BefccTextSecondary
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
