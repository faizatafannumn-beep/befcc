package com.example.data.repository

import com.example.data.model.*

object InitialData {

    // Official Leader & Super Admin
    // This is the only fixed admin account.
    val leaderUser = UserEntity(
        id = "admin_maruf_01",
        email = "maruf@befcc.org",
        fullName = "Maruf Hossain",
        username = "maruf_leader",
        playerId = "BEFCC-00001",
        role = UserRole.SUPER_ADMIN,
        avatarName = "avatar_admin",
        inGameUsername = "BEFCC_LEADER",
        favoriteTeam = "Bangladesh",
        divisionRank = "Super Admin / Leader",
        matchesPlayed = 0,
        wins = 0,
        draws = 0,
        losses = 0,
        goalsScored = 0,
        goalsConceded = 0,
        points = 0,
        achievements = "BEFCC Community Leader,Super Admin",
        selectedTeams = "Bangladesh"
    )

    // No fixed Admin accounts.
    // Any registered Player can later be promoted to ADMIN
    // through the authorized admin-management system.
    val adminTeam = listOf(leaderUser)

    // No demo players
    val emptyPlayers: List<UserEntity> = emptyList()

    // No demo tournaments
    val emptyTournaments: List<TournamentEntity> = emptyList()

    // No demo slots
    val emptySlots: List<SlotEntity> = emptyList()

    // No demo standings
    val emptyStandings: List<StandingEntity> = emptyList()

    // No demo matches
    val emptyMatches: List<MatchEntity> = emptyList()

    // Initial system notification
    val initialNotifications = listOf(
        NotificationEntity(
            id = "notif_system_welcome",
            userId = "ALL",
            title = "Welcome to BEFCC Official Platform",
            message = "Bangladesh eFootball Competitive Community (BEFCC) is live. Tournaments, fixtures, and national rankings are officially managed here.",
            type = NotificationType.ACCOUNT_CREATED,
            timestamp = System.currentTimeMillis()
        )
    )
}
