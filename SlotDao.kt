package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SlotEntity
import com.example.data.model.SlotStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotDao {
    @Query("SELECT * FROM slots WHERE tournamentId = :tournamentId ORDER BY slotNumber ASC")
    fun getSlotsByTournament(tournamentId: String): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE tournamentId = :tournamentId ORDER BY slotNumber ASC")
    suspend fun getSlotsByTournamentOnce(tournamentId: String): List<SlotEntity>

    @Query("SELECT * FROM slots WHERE playerId = :playerId ORDER BY submissionTime DESC")
    fun getSlotsByPlayer(playerId: String): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE status = :status ORDER BY submissionTime DESC")
    fun getSlotsByStatus(status: SlotStatus): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots ORDER BY submissionTime DESC")
    fun getAllSlots(): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE id = :slotId LIMIT 1")
    suspend fun getSlotById(slotId: String): SlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: SlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<SlotEntity>)

    @Update
    suspend fun updateSlot(slot: SlotEntity)

    @Query("DELETE FROM slots WHERE tournamentId = :tournamentId")
    suspend fun deleteSlotsByTournament(tournamentId: String)
}
