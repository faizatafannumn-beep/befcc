package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slots")
data class SlotEntity(
    @PrimaryKey val id: String, // e.g. "tourn1_slot_1"
    val tournamentId: String,
    val slotNumber: Int,
    val playerId: String? = null,
    val playerName: String? = null,
    val playerUsername: String? = null,
    val selectedTeam: String? = null,
    val teamType: String? = null,
    val entryFee: Double = 0.0,
    val paymentMethod: String? = null, // "bKash", "Nagad", "Rocket", "Bank"
    val transactionNumber: String? = null,
    val submissionTime: Long? = null,
    val status: SlotStatus = SlotStatus.AVAILABLE,
    val adminNotes: String? = null,
    val assignedGroup: String? = null // e.g. "Group A"
)
