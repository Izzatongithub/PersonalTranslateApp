package com.example.translateapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val idFavorite: Int = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val createdAt: Long = System.currentTimeMillis()
)