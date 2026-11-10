package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BotStrategy

@Entity(tableName = "bot_configs")
data class BotConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val strategy: BotStrategy = BotStrategy.CENT_SCALPER,
    val targetCoins: String = "PEPE,DOGE,SHIB,SOL", // comma separated
    val tradeSizeUsd: Double = 25.0,
    val targetProfitCents: Double = 0.35, // in dollars, e.g. 0.35 = 35 cents
    val stopLossPercent: Double = 2.0, // 2% stop loss
    val isRunning: Boolean = false,
    val intervalSeconds: Int = 5,
    val totalCentsGrabbedUsd: Double = 0.0,
    val totalTradesCount: Int = 0,
    val winCount: Int = 0,
    val maxOpenPositions: Int = 3
) {
    val winRatePercent: Double
        get() = if (totalTradesCount > 0) (winCount.toDouble() / totalTradesCount) * 100.0 else 0.0

    val coinsList: List<String>
        get() = targetCoins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
