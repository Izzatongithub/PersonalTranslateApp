package com.example.translateapp.network

import com.example.translateapp.model.TranslateRequest
import com.example.translateapp.model.TranslateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslateApi {

    @POST("translate")
    fun translate(
        @Body request: TranslateRequest
    ): Call<TranslateResponse>
}