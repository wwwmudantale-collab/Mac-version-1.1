package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PaymentTransactionEntity
import com.example.data.model.BlockchainNetwork
import com.example.data.model.MobileMoneyProvider
import com.example.data.model.PaymentChannelType
import com.example.data.model.PaymentDirection
import com.example.data.model.TransactionSettings
import com.example.data.model.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.CryptoCyanAccent
import com.example.ui.theme.CryptoGoldPenny
import com.example.ui.theme.CryptoLossRed
import com.example.ui.theme.CryptoProfitGreen
import com.example.ui.theme.CryptoPurpleAI
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val userProfile by viewModel.userProfile.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val totalCentsGrabbed by viewModel.totalCentsGrabbed.collectAsState()
    val openTrades by viewModel.openTrades.collectAsState()
    val allBots by viewModel.allBots.collectAsState()
    val isAccountLocked by viewModel.isAccountLocked.collectAsState()
    val settings by viewModel.transactionSettings.collectAsState()
    val transactions by viewModel.paymentTransactions.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Transaction filter state
    var selectedTxFilter by remember { mutableStateOf("ALL") }

    // Editable Transaction Settings state
    var tradeSize by remember(settings) { mutableDoubleStateOf(settings.defaultTradeSizeUsd) }
    var slippage by remember(settings) { mutableDoubleStateOf(settings.maxSlippagePercent) }
    var takeProfitCents by remember(settings) { mutableDoubleStateOf(settings.defaultTakeProfitCents * 100.0) }
    var stopLossPercent by remember(settings) { mutableDoubleStateOf(settings.defaultStopLossPercent) }
    var maxPositions by remember(settings) { mutableIntStateOf(settings.maxConcurrentPositions) }
    var autoReinvest by remember(settings) { mutableStateOf(settings.autoReinvestProfits) }
    var hapticFeedback by remember(settings) { mutableStateOf(settings.hapticFeedbackEnabled) }
    var executionMode by remember(settings) { mutableStateOf(settings.executionMode) }
    var gasPriority by remember(settings) { mutableStateOf(settings.gasPriority) }
    var antiFrontRunning by remember(settings) { mutableStateOf(settings.antiFrontRunningGuard) }

    // Chain & Mobile Money settings state
    var activeChain by remember(settings) { mutableStateOf(settings.activeChain) }
    var autoBridge by remember(settings) { mutableStateOf(settings.autoBridgeCrossChain) }
    var preferredMoMo by remember(settings) { mutableStateOf(settings.preferredMobileMoneyProvider) }
    var momoPhone by remember(settings) { mutableStateOf(settings.mobileMoneyPhoneNumber) }
    var autoCashoutToMoMo by remember(settings) { mutableStateOf(settings.autoCashoutCentsToMoMo) }
    var autoCashoutThreshold by remember(settings) { mutableDoubleStateOf(settings.autoCashoutThresholdUsd) }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updateUserProfile(updated)
                showEditProfileDialog = false
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDepositDialog) {
        DepositFundsDialog(
            defaultMoMoPhone = momoPhone,
            defaultMoMoProvider = preferredMoMo,
            defaultChain = activeChain,
            onDismiss = { showDepositDialog = false },
            onDepositMoMo = { provider, phone, amount ->
                viewModel.depositViaMobileMoney(provider, phone, amount) {
                    showDepositDialog = false
                    Toast.makeText(
                        context,
                        "✅ +$${String.format("%.2f", amount)} USD deposit confirmed via ${provider.displayName}!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onDepositChain = { chain, amount, senderAddr ->
                viewModel.depositViaBlockchain(chain, amount, senderAddr) {
                    showDepositDialog = false
                    Toast.makeText(
                        context,
                        "✅ +$${String.format("%.2f", amount)} USDT bridged successfully on ${chain.displayName}!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawFundsDialog(
            walletBalance = walletBalance,
            defaultMoMoPhone = momoPhone,
            defaultMoMoProvider = preferredMoMo,
            defaultChain = activeChain,
            onDismiss = { showWithdrawDialog = false },
            onWithdrawMoMo = { provider, phone, amount ->
                viewModel.withdrawToMobileMoney(provider, phone, amount) { success ->
                    showWithdrawDialog = false
                    if (success) {
                        Toast.makeText(
                            context,
                            "💸 Payout of $${String.format("%.2f", amount)} sent to ${provider.displayName} ($phone)!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "❌ Insufficient balance or account locked.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onWithdrawChain = { chain, address, amount ->
                viewModel.withdrawToBlockchain(chain, address, amount) { success ->
                    showWithdrawDialog = false
                    if (success) {
                        Toast.makeText(
                            context,
                            "💸 On-chain withdrawal of $${String.format("%.2f", amount)} broadcast on ${chain.displayName}!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "❌ Insufficient balance or account locked.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Paper Bankroll?") },
            text = { Text("Reset available paper trading wallet balance back to $1,000.00 and clear previous trade history?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetWallet()
                        showResetDialog = false
                        Toast.makeText(context, "Wallet reset to $1,000.00", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoLossRed)
                ) {
                    Text("Reset to $1,000", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = CryptoCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TRADER PROFILE & SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoCyanAccent,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Wallet, Chains & Mobile Money",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("edit_profile_button")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 1. User Identity & VIP Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_identity_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CryptoCyanAccent.copy(alpha = 0.2f))
                                .border(1.5.dp, CryptoCyanAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.avatarEmoji,
                                fontSize = 26.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = userProfile.email,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = userProfile.tier,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CryptoGoldPenny,
                                    modifier = Modifier
                                        .background(CryptoGoldPenny.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                Text(
                                    text = "ID: ${userProfile.accountId}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoCyanAccent,
                                    modifier = Modifier
                                        .background(CryptoCyanAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Comprehensive Wallet Balance Card (with Mobile Money & Chain rates)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CryptoProfitGreen.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = CryptoProfitGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRADING WALLET BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CryptoProfitGreen,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = if (isAccountLocked) "LOCKED" else "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAccountLocked) CryptoLossRed else CryptoProfitGreen,
                            modifier = Modifier
                                .background(
                                    (if (isAccountLocked) CryptoLossRed else CryptoProfitGreen).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = String.format(Locale.US, "$%,.2f USD", walletBalance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Local currency conversion for MTN & Airtel MoMo (e.g. UGX @ 3,750)
                    val localVal = walletBalance * preferredMoMo.fxRateToUsd
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "≈ ${String.format(Locale.US, "%,.0f", localVal)} ${preferredMoMo.currencyCode}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(preferredMoMo.brandColorHex)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "via ${preferredMoMo.displayName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF071224), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Cents Grabbed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "+$%.2f (+%.0f¢)", totalCentsGrabbed, totalCentsGrabbed * 100.0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CryptoGoldPenny
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Active Positions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${openTrades.size} Trades",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (openTrades.isNotEmpty()) CryptoCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Active Bots", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${allBots.count { it.isRunning }} Running",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CryptoProfitGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Action Buttons: Deposit & Cashout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showDepositDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoProfitGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("deposit_funds_button")
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit Funds", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = { showWithdrawDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("withdraw_funds_button")
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cash Out", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Mobile Money Gateway (MTN & Airtel) Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_money_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC00).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color(0xFFFFCC00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mobile Money Gateway",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "MTN & AIRTEL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFCC00),
                            modifier = Modifier
                                .background(Color(0xFFFFCC00).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Fund your high-frequency trading bot instantly with MTN MoMo or Airtel Money. Scalped cent profits can be auto-withdrawn directly to your phone.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mobile Money Provider Selector
                    Text("Preferred Telecom Provider:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // MTN MoMo
                        val mtnSelected = preferredMoMo == MobileMoneyProvider.MTN_MOMO
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { preferredMoMo = MobileMoneyProvider.MTN_MOMO }
                                .border(
                                    width = if (mtnSelected) 2.dp else 1.dp,
                                    color = if (mtnSelected) Color(0xFFFFCC00) else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (mtnSelected) Color(0xFFFFCC00).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFCC00))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("MTN MoMo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("*165# Push", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Airtel Money
                        val airtelSelected = preferredMoMo == MobileMoneyProvider.AIRTEL_MONEY
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { preferredMoMo = MobileMoneyProvider.AIRTEL_MONEY }
                                .border(
                                    width = if (airtelSelected) 2.dp else 1.dp,
                                    color = if (airtelSelected) Color(0xFFE60000) else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (airtelSelected) Color(0xFFE60000).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE60000))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Airtel Money", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("*185# Express", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = momoPhone,
                        onValueChange = { momoPhone = it },
                        label = { Text("Registered Mobile Money Number") },
                        placeholder = { Text("+256 772 884 921") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(preferredMoMo.brandColorHex), modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Cashout Grabbed Cents Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Sweep Cents to Mobile Money", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Automatically cash out grabbed profits when >= $${String.format("%.1f", autoCashoutThreshold)} USD", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoCashoutToMoMo,
                            onCheckedChange = { autoCashoutToMoMo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFFFFCC00)
                            )
                        )
                    }
                }
            }
        }

        // 4. Transaction Chains (Blockchain Networks) Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_chains_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CryptoCyanAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = CryptoCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Transaction Chains",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "SETTLEMENT LAYER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CryptoCyanAccent,
                            modifier = Modifier
                                .background(CryptoCyanAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select active blockchain for high-speed liquidity bridging and cent arbitrage execution.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chains chips grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(BlockchainNetwork.SOLANA, BlockchainNetwork.BINANCE_SMART_CHAIN, BlockchainNetwork.TRON).forEach { chain ->
                                val selected = activeChain == chain
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { activeChain = chain }
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) Color(chain.colorHex) else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    color = if (selected) Color(chain.colorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(chain.symbol, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(chain.colorHex))
                                        Text(chain.tokenStandard, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("< $${chain.estimatedGasFeeUsd}", fontSize = 8.sp, color = CryptoProfitGreen)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(BlockchainNetwork.BASE, BlockchainNetwork.POLYGON, BlockchainNetwork.ETHEREUM).forEach { chain ->
                                val selected = activeChain == chain
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { activeChain = chain }
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) Color(chain.colorHex) else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    color = if (selected) Color(chain.colorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(chain.symbol, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(chain.colorHex))
                                        Text(chain.tokenStandard, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Gas: ~$${chain.estimatedGasFeeUsd}", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cross-Chain Liquidity Auto-Bridge", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Auto-route lowest fee chain (Solana / Tron / BSC) for penny coin trades", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoBridge,
                            onCheckedChange = { autoBridge = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CryptoCyanAccent
                            )
                        )
                    }
                }
            }
        }

        // 5. Transaction & Execution Settings Card ("Enable Transaction Settings")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_settings_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CryptoCyanAccent.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = CryptoCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Transaction & Execution Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "CONFIGURED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoProfitGreen,
                            modifier = Modifier
                                .background(CryptoProfitGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Setting 1: Default Trade Size
                    Text(
                        text = "Default Trade Size: $${String.format(Locale.US, "%.0f", tradeSize)} USD",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 25.0, 50.0, 100.0).forEach { size ->
                            FilterChip(
                                selected = tradeSize == size,
                                onClick = { tradeSize = size },
                                label = { Text("$$size", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CryptoCyanAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = CryptoCyanAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Setting 2: Take Profit Cents Target
                    Text(
                        text = "Take Profit Target: +${String.format(Locale.US, "%.0f¢", takeProfitCents)} ($${String.format(Locale.US, "%.2f", takeProfitCents / 100.0)})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15.0, 25.0, 35.0, 50.0, 85.0).forEach { cents ->
                            FilterChip(
                                selected = takeProfitCents == cents,
                                onClick = { takeProfitCents = cents },
                                label = { Text("+${cents.toInt()}¢", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CryptoGoldPenny.copy(alpha = 0.2f),
                                    selectedLabelColor = CryptoGoldPenny
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Setting 3: Gas & Priority
                    Text(
                        text = "Gas & MEV Execution Priority:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("STANDARD", "FAST_TURBO", "WARP_MEV").forEach { mode ->
                            FilterChip(
                                selected = gasPriority == mode,
                                onClick = { gasPriority = mode },
                                label = { Text(mode.replace("_", " "), fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CryptoProfitGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = CryptoProfitGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Setting 4: Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Reinvest Profits", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Compound scalped cents into subsequent trades", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoReinvest,
                            onCheckedChange = { autoReinvest = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CryptoProfitGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Anti-Front-Running Guard (MEV Shield)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Protect micro-cent orders from sandwich bots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = antiFrontRunning,
                            onCheckedChange = { antiFrontRunning = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CryptoGoldPenny
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Save Settings Button
                    Button(
                        onClick = {
                            viewModel.saveTransactionSettings(
                                TransactionSettings(
                                    defaultTradeSizeUsd = tradeSize,
                                    maxSlippagePercent = slippage,
                                    defaultTakeProfitCents = takeProfitCents / 100.0,
                                    defaultStopLossPercent = stopLossPercent,
                                    maxConcurrentPositions = maxPositions,
                                    autoReinvestProfits = autoReinvest,
                                    hapticFeedbackEnabled = hapticFeedback,
                                    executionMode = executionMode,
                                    gasPriority = gasPriority,
                                    antiFrontRunningGuard = antiFrontRunning,
                                    activeChain = activeChain,
                                    autoBridgeCrossChain = autoBridge,
                                    preferredMobileMoneyProvider = preferredMoMo,
                                    mobileMoneyPhoneNumber = momoPhone,
                                    autoCashoutCentsToMoMo = autoCashoutToMoMo,
                                    autoCashoutThresholdUsd = autoCashoutThreshold
                                )
                            )
                            Toast.makeText(context, "✅ All transaction, chain & mobile money settings saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoProfitGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_transaction_settings_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply All Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Transaction Chain & Mobile Money Ledger Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transactions_ledger_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = CryptoGoldPenny,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Transaction History Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${transactions.size} Records",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGoldPenny,
                            modifier = Modifier
                                .background(CryptoGoldPenny.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL" to "All", "MOMO" to "Mobile Money", "CHAIN" to "Chains").forEach { (key, label) ->
                            FilterChip(
                                selected = selectedTxFilter == key,
                                onClick = { selectedTxFilter = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CryptoGoldPenny.copy(alpha = 0.2f),
                                    selectedLabelColor = CryptoGoldPenny
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredList = remember(transactions, selectedTxFilter) {
                        when (selectedTxFilter) {
                            "MOMO" -> transactions.filter { it.channelType == PaymentChannelType.MOBILE_MONEY }
                            "CHAIN" -> transactions.filter { it.channelType == PaymentChannelType.BLOCKCHAIN_NETWORK }
                            else -> transactions
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions recorded yet in this category.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredList.forEach { tx ->
                                TransactionLedgerRow(
                                    tx = tx,
                                    onCopyRef = { ref ->
                                        clipboardManager.setText(AnnotatedString(ref))
                                        Toast.makeText(context, "Copied reference $ref", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionLedgerRow(
    tx: PaymentTransactionEntity,
    onCopyRef: (String) -> Unit
) {
    val isDeposit = tx.direction == PaymentDirection.DEPOSIT
    val isMoMo = tx.channelType == PaymentChannelType.MOBILE_MONEY

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDeposit) CryptoProfitGreen.copy(alpha = 0.15f) else CryptoLossRed.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isDeposit) CryptoProfitGreen else CryptoLossRed,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.providerOrChain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMoMo) "MOMO" else "CHAIN",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMoMo) Color(0xFFFFCC00) else CryptoCyanAccent,
                        modifier = Modifier
                            .background(
                                (if (isMoMo) Color(0xFFFFCC00) else CryptoCyanAccent).copy(alpha = 0.15f),
                                RoundedCornerShape(3.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ref: ${tx.transactionRef}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { onCopyRef(tx.transactionRef) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Ref", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    }
                }

                Text(
                    text = "${tx.formattedDate} • ${tx.accountOrAddress.take(18)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = tx.formattedUsdAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDeposit) CryptoProfitGreen else CryptoLossRed
                )
                if (tx.formattedLocalAmount != null) {
                    Text(
                        text = tx.formattedLocalAmount!!,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFCC00)
                    )
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.displayName) }
    var email by remember { mutableStateOf(currentProfile.email) }
    var accountId by remember { mutableStateOf(currentProfile.accountId) }
    var emoji by remember { mutableStateOf(currentProfile.avatarEmoji) }

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
                    text = "Edit Trader Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Configured Account ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Avatar Emoji:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("⚡", "🤖", "🚀", "💰", "💎", "🦁").forEach { em ->
                        FilterChip(
                            selected = emoji == em,
                            onClick = { emoji = em },
                            label = { Text(em, fontSize = 16.sp) }
                        )
                    }
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
                            onSave(
                                currentProfile.copy(
                                    displayName = name,
                                    email = email,
                                    accountId = accountId,
                                    avatarEmoji = emoji
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoCyanAccent)
                    ) {
                        Text("Save Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Interactive Deposit Funds Dialog supporting:
 * Tab 1: Mobile Money (MTN MoMo & Airtel Money)
 * Tab 2: Blockchain Networks (Solana, BSC, Tron, Base, etc.)
 */
@Composable
fun DepositFundsDialog(
    defaultMoMoPhone: String,
    defaultMoMoProvider: MobileMoneyProvider,
    defaultChain: BlockchainNetwork,
    onDismiss: () -> Unit,
    onDepositMoMo: (MobileMoneyProvider, String, Double) -> Unit,
    onDepositChain: (BlockchainNetwork, Double, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mobile Money, 1: Blockchain

    // MoMo state
    var momoProvider by remember { mutableStateOf(defaultMoMoProvider) }
    var momoPhoneInput by remember { mutableStateOf(defaultMoMoPhone) }
    var momoAmountStr by remember { mutableStateOf("50") }
    var isSimulatingUssd by remember { mutableStateOf(false) }

    // Chain state
    var selectedChain by remember { mutableStateOf(defaultChain) }
    var chainAmountStr by remember { mutableStateOf("100") }
    var senderAddress by remember { mutableStateOf("0x4f82...3b19") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Deposit & Fund Trading Wallet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (selectedTab == 0) Color(0xFFFFCC00) else CryptoCyanAccent
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📱 Mobile Money", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("⛓️ Blockchain Chain", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // TAB 1: MOBILE MONEY (MTN & AIRTEL)
                    if (isSimulatingUssd) {
                        // USSD PIN Simulation Screen
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F2027), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(momoProvider.brandColorHex), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📱 INCOMING USSD PUSH PROMPT", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(momoProvider.brandColorHex))
                            Spacer(modifier = Modifier.height(8.dp))
                            val usdAmount = momoAmountStr.toDoubleOrNull() ?: 50.0
                            val localCur = usdAmount * momoProvider.fxRateToUsd
                            Text(
                                text = "Approve payment of ${String.format(Locale.US, "%,.0f", localCur)} ${momoProvider.currencyCode} ($$usdAmount USD) to CoinSniper AI Trading Bot?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Enter 4-Digit MoMo PIN on your phone keypad:", fontSize = 11.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("••••", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(momoProvider.brandColorHex))
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    isSimulatingUssd = false
                                    onDepositMoMo(momoProvider, momoPhoneInput, usdAmount)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(momoProvider.brandColorHex))
                            ) {
                                Text("Authorize & Confirm PIN", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    } else {
                        // MoMo Provider Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = momoProvider == MobileMoneyProvider.MTN_MOMO,
                                onClick = { momoProvider = MobileMoneyProvider.MTN_MOMO },
                                label = { Text("MTN MoMo (*165#)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFCC00).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFFFCC00)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = momoProvider == MobileMoneyProvider.AIRTEL_MONEY,
                                onClick = { momoProvider = MobileMoneyProvider.AIRTEL_MONEY },
                                label = { Text("Airtel Money (*185#)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE60000).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFE60000)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = momoPhoneInput,
                            onValueChange = { momoPhoneInput = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = momoAmountStr,
                            onValueChange = { momoAmountStr = it },
                            label = { Text("Amount ($ USD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val usdAmount = momoAmountStr.toDoubleOrNull() ?: 0.0
                        if (usdAmount > 0) {
                            val localCur = usdAmount * momoProvider.fxRateToUsd
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "≈ ${String.format(Locale.US, "%,.0f", localCur)} ${momoProvider.currencyCode} (Rate: 1 USD = 3,750 UGX)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(momoProvider.brandColorHex)
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
                                onClick = { isSimulatingUssd = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(momoProvider.brandColorHex))
                            ) {
                                Text("Send Push Request", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // TAB 2: BLOCKCHAIN NETWORKS
                    Text("Select Blockchain Network:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(BlockchainNetwork.SOLANA, BlockchainNetwork.BINANCE_SMART_CHAIN, BlockchainNetwork.TRON).forEach { chain ->
                            FilterChip(
                                selected = selectedChain == chain,
                                onClick = { selectedChain = chain },
                                label = { Text(chain.symbol, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(chain.colorHex).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(chain.colorHex)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = chainAmountStr,
                        onValueChange = { chainAmountStr = it },
                        label = { Text("Bridge Amount (USDT)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = senderAddress,
                        onValueChange = { senderAddress = it },
                        label = { Text("Sender Wallet Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CryptoProfitGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Estimated Gas: ~$${selectedChain.estimatedGasFeeUsd} (${selectedChain.tokenStandard})",
                            fontSize = 11.sp,
                            color = CryptoProfitGreen
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
                                val amount = chainAmountStr.toDoubleOrNull() ?: 100.0
                                onDepositChain(selectedChain, amount, senderAddress)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoCyanAccent)
                        ) {
                            Text("Confirm Bridge", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Withdraw / Cash Out Dialog supporting:
 * Tab 1: Payout directly to MTN MoMo or Airtel Money
 * Tab 2: Payout to Blockchain Wallet Address
 */
@Composable
fun WithdrawFundsDialog(
    walletBalance: Double,
    defaultMoMoPhone: String,
    defaultMoMoProvider: MobileMoneyProvider,
    defaultChain: BlockchainNetwork,
    onDismiss: () -> Unit,
    onWithdrawMoMo: (MobileMoneyProvider, String, Double) -> Unit,
    onWithdrawChain: (BlockchainNetwork, String, Double) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var momoProvider by remember { mutableStateOf(defaultMoMoProvider) }
    var momoPhoneInput by remember { mutableStateOf(defaultMoMoPhone) }
    var momoAmountStr by remember { mutableStateOf("25") }

    var selectedChain by remember { mutableStateOf(defaultChain) }
    var chainAmountStr by remember { mutableStateOf("50") }
    var destAddress by remember { mutableStateOf("0x7e8b9...f231") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Cash Out & Withdraw Profits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Available Balance: $${String.format(Locale.US, "%.2f", walletBalance)} USD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoProfitGreen
                )

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (selectedTab == 0) Color(0xFFFFCC00) else CryptoCyanAccent
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📱 Mobile Money", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("⛓️ Blockchain", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // MoMo Provider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = momoProvider == MobileMoneyProvider.MTN_MOMO,
                            onClick = { momoProvider = MobileMoneyProvider.MTN_MOMO },
                            label = { Text("MTN MoMo", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFCC00).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFFFCC00)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = momoProvider == MobileMoneyProvider.AIRTEL_MONEY,
                            onClick = { momoProvider = MobileMoneyProvider.AIRTEL_MONEY },
                            label = { Text("Airtel Money", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE60000).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFE60000)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = momoPhoneInput,
                        onValueChange = { momoPhoneInput = it },
                        label = { Text("Receiver Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = momoAmountStr,
                        onValueChange = { momoAmountStr = it },
                        label = { Text("Withdraw Amount ($ USD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val withdrawUsd = momoAmountStr.toDoubleOrNull() ?: 0.0
                    if (withdrawUsd > 0) {
                        val localPayout = withdrawUsd * momoProvider.fxRateToUsd
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You will receive: ${String.format(Locale.US, "%,.0f", localPayout)} ${momoProvider.currencyCode}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(momoProvider.brandColorHex)
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
                                val amount = momoAmountStr.toDoubleOrNull() ?: 25.0
                                onWithdrawMoMo(momoProvider, momoPhoneInput, amount)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(momoProvider.brandColorHex))
                        ) {
                            Text("Send Payout", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Blockchain
                    Text("Select Destination Chain:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(BlockchainNetwork.SOLANA, BlockchainNetwork.BINANCE_SMART_CHAIN, BlockchainNetwork.TRON).forEach { chain ->
                            FilterChip(
                                selected = selectedChain == chain,
                                onClick = { selectedChain = chain },
                                label = { Text(chain.symbol, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(chain.colorHex).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(chain.colorHex)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = chainAmountStr,
                        onValueChange = { chainAmountStr = it },
                        label = { Text("Amount ($ USD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destAddress,
                        onValueChange = { destAddress = it },
                        label = { Text("Destination Wallet Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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
                                val amount = chainAmountStr.toDoubleOrNull() ?: 50.0
                                onWithdrawChain(selectedChain, destAddress, amount)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoProfitGreen)
                        ) {
                            Text("Confirm Withdrawal", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
