package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    @Query("SELECT * FROM bot_configs ORDER BY id ASC")
    fun getAllBots(): Flow<List<BotConfig>>

    @Query("SELECT * FROM bot_configs WHERE isRunning = 1")
    fun getRunningBots(): Flow<List<BotConfig>>

    @Query("SELECT * FROM bot_configs WHERE id = :id")
    suspend fun getBotById(id: Long): BotConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBot(bot: BotConfig): Long

    @Update
    suspend fun updateBot(bot: BotConfig)

    @Delete
    suspend fun deleteBot(bot: BotConfig)

    @Query("UPDATE bot_configs SET isRunning = :isRunning WHERE id = :botId")
    suspend fun setBotRunning(botId: Long, isRunning: Boolean)
}
