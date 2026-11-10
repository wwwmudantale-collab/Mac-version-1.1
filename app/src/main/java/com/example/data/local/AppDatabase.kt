package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.BotStrategy
import com.example.data.model.PaymentChannelType
import com.example.data.model.PaymentDirection
import com.example.data.model.PaymentStatus

class Converters {
    @TypeConverter
    fun fromStrategy(strategy: BotStrategy): String = strategy.name

    @TypeConverter
    fun toStrategy(name: String): BotStrategy = try {
        BotStrategy.valueOf(name)
    } catch (e: Exception) {
        BotStrategy.CENT_SCALPER
    }

    @TypeConverter
    fun fromChannelType(channelType: PaymentChannelType): String = channelType.name

    @TypeConverter
    fun toChannelType(name: String): PaymentChannelType = try {
        PaymentChannelType.valueOf(name)
    } catch (e: Exception) {
        PaymentChannelType.MOBILE_MONEY
    }

    @TypeConverter
    fun fromPaymentDirection(direction: PaymentDirection): String = direction.name

    @TypeConverter
    fun toPaymentDirection(name: String): PaymentDirection = try {
        PaymentDirection.valueOf(name)
    } catch (e: Exception) {
        PaymentDirection.DEPOSIT
    }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(name: String): PaymentStatus = try {
        PaymentStatus.valueOf(name)
    } catch (e: Exception) {
        PaymentStatus.CONFIRMED
    }
}

@Database(
    entities = [
        PaperTrade::class,
        BotConfig::class,
        BotActivityLog::class,
        PaymentTransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun botDao(): BotDao
    abstract fun transactionDao(): PaymentTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coinsniper_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
