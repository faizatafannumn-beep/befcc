package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val username: String,
    val playerId: String, // e.g. "BEFCC-78901"
    val role: UserRole = UserRole.PLAYER,
    val avatarName: String = "avatar_1",
    val inGameUsername: String = "",
    val favoriteTeam: String = "Bangladesh",
    val divisionRank: String = "Division 1",
    val createdAt: Long = System.currentTimeMillis(),
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsScored: Int = 0,
    val goalsConceded: Int = 0,
    val points: Int = 0,
    val achievements: String = "BEFCC Verified Player", // Comma separated
    val selectedTeams: String = "Bangladesh", // Comma separated
    val isActive: Boolean = true
) {
    val goalDifference: Int
        get() = goalsScored - goalsConceded

    val winRate: Double
        get() = if (matchesPlayed > 0) (wins.toDouble() / matchesPlayed.toDouble()) * 100.0 else 0.0

    val isSuperAdmin: Boolean
        get() = role == UserRole.SUPER_ADMIN

    val isAdminOrLeader: Boolean
        get() = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN
}
