package com.example.data.engine

import android.content.Context
import android.util.Log
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
import com.example.data.model.PaymentChannelType
import com.example.data.model.PaymentDirection
import com.example.data.model.PaymentStatus
import com.example.data.model.TransactionSettings
import com.example.data.model.UserProfile
import com.example.data.repository.CryptoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val TAG = "BotExecutionEngine"

class BotExecutionEngine(
    private val context: Context,
    private val database: AppDatabase,
    private val cryptoRepository: CryptoRepository,
    private val scope: CoroutineScope
) {
    private val tradeDao = database.tradeDao()
    private val botDao = database.botDao()
    private val transactionDao = database.transactionDao()

    // Paper Trading Wallet
    private val prefs = context.getSharedPreferences("coinsniper_wallet_prefs", Context.MODE_PRIVATE)

    private val _walletBalance = MutableStateFlow(
        prefs.getFloat("wallet_balance", 1000.0f).toDouble()
    )
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _totalCentsGrabbed = MutableStateFlow(
        prefs.getFloat("total_cents_grabbed", 0.0f).toDouble()
    )
    val totalCentsGrabbed: StateFlow<Double> = _totalCentsGrabbed.asStateFlow()

    private val _activeBotsCount = MutableStateFlow(0)
    val activeBotsCount: StateFlow<Int> = _activeBotsCount.asStateFlow()

    // Account ID & Emergency Lock Control
    private val _accountId = MutableStateFlow("CS-8842-EXA")
    val accountId: StateFlow<String> = _accountId.asStateFlow()

    private val _isAccountLocked = MutableStateFlow(
        prefs.getBoolean("is_account_locked", false)
    )
    val isAccountLocked: StateFlow<Boolean> = _isAccountLocked.asStateFlow()

    suspend fun lockAccountId() {
        _isAccountLocked.value = true
        prefs.edit().putBoolean("is_account_locked", true).apply()

        // Emergency freeze: immediately halt all active bots
        val runningBots = listOfNotNull(
            botDao.getBotById(1L),
            botDao.getBotById(2L),
            botDao.getBotById(3L)
        ).filter { it.isRunning }

        for (bot in runningBots) {
            botDao.setBotRunning(bot.id, false)
        }
        _activeBotsCount.value = 0

        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = "LOCK",
                action = "ACCOUNT_LOCKED",
                message = "🚨 EMERGENCY ACCOUNT LOCK: Account ID ${_accountId.value} locked down instantly. All trading bot executions suspended without requiring company confirmation.",
                pnlDelta = null,
                isWin = false
            )
        )
    }

    suspend fun unlockAccountId() {
        _isAccountLocked.value = false
        prefs.edit().putBoolean("is_account_locked", false).apply()

        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = "UNLOCK",
                action = "ACCOUNT_UNLOCKED",
                message = "🔓 Account ID ${_accountId.value} unlocked. Trading bot permissions restored.",
                pnlDelta = null,
                isWin = false
            )
        )
    }

    // 1 Quintillion Ops/Sec (1,000,000,000,000,000,000 = 10^18)
    val WARP_SPEED_THRESHOLD_OPS = 1_000_000_000_000_000_000.0

    // Execution speed telemetry (defaulting to 1.25 × 10¹⁸ ops/sec when Warp mode engaged)
    private val _executionSpeedOps = MutableStateFlow(1_250_000_000_000_000_000.0)
    val executionSpeedOps: StateFlow<Double> = _executionSpeedOps.asStateFlow()

    val isWarpSpeedActive: Boolean
        get() = _executionSpeedOps.value >= WARP_SPEED_THRESHOLD_OPS

    fun setExecutionSpeedOps(ops: Double) {
        _executionSpeedOps.value = ops.coerceAtLeast(100.0)
    }

    fun toggleWarpSpeed(enabled: Boolean) {
        _executionSpeedOps.value = if (enabled) {
            1_500_000_000_000_000_000.0 // 1.5 Quintillion ops/sec
        } else {
            10_000.0 // Standard mode
        }
    }

    init {
        // Seed default bots and initial transactions if database is empty
        scope.launch(Dispatchers.IO) {
            seedDefaultBotsIfNeeded()
            seedDefaultTransactionsIfNeeded()
            startBotExecutionLoop()
        }
    }

    private suspend fun seedDefaultTransactionsIfNeeded() {
        val existingTx = transactionDao.getAllTransactions().firstOrNull()
        if (existingTx.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val initialTxs = listOf(
                PaymentTransactionEntity(
                    transactionRef = "MTN-MOMO-883921",
                    channelType = PaymentChannelType.MOBILE_MONEY,
                    providerOrChain = "MTN Mobile Money",
                    direction = PaymentDirection.DEPOSIT,
                    amountUsd = 250.0,
                    localAmount = 937500.0,
                    localCurrency = "UGX",
                    accountOrAddress = "+256 772 884 921",
                    status = PaymentStatus.SETTLED,
                    notes = "Initial bankroll deposit via MTN MoMo Push *165#",
                    timestamp = now - 86400000L * 2
                ),
                PaymentTransactionEntity(
                    transactionRef = "AIRTEL-MONEY-554201",
                    channelType = PaymentChannelType.MOBILE_MONEY,
                    providerOrChain = "Airtel Money",
                    direction = PaymentDirection.DEPOSIT,
                    amountUsd = 150.0,
                    localAmount = 562500.0,
                    localCurrency = "UGX",
                    accountOrAddress = "+256 755 420 188",
                    status = PaymentStatus.SETTLED,
                    notes = "Top-up via Airtel Money Express *185#",
                    timestamp = now - 86400000L
                ),
                PaymentTransactionEntity(
                    transactionRef = "5kM9z8eWv2NqL7tYb1p...",
                    channelType = PaymentChannelType.BLOCKCHAIN_NETWORK,
                    providerOrChain = "Solana (SOL)",
                    direction = PaymentDirection.DEPOSIT,
                    amountUsd = 600.0,
                    localAmount = null,
                    localCurrency = null,
                    accountOrAddress = "7xKX...qW8s",
                    status = PaymentStatus.CONFIRMED,
                    notes = "Bridge SPL-USDT liquidity for cent scalper",
                    timestamp = now - 3600000L * 6
                )
            )
            initialTxs.forEach { transactionDao.insertTransaction(it) }
        }
    }

    private suspend fun seedDefaultBotsIfNeeded() {
        val existing = botDao.getBotById(1L)
        if (existing == null) {
            val defaultBots = listOf(
                BotConfig(
                    id = 1L,
                    name = "⚡ Penny Cent Grabber",
                    strategy = BotStrategy.CENT_SCALPER,
                    targetCoins = "PEPE,DOGE,SHIB,BONK,FLOKI",
                    tradeSizeUsd = 20.0,
                    targetProfitCents = 0.35, // 35 cents target
                    stopLossPercent = 1.8,
                    isRunning = true,
                    intervalSeconds = 3,
                    maxOpenPositions = 3
                ),
                BotConfig(
                    id = 2L,
                    name = "🎯 AI Momentum Sniper",
                    strategy = BotStrategy.MOMENTUM_SNIPER,
                    targetCoins = "SOL,XRP,KAS,PEPE",
                    tradeSizeUsd = 50.0,
                    targetProfitCents = 0.85, // 85 cents target
                    stopLossPercent = 2.5,
                    isRunning = false,
                    intervalSeconds = 5,
                    maxOpenPositions = 2
                ),
                BotConfig(
                    id = 3L,
                    name = "📐 Grid Cent Harvester",
                    strategy = BotStrategy.GRID_MAKER,
                    targetCoins = "DOGE,TRX,VET,XLM",
                    tradeSizeUsd = 15.0,
                    targetProfitCents = 0.25, // 25 cents target
                    stopLossPercent = 1.5,
                    isRunning = false,
                    intervalSeconds = 4,
                    maxOpenPositions = 4
                )
            )
            defaultBots.forEach { botDao.insertBot(it) }
        }
    }

    private fun startBotExecutionLoop() {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                // When speed is more than 1,000,000,000,000,000,000 ops/sec, accelerate execution down to 180ms!
                val isWarp = _executionSpeedOps.value >= WARP_SPEED_THRESHOLD_OPS
                val cycleDelay = if (isWarp) 180L else 2500L
                delay(cycleDelay)

                // If account ID is locked, immediately suspend all autonomous bot executions
                if (_isAccountLocked.value) {
                    _activeBotsCount.value = 0
                    continue
                }

                try {
                    val allCoins = cryptoRepository.coins.value
                    if (allCoins.isEmpty()) continue

                    val runningBots = botDao.getBotById(1L)?.let {
                        // Quick poll running bots
                        listOfNotNull(
                            botDao.getBotById(1L),
                            botDao.getBotById(2L),
                            botDao.getBotById(3L)
                        ).filter { it.isRunning }
                    } ?: emptyList()

                    _activeBotsCount.value = runningBots.size

                    for (bot in runningBots) {
                        processBotCycle(bot, allCoins)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in bot execution cycle", e)
                }
            }
        }
    }

    private suspend fun processBotCycle(bot: BotConfig, coins: List<CryptoCoin>) {
        val openTrades = tradeDao.getOpenTradesForBot(bot.id)

        // 1. Evaluate open trades for Take Profit (Grab Cents) or Stop Loss
        for (trade in openTrades) {
            val coin = coins.firstOrNull { it.symbol.equals(trade.symbol, ignoreCase = true) } ?: continue
            val currentPrice = coin.currentPrice

            val pnlUsd = (currentPrice - trade.entryPrice) * trade.coinQuantity
            val pnlPercent = if (trade.entryPrice > 0) ((currentPrice - trade.entryPrice) / trade.entryPrice) * 100.0 else 0.0

            val targetHit = pnlUsd >= bot.targetProfitCents
            val stopLossHit = pnlPercent <= -bot.stopLossPercent

            if (targetHit || stopLossHit) {
                val isWin = targetHit
                val closedTrade = trade.copy(
                    exitPrice = currentPrice,
                    exitTimestamp = System.currentTimeMillis(),
                    isOpen = false,
                    pnlUsd = pnlUsd,
                    pnlPercent = pnlPercent,
                    centsGrabbed = if (isWin) pnlUsd else 0.0
                )
                tradeDao.updateTrade(closedTrade)

                // Return capital + profit/loss to wallet
                val returnedCapital = trade.amountUsd + pnlUsd
                updateWalletBalance(returnedCapital)

                if (isWin) {
                    updateTotalCentsGrabbed(pnlUsd)
                }

                // Update bot statistics
                val updatedBot = bot.copy(
                    totalTradesCount = bot.totalTradesCount + 1,
                    winCount = if (isWin) bot.winCount + 1 else bot.winCount,
                    totalCentsGrabbedUsd = bot.totalCentsGrabbedUsd + (if (isWin) pnlUsd else 0.0)
                )
                botDao.updateBot(updatedBot)

                // Log the result
                val message = if (isWin) {
                    "🎯 GRABBED +$${String.format("%.2f", pnlUsd)} (+${String.format("%.1f", pnlUsd * 100)}¢) on ${trade.symbol}! Scalp filled at ${PaperTrade.formatPrice(currentPrice)}."
                } else {
                    "🛑 Stop Loss triggered on ${trade.symbol} at ${String.format("%.2f%%", pnlPercent)}. Position safely closed."
                }

                tradeDao.insertLog(
                    BotActivityLog(
                        botId = bot.id,
                        symbol = trade.symbol,
                        action = if (isWin) "PROFIT_GRAB" else "STOP_LOSS",
                        message = message,
                        pnlDelta = pnlUsd,
                        isWin = isWin
                    )
                )
            }
        }

        // 2. Scan target coins to open new trade if capacity permits
        if (openTrades.size < bot.maxOpenPositions && _walletBalance.value >= bot.tradeSizeUsd) {
            val targetCoinsList = bot.coinsList
            val eligibleCoins = coins.filter { coin ->
                targetCoinsList.any { it.equals(coin.symbol, ignoreCase = true) } &&
                        openTrades.none { it.symbol.equals(coin.symbol, ignoreCase = true) }
            }

            for (coin in eligibleCoins) {
                val shouldEnter = evaluateEntryTrigger(bot.strategy, coin)
                if (shouldEnter) {
                    executeBuyOrder(bot, coin)
                    break // Open one trade per cycle
                }
            }
        }
    }

    private fun evaluateEntryTrigger(strategy: BotStrategy, coin: CryptoCoin): Boolean {
        return when (strategy) {
            BotStrategy.CENT_SCALPER -> {
                // Penny coins with high micro-volatility: enter on slight RSI dip or micro-consolidation
                (coin.rsi14 in 35.0..62.0) && (coin.isPennyCoin || Random.nextFloat() < 0.65f)
            }
            BotStrategy.MOMENTUM_SNIPER -> {
                // Surging coins with momentum
                (coin.priceChangePercentage24h > 2.0 || coin.rsi14 > 55.0)
            }
            BotStrategy.GRID_MAKER -> {
                // Oscillating market conditions
                coin.rsi14 in 42.0..58.0
            }
            BotStrategy.DIP_ACCUMULATOR -> {
                // Oversold dip
                coin.rsi14 < 45.0 || coin.priceChangePercentage24h < 0.0
            }
        }
    }

    private suspend fun executeBuyOrder(bot: BotConfig, coin: CryptoCoin) {
        val tradeCost = bot.tradeSizeUsd
        if (_walletBalance.value < tradeCost) return

        // Deduct from wallet
        updateWalletBalance(-tradeCost)

        val quantity = tradeCost / coin.currentPrice
        val newTrade = PaperTrade(
            botId = bot.id,
            symbol = coin.symbol,
            coinName = coin.name,
            side = "BUY",
            entryPrice = coin.currentPrice,
            amountUsd = tradeCost,
            coinQuantity = quantity,
            entryTimestamp = System.currentTimeMillis(),
            isOpen = true,
            strategyName = bot.strategy.displayName,
            triggerReason = "${bot.strategy.displayName} Scalp Signal"
        )

        tradeDao.insertTrade(newTrade)

        tradeDao.insertLog(
            BotActivityLog(
                botId = bot.id,
                symbol = coin.symbol,
                action = "BUY",
                message = "⚡ ${bot.name} BUY: $${String.format("%.2f", tradeCost)} into ${coin.symbol} at ${PaperTrade.formatPrice(coin.currentPrice)}.",
                pnlDelta = 0.0,
                isWin = false
            )
        )
    }

    suspend fun manualCloseTrade(tradeId: Long) {
        val trade = tradeDao.getTradeById(tradeId) ?: return
        if (!trade.isOpen) return

        val coin = cryptoRepository.getCoinBySymbol(trade.symbol)
        val currentPrice = coin?.currentPrice ?: trade.entryPrice

        val pnlUsd = (currentPrice - trade.entryPrice) * trade.coinQuantity
        val pnlPercent = if (trade.entryPrice > 0) ((currentPrice - trade.entryPrice) / trade.entryPrice) * 100.0 else 0.0
        val isWin = pnlUsd >= 0.0

        val closed = trade.copy(
            exitPrice = currentPrice,
            exitTimestamp = System.currentTimeMillis(),
            isOpen = false,
            pnlUsd = pnlUsd,
            pnlPercent = pnlPercent,
            centsGrabbed = if (isWin) pnlUsd else 0.0
        )
        tradeDao.updateTrade(closed)

        updateWalletBalance(trade.amountUsd + pnlUsd)
        if (isWin) {
            updateTotalCentsGrabbed(pnlUsd)
        }

        tradeDao.insertLog(
            BotActivityLog(
                botId = trade.botId,
                symbol = trade.symbol,
                action = "MANUAL_CLOSE",
                message = "🖐️ Manual position exit on ${trade.symbol}: ${if (pnlUsd >= 0) "+$" else "-$"}${String.format("%.2f", Math.abs(pnlUsd))} (${String.format("%.2f%%", pnlPercent)})",
                pnlDelta = pnlUsd,
                isWin = isWin
            )
        )
    }

    suspend fun executeManualSignalTrade(signal: AiTradeSignal, amountUsd: Double = 25.0): Boolean {
        if (_isAccountLocked.value) return false
        val coin = cryptoRepository.getCoinBySymbol(signal.symbol) ?: return false
        if (_walletBalance.value < amountUsd) return false

        updateWalletBalance(-amountUsd)
        val quantity = amountUsd / coin.currentPrice

        val trade = PaperTrade(
            botId = 0L,
            symbol = coin.symbol,
            coinName = coin.name,
            side = "BUY",
            entryPrice = coin.currentPrice,
            amountUsd = amountUsd,
            coinQuantity = quantity,
            entryTimestamp = System.currentTimeMillis(),
            isOpen = true,
            strategyName = "AI ${signal.action.replace("_", " ")}",
            triggerReason = "Dashboard Signal: Target +${String.format("%.1f¢", signal.targetProfitCents * 100)}"
        )

        tradeDao.insertTrade(trade)
        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = coin.symbol,
                action = "SIGNAL_SNIPE",
                message = "🎯 Quick Scalp entry: $${String.format("%.0f", amountUsd)} into ${coin.symbol} at ${PaperTrade.formatPrice(coin.currentPrice)} based on ${signal.confidenceScore}% ${signal.action} signal.",
                pnlDelta = 0.0,
                isWin = false
            )
        )
        return true
    }

    suspend fun toggleBotRunning(botId: Long, isRunning: Boolean) {
        botDao.setBotRunning(botId, isRunning)
        val bot = botDao.getBotById(botId)
        val botName = bot?.name ?: "Bot #$botId"
        tradeDao.insertLog(
            BotActivityLog(
                botId = botId,
                symbol = "SYS",
                action = if (isRunning) "START" else "PAUSE",
                message = if (isRunning) "🚀 $botName activated! Sniping cent scalps." else "⏸️ $botName paused.",
                pnlDelta = null,
                isWin = false
            )
        )
    }

    suspend fun saveBot(bot: BotConfig) {
        if (bot.id == 0L) {
            botDao.insertBot(bot)
        } else {
            botDao.updateBot(bot)
        }
    }

    suspend fun resetWalletAndStats() {
        tradeDao.clearAllTrades()
        tradeDao.clearLogs()
        _walletBalance.value = 1000.0
        _totalCentsGrabbed.value = 0.0
        prefs.edit()
            .putFloat("wallet_balance", 1000.0f)
            .putFloat("total_cents_grabbed", 0.0f)
            .apply()
    }

    private fun updateWalletBalance(delta: Double) {
        val newBal = (_walletBalance.value + delta).coerceAtLeast(0.0)
        _walletBalance.value = newBal
        prefs.edit().putFloat("wallet_balance", newBal.toFloat()).apply()
    }

    private fun updateTotalCentsGrabbed(delta: Double) {
        val newTotal = (_totalCentsGrabbed.value + delta).coerceAtLeast(0.0)
        _totalCentsGrabbed.value = newTotal
        prefs.edit().putFloat("total_cents_grabbed", newTotal.toFloat()).apply()
    }

    // Transaction Settings State & Persistence
    private val _transactionSettings = MutableStateFlow(
        TransactionSettings(
            defaultTradeSizeUsd = prefs.getFloat("setting_trade_size", 25.0f).toDouble(),
            maxSlippagePercent = prefs.getFloat("setting_slippage", 0.5f).toDouble(),
            defaultTakeProfitCents = prefs.getFloat("setting_take_profit", 0.35f).toDouble(),
            defaultStopLossPercent = prefs.getFloat("setting_stop_loss", 1.8f).toDouble(),
            maxConcurrentPositions = prefs.getInt("setting_max_positions", 3),
            autoReinvestProfits = prefs.getBoolean("setting_auto_reinvest", true),
            hapticFeedbackEnabled = prefs.getBoolean("setting_haptics", true),
            executionMode = prefs.getString("setting_exec_mode", "MARKET_SNIPE") ?: "MARKET_SNIPE",
            gasPriority = prefs.getString("setting_gas_priority", "FAST_TURBO") ?: "FAST_TURBO",
            highValueConfirmThreshold = prefs.getFloat("setting_confirm_thresh", 100.0f).toDouble(),
            antiFrontRunningGuard = prefs.getBoolean("setting_anti_frontrun", true),
            activeChain = try {
                BlockchainNetwork.valueOf(prefs.getString("setting_active_chain", BlockchainNetwork.SOLANA.name) ?: BlockchainNetwork.SOLANA.name)
            } catch (e: Exception) {
                BlockchainNetwork.SOLANA
            },
            autoBridgeCrossChain = prefs.getBoolean("setting_auto_bridge", true),
            chainGasMultiplier = prefs.getFloat("setting_chain_gas_multiplier", 1.25f).toDouble(),
            maxGasFeeGwei = prefs.getFloat("setting_max_gas_gwei", 25.0f).toDouble(),
            preferredMobileMoneyProvider = try {
                MobileMoneyProvider.valueOf(prefs.getString("setting_momo_provider", MobileMoneyProvider.MTN_MOMO.name) ?: MobileMoneyProvider.MTN_MOMO.name)
            } catch (e: Exception) {
                MobileMoneyProvider.MTN_MOMO
            },
            mobileMoneyPhoneNumber = prefs.getString("setting_momo_phone", "+256 772 884 921") ?: "+256 772 884 921",
            mobileMoneyAccountName = prefs.getString("setting_momo_name", "Mudantale Alpha") ?: "Mudantale Alpha",
            autoCashoutCentsToMoMo = prefs.getBoolean("setting_momo_autocashout", false),
            autoCashoutThresholdUsd = prefs.getFloat("setting_momo_autocashout_thresh", 5.0f).toDouble()
        )
    )
    val transactionSettings: StateFlow<TransactionSettings> = _transactionSettings.asStateFlow()

    fun saveTransactionSettings(settings: TransactionSettings) {
        _transactionSettings.value = settings
        prefs.edit()
            .putFloat("setting_trade_size", settings.defaultTradeSizeUsd.toFloat())
            .putFloat("setting_slippage", settings.maxSlippagePercent.toFloat())
            .putFloat("setting_take_profit", settings.defaultTakeProfitCents.toFloat())
            .putFloat("setting_stop_loss", settings.defaultStopLossPercent.toFloat())
            .putInt("setting_max_positions", settings.maxConcurrentPositions)
            .putBoolean("setting_auto_reinvest", settings.autoReinvestProfits)
            .putBoolean("setting_haptics", settings.hapticFeedbackEnabled)
            .putString("setting_exec_mode", settings.executionMode)
            .putString("setting_gas_priority", settings.gasPriority)
            .putFloat("setting_confirm_thresh", settings.highValueConfirmThreshold.toFloat())
            .putBoolean("setting_anti_frontrun", settings.antiFrontRunningGuard)
            .putString("setting_active_chain", settings.activeChain.name)
            .putBoolean("setting_auto_bridge", settings.autoBridgeCrossChain)
            .putFloat("setting_chain_gas_multiplier", settings.chainGasMultiplier.toFloat())
            .putFloat("setting_max_gas_gwei", settings.maxGasFeeGwei.toFloat())
            .putString("setting_momo_provider", settings.preferredMobileMoneyProvider.name)
            .putString("setting_momo_phone", settings.mobileMoneyPhoneNumber)
            .putString("setting_momo_name", settings.mobileMoneyAccountName)
            .putBoolean("setting_momo_autocashout", settings.autoCashoutCentsToMoMo)
            .putFloat("setting_momo_autocashout_thresh", settings.autoCashoutThresholdUsd.toFloat())
            .apply()
    }

    // User Profile State & Persistence
    private val _userProfile = MutableStateFlow(
        UserProfile(
            displayName = prefs.getString("user_display_name", "Mudantale Alpha Trader") ?: "Mudantale Alpha Trader",
            email = prefs.getString("user_email", "www.mudantale@gmail.com") ?: "www.mudantale@gmail.com",
            accountId = _accountId.value,
            tier = "Quantum Exa VIP",
            memberSince = "Nov 2026",
            avatarEmoji = "⚡",
            defaultMoMoProvider = try {
                MobileMoneyProvider.valueOf(prefs.getString("user_momo_provider", MobileMoneyProvider.MTN_MOMO.name) ?: MobileMoneyProvider.MTN_MOMO.name)
            } catch (e: Exception) {
                MobileMoneyProvider.MTN_MOMO
            },
            defaultMoMoNumber = prefs.getString("user_momo_phone", "+256 772 884 921") ?: "+256 772 884 921",
            preferredSettlementChain = try {
                BlockchainNetwork.valueOf(prefs.getString("user_chain", BlockchainNetwork.SOLANA.name) ?: BlockchainNetwork.SOLANA.name)
            } catch (e: Exception) {
                BlockchainNetwork.SOLANA
            }
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        _accountId.value = profile.accountId
        prefs.edit()
            .putString("user_display_name", profile.displayName)
            .putString("user_email", profile.email)
            .putString("user_account_id", profile.accountId)
            .putString("user_momo_provider", profile.defaultMoMoProvider.name)
            .putString("user_momo_phone", profile.defaultMoMoNumber)
            .putString("user_chain", profile.preferredSettlementChain.name)
            .apply()
    }

    suspend fun depositFunds(amount: Double) {
        if (amount <= 0) return
        updateWalletBalance(amount)
        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = "USD",
                action = "DEPOSIT",
                message = "💰 Added +$${String.format("%.2f", amount)} USD to paper balance. New balance: $${String.format("%.2f", _walletBalance.value)}.",
                pnlDelta = amount,
                isWin = true
            )
        )
    }

    // ==========================================
    // MOBILE MONEY (MTN & AIRTEL) TRANSACTION FLOWS
    // ==========================================

    suspend fun depositViaMobileMoney(
        provider: MobileMoneyProvider,
        phoneNumber: String,
        amountUsd: Double
    ): PaymentTransactionEntity {
        val randomDigits = Random.nextInt(100000, 999999)
        val prefix = if (provider == MobileMoneyProvider.MTN_MOMO) "MTN-MOMO" else "AIRTEL"
        val ref = "$prefix-$randomDigits"
        val localAmount = amountUsd * provider.fxRateToUsd

        val entity = PaymentTransactionEntity(
            transactionRef = ref,
            channelType = PaymentChannelType.MOBILE_MONEY,
            providerOrChain = provider.displayName,
            direction = PaymentDirection.DEPOSIT,
            amountUsd = amountUsd,
            localAmount = localAmount,
            localCurrency = provider.currencyCode,
            accountOrAddress = phoneNumber,
            status = PaymentStatus.SETTLED,
            notes = "Instant Mobile Money Cash-In via ${provider.dialCode} push authorization",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(entity)
        updateWalletBalance(amountUsd)

        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = "MOMO",
                action = "MOMO_DEPOSIT",
                message = "📱 ${provider.shortName} Deposit: +$${String.format("%.2f", amountUsd)} (≈ ${String.format("%,.0f", localAmount)} ${provider.currencyCode}) from $phoneNumber. Ref: $ref.",
                pnlDelta = amountUsd,
                isWin = true
            )
        )
        return entity
    }

    suspend fun withdrawToMobileMoney(
        provider: MobileMoneyProvider,
        phoneNumber: String,
        amountUsd: Double
    ): PaymentTransactionEntity? {
        if (_isAccountLocked.value || _walletBalance.value < amountUsd || amountUsd <= 0.0) {
            return null
        }

        val randomDigits = Random.nextInt(100000, 999999)
        val prefix = if (provider == MobileMoneyProvider.MTN_MOMO) "MTN-OUT" else "AIRTEL-OUT"
        val ref = "$prefix-$randomDigits"
        val localAmount = amountUsd * provider.fxRateToUsd

        val entity = PaymentTransactionEntity(
            transactionRef = ref,
            channelType = PaymentChannelType.MOBILE_MONEY,
            providerOrChain = provider.displayName,
            direction = PaymentDirection.WITHDRAWAL,
            amountUsd = amountUsd,
            localAmount = localAmount,
            localCurrency = provider.currencyCode,
            accountOrAddress = phoneNumber,
            status = PaymentStatus.SETTLED,
            notes = "Direct cashout payout to ${provider.shortName} mobile wallet",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(entity)
        updateWalletBalance(-amountUsd)

        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = "MOMO",
                action = "MOMO_WITHDRAWAL",
                message = "💸 ${provider.shortName} Payout: -$${String.format("%.2f", amountUsd)} (≈ ${String.format("%,.0f", localAmount)} ${provider.currencyCode}) to $phoneNumber. Ref: $ref.",
                pnlDelta = -amountUsd,
                isWin = false
            )
        )
        return entity
    }

    // ==========================================
    // BLOCKCHAIN TRANSACTION CHAIN FLOWS
    // ==========================================

    suspend fun depositViaBlockchain(
        chain: BlockchainNetwork,
        amountUsd: Double,
        senderAddress: String
    ): PaymentTransactionEntity {
        val hashChars = "0123456789abcdef"
        val randomHash = if (chain == BlockchainNetwork.SOLANA) {
            (1..44).map { "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".random() }.joinToString("")
        } else {
            "0x" + (1..64).map { hashChars.random() }.joinToString("")
        }

        val entity = PaymentTransactionEntity(
            transactionRef = randomHash.take(16) + "...",
            channelType = PaymentChannelType.BLOCKCHAIN_NETWORK,
            providerOrChain = "${chain.displayName} (${chain.tokenStandard})",
            direction = PaymentDirection.DEPOSIT,
            amountUsd = amountUsd,
            localAmount = null,
            localCurrency = null,
            accountOrAddress = senderAddress.ifBlank { "0x94b...f412" },
            status = PaymentStatus.CONFIRMED,
            notes = "On-chain liquidity bridge on ${chain.displayName} (Gas: ~$${chain.estimatedGasFeeUsd})",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(entity)
        updateWalletBalance(amountUsd)

        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = chain.symbol,
                action = "CHAIN_DEPOSIT",
                message = "⛓️ ${chain.displayName} Bridge: +$${String.format("%.2f", amountUsd)} USDT on ${chain.tokenStandard}. Tx: ${entity.transactionRef}",
                pnlDelta = amountUsd,
                isWin = true
            )
        )
        return entity
    }

    suspend fun withdrawToBlockchain(
        chain: BlockchainNetwork,
        destinationAddress: String,
        amountUsd: Double
    ): PaymentTransactionEntity? {
        if (_isAccountLocked.value || _walletBalance.value < amountUsd || amountUsd <= 0.0) {
            return null
        }

        val hashChars = "0123456789abcdef"
        val randomHash = if (chain == BlockchainNetwork.SOLANA) {
            (1..44).map { "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".random() }.joinToString("")
        } else {
            "0x" + (1..64).map { hashChars.random() }.joinToString("")
        }

        val entity = PaymentTransactionEntity(
            transactionRef = randomHash.take(16) + "...",
            channelType = PaymentChannelType.BLOCKCHAIN_NETWORK,
            providerOrChain = "${chain.displayName} (${chain.tokenStandard})",
            direction = PaymentDirection.WITHDRAWAL,
            amountUsd = amountUsd,
            localAmount = null,
            localCurrency = null,
            accountOrAddress = destinationAddress,
            status = PaymentStatus.CONFIRMED,
            notes = "On-chain withdrawal to self-custody wallet",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(entity)
        updateWalletBalance(-amountUsd)

        tradeDao.insertLog(
            BotActivityLog(
                botId = 0L,
                symbol = chain.symbol,
                action = "CHAIN_WITHDRAWAL",
                message = "⛓️ ${chain.displayName} Withdrawal: -$${String.format("%.2f", amountUsd)} to ${destinationAddress.take(10)}... Tx: ${entity.transactionRef}",
                pnlDelta = -amountUsd,
                isWin = false
            )
        )
        return entity
    }
}

