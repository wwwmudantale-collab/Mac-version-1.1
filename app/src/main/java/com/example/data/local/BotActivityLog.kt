package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "bot_activity_logs")
data class BotActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val botId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,
    val action: String, // e.g. "BUY", "SELL", "SCAN", "PROFIT_GRAB"
    val message: String,
    val pnlDelta: Double? = null,
    val isWin: Boolean = false
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
