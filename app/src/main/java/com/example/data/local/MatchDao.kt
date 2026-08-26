package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY scheduledTime DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundStage ASC, id ASC")
    fun getMatchesByTournament(tournamentId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundStage ASC, id ASC")
    suspend fun getMatchesByTournamentOnce(tournamentId: String): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE player1Id = :playerId OR player2Id = :playerId ORDER BY scheduledTime DESC")
    fun getMatchesByPlayer(playerId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE verificationStatus = 'PENDING_VERIFICATION' ORDER BY scheduledTime DESC")
    fun getPendingMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId AND bracketNodeId = :nodeId LIMIT 1")
    suspend fun getMatchByBracketNode(tournamentId: String, nodeId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE tournamentId = :tournamentId")
    suspend fun deleteMatchesByTournament(tournamentId: String)
}
