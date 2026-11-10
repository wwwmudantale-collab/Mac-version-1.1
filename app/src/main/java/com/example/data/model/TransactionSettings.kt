package com.example.data.model

data class TransactionSettings(
    val defaultTradeSizeUsd: Double = 25.0,
    val maxSlippagePercent: Double = 0.5,
    val defaultTakeProfitCents: Double = 0.35, // in dollars (0.35 = 35¢)
    val defaultStopLossPercent: Double = 1.8,
    val maxConcurrentPositions: Int = 3,
    val autoReinvestProfits: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val executionMode: String = "MARKET_SNIPE", // "MARKET_SNIPE" vs "LIMIT_MAKER"
    val gasPriority: String = "FAST_TURBO", // "FAST_TURBO" vs "STANDARD" vs "WARP_MEV"
    val highValueConfirmThreshold: Double = 100.0,
    val antiFrontRunningGuard: Boolean = true,

    // Transaction Chain Configuration
    val activeChain: BlockchainNetwork = BlockchainNetwork.SOLANA,
    val autoBridgeCrossChain: Boolean = true,
    val chainGasMultiplier: Double = 1.25,
    val maxGasFeeGwei: Double = 25.0,

    // Mobile Money Settings (MTN & Airtel)
    val preferredMobileMoneyProvider: MobileMoneyProvider = MobileMoneyProvider.MTN_MOMO,
    val mobileMoneyPhoneNumber: String = "+256 772 884 921",
    val mobileMoneyAccountName: String = "Mudantale Alpha",
    val autoCashoutCentsToMoMo: Boolean = false,
    val autoCashoutThresholdUsd: Double = 5.0 // Trigger auto payout to MoMo when profit >= $5.00
)

data class UserProfile(
    val displayName: String = "Mudantale Alpha Trader",
    val email: String = "www.mudantale@gmail.com",
    val accountId: String = "CS-8842-EXA",
    val tier: String = "Quantum Exa VIP",
    val memberSince: String = "Nov 2026",
    val avatarEmoji: String = "⚡",
    val defaultMoMoProvider: MobileMoneyProvider = MobileMoneyProvider.MTN_MOMO,
    val defaultMoMoNumber: String = "+256 772 884 921",
    val preferredSettlementChain: BlockchainNetwork = BlockchainNetwork.SOLANA
)
