package com.example.translateapp.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.translateapp.R
import com.example.translateapp.network.RetrofitClient
import kotlinx.coroutines.launch

class DictionaryActivity : AppCompatActivity() {

    private lateinit var etWord: EditText
    private lateinit var btnSearch: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private lateinit var tvWord: TextView
    private lateinit var tvPhonetic: TextView
    private lateinit var tvPartOfSpeech: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var tvExample: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)

        setupToolbar()
        bindViews()

        btnSearch.setOnClickListener {
            val word = etWord.text.toString().trim()
            if (word.isNotEmpty()) {
                searchWord(word)
            } else {
                Toast.makeText(this, "Masukkan kata terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarDictionary)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun bindViews() {
        etWord = findViewById(R.id.etWord)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)

        tvWord = findViewById(R.id.tvWord)
        tvPhonetic = findViewById(R.id.tvPhonetic)
        tvPartOfSpeech = findViewById(R.id.tvPartOfSpeech)
        tvDefinition = findViewById(R.id.tvDefinition)
        tvExample = findViewById(R.id.tvExample)
    }

    private fun searchWord(word: String) {
        showLoading(true)
        clearResult()

        lifecycleScope.launch {
            try {
                val result = RetrofitClient.apiDictionary.getWordMeaning(word)
                val firstEntry = result.firstOrNull()

                if (firstEntry != null) {
                    displayResult(firstEntry)
                } else {
                    showNotFound()
                }
            } catch (e: Exception) {
                showNotFound()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayResult(entry: com.example.translateapp.model.VocabularyModel) {
        tvWord.text = entry.word ?: "-"
        tvPhonetic.text = entry.phonetic ?: ""

        val firstMeaning = entry.meanings?.firstOrNull()
        val firstDefinition = firstMeaning?.definitions?.firstOrNull()

        tvPartOfSpeech.text = firstMeaning?.partOfSpeech ?: ""
        tvDefinition.text = firstDefinition?.definition ?: "Definisi tidak ditemukan."

        val example = firstDefinition?.example
        if (!example.isNullOrEmpty()) {
            tvExample.visibility = View.VISIBLE
            tvExample.text = "\"$example\""
        } else {
            tvExample.visibility = View.GONE
        }
    }

    private fun showNotFound() {
        tvWord.text = "Kata tidak ditemukan"
        tvPhonetic.text = ""
        tvPartOfSpeech.text = ""
        tvDefinition.text = "Coba periksa kembali ejaan kata yang dicari."
        tvExample.visibility = View.GONE
    }

    private fun clearResult() {
        tvWord.text = ""
        tvPhonetic.text = ""
        tvPartOfSpeech.text = ""
        tvDefinition.text = ""
        tvExample.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !isLoading
    }
}