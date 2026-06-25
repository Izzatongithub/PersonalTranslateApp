package com.example.translateapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL_DICTIONARY =
        "https://api.dictionaryapi.dev/"

    val apiDictionary: DictionaryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_DICTIONARY)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApi::class.java)
    }
}