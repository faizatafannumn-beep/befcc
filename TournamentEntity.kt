package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // e.g. "Championship Cup", "Pro League", "National Derby"
    val playerLimit: Int = 16, // 12, 16, 20, 24, 28, 32, 36, 40, 44, 48
    val entryFee: Double = 200.0, // in BDT ৳
    val prizePool: String = "৳15,000 + BEFCC Trophy",
    val registrationStatus: String = "OPEN", // "OPEN", "CLOSED"
    val status: TournamentStatus = TournamentStatus.REGISTRATION_OPEN,
    val startDate: String = "2026-09-01",
    val endDate: String = "2026-09-15",
    val teamType: TournamentTeamType = TournamentTeamType.BOTH,
    val availableTeams: String = "Bangladesh,Brazil,Argentina,France,Portugal,England,Spain,Germany,Real Madrid,Barcelona,Liverpool,Manchester City,Bayern Munich,PSG,Arsenal,Inter Milan", // Comma-separated
    val rules: String = "1. Standard 10-minute eFootball Match.\n2. Match condition: Excellent.\n3. Screenshot of final score must be submitted.\n4. Toxic behavior or network tampering results in disqualification.\n5. Knockout matches require Extra Time and Penalties.",
    val groupsCount: Int = 4,
    val groupSize: Int = 4,
    val groupsGenerated: Boolean = false,
    val fixturesGenerated: Boolean = false,
    val knockoutBracketGenerated: Boolean = false,
    val currentRound: String = "Group Stage",
    val championName: String? = null,
    val runnerUpName: String? = null
)
