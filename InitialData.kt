package com.example.data.repository

import com.example.data.model.*
import java.util.UUID

object InitialData {
    // Official Leader & Super Admin
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

    // Official Admins
    val adminJabir = UserEntity(
        id = "admin_jabir_02",
        email = "jabir@befcc.org",
        fullName = "Jabir",
        username = "jabir_admin",
        playerId = "BEFCC-00002",
        role = UserRole.ADMIN,
        avatarName = "avatar_admin_2",
        inGameUsername = "BEFCC_JABIR",
        favoriteTeam = "Bangladesh",
        divisionRank = "Tournament Admin",
        matchesPlayed = 0,
        wins = 0,
        draws = 0,
        losses = 0,
        goalsScored = 0,
        goalsConceded = 0,
        points = 0,
        achievements = "BEFCC Tournament Admin",
        selectedTeams = "Bangladesh"
    )

    val adminMahi = UserEntity(
        id = "admin_mahi_03",
        email = "mahi@befcc.org",
        fullName = "Mahi",
        username = "mahi_admin",
        playerId = "BEFCC-00003",
        role = UserRole.ADMIN,
        avatarName = "avatar_admin_3",
        inGameUsername = "BEFCC_MAHI",
        favoriteTeam = "Bangladesh",
        divisionRank = "Tournament Admin",
        matchesPlayed = 0,
        wins = 0,
        draws = 0,
        losses = 0,
        goalsScored = 0,
        goalsConceded = 0,
        points = 0,
        achievements = "BEFCC Tournament Admin",
        selectedTeams = "Bangladesh"
    )

    val adminJon = UserEntity(
        id = "admin_jon_04",
        email = "jon@befcc.org",
        fullName = "Jon",
        username = "jon_admin",
        playerId = "BEFCC-00004",
        role = UserRole.ADMIN,
        avatarName = "avatar_admin_4",
        inGameUsername = "BEFCC_JON",
        favoriteTeam = "Bangladesh",
        divisionRank = "Tournament Admin",
        matchesPlayed = 0,
        wins = 0,
        draws = 0,
        losses = 0,
        goalsScored = 0,
        goalsConceded = 0,
        points = 0,
        achievements = "BEFCC Tournament Admin",
        selectedTeams = "Bangladesh"
    )

    // Initial system accounts (Leader & Admins)
    val adminTeam = listOf(leaderUser, adminJabir, adminMahi, adminJon)

    // Real production state: NO demo players, NO demo tournaments, NO demo matches, NO demo standings
    val emptyPlayers: List<UserEntity> = emptyList()
    val emptyTournaments: List<TournamentEntity> = emptyList()
    val emptySlots: List<SlotEntity> = emptyList()
    val emptyStandings: List<StandingEntity> = emptyList()
    val emptyMatches: List<MatchEntity> = emptyList()

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
