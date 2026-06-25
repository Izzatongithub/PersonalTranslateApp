package com.example.translateapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.translateapp.R
import com.example.translateapp.model.VocabularyModel
import com.example.translateapp.network.RetrofitClient
import kotlinx.coroutines.launch

class DictionaryFragment : Fragment() {

    private lateinit var etWord: EditText
    private lateinit var btnSearch: AppCompatButton
    private lateinit var progressBar: ProgressBar

    private lateinit var tvWord: TextView
    private lateinit var tvPhonetic: TextView
    private lateinit var tvPartOfSpeech: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var tvExample: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_dictionary,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)


        btnSearch.setOnClickListener {
            val word = etWord.text.toString().trim()

            if (word.isNotEmpty()) {
                searchWord(word)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Masukkan kata terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun bindViews(view: View) {

        etWord = view.findViewById(R.id.etWord)
        btnSearch = view.findViewById(R.id.btnSearch)
        progressBar = view.findViewById(R.id.progressBar)

        tvWord = view.findViewById(R.id.tvWord)
        tvPhonetic = view.findViewById(R.id.tvPhonetic)
        tvPartOfSpeech = view.findViewById(R.id.tvPartOfSpeech)
        tvDefinition = view.findViewById(R.id.tvDefinition)
        tvExample = view.findViewById(R.id.tvExample)
    }

    private fun searchWord(word: String) {
        showLoading(true)
        clearResult()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result =
                    RetrofitClient.apiDictionary
                        .getWordMeaning(word)

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

    private fun displayResult(entry: VocabularyModel) {
        tvWord.text = entry.word ?: "-"
        tvPhonetic.text = entry.phonetic ?: ""

        val firstMeaning = entry.meanings?.firstOrNull()
        val firstDefinition = firstMeaning?.definitions?.firstOrNull()

        tvPartOfSpeech.text = firstMeaning?.partOfSpeech ?: ""

        val definitionInEnglish = firstDefinition?.definition

        if (!definitionInEnglish.isNullOrEmpty()) {

            tvDefinition.text = "Menerjemahkan definisi..."


            val options = com.google.mlkit.nl.translate.TranslatorOptions.Builder()
                .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
                .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.INDONESIAN)
                .build()

            val translator = com.google.mlkit.nl.translate.Translation.getClient(options)

            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translator.translate(definitionInEnglish)
                        .addOnSuccessListener { translatedDefinition ->

                            tvDefinition.text = translatedDefinition
                        }
                        .addOnFailureListener {

                            tvDefinition.text = definitionInEnglish
                        }
                }
                .addOnFailureListener {
                    tvDefinition.text = definitionInEnglish
                }
        } else {
            tvDefinition.text = "Definisi tidak ditemukan."
        }


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
        tvDefinition.text = "Coba periksa kembali ejaan kata bahasa Inggris yang kamu cari."
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
        progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        btnSearch.isEnabled = !isLoading
    }
}