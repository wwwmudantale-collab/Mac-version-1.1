package com.example.data.ai

import android.util.Log
import com.example.data.model.AiTradeSignal
import com.example.data.model.CryptoCoin
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiMarketAnalyzer"

class AiMarketAnalyzer {

    // 1. Structured JSON Schema for Trade Signals
    private val signalSchema = Schema.obj(
        properties = mapOf(
            "symbol" to Schema.string(description = "Crypto ticker symbol e.g. PEPE, DOGE, SOL"),
            "action" to Schema.enumeration(
                values = listOf("STRONG_BUY", "SCALP_LONG", "WAIT", "TAKE_PROFIT"),
                description = "Recommended trading bot action"
            ),
            "confidenceScore" to Schema.integer(description = "Signal confidence from 0 to 100"),
            "targetProfitCents" to Schema.double(description = "Target profit in cents/USD e.g. 0.35"),
            "suggestedStopLoss" to Schema.double(description = "Suggested stop loss percentage e.g. 1.8"),
            "reasoning" to Schema.string(description = "Brief reasoning based on technical indicators and volume"),
            "timeframe" to Schema.string(description = "Scalp execution horizon e.g. 5m - 15m")
        )
    )

    private val structuredConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = signalSchema
    }

    // Lazy model initialization to prevent startup crashes
    private val signalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = structuredConfig
        )
    }

    private val conversationalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    /**
     * Generates a structured AI scalp signal for a specific coin.
     */
    suspend fun analyzeCoinSignal(coin: CryptoCoin): Result<AiTradeSignal> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are an algorithmic high-frequency cryptocurrency trading bot AI.
                Analyze the following coin data for a quick micro-cent scalping or swing opportunity:
                - Coin: ${coin.name} (${coin.symbol})
                - Current Price: ${coin.formattedPrice}
                - 24h Change: ${coin.formattedChange}
                - 24h High: ${coin.high24h}, Low: ${coin.low24h}
                - 24h Volume: ${coin.volume24h}
                - RSI (14): ${String.format("%.1f", coin.rsi14)}
                - Is Penny/Micro-cap Coin: ${coin.isPennyCoin}

                Evaluate whether a trading bot should enter a micro-cent scalp (SCALP_LONG), strong momentum buy (STRONG_BUY), take profit (TAKE_PROFIT), or wait for volatility pullback (WAIT).
                Set targetProfitCents between 0.15 and 1.50 cents USD. Return structured JSON.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                signalModel.generateContent(prompt)
            }

            val jsonString = response.text ?: throw IllegalStateException("Model returned empty text.")
            val json = JSONObject(jsonString)

            val signal = AiTradeSignal(
                symbol = json.optString("symbol", coin.symbol),
                action = json.optString("action", "SCALP_LONG"),
                confidenceScore = json.optInt("confidenceScore", 82),
                targetProfitCents = json.optDouble("targetProfitCents", 0.35),
                suggestedStopLoss = json.optDouble("suggestedStopLoss", 1.8),
                reasoning = json.optString("reasoning", "Favorable RSI support bounce with micro-cent liquidity depth."),
                timeframe = json.optString("timeframe", "5m - 15m")
            )

            Result.success(signal)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI signal generation timed out", e)
            Result.failure(Exception("AI analysis timed out. Using technical heuristic signal."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI signal generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Streams comprehensive AI market deep-dive analysis.
     */
    fun streamMarketDeepDive(coins: List<CryptoCoin>): Flow<String> = flow {
        val pennyCoins = coins.filter { it.isPennyCoin }.take(5).joinToString { "${it.symbol} (${it.formattedPrice}, ${it.formattedChange})" }
        val majorCoins = coins.filter { !it.isPennyCoin }.take(3).joinToString { "${it.symbol} (${it.formattedPrice}, ${it.formattedChange})" }

        val prompt = """
            You are the Chief AI Trading Engine for CoinSniper AI, specialized in grabbing cents and scalping micro-profits across cryptocurrency coins.
            Market Snapshot:
            - Major Drivers: $majorCoins
            - High-Frequency Penny Coins: $pennyCoins

            Provide a crisp, actionable 3-part intelligence brief:
            1. 🎯 Top 2 Cent-Grabbing Opportunities (Best coins to scalp right now and exact target cents)
            2. ⚡ Volatility & Liquidity Warning (Spread traps to avoid)
            3. 🤖 Optimal Bot Preset (Recommended trade size, stop-loss, and scalping speed for current market sentiment)
            Keep formatting clean with bullet points and bold highlights.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                conversationalModel.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            emit("\n[Deep dive timed out. Live telemetry is still active.]")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit("Market Analysis Notice: ${e.localizedMessage ?: "Unable to stream analysis"}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Ask AI bot assistant any trading strategy question.
     */
    suspend fun askBotAssistant(question: String, activeCoins: List<CryptoCoin>): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are CoinSniper AI Bot Advisor. The user is asking:
                "$question"

                Available coins in watchlist: ${activeCoins.take(6).joinToString { it.symbol }}
                Give an expert, concise, practical crypto trading answer focused on micro-scalping, risk management, and profit grabbing.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                conversationalModel.generateContent(prompt)
            }
            response.text ?: "No response from AI engine."
        } catch (e: TimeoutCancellationException) {
            "Request timed out. Please try again."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            "AI Advisor unavailable: ${e.localizedMessage}"
        }
    }
}
