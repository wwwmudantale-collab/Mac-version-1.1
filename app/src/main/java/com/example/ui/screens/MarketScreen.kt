package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.ui.CoinFilterCategory
import com.example.ui.MainViewModel
import com.example.ui.components.CoinDetailDialog
import com.example.ui.components.SparklineChart
import com.example.ui.theme.CryptoCyanAccent
import com.example.ui.theme.CryptoGoldPenny
import com.example.ui.theme.CryptoLossRed
import com.example.ui.theme.CryptoProfitGreen

@Composable
fun MarketScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val coins by viewModel.coins.collectAsState()
    val isConnecting by viewModel.isLiveConnecting.collectAsState()
    val selectedCoin by viewModel.selectedCoin.collectAsState()
    val aiSignalState by viewModel.aiSignalState.collectAsState()
    val currentFilter by viewModel.coinFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalCentsGrabbed by viewModel.totalCentsGrabbed.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }

    // Filtered coins
    val filteredCoins = remember(coins, currentFilter, searchQuery) {
        coins.filter { coin ->
            val matchesSearch = searchQuery.isBlank() ||
                    coin.symbol.contains(searchQuery, ignoreCase = true) ||
                    coin.name.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (currentFilter) {
                CoinFilterCategory.ALL -> true
                CoinFilterCategory.PENNY_COINS -> coin.isPennyCoin
                CoinFilterCategory.TOP_GAINERS -> coin.priceChangePercentage24h > 3.0
                CoinFilterCategory.MAJORS -> !coin.isPennyCoin
            }

            matchesSearch && matchesCategory
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header & Live Pulse
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
                            .background(CryptoProfitGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COINSNIPER AI RADAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoProfitGreen,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Live Crypto Market",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { viewModel.refreshMarket() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("refresh_market_button")
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
        }

        // Cents Grabbed Header Metric Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B33)),
            border = androidx.compose.foundation.BorderStroke(1.dp, CryptoCyanAccent.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CryptoProfitGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("(+%.0f¢)", totalCentsGrabbed * 100.0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGoldPenny,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CryptoCyanAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CryptoCyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        viewModel.requestAiMarketDeepDive()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CryptoCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Scanner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoCyanAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search coin or ticker (e.g. PEPE, DOGE, SOL)...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("coin_search_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = CryptoCyanAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = currentFilter == CoinFilterCategory.ALL,
                    onClick = { viewModel.setCoinFilter(CoinFilterCategory.ALL) },
                    label = { Text("All Coins (${coins.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoCyanAccent,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("filter_all_coins")
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == CoinFilterCategory.PENNY_COINS,
                    onClick = { viewModel.setCoinFilter(CoinFilterCategory.PENNY_COINS) },
                    label = { Text("⚡ Penny Coins (Sub-Cent)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoGoldPenny,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("filter_penny_coins")
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == CoinFilterCategory.TOP_GAINERS,
                    onClick = { viewModel.setCoinFilter(CoinFilterCategory.TOP_GAINERS) },
                    label = { Text("🔥 Top Gainers") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoProfitGreen,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("filter_gainers")
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == CoinFilterCategory.MAJORS,
                    onClick = { viewModel.setCoinFilter(CoinFilterCategory.MAJORS) },
                    label = { Text("💎 Major Drivers") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_majors")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Coins List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredCoins, key = { it.symbol }) { coin ->
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
fun CoinMarketCard(
    coin: CryptoCoin,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("coin_card_${coin.symbol.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Coin Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (coin.isPennyCoin) CryptoGoldPenny.copy(alpha = 0.2f) else CryptoCyanAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = coin.symbol.take(3),
                        fontWeight = FontWeight.Bold,
                        color = if (coin.isPennyCoin) CryptoGoldPenny else CryptoCyanAccent,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = coin.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (coin.isPennyCoin) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PENNY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = CryptoGoldPenny,
                                modifier = Modifier
                                    .background(CryptoGoldPenny.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "${coin.name} • RSI: ${String.format("%.0f", coin.rsi14)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mini Sparkline
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(34.dp)
                    .padding(horizontal = 6.dp)
            ) {
                SparklineChart(
                    points = if (coin.sparkline.size > 2) coin.sparkline else listOf(coin.low24h, coin.currentPrice, coin.high24h),
                    lineColor = if (coin.isPositive) CryptoProfitGreen else CryptoLossRed,
                    modifier = Modifier.fillMaxSize(),
                    fillGradient = false
                )
            }

            // Price & 24h Change
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    text = coin.formattedPrice,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = coin.formattedChange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (coin.isPositive) CryptoProfitGreen else CryptoLossRed,
                    modifier = Modifier
                        .background(
                            (if (coin.isPositive) CryptoProfitGreen else CryptoLossRed).copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
