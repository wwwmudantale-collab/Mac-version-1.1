package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PaperTrade
import com.example.ui.MainViewModel
import com.example.ui.theme.CryptoCyanAccent
import com.example.ui.theme.CryptoGoldPenny
import com.example.ui.theme.CryptoLossRed
import com.example.ui.theme.CryptoProfitGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    val totalCentsGrabbed by viewModel.totalCentsGrabbed.collectAsState()
    val openTrades by viewModel.openTrades.collectAsState()
    val closedTrades by viewModel.closedTrades.collectAsState()
    val coins by viewModel.coins.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Paper Portfolio?") },
            text = { Text("This will reset your paper wallet back to $1,000.00 and clear past trade logs.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetWallet()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoLossRed)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PORTFOLIO & TRADES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Execution History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("reset_wallet_button")
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Reset Portfolio",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Wallet Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PAPER TRADING BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("$%,.2f", walletBalance),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CENTS HARVESTED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGoldPenny
                    )
                    Text(
                        text = String.format("+$%.2f (+%.0f¢)", totalCentsGrabbed, totalCentsGrabbed * 100.0),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoProfitGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs
        androidx.compose.material3.TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CryptoCyanAccent,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Active Positions (${openTrades.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_active_positions")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Closed Trades (${closedTrades.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_closed_trades")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // List
        if (selectedTabIndex == 0) {
            if (openTrades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = CryptoCyanAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No open positions right now.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When bots identify micro-cent scalping dips, they will enter positions automatically.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(openTrades, key = { it.id }) { trade ->
                        val currentCoin = coins.firstOrNull { it.symbol.equals(trade.symbol, ignoreCase = true) }
                        val currentPrice = currentCoin?.currentPrice ?: trade.entryPrice
                        val pnlUsd = (currentPrice - trade.entryPrice) * trade.coinQuantity
                        val pnlPercent = if (trade.entryPrice > 0) ((currentPrice - trade.entryPrice) / trade.entryPrice) * 100.0 else 0.0

                        OpenTradeCard(
                            trade = trade,
                            currentPrice = currentPrice,
                            pnlUsd = pnlUsd,
                            pnlPercent = pnlPercent,
                            onClose = { viewModel.manualCloseTrade(trade.id) }
                        )
                    }
                }
            }
        } else {
            if (closedTrades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CryptoProfitGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No closed trades yet.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Completed scalps and profit grabs will appear here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(closedTrades, key = { it.id }) { trade ->
                        ClosedTradeCard(trade = trade)
                    }
                }
            }
        }
    }
}

@Composable
fun OpenTradeCard(
    trade: PaperTrade,
    currentPrice: Double,
    pnlUsd: Double,
    pnlPercent: Double,
    onClose: () -> Unit
) {
    val isProfit = pnlUsd >= 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("open_trade_${trade.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isProfit) CryptoProfitGreen.copy(alpha = 0.4f) else CryptoLossRed.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CryptoCyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trade.symbol.take(3),
                            fontWeight = FontWeight.Bold,
                            color = CryptoCyanAccent,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${trade.symbol} • ${trade.strategyName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Entry: ${trade.formattedEntryPrice}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // P&L Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isProfit) "+$${String.format("%.2f", pnlUsd)} (+${String.format("%.1f¢", pnlUsd * 100)})" else "-$${String.format("%.2f", -pnlUsd)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isProfit) CryptoProfitGreen else CryptoLossRed
                    )
                    Text(
                        text = String.format("%+.2f%%", pnlPercent),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isProfit) CryptoProfitGreen else CryptoLossRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Mark: ${PaperTrade.formatPrice(currentPrice)} (Size: $${String.format("%.0f", trade.amountUsd)})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isProfit) CryptoProfitGreen else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("manual_close_button_${trade.id}")
                ) {
                    Text(
                        text = if (isProfit) "Take Profit" else "Close",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ClosedTradeCard(trade: PaperTrade) {
    val isWin = trade.pnlUsd >= 0.0
    val timeFormatted = trade.exitTimestamp?.let {
        SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(it))
    } ?: "Closed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isWin) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isWin) CryptoProfitGreen else CryptoLossRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${trade.symbol} (${trade.strategyName})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$timeFormatted • In: ${trade.formattedEntryPrice} Out: ${trade.formattedExitPrice}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isWin) "+$${String.format("%.2f", trade.pnlUsd)}" else "-$${String.format("%.2f", -trade.pnlUsd)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isWin) CryptoProfitGreen else CryptoLossRed
                )
                if (isWin) {
                    Text(
                        text = "+${String.format("%.1f¢ grabbed", trade.pnlUsd * 100.0)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGoldPenny
                    )
                } else {
                    Text(
                        text = String.format("%.2f%%", trade.pnlPercent),
                        fontSize = 10.sp,
                        color = CryptoLossRed
                    )
                }
            }
        }
    }
}
