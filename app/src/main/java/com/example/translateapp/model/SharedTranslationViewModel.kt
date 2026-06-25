package com.example.translateapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedTranslationViewModel : ViewModel() {

    private val _sourceText = MutableLiveData<String>()
    private val _translatedText = MutableLiveData<String>()
    private val _sourceLang = MutableLiveData<String>()
    private val _targetLang = MutableLiveData<String>()

    val sourceText: LiveData<String> = _sourceText
    val translatedText: LiveData<String> = _translatedText
    val sourceLang: LiveData<String> = _sourceLang
    val targetLang: LiveData<String> = _targetLang

    fun setTranslation(
        source: String,
        result: String,
        sourceLang: String,
        targetLang: String
    ) {
        _sourceText.value = source
        _translatedText.value = result
        _sourceLang.value = sourceLang
        _targetLang.value = targetLang
    }
}