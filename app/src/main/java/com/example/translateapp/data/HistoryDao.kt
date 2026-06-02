package com.example.translateapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.translateapp.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("DELETE FROM translation_history")
    suspend fun clearAllHistory()
}
