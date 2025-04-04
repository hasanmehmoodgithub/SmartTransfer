package com.smart.transfer.app.com.smart.transfer.app.features.history.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.dao.HistoryDao
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History

@Database(entities = [History::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "history_database_test_1"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
