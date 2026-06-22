package com.example.translateapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.example.translateapp.model.HistoryEntity
import androidx.room.RoomDatabase
import com.example.translateapp.model.FavoriteEntity

@Database(entities = [HistoryEntity::class, FavoriteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "translate_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
