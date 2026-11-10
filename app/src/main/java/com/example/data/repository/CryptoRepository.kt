package com.example.data.repository

import com.example.data.model.CryptoCoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CryptoRepository(
    private val scope: CoroutineScope
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val _coins = MutableStateFlow<List<CryptoCoin>>(initialCoinList())
    val coins: StateFlow<List<CryptoCoin>> = _coins.asStateFlow()

    private val _isLiveConnecting = MutableStateFlow(false)
    val isLiveConnecting: StateFlow<Boolean> = _isLiveConnecting.asStateFlow()

    init {
        // Start background live market ticker & micro-fluctuation engine
        scope.launch(Dispatchers.IO) {
            // Initial real market sync attempt from public API
            fetchLiveMarketPrices()

            // Run continuous high-frequency market simulation / price updates (ticks every 1.5s)
            while (isActive) {
                delay(1500L)
                simulateMarketTicks()
            }
        }
    }

    private fun simulateMarketTicks() {
        val current = _coins.value
        val updated = current.map { coin ->
            // Volatility scale based on penny coin vs major coin
            val baseVol = if (coin.isPennyCoin) 0.0035 else 0.0012
            val randomFactor = (Random.nextDouble() - 0.495) * baseVol
            val newPrice = (coin.currentPrice * (1.0 + randomFactor)).coerceAtLeast(0.00000001)

            // Update sparkline (last 16 points)
            val updatedSparkline = if (coin.sparkline.isEmpty()) {
                listOf(newPrice)
            } else {
                (coin.sparkline + newPrice).takeLast(20)
            }

            // Approximate dynamic RSI
            val rsiShift = (randomFactor * 250).coerceIn(-4.0, 4.0)
            val newRsi = (coin.rsi14 + rsiShift).coerceIn(15.0, 85.0)

            val priceChangePercent = ((newPrice - coin.low24h) / coin.low24h * 100.0) - (coin.high24h - newPrice) / coin.high24h * 50.0

            coin.copy(
                currentPrice = newPrice,
                priceChangePercentage24h = coin.priceChangePercentage24h + (randomFactor * 10.0),
                high24h = maxOf(coin.high24h, newPrice),
                low24h = minOf(coin.low24h, newPrice),
                sparkline = updatedSparkline,
                rsi14 = newRsi
            )
        }
        _coins.value = updated
    }

    suspend fun fetchLiveMarketPrices() {
        _isLiveConnecting.value = true
        try {
            // Fetch live prices from public CoinGecko simple/price endpoint
            val ids = "bitcoin,ethereum,solana,binancecoin,ripple,cardano,dogecoin,shiba-inu,pepe,bonk,floki,tron,vechain,stellar,kaspa"
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=$ids&vs_currencies=usd&include_24hr_change=true"
            val request = Request.Builder().url(url).build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = org.json.JSONObject(body)
                    val mapping = mapOf(
                        "bitcoin" to "BTC",
                        "ethereum" to "ETH",
                        "solana" to "SOL",
                        "binancecoin" to "BNB",
                        "ripple" to "XRP",
                        "cardano" to "ADA",
                        "dogecoin" to "DOGE",
                        "shiba-inu" to "SHIB",
                        "pepe" to "PEPE",
                        "bonk" to "BONK",
                        "floki" to "FLOKI",
                        "tron" to "TRX",
                        "vechain" to "VET",
                        "stellar" to "XLM",
                        "kaspa" to "KAS"
                    )

                    val currentList = _coins.value.toMutableList()
                    for ((idKey, symbol) in mapping) {
                        if (json.has(idKey)) {
                            val coinObj = json.getJSONObject(idKey)
                            val price = coinObj.optDouble("usd", 0.0)
                            val change24h = coinObj.optDouble("usd_24h_change", 0.0)
                            val index = currentList.indexOfFirst { it.symbol.equals(symbol, ignoreCase = true) }
                            if (index != -1 && price > 0.0) {
                                val old = currentList[index]
                                currentList[index] = old.copy(
                                    currentPrice = price,
                                    priceChangePercentage24h = change24h,
                                    sparkline = if (old.sparkline.isEmpty()) listOf(price * 0.98, price * 0.99, price) else old.sparkline
                                )
                            }
                        }
                    }
                    _coins.value = currentList
                }
            }
        } catch (e: Exception) {
            // Gracefully keep current/simulated prices
        } finally {
            _isLiveConnecting.value = false
        }
    }

    fun getCoinBySymbol(symbol: String): CryptoCoin? {
        return _coins.value.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) }
    }

    companion object {
        fun initialCoinList(): List<CryptoCoin> = listOf(
            // High-Frequency Penny & Micro-Cent Coins
            CryptoCoin(
                id = "pepe",
                symbol = "PEPE",
                name = "Pepe",
                currentPrice = 0.00001085,
                priceChange24h = 0.00000092,
                priceChangePercentage24h = 9.28,
                high24h = 0.00001150,
                low24h = 0.00000980,
                volume24h = 940000000.0,
                isPennyCoin = true,
                rsi14 = 62.4,
                sparkline = listOf(0.0000098, 0.0000101, 0.0000103, 0.0000100, 0.0000106, 0.00001085)
            ),
            CryptoCoin(
                id = "dogecoin",
                symbol = "DOGE",
                name = "Dogecoin",
                currentPrice = 0.1845,
                priceChange24h = 0.012,
                priceChangePercentage24h = 6.95,
                high24h = 0.1920,
                low24h = 0.1710,
                volume24h = 2450000000.0,
                isPennyCoin = true,
                rsi14 = 58.0,
                sparkline = listOf(0.172, 0.175, 0.180, 0.178, 0.182, 0.1845)
            ),
            CryptoCoin(
                id = "shiba-inu",
                symbol = "SHIB",
                name = "Shiba Inu",
                currentPrice = 0.00002480,
                priceChange24h = 0.00000110,
                priceChangePercentage24h = 4.64,
                high24h = 0.00002590,
                low24h = 0.00002320,
                volume24h = 620000000.0,
                isPennyCoin = true,
                rsi14 = 52.8,
                sparkline = listOf(0.0000234, 0.0000239, 0.0000242, 0.0000240, 0.0000248)
            ),
            CryptoCoin(
                id = "bonk",
                symbol = "BONK",
                name = "Bonk",
                currentPrice = 0.00002190,
                priceChange24h = -0.00000085,
                priceChangePercentage24h = -3.74,
                high24h = 0.00002350,
                low24h = 0.00002100,
                volume24h = 410000000.0,
                isPennyCoin = true,
                rsi14 = 41.2,
                sparkline = listOf(0.0000232, 0.0000228, 0.0000225, 0.0000221, 0.0000219)
            ),
            CryptoCoin(
                id = "floki",
                symbol = "FLOKI",
                name = "Floki",
                currentPrice = 0.000142,
                priceChange24h = 0.000011,
                priceChangePercentage24h = 8.40,
                high24h = 0.000149,
                low24h = 0.000128,
                volume24h = 280000000.0,
                isPennyCoin = true,
                rsi14 = 66.5,
                sparkline = listOf(0.000130, 0.000133, 0.000138, 0.000135, 0.000142)
            ),
            CryptoCoin(
                id = "tron",
                symbol = "TRX",
                name = "TRON",
                currentPrice = 0.1620,
                priceChange24h = 0.0040,
                priceChangePercentage24h = 2.53,
                high24h = 0.1650,
                low24h = 0.1580,
                volume24h = 510000000.0,
                isPennyCoin = true,
                rsi14 = 54.1,
                sparkline = listOf(0.159, 0.160, 0.161, 0.1605, 0.162)
            ),
            CryptoCoin(
                id = "vechain",
                symbol = "VET",
                name = "VeChain",
                currentPrice = 0.0315,
                priceChange24h = 0.0018,
                priceChangePercentage24h = 6.06,
                high24h = 0.0330,
                low24h = 0.0295,
                volume24h = 85000000.0,
                isPennyCoin = true,
                rsi14 = 59.8,
                sparkline = listOf(0.0298, 0.0302, 0.0308, 0.0311, 0.0315)
            ),
            CryptoCoin(
                id = "stellar",
                symbol = "XLM",
                name = "Stellar",
                currentPrice = 0.1280,
                priceChange24h = -0.0022,
                priceChangePercentage24h = -1.69,
                high24h = 0.1340,
                low24h = 0.1250,
                volume24h = 130000000.0,
                isPennyCoin = true,
                rsi14 = 47.3,
                sparkline = listOf(0.131, 0.129, 0.127, 0.1285, 0.128)
            ),
            CryptoCoin(
                id = "kaspa",
                symbol = "KAS",
                name = "Kaspa",
                currentPrice = 0.1450,
                priceChange24h = 0.0085,
                priceChangePercentage24h = 6.23,
                high24h = 0.1490,
                low24h = 0.1340,
                volume24h = 95000000.0,
                isPennyCoin = true,
                rsi14 = 63.1,
                sparkline = listOf(0.136, 0.139, 0.141, 0.142, 0.145)
            ),
            // Major Market Drivers (Blue Chips)
            CryptoCoin(
                id = "bitcoin",
                symbol = "BTC",
                name = "Bitcoin",
                currentPrice = 88450.0,
                priceChange24h = 2410.0,
                priceChangePercentage24h = 2.80,
                high24h = 89200.0,
                low24h = 85600.0,
                volume24h = 42000000000.0,
                isPennyCoin = false,
                rsi14 = 61.2,
                sparkline = listOf(86200.0, 86800.0, 87400.0, 87900.0, 88450.0)
            ),
            CryptoCoin(
                id = "ethereum",
                symbol = "ETH",
                name = "Ethereum",
                currentPrice = 3380.0,
                priceChange24h = 95.0,
                priceChangePercentage24h = 2.89,
                high24h = 3440.0,
                low24h = 3260.0,
                volume24h = 21000000000.0,
                isPennyCoin = false,
                rsi14 = 55.4,
                sparkline = listOf(3290.0, 3320.0, 3350.0, 3340.0, 3380.0)
            ),
            CryptoCoin(
                id = "solana",
                symbol = "SOL",
                name = "Solana",
                currentPrice = 214.50,
                priceChange24h = 12.80,
                priceChangePercentage24h = 6.35,
                high24h = 219.00,
                low24h = 198.50,
                volume24h = 7800000000.0,
                isPennyCoin = false,
                rsi14 = 68.9,
                sparkline = listOf(201.0, 205.0, 209.0, 211.5, 214.5)
            ),
            CryptoCoin(
                id = "ripple",
                symbol = "XRP",
                name = "XRP",
                currentPrice = 0.6850,
                priceChange24h = 0.042,
                priceChangePercentage24h = 6.53,
                high24h = 0.7100,
                low24h = 0.6350,
                volume24h = 2200000000.0,
                isPennyCoin = true,
                rsi14 = 64.0,
                sparkline = listOf(0.642, 0.655, 0.670, 0.665, 0.685)
            ),
            CryptoCoin(
                id = "binancecoin",
                symbol = "BNB",
                name = "BNB",
                currentPrice = 645.20,
                priceChange24h = 8.40,
                priceChangePercentage24h = 1.32,
                high24h = 652.00,
                low24h = 632.00,
                volume24h = 1400000000.0,
                isPennyCoin = false,
                rsi14 = 51.5,
                sparkline = listOf(634.0, 638.0, 642.0, 640.0, 645.2)
            ),
            CryptoCoin(
                id = "cardano",
                symbol = "ADA",
                name = "Cardano",
                currentPrice = 0.5820,
                priceChange24h = 0.038,
                priceChangePercentage24h = 6.99,
                high24h = 0.6050,
                low24h = 0.5380,
                volume24h = 920000000.0,
                isPennyCoin = true,
                rsi14 = 65.2,
                sparkline = listOf(0.545, 0.558, 0.569, 0.575, 0.582)
            )
        )
    }
}
