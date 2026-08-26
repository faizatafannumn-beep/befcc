package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MatchStatus
import com.example.data.model.MatchType
import com.example.data.model.NotificationType
import com.example.data.model.SlotStatus
import com.example.data.model.TournamentStatus
import com.example.data.model.TournamentTeamType
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.PLAYER
    }

    @TypeConverter
    fun fromTournamentStatus(value: TournamentStatus): String = value.name

    @TypeConverter
    fun toTournamentStatus(value: String): TournamentStatus = try {
        TournamentStatus.valueOf(value)
    } catch (e: Exception) {
        TournamentStatus.REGISTRATION_OPEN
    }

    @TypeConverter
    fun fromTournamentTeamType(value: TournamentTeamType): String = value.name

    @TypeConverter
    fun toTournamentTeamType(value: String): TournamentTeamType = try {
        TournamentTeamType.valueOf(value)
    } catch (e: Exception) {
        TournamentTeamType.BOTH
    }

    @TypeConverter
    fun fromSlotStatus(value: SlotStatus): String = value.name

    @TypeConverter
    fun toSlotStatus(value: String): SlotStatus = try {
        SlotStatus.valueOf(value)
    } catch (e: Exception) {
        SlotStatus.AVAILABLE
    }

    @TypeConverter
    fun fromMatchStatus(value: MatchStatus): String = value.name

    @TypeConverter
    fun toMatchStatus(value: String): MatchStatus = try {
        MatchStatus.valueOf(value)
    } catch (e: Exception) {
        MatchStatus.SCHEDULED
    }

    @TypeConverter
    fun fromMatchType(value: MatchType): String = value.name

    @TypeConverter
    fun toMatchType(value: String): MatchType = try {
        MatchType.valueOf(value)
    } catch (e: Exception) {
        MatchType.ONE_VS_ONE
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (e: Exception) {
        NotificationType.TOURNAMENT_REGISTERED
    }
}
