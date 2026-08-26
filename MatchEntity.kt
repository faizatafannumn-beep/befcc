package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val tournamentId: String? = null,
    val tournamentName: String = "Independent 1v1",
    val matchType: MatchType = MatchType.ONE_VS_ONE,
    val roundStage: String = "Matchday 1",
    val groupName: String? = null, // "Group A", "Group B", etc.
    val player1Id: String,
    val player1Name: String,
    val player1Team: String,
    val player2Id: String,
    val player2Name: String,
    val player2Team: String,
    val player1Score: Int? = null,
    val player2Score: Int? = null,
    val player1Penalties: Int? = null,
    val player2Penalties: Int? = null,
    val isKnockout: Boolean = false,
    val scheduledTime: String = "2026-09-02 20:00",
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val verificationStatus: String = "NOT_SUBMITTED", // "NOT_SUBMITTED", "PENDING_VERIFICATION", "APPROVED", "REJECTED"
    val submittedByPlayerId: String? = null,
    val matchNotes: String? = null,
    val winnerId: String? = null,
    val bracketNodeId: String? = null, // "R16_1", "QF_1", "SF_1", "FINAL"
    val nextBracketNodeId: String? = null,
    val nextBracketSlot: Int = 1 // 1 (player1) or 2 (player2)
)
