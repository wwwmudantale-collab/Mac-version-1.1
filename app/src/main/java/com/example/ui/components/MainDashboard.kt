package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PaperTrade
import com.example.data.model.AiTradeSignal
import com.example.data.model.CryptoCoin
import com.example.ui.CoinFilterCategory
import com.example.ui.MainViewModel
import com.example.ui.screens.CoinMarketCard
import com.example.ui.theme.CryptoCyanAccent
import com.example.ui.theme.CryptoGoldPenny
import com.example.ui.theme.CryptoLossRed
import com.example.ui.theme.CryptoProfitGreen
import com.example.ui.theme.CryptoPurpleAI

/**
 * MainDashboard: Jetpack Compose component that unites real-time cryptocurrency market
 * telemetry, live technical price feeds, and active automated AI trade signals.
 */
@Composable
fun MainDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToMarket: () -> Unit = {},
    onNavigateToBots: () -> Unit = {},
    onNavigateToSignals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val isConnecting by viewModel.isLiveConnecting.collectAsState()
    val activeSignals by viewModel.activeSignals.collectAsState()
    val openTrades by viewModel.openTrades.collectAsState()
    val allBots by viewModel.allBots.collectAsState()
    val activeBotsCount by viewModel.activeBotsCount.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val totalCentsGrabbed by viewModel.totalCentsGrabbed.collectAsState()
    val executionSpeedOps by viewModel.executionSpeedOps.collectAsState()
    val accountId by viewModel.accountId.collectAsState()
    val isAccountLocked by viewModel.isAccountLocked.collectAsState()
    val selectedCoin by viewModel.selectedCoin.collectAsState()
    val aiSignalState by viewModel.aiSignalState.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var dashboardFilter by remember { mutableStateOf(CoinFilterCategory.ALL) }

    val filteredCoins = remember(coins, dashboardFilter) {
        when (dashboardFilter) {
            CoinFilterCategory.ALL -> coins
            CoinFilterCategory.PENNY_COINS -> coins.filter { it.isPennyCoin }
            CoinFilterCategory.TOP_GAINERS -> coins.filter { it.priceChangePercentage24h > 3.0 }
            CoinFilterCategory.MAJORS -> coins.filter { !it.isPennyCoin }
        }
    }

    if (showDetailDialog && selectedCoin != null) {
        CoinDetailDialog(
            coin = selectedCoin!!,
            aiSignalState = aiSignalState,
            onDismiss = { showDetailDialog = false },
            onAnalyzeAi = { viewModel.generateAiSignalForCoin(selectedCoin!!) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("main_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // 1. Dashboard Top Command Bar
        item {
            DashboardHeader(
                userProfile = userProfile,
                activeBotsCount = activeBotsCount,
                isConnecting = isConnecting,
                onRefresh = { viewModel.refreshMarket() },
                onOpenProfile = onNavigateToProfile
            )
        }

        // 1b. Direct Emergency Account ID Lock (No Company Confirmation Required)
        item {
            AccountIdLockCard(
                accountId = accountId,
                isLocked = isAccountLocked,
                onLock = {
                    viewModel.lockAccountId()
                    Toast.makeText(
                        context,
                        "🔒 Account ID $accountId locked immediately! Bots frozen without external company confirmation.",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onUnlock = {
                    viewModel.unlockAccountId()
                    Toast.makeText(
                        context,
                        "🔓 Account ID $accountId unlocked.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // 2. Heads-Up Display (HUD) Telemetry Card
        item {
            DashboardHudCard(
                totalCentsGrabbed = totalCentsGrabbed,
                walletBalance = walletBalance,
                activeBotsCount = activeBotsCount,
                openPositionsCount = openTrades.size,
                allBots = allBots
            )
        }

        // 2b. Quantum Warp Speed Accelerator (> 1,000,000,000,000,000,000 / sec)
        item {
            HyperSpeedWarpCard(
                executionSpeedOps = executionSpeedOps,
                onToggleWarp = { viewModel.toggleWarpSpeed(it) },
                onSetSpeed = { viewModel.setExecutionSpeed(it) }
            )
        }

        // 3. Active Trade Signals Carousel
        item {
            ActiveTradeSignalsSection(
                signals = activeSignals,
                onExecuteSignal = { signal ->
                    viewModel.quickExecuteSignal(signal)
                    Toast.makeText(
                        context,
                        "⚡ Snipe order executed for ${signal.symbol}! Target: +${String.format("%.1f¢", signal.targetProfitCents * 100)}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onViewAllSignals = onNavigateToSignals
            )
        }

        // 4. Live Open Positions Alert (if any position is currently capturing cents)
        if (openTrades.isNotEmpty()) {
            item {
                ActivePositionsSnapshot(
                    openTrades = openTrades,
                    coins = coins,
                    onCloseTrade = { tradeId -> viewModel.manualCloseTrade(tradeId) }
                )
            }
        }

        // 5. Real-Time Market Feed Header & Category Filters
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = CryptoProfitGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Real-Time Market Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "1.5s Live Ticks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoProfitGreen,
                        modifier = Modifier
                            .background(CryptoProfitGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = dashboardFilter == CoinFilterCategory.ALL,
                            onClick = { dashboardFilter = CoinFilterCategory.ALL },
                            label = { Text("All Coins") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CryptoCyanAccent,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("dashboard_filter_all")
                        )
                    }
                    item {
                        FilterChip(
                            selected = dashboardFilter == CoinFilterCategory.PENNY_COINS,
                            onClick = { dashboardFilter = CoinFilterCategory.PENNY_COINS },
                            label = { Text("⚡ Penny Scalps") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CryptoGoldPenny,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("dashboard_filter_penny")
                        )
                    }
                    item {
                        FilterChip(
                            selected = dashboardFilter == CoinFilterCategory.TOP_GAINERS,
                            onClick = { dashboardFilter = CoinFilterCategory.TOP_GAINERS },
                            label = { Text("🔥 Top Gainers") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CryptoProfitGreen,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("dashboard_filter_gainers")
                        )
                    }
                    item {
                        FilterChip(
                            selected = dashboardFilter == CoinFilterCategory.MAJORS,
                            onClick = { dashboardFilter = CoinFilterCategory.MAJORS },
                            label = { Text("💎 Major Drivers") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("dashboard_filter_majors")
                        )
                    }
                }
            }
        }

        // 6. Real-Time Coin Ticker List
        items(filteredCoins, key = { it.symbol }) { coin ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CoinMarketCard(
                    coin = coin,
                    onClick = {
                        viewModel.selectCoin(coin)
                        showDetailDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardHeader(
    userProfile: com.example.data.model.UserProfile,
    activeBotsCount: Int,
    isConnecting: Boolean,
    onRefresh: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (activeBotsCount > 0) CryptoProfitGreen else CryptoGoldPenny)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COINSNIPER AI TERMINAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyanAccent,
                    letterSpacing = 1.2.sp
                )
            }
            Text(
                text = "Trading Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("dashboard_refresh_button")
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = CryptoCyanAccent
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Market",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("dashboard_profile_button")
            ) {
                Text(
                    text = userProfile.avatarEmoji,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun DashboardHudCard(
    totalCentsGrabbed: Double,
    walletBalance: Double,
    activeBotsCount: Int,
    openPositionsCount: Int,
    allBots: List<com.example.data.local.BotConfig>
) {
    val totalTrades = allBots.sumOf { it.totalTradesCount }
    val totalWins = allBots.sumOf { it.winCount }
    val winRate = if (totalTrades > 0) (totalWins.toDouble() / totalTrades) * 100.0 else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_hud_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D182E)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CryptoCyanAccent.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Main Cent Grab Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL CENTS GRABBED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGoldPenny
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("+$%.2f", totalCentsGrabbed),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CryptoProfitGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("(+%.0f¢)", totalCentsGrabbed * 100.0),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGoldPenny,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (activeBotsCount > 0) CryptoProfitGreen.copy(alpha = 0.15f) else CryptoGoldPenny.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (activeBotsCount > 0) CryptoProfitGreen.copy(alpha = 0.4f) else CryptoGoldPenny.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (activeBotsCount > 0) CryptoProfitGreen else CryptoGoldPenny,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeBotsCount > 0) "$activeBotsCount BOTS ON" else "STANDBY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeBotsCount > 0) CryptoProfitGreen else CryptoGoldPenny
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF081020), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Paper Wallet", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("$%,.2f", walletBalance),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Open Trades", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$openPositionsCount active",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (openPositionsCount > 0) CryptoCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Win Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format("%.1f%%", winRate)} ($totalWins/$totalTrades)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoProfitGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFCC00).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📱 MTN & Airtel MoMo Ready", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCC00))
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CryptoCyanAccent.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⛓️ Solana / BSC Chain", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CryptoCyanAccent)
                }
            }
        }
    }
}

@Composable
fun ActiveTradeSignalsSection(
    signals: List<AiTradeSignal>,
    onExecuteSignal: (AiTradeSignal) -> Unit,
    onViewAllSignals: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CryptoCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active AI Trade Signals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "View All",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyanAccent,
                modifier = Modifier
                    .clickable(onClick = onViewAllSignals)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (signals.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "AI engine scanning market order books for cent-scalping signals...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(signals, key = { it.symbol }) { signal ->
                    ActiveSignalCard(
                        signal = signal,
                        onExecute = { onExecuteSignal(signal) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSignalCard(
    signal: AiTradeSignal,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .testTag("active_signal_card_${signal.symbol.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF101B30)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (signal.action) {
                "STRONG_BUY", "SCALP_LONG" -> CryptoProfitGreen.copy(alpha = 0.4f)
                "TAKE_PROFIT" -> CryptoGoldPenny.copy(alpha = 0.4f)
                else -> CryptoCyanAccent.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Symbol & Action Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${signal.symbol}/USDT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = signal.action.replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (signal.action) {
                        "STRONG_BUY", "SCALP_LONG" -> CryptoProfitGreen
                        "TAKE_PROFIT" -> CryptoGoldPenny
                        else -> CryptoCyanAccent
                    },
                    modifier = Modifier
                        .background(
                            (when (signal.action) {
                                "STRONG_BUY", "SCALP_LONG" -> CryptoProfitGreen
                                "TAKE_PROFIT" -> CryptoGoldPenny
                                else -> CryptoCyanAccent
                            }).copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AI Confidence",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${signal.confidenceScore}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyanAccent
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { signal.confidenceScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CryptoCyanAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Target profit and stop-loss
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A1324), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Target", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "+${String.format("%.1f¢", signal.targetProfitCents * 100)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoProfitGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stop Loss", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "-${String.format("%.1f%%", signal.suggestedStopLoss)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoLossRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = signal.reasoning,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick execute scalp action
            Button(
                onClick = onExecute,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (signal.action) {
                        "STRONG_BUY", "SCALP_LONG" -> CryptoProfitGreen
                        else -> CryptoCyanAccent
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_signal_${signal.symbol.lowercase()}")
            ) {
                Icon(
                    Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Quick Snipe Scalp ($25)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ActivePositionsSnapshot(
    openTrades: List<PaperTrade>,
    coins: List<CryptoCoin>,
    onCloseTrade: (Long) -> Unit
) {
    val topTrade = openTrades.first()
    val currentCoin = coins.firstOrNull { it.symbol.equals(topTrade.symbol, ignoreCase = true) }
    val currentPrice = currentCoin?.currentPrice ?: topTrade.entryPrice
    val pnlUsd = (currentPrice - topTrade.entryPrice) * topTrade.coinQuantity
    val pnlPercent = if (topTrade.entryPrice > 0) ((currentPrice - topTrade.entryPrice) / topTrade.entryPrice) * 100.0 else 0.0
    val isWin = pnlUsd >= 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_active_position_alert"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWin) Color(0xFF09291A) else Color(0xFF290D14)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isWin) CryptoProfitGreen.copy(alpha = 0.5f) else CryptoLossRed.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isWin) CryptoProfitGreen else CryptoLossRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE ACTIVE POSITION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWin) CryptoProfitGreen else CryptoLossRed
                    )
                }
                Text(
                    text = "${topTrade.symbol} • Size: $${String.format("%.0f", topTrade.amountUsd)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Live Mark: ${PaperTrade.formatPrice(currentPrice)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isWin) "+$${String.format("%.2f", pnlUsd)} (+${String.format("%.1f¢", pnlUsd * 100)})" else "-$${String.format("%.2f", -pnlUsd)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (isWin) CryptoProfitGreen else CryptoLossRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onCloseTrade(topTrade.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWin) CryptoProfitGreen else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isWin) "Grab Cents" else "Close",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWin) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * HyperSpeedWarpCard: Overclocking accelerator that dynamically boosts bot execution
 * and market tick evaluation when speed exceeds 1,000,000,000,000,000,000 (10^18) per second.
 */
@Composable
fun HyperSpeedWarpCard(
    executionSpeedOps: Double,
    onToggleWarp: (Boolean) -> Unit,
    onSetSpeed: (Double) -> Unit
) {
    val quintillionThreshold = 1_000_000_000_000_000_000.0
    val isWarpActive = executionSpeedOps >= quintillionThreshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("hyper_speed_warp_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarpActive) Color(0xFF07192C) else Color(0xFF0E1726)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isWarpActive) CryptoCyanAccent else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Overclock Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isWarpActive) CryptoCyanAccent else CryptoGoldPenny)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "QUANTUM OVERCLOCK ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isWarpActive) CryptoCyanAccent else CryptoGoldPenny,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isWarpActive) "⚡ Warp Speed Active (>10¹⁸ / sec)" else "High-Frequency Speed Control",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                androidx.compose.material3.Switch(
                    checked = isWarpActive,
                    onCheckedChange = onToggleWarp,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CryptoCyanAccent,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("toggle_warp_speed_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Throughput Display Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF060D1A), RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isWarpActive) CryptoProfitGreen.copy(alpha = 0.4f) else Color(0xFF1E293B),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "THROUGHPUT VELOCITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (isWarpActive) "180ms TICK INTERVAL" else "2500ms INTERVAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isWarpActive) CryptoProfitGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    (if (isWarpActive) CryptoProfitGreen else MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Formatted speed number (scientific + full quintillions)
                    if (isWarpActive) {
                        Text(
                            text = String.format("%,.0f ops/second", executionSpeedOps),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CryptoCyanAccent
                        )
                        Text(
                            text = "⚡ Speed: ${String.format("%.2f", executionSpeedOps / 1e18)} × 10¹⁸ per second (Quantum Exa-Rate)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoProfitGreen
                        )
                    } else {
                        Text(
                            text = String.format("%,.0f ops/second", executionSpeedOps),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Standard speed. Toggle switch to ramp past 1,000,000,000,000,000,000/s.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speed Presets Chips
            Text(
                text = "Preset Execution Speed:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = executionSpeedOps < 1_000_000.0,
                    onClick = { onSetSpeed(10_000.0) },
                    label = { Text("Standard", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = executionSpeedOps in 1_000_000.0..999_999_999_999_999_999.0,
                    onClick = { onSetSpeed(10_000_000.0) },
                    label = { Text("HFT Turbo", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoGoldPenny,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = executionSpeedOps >= quintillionThreshold,
                    onClick = { onSetSpeed(1_500_000_000_000_000_000.0) },
                    label = { Text("⚡ 1.5×10¹⁸/s", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoCyanAccent,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("preset_quintillion_speed")
                )
            }
        }
    }
}

/**
 * AccountIdLockCard: Allows the user to instantly lock the Account ID and freeze all
 * autonomous bot order execution with a single tap, without requiring confirmation
 * or authorization from an external account company or exchange.
 */
@Composable
fun AccountIdLockCard(
    accountId: String,
    isLocked: Boolean,
    onLock: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("account_id_lock_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFF2B0A11) else Color(0xFF0F1A2D)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isLocked) CryptoLossRed else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Security Status & Account ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLocked) CryptoLossRed.copy(alpha = 0.2f) else CryptoCyanAccent.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isLocked) CryptoLossRed else CryptoCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isLocked) "ACCOUNT ID LOCKED" else "ACCOUNT SECURITY CONTROL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLocked) CryptoLossRed else CryptoCyanAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Account: $accountId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = if (isLocked) "FROZEN" else "ACTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLocked) CryptoLossRed else CryptoProfitGreen,
                    modifier = Modifier
                        .background(
                            (if (isLocked) CryptoLossRed else CryptoProfitGreen).copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanatory Text
            Text(
                text = if (isLocked) {
                    "Trading is completely frozen for $accountId. All running bots have been stopped locally. No order placements or cent scalps can execute until unlocked."
                } else {
                    "Emergency Kill Switch: Tap below to lock Account ID ($accountId) immediately and halt all bot trading without needing confirmation from the account company."
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            if (isLocked) {
                Button(
                    onClick = onUnlock,
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoCyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_account_id_button")
                ) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unlock Account ID ($accountId)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            } else {
                Button(
                    onClick = onLock,
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoLossRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lock_account_id_button")
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lock Account ID (Direct Emergency Lock)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


