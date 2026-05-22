package com.example.translateapp.network

import com.example.translateapp.model.VocabularyModel
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun getWordMeaning(
        @Path("word") word: String
    ): List<VocabularyModel>
}