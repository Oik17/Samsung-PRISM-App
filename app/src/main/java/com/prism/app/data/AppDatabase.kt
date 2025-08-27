package com.prism.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WifiFingerprint::class, SensorRaw::class, PdrFeature::class, RoomLabel::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {   // ✅ FIXED name
    abstract fun prismDao(): PrismDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prism-db"
                ).build().also { instance = it }
            }
    }
}
