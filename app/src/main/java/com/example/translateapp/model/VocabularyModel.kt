package com.example.translateapp.model

data class VocabularyModel(
    val word: String? = null,
    val phonetic: String? = null,
    val phonetics: List<Phonetic>? = null,
    val meanings: List<Meaning>? = null
)

data class Phonetic(
    val text: String? = null
)

data class Meaning(
    val partOfSpeech: String? = null,
    val definitions: List<Definition>? = null,
    val synonyms: List<String>? = null,
    val antonyms: List<String>? = null
)

data class Definition(
    val definition: String? = null,
    val example: String? = null,
    val synonyms: List<String>? = null,
    val antonyms: List<String>? = null
)
