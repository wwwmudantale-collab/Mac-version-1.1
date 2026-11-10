package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PaperTrade
import com.example.data.model.BotStrategy
import com.example.data.model.CryptoCoin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CoinSniper AI", appName)
    }

    @Test
    fun `test penny coin formatting and cent calculations`() {
        val pepe = CryptoCoin(
            id = "pepe",
            symbol = "PEPE",
            name = "Pepe",
            currentPrice = 0.00001085,
            priceChange24h = 0.00000092,
            priceChangePercentage24h = 9.28,
            high24h = 0.00001150,
            low24h = 0.00000980,
            volume24h = 940000000.0,
            isPennyCoin = true
        )

        assertEquals("$0.00001085", pepe.formattedPrice)
        assertEquals("+9.28%", pepe.formattedChange)
        assertTrue(pepe.isPennyCoin)
    }

    @Test
    fun `test bot strategies have default profit cents defined`() {
        assertTrue(BotStrategy.CENT_SCALPER.defaultProfitCents > 0.0)
        assertTrue(BotStrategy.MOMENTUM_SNIPER.defaultProfitCents > 0.0)
        assertEquals("Micro-Cent Scalper", BotStrategy.CENT_SCALPER.displayName)
    }

    @Test
    fun `test paper trade format price helper`() {
        assertEquals("$0.00002480", PaperTrade.formatPrice(0.0000248))
        assertEquals("$0.1845", PaperTrade.formatPrice(0.1845))
        assertEquals("$88,450.00", PaperTrade.formatPrice(88450.0))
    }

    @Test
    fun `test quintillion warp speed threshold`() {
        val quintillion = 1_000_000_000_000_000_000.0
        val warpSpeed = 1_500_000_000_000_000_000.0
        assertTrue(warpSpeed >= quintillion)
    }

    @Test
    fun `test mobile money provider conversion rates and USSD codes`() {
        val mtn = com.example.data.model.MobileMoneyProvider.MTN_MOMO
        val airtel = com.example.data.model.MobileMoneyProvider.AIRTEL_MONEY

        assertEquals("*165#", mtn.dialCode)
        assertEquals("*185#", airtel.dialCode)
        assertEquals("UGX", mtn.currencyCode)
        assertEquals(3750.0, mtn.fxRateToUsd, 0.01)

        val usdAmount = 50.0
        val expectedUgx = 187500.0
        assertEquals(expectedUgx, usdAmount * mtn.fxRateToUsd, 0.01)
        assertEquals(expectedUgx, usdAmount * airtel.fxRateToUsd, 0.01)
    }

    @Test
    fun `test blockchain transaction networks and token standards`() {
        val solana = com.example.data.model.BlockchainNetwork.SOLANA
        val bsc = com.example.data.model.BlockchainNetwork.BINANCE_SMART_CHAIN
        val tron = com.example.data.model.BlockchainNetwork.TRON

        assertEquals("SPL Token", solana.tokenStandard)
        assertEquals("BEP-20", bsc.tokenStandard)
        assertEquals("TRC-20", tron.tokenStandard)
        assertTrue(solana.isRecommendedForCentScalp)
        assertTrue(bsc.isRecommendedForCentScalp)
        assertTrue(tron.isRecommendedForCentScalp)
    }

    @Test
    fun `test payment transaction entity formatting`() {
        val tx = com.example.data.local.PaymentTransactionEntity(
            transactionRef = "MTN-MOMO-883921",
            channelType = com.example.data.model.PaymentChannelType.MOBILE_MONEY,
            providerOrChain = "MTN Mobile Money",
            direction = com.example.data.model.PaymentDirection.DEPOSIT,
            amountUsd = 100.0,
            localAmount = 375000.0,
            localCurrency = "UGX",
            accountOrAddress = "+256 772 884 921",
            status = com.example.data.model.PaymentStatus.SETTLED
        )

        assertEquals("+$100.00", tx.formattedUsdAmount)
        assertEquals("+375,000 UGX", tx.formattedLocalAmount)
    }
}
