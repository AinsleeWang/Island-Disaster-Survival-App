package com.example.islanddisastersurvivalguideapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.islanddisastersurvivalguideapp.data.local.dao.FrequentLocationDao
import com.example.islanddisastersurvivalguideapp.data.local.entity.FrequentLocationEntity
import com.example.islanddisastersurvivalguideapp.data.local.converter.Converters
import com.example.islanddisastersurvivalguideapp.data.local.dao.MedicalCardDao
import com.example.islanddisastersurvivalguideapp.data.local.dao.OfflineRouteDao
import com.example.islanddisastersurvivalguideapp.data.local.entity.PrecomputedRouteEntity


@Database(
    entities = [
        PrecomputedRouteEntity::class,
        FrequentLocationEntity::class,
        MedicalCardEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun frequentLocationDao(): FrequentLocationDao
    abstract fun OfflineRouteDao(): OfflineRouteDao
    abstract fun medicalCardDao(): MedicalCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 定義版本 2 到版本 3 的遷移策略
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 創建醫療卡表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS medical_cards (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        birthDate TEXT NOT NULL,
                        bloodType TEXT NOT NULL,
                        medicalHistory TEXT NOT NULL,
                        medications TEXT NOT NULL,
                        emergencyContact TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_2_3)  // 添加遷移策略
                    .fallbackToDestructiveMigration()  // 保留作為最後的後備選項
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}