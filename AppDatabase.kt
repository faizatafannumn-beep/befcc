package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.MatchEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SlotEntity
import com.example.data.model.StandingEntity
import com.example.data.model.SystemSettingEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TournamentEntity::class,
        SlotEntity::class,
        MatchEntity::class,
        StandingEntity::class,
        NotificationEntity::class,
        SystemSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun slotDao(): SlotDao
    abstract fun matchDao(): MatchDao
    abstract fun standingDao(): StandingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun systemSettingDao(): SystemSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "befcc_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
