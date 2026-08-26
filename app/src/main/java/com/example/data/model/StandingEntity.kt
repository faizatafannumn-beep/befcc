package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standings")
data class StandingEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val groupName: String,
    val playerId: String,
    val playerName: String,
    val playerTeam: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val position: Int = 1,
    val isQualified: Boolean = false
)
