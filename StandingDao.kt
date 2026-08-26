package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StandingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StandingDao {
    @Query("SELECT * FROM standings WHERE tournamentId = :tournamentId ORDER BY groupName ASC, points DESC, goalDifference DESC, goalsFor DESC")
    fun getStandingsByTournament(tournamentId: String): Flow<List<StandingEntity>>

    @Query("SELECT * FROM standings WHERE tournamentId = :tournamentId ORDER BY groupName ASC, points DESC, goalDifference DESC, goalsFor DESC")
    suspend fun getStandingsByTournamentOnce(tournamentId: String): List<StandingEntity>

    @Query("SELECT * FROM standings WHERE tournamentId = :tournamentId AND groupName = :groupName ORDER BY points DESC, goalDifference DESC, goalsFor DESC")
    suspend fun getStandingsByGroupOnce(tournamentId: String, groupName: String): List<StandingEntity>

    @Query("SELECT * FROM standings WHERE tournamentId = :tournamentId AND playerId = :playerId LIMIT 1")
    suspend fun getStandingForPlayer(tournamentId: String, playerId: String): StandingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStanding(standing: StandingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandings(standings: List<StandingEntity>)

    @Update
    suspend fun updateStanding(standing: StandingEntity)

    @Query("DELETE FROM standings WHERE tournamentId = :tournamentId")
    suspend fun deleteStandingsByTournament(tournamentId: String)
}
