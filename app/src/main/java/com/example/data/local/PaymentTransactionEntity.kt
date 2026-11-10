package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PaymentChannelType
import com.example.data.model.PaymentDirection
import com.example.data.model.PaymentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionRef: String, // e.g. "MTN-MOMO-748291", "AIRTEL-993821", or "0x7a3...c91"
    val channelType: PaymentChannelType, // MOBILE_MONEY or BLOCKCHAIN_NETWORK
    val providerOrChain: String, // "MTN Mobile Money", "Airtel Money", "Solana", "BNB Chain"
    val direction: PaymentDirection, // DEPOSIT, WITHDRAWAL, AUTO_CASHOUT_CENT_PROFIT
    val amountUsd: Double,
    val localAmount: Double? = null,
    val localCurrency: String? = null,
    val accountOrAddress: String, // Phone number or wallet address
    val status: PaymentStatus = PaymentStatus.CONFIRMED,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedUsdAmount: String
        get() {
            val sign = if (direction == PaymentDirection.DEPOSIT) "+" else "-"
            return "$sign$${String.format(Locale.US, "%.2f", amountUsd)}"
        }

    val formattedLocalAmount: String?
        get() {
            if (localAmount == null || localCurrency == null) return null
            val sign = if (direction == PaymentDirection.DEPOSIT) "+" else "-"
            return "$sign${String.format(Locale.US, "%,.0f", localAmount)} $localCurrency"
        }
}
