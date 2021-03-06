package com.suji.android.suji_android.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.suji.android.suji_android.base.BaseApp
import com.suji.android.suji_android.database.dao.FoodDAO
import com.suji.android.suji_android.database.dao.SaleDAO
import com.suji.android.suji_android.database.model.Food
import com.suji.android.suji_android.database.model.Sale
import org.threeten.bp.Instant

@Database(entities = [Food::class, Sale::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDAO(): FoodDAO
    abstract fun saleDAO(): SaleDAO

    object Singleton {
        private const val DATABASE_NAME = "suji-android"

        private val INSTANCE: AppDatabase by lazy {
            buildDatabase(BaseApp.instance)
        }

        fun getInstance(): AppDatabase {
            return INSTANCE
        }

        /**
         * 데이터 베이스 생성
         * addCallback가 작동되지 않음
         * 이유는 모르겠음
         */
        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val database = getInstance()
                        insertData(database)
                        Log.i("AppDataBase", "after insertFood data")
                    }
                })
                .setJournalMode(JournalMode.TRUNCATE)
//                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun insertData(database: AppDatabase) {
            val sub = mutableListOf<Food>()
            sub.add(Food("치즈", 1000))
            sub.add(Food("볶음밥", 2000))
            sub.add(Food("우동 사리", 1000))
            sub.add(Food("라면 사리", 1000))

            val foodList = mutableListOf<Food>()
            foodList.add(Food("보리밥", 6000, mutableListOf(), 2))

            BaseApp.instance.getRepository().insert(Food("보리밥", 6000))
            BaseApp.instance.getRepository().insert(Food("뚝배기 묶은지 쪽갈비", 7000))
            BaseApp.instance.getRepository().insert(Food("닭볶음탕", 20000))
            BaseApp.instance.getRepository().insert(Food("닭갈비", 8000, sub))
            BaseApp.instance.getRepository().insert(Sale("1번", 12000, Instant.now().toEpochMilli(), foodList))
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS food (name TEXT, price LONG)")
            }
        }
    }
}