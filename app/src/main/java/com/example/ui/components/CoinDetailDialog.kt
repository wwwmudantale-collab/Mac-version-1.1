package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PaperTrade
import com.example.data.model.CryptoCoin
import com.example.ui.AiSignalUiState
import com.example.ui.theme.CryptoCyanAccent
import com.example.ui.theme.CryptoGoldPenny
import com.example.ui.theme.CryptoLossRed
import com.example.ui.theme.CryptoProfitGreen

@Composable
fun CoinDetailDialog(
    coin: CryptoCoin,
    aiSignalState: AiSignalUiState,
    onDismiss: () -> Unit,
    onAnalyzeAi: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("coin_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (coin.isPennyCoin) CryptoGoldPenny.copy(alpha = 0.2f) else CryptoCyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coin.symbol.take(3),
                                fontWeight = FontWeight.Bold,
                                color = if (coin.isPennyCoin) CryptoGoldPenny else CryptoCyanAccent,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = coin.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (coin.isPennyCoin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PENNY COIN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CryptoGoldPenny,
                                        modifier = Modifier
                                            .background(CryptoGoldPenny.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${coin.symbol} • RSI: ${String.format("%.1f", coin.rsi14)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_coin_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price & 24h Change
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Current Price",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = coin.formattedPrice,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = coin.formattedChange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (coin.isPositive) CryptoProfitGreen else CryptoLossRed,
                        modifier = Modifier
                            .background(
                                (if (coin.isPositive) CryptoProfitGreen else CryptoLossRed).copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sparkline Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        SparklineChart(
                            points = if (coin.sparkline.size > 2) coin.sparkline else listOf(coin.low24h, coin.currentPrice, coin.high24h),
                            lineColor = if (coin.isPositive) CryptoProfitGreen else CryptoLossRed,
                            modifier = Modifier.fillMaxWidth().height(94.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 24h High/Low Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("24h Low", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(PaperTrade.formatPrice(coin.low24h), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Scalp Volatility", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (coin.isPennyCoin) "High (Prime Cent Grab)" else "Moderate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CryptoGoldPenny)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("24h High", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(PaperTrade.formatPrice(coin.high24h), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Scalp Signal Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF131D33)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CryptoCyanAccent.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CryptoCyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Cent Scalp Signal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoCyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (aiSignalState) {
                            is AiSignalUiState.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = CryptoCyanAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini 3.8 Flash evaluating order book & micro-dip...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            is AiSignalUiState.Success -> {
                                val sig = aiSignalState.signal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sig.action.replace("_", " "),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = when (sig.action) {
                                            "STRONG_BUY", "SCALP_LONG" -> CryptoProfitGreen
                                            "TAKE_PROFIT" -> CryptoGoldPenny
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    Text(
                                        text = "Target: +${String.format("%.1f¢", sig.targetProfitCents * 100)} (${sig.confidenceScore}% conf)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = CryptoProfitGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sig.reasoning,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                            is AiSignalUiState.Error -> {
                                Text(
                                    text = "Signal: ${aiSignalState.message}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            is AiSignalUiState.Idle -> {
                                Text(
                                    text = "Tap below to request Gemini 3.8 Flash technical signal.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAnalyzeAi,
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoCyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_ai_signal_button")
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyze Micro-Cent Setup (AI)",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
