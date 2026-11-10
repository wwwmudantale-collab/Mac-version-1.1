package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiMarketAnalyzer
import com.example.data.engine.BotExecutionEngine
import com.example.data.local.AppDatabase
import com.example.data.local.BotActivityLog
import com.example.data.local.BotConfig
import com.example.data.local.PaperTrade
import com.example.data.local.PaymentTransactionEntity
import com.example.data.model.AiTradeSignal
import com.example.data.model.BlockchainNetwork
import com.example.data.model.BotStrategy
import com.example.data.model.CryptoCoin
import com.example.data.model.MobileMoneyProvider
import com.example.data.model.TransactionSettings
import com.example.data.model.UserProfile
import com.example.data.repository.CryptoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiDeepDiveUiState {
    data object Idle : AiDeepDiveUiState
    data object Loading : AiDeepDiveUiState
    data class Streaming(val text: String) : AiDeepDiveUiState
    data class Success(val text: String) : AiDeepDiveUiState
    data class Error(val message: String) : AiDeepDiveUiState
}

sealed interface AiSignalUiState {
    data object Idle : AiSignalUiState
    data object Loading : AiSignalUiState
    data class Success(val signal: AiTradeSignal) : AiSignalUiState
    data class Error(val message: String) : AiSignalUiState
}

enum class CoinFilterCategory {
    ALL,
    PENNY_COINS,
    TOP_GAINERS,
    MAJORS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val cryptoRepository = CryptoRepository(viewModelScope)
    val botEngine = BotExecutionEngine(application, database, cryptoRepository, viewModelScope)
    private val aiAnalyzer = AiMarketAnalyzer()

    // Market data
    val coins: StateFlow<List<CryptoCoin>> = cryptoRepository.coins
    val isLiveConnecting: StateFlow<Boolean> = cryptoRepository.isLiveConnecting

    private val _selectedCoin = MutableStateFlow<CryptoCoin?>(null)
    val selectedCoin: StateFlow<CryptoCoin?> = _selectedCoin.asStateFlow()

    private val _coinFilter = MutableStateFlow(CoinFilterCategory.ALL)
    val coinFilter: StateFlow<CoinFilterCategory> = _coinFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Engine & Wallet states
    val walletBalance: StateFlow<Double> = botEngine.walletBalance
    val totalCentsGrabbed: StateFlow<Double> = botEngine.totalCentsGrabbed
    val activeBotsCount: StateFlow<Int> = botEngine.activeBotsCount
    val executionSpeedOps: StateFlow<Double> = botEngine.executionSpeedOps
    val accountId: StateFlow<String> = botEngine.accountId
    val isAccountLocked: StateFlow<Boolean> = botEngine.isAccountLocked
    val userProfile: StateFlow<UserProfile> = botEngine.userProfile
    val transactionSettings: StateFlow<TransactionSettings> = botEngine.transactionSettings

    fun lockAccountId() {
        viewModelScope.launch {
            botEngine.lockAccountId()
        }
    }

    fun unlockAccountId() {
        viewModelScope.launch {
            botEngine.unlockAccountId()
        }
    }

    fun toggleWarpSpeed(enabled: Boolean) {
        botEngine.toggleWarpSpeed(enabled)
    }

    fun setExecutionSpeed(ops: Double) {
        botEngine.setExecutionSpeedOps(ops)
    }

    fun updateUserProfile(profile: UserProfile) {
        botEngine.updateUserProfile(profile)
    }

    fun saveTransactionSettings(settings: TransactionSettings) {
        botEngine.saveTransactionSettings(settings)
    }

    fun depositFunds(amount: Double) {
        viewModelScope.launch {
            botEngine.depositFunds(amount)
        }
    }

    // Database flows
    val allBots: StateFlow<List<BotConfig>> = database.botDao().getAllBots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openTrades: StateFlow<List<PaperTrade>> = database.tradeDao().getOpenTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedTrades: StateFlow<List<PaperTrade>> = database.tradeDao().getClosedTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<BotActivityLog>> = database.tradeDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentTransactions: StateFlow<List<PaymentTransactionEntity>> = database.transactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun depositViaMobileMoney(provider: MobileMoneyProvider, phoneNumber: String, amountUsd: Double, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            botEngine.depositViaMobileMoney(provider, phoneNumber, amountUsd)
            onComplete()
        }
    }

    fun withdrawToMobileMoney(provider: MobileMoneyProvider, phoneNumber: String, amountUsd: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = botEngine.withdrawToMobileMoney(provider, phoneNumber, amountUsd)
            onResult(result != null)
        }
    }

    fun depositViaBlockchain(chain: BlockchainNetwork, amountUsd: Double, senderAddress: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            botEngine.depositViaBlockchain(chain, amountUsd, senderAddress)
            onComplete()
        }
    }

    fun withdrawToBlockchain(chain: BlockchainNetwork, destinationAddress: String, amountUsd: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = botEngine.withdrawToBlockchain(chain, destinationAddress, amountUsd)
            onResult(result != null)
        }
    }

    // AI States
    private val _aiDeepDiveState = MutableStateFlow<AiDeepDiveUiState>(AiDeepDiveUiState.Idle)
    val aiDeepDiveState: StateFlow<AiDeepDiveUiState> = _aiDeepDiveState.asStateFlow()

    private val _aiSignalState = MutableStateFlow<AiSignalUiState>(AiSignalUiState.Idle)
    val aiSignalState: StateFlow<AiSignalUiState> = _aiSignalState.asStateFlow()

    private val _assistantChatAnswer = MutableStateFlow<String?>(null)
    val assistantChatAnswer: StateFlow<String?> = _assistantChatAnswer.asStateFlow()

    private val _isAssistantLoading = MutableStateFlow(false)
    val isAssistantLoading: StateFlow<Boolean> = _isAssistantLoading.asStateFlow()

    // Real-time active trade signals across the market
    private val _activeSignals = MutableStateFlow<List<AiTradeSignal>>(emptyList())
    val activeSignals: StateFlow<List<AiTradeSignal>> = _activeSignals.asStateFlow()

    init {
        // Automatically set initial selected coin and evaluate active trade signals
        viewModelScope.launch {
            coins.collect { list ->
                if (_selectedCoin.value == null && list.isNotEmpty()) {
                    _selectedCoin.value = list.firstOrNull { it.symbol == "PEPE" } ?: list.first()
                }

                // Generate active algorithmic trade signals for high-volatility & penny coins
                if (list.isNotEmpty()) {
                    val keyCoins = list.filter { it.symbol in listOf("PEPE", "DOGE", "BONK", "SHIB", "SOL", "XRP", "FLOKI", "KAS") }
                    val signals = keyCoins.map { coin ->
                        val action = when {
                            coin.rsi14 < 42.0 -> "SCALP_LONG"
                            coin.priceChangePercentage24h > 5.0 && coin.rsi14 < 68.0 -> "STRONG_BUY"
                            coin.rsi14 > 72.0 -> "TAKE_PROFIT"
                            else -> "SCALP_LONG"
                        }
                        val conf = (78 + (coin.rsi14.toInt() % 16)).coerceIn(75, 96)
                        val targetCents = if (coin.isPennyCoin) 0.35 else 0.85
                        val stop = if (coin.isPennyCoin) 1.8 else 2.2
                        val reason = when (action) {
                            "STRONG_BUY" -> "High volume breakout on ${coin.symbol}. Momentum indicators show rapid continuation."
                            "SCALP_LONG" -> "RSI oversold bounce on ${coin.symbol}. Micro-cent spread is prime for rapid scalp."
                            "TAKE_PROFIT" -> "Approaching 24h high resistance on ${coin.symbol}. Lock in accumulated cents."
                            else -> "Range consolidation on ${coin.symbol}. Optimal grid oscillation."
                        }
                        AiTradeSignal(
                            symbol = coin.symbol,
                            action = action,
                            confidenceScore = conf,
                            targetProfitCents = targetCents,
                            suggestedStopLoss = stop,
                            reasoning = reason,
                            timeframe = if (coin.isPennyCoin) "1m - 5m Scalp" else "5m - 15m"
                        )
                    }
                    _activeSignals.value = signals
                }
            }
        }
    }

    fun quickExecuteSignal(signal: AiTradeSignal, amountUsd: Double = 25.0) {
        viewModelScope.launch {
            botEngine.executeManualSignalTrade(signal, amountUsd)
        }
    }

    fun selectCoin(coin: CryptoCoin) {
        _selectedCoin.value = coin
        generateAiSignalForCoin(coin)
    }

    fun setCoinFilter(filter: CoinFilterCategory) {
        _coinFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBot(botId: Long, isRunning: Boolean) {
        viewModelScope.launch {
            botEngine.toggleBotRunning(botId, isRunning)
        }
    }

    fun manualCloseTrade(tradeId: Long) {
        viewModelScope.launch {
            botEngine.manualCloseTrade(tradeId)
        }
    }

    fun resetWallet() {
        viewModelScope.launch {
            botEngine.resetWalletAndStats()
        }
    }

    fun saveBotConfig(bot: BotConfig) {
        viewModelScope.launch {
            botEngine.saveBot(bot)
        }
    }

    fun refreshMarket() {
        viewModelScope.launch {
            cryptoRepository.fetchLiveMarketPrices()
        }
    }

    fun generateAiSignalForCoin(coin: CryptoCoin) {
        viewModelScope.launch {
            _aiSignalState.value = AiSignalUiState.Loading
            val result = aiAnalyzer.analyzeCoinSignal(coin)
            result.onSuccess { signal ->
                _aiSignalState.value = AiSignalUiState.Success(signal)
            }.onFailure { err ->
                // Fallback heuristic if API check is throttled
                val heuristicAction = if (coin.rsi14 < 42.0) "SCALP_LONG" else if (coin.rsi14 > 68.0) "TAKE_PROFIT" else "WAIT"
                val heuristicSignal = AiTradeSignal(
                    symbol = coin.symbol,
                    action = heuristicAction,
                    confidenceScore = 78,
                    targetProfitCents = if (coin.isPennyCoin) 0.35 else 0.80,
                    suggestedStopLoss = 1.8,
                    reasoning = "Heuristic analysis: RSI is at ${String.format("%.1f", coin.rsi14)} with 24h momentum at ${coin.formattedChange}. Recommended for micro-cent scalping.",
                    timeframe = "5m - 15m"
                )
                _aiSignalState.value = AiSignalUiState.Success(heuristicSignal)
            }
        }
    }

    fun requestAiMarketDeepDive() {
        viewModelScope.launch {
            _aiDeepDiveState.value = AiDeepDiveUiState.Loading
            val accumulated = StringBuilder()
            try {
                aiAnalyzer.streamMarketDeepDive(coins.value).collect { chunk ->
                    accumulated.append(chunk)
                    _aiDeepDiveState.value = AiDeepDiveUiState.Streaming(accumulated.toString())
                }
                if (accumulated.isNotEmpty()) {
                    _aiDeepDiveState.value = AiDeepDiveUiState.Success(accumulated.toString())
                } else {
                    _aiDeepDiveState.value = AiDeepDiveUiState.Error("No response from AI engine.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiDeepDiveState.value = AiDeepDiveUiState.Error(e.localizedMessage ?: "Analysis failed")
            }
        }
    }

    fun askAiAdvisor(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isAssistantLoading.value = true
            _assistantChatAnswer.value = null
            try {
                val answer = aiAnalyzer.askBotAssistant(prompt, coins.value)
                _assistantChatAnswer.value = answer
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _assistantChatAnswer.value = "Error: ${e.localizedMessage}"
            } finally {
                _isAssistantLoading.value = false
            }
        }
    }
}
