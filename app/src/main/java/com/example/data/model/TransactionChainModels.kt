package com.example.data.model

/**
 * Supported Blockchain Transaction Networks
 */
enum class BlockchainNetwork(
    val id: String,
    val displayName: String,
    val symbol: String,
    val tokenStandard: String,
    val colorHex: Long,
    val avgBlockTimeSec: Double,
    val estimatedGasFeeUsd: Double,
    val explorerUrlPrefix: String,
    val isRecommendedForCentScalp: Boolean
) {
    SOLANA(
        id = "SOL",
        displayName = "Solana",
        symbol = "SOL",
        tokenStandard = "SPL Token",
        colorHex = 0xFF14F195,
        avgBlockTimeSec = 0.4,
        estimatedGasFeeUsd = 0.0005,
        explorerUrlPrefix = "https://solscan.io/tx/",
        isRecommendedForCentScalp = true
    ),
    BINANCE_SMART_CHAIN(
        id = "BSC",
        displayName = "BNB Chain",
        symbol = "BNB",
        tokenStandard = "BEP-20",
        colorHex = 0xFFF0B90B,
        avgBlockTimeSec = 3.0,
        estimatedGasFeeUsd = 0.08,
        explorerUrlPrefix = "https://bscscan.com/tx/",
        isRecommendedForCentScalp = true
    ),
    TRON(
        id = "TRX",
        displayName = "TRON",
        symbol = "TRX",
        tokenStandard = "TRC-20",
        colorHex = 0xFFFF001F,
        avgBlockTimeSec = 1.5,
        estimatedGasFeeUsd = 0.02,
        explorerUrlPrefix = "https://tronscan.org/#/transaction/",
        isRecommendedForCentScalp = true
    ),
    BASE(
        id = "BASE",
        displayName = "Base L2",
        symbol = "ETH",
        tokenStandard = "ERC-20",
        colorHex = 0xFF0052FF,
        avgBlockTimeSec = 0.8,
        estimatedGasFeeUsd = 0.01,
        explorerUrlPrefix = "https://basescan.org/tx/",
        isRecommendedForCentScalp = true
    ),
    ETHEREUM(
        id = "ETH",
        displayName = "Ethereum",
        symbol = "ETH",
        tokenStandard = "ERC-20",
        colorHex = 0xFF627EEA,
        avgBlockTimeSec = 12.0,
        estimatedGasFeeUsd = 1.65,
        explorerUrlPrefix = "https://etherscan.io/tx/",
        isRecommendedForCentScalp = false
    ),
    POLYGON(
        id = "POL",
        displayName = "Polygon",
        symbol = "POL",
        tokenStandard = "ERC-20",
        colorHex = 0xFF8247E5,
        avgBlockTimeSec = 2.0,
        estimatedGasFeeUsd = 0.015,
        explorerUrlPrefix = "https://polygonscan.com/tx/",
        isRecommendedForCentScalp = true
    )
}

/**
 * Mobile Money Telecom Networks (MTN and Airtel)
 */
enum class MobileMoneyProvider(
    val id: String,
    val displayName: String,
    val shortName: String,
    val dialCode: String,
    val brandColorHex: Long,
    val brandSecondaryColorHex: Long,
    val currencyCode: String,
    val fxRateToUsd: Double, // 1 USD in local shillings/units
    val operatorFeePercent: Double,
    val samplePrefixes: List<String>
) {
    MTN_MOMO(
        id = "MTN",
        displayName = "MTN Mobile Money",
        shortName = "MTN MoMo",
        dialCode = "*165#",
        brandColorHex = 0xFFFFCC00,
        brandSecondaryColorHex = 0xFF002F6C,
        currencyCode = "UGX",
        fxRateToUsd = 3750.0,
        operatorFeePercent = 0.5,
        samplePrefixes = listOf("077", "078", "076")
    ),
    AIRTEL_MONEY(
        id = "AIRTEL",
        displayName = "Airtel Money",
        shortName = "Airtel Money",
        dialCode = "*185#",
        brandColorHex = 0xFFE60000,
        brandSecondaryColorHex = 0xFFFFFFFF,
        currencyCode = "UGX",
        fxRateToUsd = 3750.0,
        operatorFeePercent = 0.5,
        samplePrefixes = listOf("075", "070", "074")
    )
}

/**
 * Channel types for deposits & withdrawals
 */
enum class PaymentChannelType {
    MOBILE_MONEY,
    BLOCKCHAIN_NETWORK
}

/**
 * Direction of transaction
 */
enum class PaymentDirection {
    DEPOSIT,
    WITHDRAWAL,
    AUTO_CASHOUT_CENT_PROFIT
}

/**
 * Lifecycle status of transaction
 */
enum class PaymentStatus {
    CONFIRMED,
    PENDING_USSD_PIN,
    PROCESSING,
    SETTLED,
    FAILED
}
