package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paper_trades")
data class PaperTrade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val botId: Long,
    val symbol: String,
    val coinName: String,
    val side: String = "BUY", // BUY or SELL
    val entryPrice: Double,
    val exitPrice: Double? = null,
    val amountUsd: Double,
    val coinQuantity: Double,
    val entryTimestamp: Long = System.currentTimeMillis(),
    val exitTimestamp: Long? = null,
    val isOpen: Boolean = true,
    val pnlUsd: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val centsGrabbed: Double = 0.0,
    val strategyName: String = "Cent Scalper",
    val triggerReason: String = "AI Micro-Dip Entry"
) {
    val formattedEntryPrice: String
        get() = formatPrice(entryPrice)

    val formattedExitPrice: String
        get() = exitPrice?.let { formatPrice(it) } ?: "Open"

    val formattedPnl: String
        get() = when {
            pnlUsd >= 0 -> String.format("+$%.2f (+%.2f%%)", pnlUsd, pnlPercent)
            else -> String.format("-$%.2f (%.2f%%)", -pnlUsd, pnlPercent)
        }

    val formattedCents: String
        get() = String.format("%+.1f¢", centsGrabbed * 100.0)

    companion object {
        fun formatPrice(price: Double): String = when {
            price < 0.0001 -> String.format("$%.8f", price)
            price < 0.01 -> String.format("$%.6f", price)
            price < 1.0 -> String.format("$%.4f", price)
            else -> String.format("$%,.2f", price)
        }
    }
}
