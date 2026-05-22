package com.example.translateapp.model

data class TranslateRequest(
    val q: String,
    val source: String,
    val target: String,
    val format: String = "text"
)