package com.example.data.model

data class CryptoCoin(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChange24h: Double,
    val priceChangePercentage24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val marketCap: Double = 0.0,
    val sparkline: List<Double> = emptyList(),
    val isPennyCoin: Boolean = false,
    val rsi14: Double = 50.0,
    val volatilityScore: Double = 1.0 // 1.0 to 5.0 indicator for scalping
) {
    val formattedPrice: String
        get() = when {
            currentPrice < 0.0001 -> String.format("$%.8f", currentPrice)
            currentPrice < 0.01 -> String.format("$%.6f", currentPrice)
            currentPrice < 1.0 -> String.format("$%.4f", currentPrice)
            currentPrice < 100.0 -> String.format("$%.2f", currentPrice)
            else -> String.format("$%,.2f", currentPrice)
        }

    val formattedChange: String
        get() = String.format("%+.2f%%", priceChangePercentage24h)

    val isPositive: Boolean
        get() = priceChangePercentage24h >= 0.0
}

enum class BotStrategy(val displayName: String, val description: String, val defaultProfitCents: Double) {
    CENT_SCALPER(
        displayName = "Micro-Cent Scalper",
        description = "Rapidly snipes micro-fractions (0.1¢ - 5¢) on high-volatility penny coins.",
        defaultProfitCents = 0.25
    ),
    MOMENTUM_SNIPER(
        displayName = "AI Momentum Sniper",
        description = "Enters on AI-detected momentum surges and rides breakout candles.",
        defaultProfitCents = 0.75
    ),
    GRID_MAKER(
        displayName = "Grid Cent Grabber",
        description = "Places buy/sell ladder orders in fluctuating ranges to harvest cents.",
        defaultProfitCents = 0.50
    ),
    DIP_ACCUMULATOR(
        displayName = "Flash-Dip Buyer",
        description = "Detects flash selloffs, buys instant bottom, and sells on initial bounce.",
        defaultProfitCents = 1.00
    )
}

data class AiTradeSignal(
    val symbol: String,
    val action: String, // "STRONG_BUY", "SCALP_LONG", "WAIT", "TAKE_PROFIT"
    val confidenceScore: Int, // 0 to 100
    val targetProfitCents: Double,
    val suggestedStopLoss: Double,
    val reasoning: String,
    val timeframe: String = "5m - 15m"
)
