package com.example.data.local

import androidx.room.*
import com.example.data.model.SystemSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemSettingDao {
    @Query("SELECT * FROM system_settings WHERE `key` = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<SystemSettingEntity?>

    @Query("SELECT * FROM system_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingOnce(key: String): SystemSettingEntity?

    @Query("SELECT * FROM system_settings")
    fun getAllSettingsFlow(): Flow<List<SystemSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: SystemSettingEntity)

    @Query("DELETE FROM system_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}
