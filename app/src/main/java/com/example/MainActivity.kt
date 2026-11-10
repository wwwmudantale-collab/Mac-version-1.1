package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MainDashboard
import com.example.ui.screens.AiSignalsScreen
import com.example.ui.screens.BotsScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.TradesScreen
import com.example.ui.theme.CryptoProfitGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck

import androidx.compose.material.icons.filled.Person
import com.example.ui.screens.ProfileScreen

enum class AppNavTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    MARKET("Market", Icons.Default.ShowChart, "nav_market"),
    BOTS("AI Bots", Icons.Default.ElectricBolt, "nav_bots"),
    TRADES("Trades", Icons.Default.AccountBalanceWallet, "nav_trades"),
    SIGNALS("AI Signals", Icons.Default.AutoAwesome, "nav_signals"),
    PROFILE("Profile", Icons.Default.Person, "nav_profile")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check initialization with launch intent BEFORE setContent or ViewModels
        configureAppCheck(this, intent)

        // STEP 2: Edge-to-edge display
        enableEdgeToEdge()

        // STEP 3: Compose UI initialization
        setContent {
            MyApplicationTheme {
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(AppNavTab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav")
            ) {
                AppNavTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selected) CryptoProfitGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) CryptoProfitGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CryptoProfitGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "ScreenTransition") { tab ->
                when (tab) {
                    AppNavTab.DASHBOARD -> MainDashboard(
                        viewModel = viewModel,
                        onNavigateToMarket = { currentTab = AppNavTab.MARKET },
                        onNavigateToBots = { currentTab = AppNavTab.BOTS },
                        onNavigateToSignals = { currentTab = AppNavTab.SIGNALS },
                        onNavigateToProfile = { currentTab = AppNavTab.PROFILE }
                    )
                    AppNavTab.MARKET -> MarketScreen(viewModel = viewModel)
                    AppNavTab.BOTS -> BotsScreen(viewModel = viewModel)
                    AppNavTab.TRADES -> TradesScreen(viewModel = viewModel)
                    AppNavTab.SIGNALS -> AiSignalsScreen(viewModel = viewModel)
                    AppNavTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}
