package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM paper_trades ORDER BY entryTimestamp DESC")
    fun getAllTrades(): Flow<List<PaperTrade>>

    @Query("SELECT * FROM paper_trades WHERE isOpen = 1 ORDER BY entryTimestamp DESC")
    fun getOpenTrades(): Flow<List<PaperTrade>>

    @Query("SELECT * FROM paper_trades WHERE isOpen = 0 ORDER BY exitTimestamp DESC LIMIT 100")
    fun getClosedTrades(): Flow<List<PaperTrade>>

    @Query("SELECT * FROM paper_trades WHERE botId = :botId AND isOpen = 1")
    suspend fun getOpenTradesForBot(botId: Long): List<PaperTrade>

    @Query("SELECT * FROM paper_trades WHERE id = :id")
    suspend fun getTradeById(id: Long): PaperTrade?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: PaperTrade): Long

    @Update
    suspend fun updateTrade(trade: PaperTrade)

    @Query("DELETE FROM paper_trades")
    suspend fun clearAllTrades()

    // Activity Logs
    @Query("SELECT * FROM bot_activity_logs ORDER BY timestamp DESC LIMIT 60")
    fun getRecentLogs(): Flow<List<BotActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BotActivityLog): Long

    @Query("DELETE FROM bot_activity_logs")
    suspend fun clearLogs()
}
