package com.example.translateapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.translateapp.model.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("SELECT * FROM translation_favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("DELETE FROM translation_favorites WHERE idFavorite = :id")
    suspend fun deleteFavoriteById(id: Long)
}