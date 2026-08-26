package com.example.data.model

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    PLAYER
}

enum class TournamentStatus {
    REGISTRATION_OPEN,
    REGISTRATION_CLOSED,
    GROUP_STAGE,
    KNOCKOUT_STAGE,
    COMPLETED
}

enum class TournamentTeamType {
    NATIONAL_TEAMS,
    CLUB_TEAMS,
    BOTH
}

enum class SlotStatus {
    AVAILABLE,
    PENDING,
    CONFIRMED,
    REJECTED
}

enum class MatchStatus {
    SCHEDULED,
    IN_PROGRESS,
    PENDING_VERIFICATION,
    VERIFIED,
    CANCELLED
}

enum class MatchType {
    TOURNAMENT_GROUP,
    TOURNAMENT_KNOCKOUT,
    ONE_VS_ONE
}

enum class KnockoutRound {
    ROUND_OF_16,
    QUARTER_FINAL,
    SEMI_FINAL,
    THIRD_PLACE,
    FINAL
}

enum class NotificationType {
    ACCOUNT_CREATED,
    TOURNAMENT_REGISTERED,
    SLOT_SUBMITTED,
    SLOT_APPROVED,
    SLOT_REJECTED,
    MATCH_ASSIGNED,
    RESULT_SUBMITTED,
    RESULT_APPROVED,
    RESULT_REJECTED,
    QUALIFICATION_ACHIEVED,
    KNOCKOUT_ADVANCED,
    UPCOMING_MATCH
}
