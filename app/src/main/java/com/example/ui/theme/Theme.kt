package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CryptoProfitGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00381B),
    onPrimaryContainer = CryptoProfitGreen,
    secondary = CryptoCyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003B46),
    onSecondaryContainer = CryptoCyanAccent,
    tertiary = CryptoGoldPenny,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF3F3200),
    onTertiaryContainer = CryptoGoldPenny,
    background = CryptoDarkBackground,
    onBackground = TextPrimaryDark,
    surface = CryptoDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CryptoDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = CryptoLossRed,
    onError = Color.White,
    outline = CryptoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep bespoke crypto terminal theme consistent
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
