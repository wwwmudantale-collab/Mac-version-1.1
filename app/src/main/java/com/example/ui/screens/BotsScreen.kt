package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.BotActivityLog
import com.example.data.local.BotConfig
import com.example.data.model.BotStrategy
import com.example.ui.MainViewModel
import com.example.ui.theme.CryptoCyanAccent
import com.example.ui.theme.CryptoGoldPenny
import com.example.ui.theme.CryptoLossRed
import com.example.ui.theme.CryptoProfitGreen
import com.example.ui.theme.CryptoPurpleAI

@Composable
fun BotsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allBots by viewModel.allBots.collectAsState()
    val activeBotsCount by viewModel.activeBotsCount.collectAsState()
    val totalCentsGrabbed by viewModel.totalCentsGrabbed.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    var editingBot by remember { mutableStateOf<BotConfig?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    if (editingBot != null) {
        BotEditDialog(
            bot = editingBot!!,
            onDismiss = { editingBot = null },
            onSave = { updated ->
                viewModel.saveBotConfig(updated)
                editingBot = null
            }
        )
    }

    if (showCreateDialog) {
        BotEditDialog(
            bot = BotConfig(
                id = 0L,
                name = "🤖 Custom Cent Bot",
                strategy = BotStrategy.CENT_SCALPER,
                targetCoins = "PEPE,DOGE,SHIB,BONK",
                tradeSizeUsd = 20.0,
                targetProfitCents = 0.30,
                stopLossPercent = 1.8,
                isRunning = true
            ),
            isNew = true,
            onDismiss = { showCreateDialog = false },
            onSave = { newBot ->
                viewModel.saveBotConfig(newBot)
                showCreateDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (activeBotsCount > 0) CryptoProfitGreen else CryptoGoldPenny)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeBotsCount > 0) "$activeBotsCount BOTS ACTIVELY SNIPING" else "BOTS IDLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeBotsCount > 0) CryptoProfitGreen else CryptoGoldPenny,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "AI Trading Bots",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CryptoCyanAccent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("create_new_bot_button")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Bot", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Live Performance Matrix Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TOTAL CENTS GRABBED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGoldPenny
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("+$%.2f", totalCentsGrabbed),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CryptoProfitGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("(+%.0f¢)", totalCentsGrabbed * 100.0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CryptoGoldPenny,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PAPER WALLET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("$%,.2f", walletBalance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                val totalTrades = allBots.sumOf { it.totalTradesCount }
                val totalWins = allBots.sumOf { it.winCount }
                val overallWinRate = if (totalTrades > 0) (totalWins.toDouble() / totalTrades) * 100.0 else 0.0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Trades: $totalTrades",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wins: $totalWins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CryptoProfitGreen
                    )
                    Text(
                        text = "Win Rate: ${String.format("%.1f%%", overallWinRate)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (overallWinRate >= 70) CryptoProfitGreen else CryptoCyanAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Configured AI Bots (${allBots.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(allBots, key = { it.id }) { bot ->
                BotCard(
                    bot = bot,
                    onToggle = { isRunning -> viewModel.toggleBot(bot.id, isRunning) },
                    onEdit = { editingBot = bot }
                )
            }

            // Live Activity Feed Ticker
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = CryptoGoldPenny,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-Time Bot Scalping Activity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (recentLogs.isEmpty()) {
                item {
                    Text(
                        text = "Bots are standing by. Toggle any bot ON above to start automated micro-cent scalping.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(recentLogs.take(15), key = { it.id }) { log ->
                    BotActivityRow(log = log)
                }
            }
        }
    }
}

@Composable
fun BotCard(
    bot: BotConfig,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bot_card_${bot.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bot.isRunning) Color(0xFF0C192E) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (bot.isRunning) CryptoProfitGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = bot.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (bot.isRunning) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CryptoProfitGreen,
                                modifier = Modifier
                                    .background(CryptoProfitGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = bot.strategy.displayName,
                        fontSize = 12.sp,
                        color = CryptoCyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_bot_button_${bot.id}")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Edit Bot Parameters",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = bot.isRunning,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CryptoProfitGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("toggle_bot_switch_${bot.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Strategy description
            Text(
                text = bot.strategy.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Coins Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Coins: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                bot.coinsList.forEach { coin ->
                    Text(
                        text = coin,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGoldPenny,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(CryptoGoldPenny.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parameter badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Trade Size", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.0f", bot.tradeSizeUsd)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Target Profit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+${String.format("%.0f¢", bot.targetProfitCents * 100)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoProfitGreen)
                }
                Column {
                    Text("Stop Loss", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-${String.format("%.1f%%", bot.stopLossPercent)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoLossRed)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Grabbed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+$${String.format("%.2f", bot.totalCentsGrabbedUsd)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoGoldPenny)
                }
            }
        }
    }
}

@Composable
fun BotActivityRow(log: BotActivityLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (log.action) {
                "PROFIT_GRAB" -> Color(0xFF09291A)
                "STOP_LOSS" -> Color(0xFF290D14)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (log.isWin) CryptoProfitGreen else CryptoCyanAccent
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.message,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun BotEditDialog(
    bot: BotConfig,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (BotConfig) -> Unit
) {
    var name by remember { mutableStateOf(bot.name) }
    var coins by remember { mutableStateOf(bot.targetCoins) }
    var tradeSizeStr by remember { mutableStateOf(bot.tradeSizeUsd.toString()) }
    var targetProfitCentsStr by remember { mutableStateOf((bot.targetProfitCents * 100).toInt().toString()) } // in cents
    var stopLossStr by remember { mutableStateOf(bot.stopLossPercent.toString()) }
    var selectedStrategy by remember { mutableStateOf(bot.strategy) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isNew) "Create Trading Bot" else "Configure Bot Parameters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bot Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = coins,
                    onValueChange = { coins = it },
                    label = { Text("Target Coins (comma separated)") },
                    placeholder = { Text("PEPE, DOGE, SHIB, SOL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tradeSizeStr,
                        onValueChange = { tradeSizeStr = it },
                        label = { Text("Size ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetProfitCentsStr,
                        onValueChange = { targetProfitCentsStr = it },
                        label = { Text("Profit (¢)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stopLossStr,
                        onValueChange = { stopLossStr = it },
                        label = { Text("Stop (%)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val size = tradeSizeStr.toDoubleOrNull() ?: 20.0
                            val cents = (targetProfitCentsStr.toDoubleOrNull() ?: 35.0) / 100.0
                            val stop = stopLossStr.toDoubleOrNull() ?: 1.8

                            onSave(
                                bot.copy(
                                    name = name,
                                    targetCoins = coins,
                                    tradeSizeUsd = size,
                                    targetProfitCents = cents,
                                    stopLossPercent = stop,
                                    strategy = selectedStrategy
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoProfitGreen),
                        modifier = Modifier.testTag("save_bot_button")
                    ) {
                        Text("Save Bot", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
