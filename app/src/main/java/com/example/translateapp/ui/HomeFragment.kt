package com.example.translateapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.translateapp.data.AppDatabase
import com.example.translateapp.data.FavoriteDao
import com.example.translateapp.model.FavoriteEntity
import com.example.translateapp.model.HistoryEntity
import com.example.translateapp.R
import com.example.translateapp.data.HistoryDao
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var favoriteDao: FavoriteDao
    private lateinit var historyDao: HistoryDao
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var database: AppDatabase
    private var isFavorite = false
    private var currentFavorite: FavoriteEntity? = null

    //Inisialisasi bahasa
    private var sourceLanguage = TranslateLanguage.INDONESIAN
    private var targetLanguage = TranslateLanguage.ENGLISH

    private var sourceLangCode = "ID"
    private var targetLangCode = "EN"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(requireContext())
        favoriteDao = database.favoriteDao()
        historyDao = database.historyDao()

        val etInput = view.findViewById<EditText>(R.id.etInput)
        val btnTranslate = view.findViewById<Button>(R.id.btnTranslate)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val btnSpeak = view.findViewById<ImageView>(R.id.btnSpeak)
        val btnCopy = view.findViewById<ImageView>(R.id.btnCopy)
        val btnFavorite = view.findViewById<ImageView>(R.id.btnFavorite)
        val btnSwapLang = view.findViewById<ImageView>(R.id.btnSwapLang)

        val tvSourceLanguage = view.findViewById<TextView>(R.id.tvSourceLanguage)
        val tvTargetLanguage = view.findViewById<TextView>(R.id.tvTargetLanguage)


        val tvWordCount = view.findViewById<TextView>(R.id.tvWordCount)


        etInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s.toString().trim()
                val wordCount = if (text.isEmpty()) 0 else text.split("\\s+".toRegex()).size


                tvWordCount.text = "$wordCount/1500"

                if (wordCount > 1500) {
                    tvWordCount.setTextColor(android.graphics.Color.RED)
                } else {
                    tvWordCount.setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
                }
            }
        })

//        isFavorite = false

        //Inisialisasi text to speech
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (targetLanguage == TranslateLanguage.ENGLISH) {
                textToSpeech.language = Locale.ENGLISH
            } else {
                textToSpeech.language = Locale("id")
            }
        }

//        btnOpenHistory.setOnClickListener {
//            startActivity(Intent(requireContext(), HistoryFragment::class.java))
//        }
//
//        btnOpenDictionary.setOnClickListener {
//            startActivity(Intent(requireContext(), DictionaryFragment::class.java))
//        }

        btnSwapLang.setOnClickListener {

            val tempLanguage = sourceLanguage
            sourceLanguage = targetLanguage
            targetLanguage = tempLanguage

            val tempCode = sourceLangCode
            sourceLangCode = targetLangCode
            targetLangCode = tempCode

            val tempText =
                tvSourceLanguage.text

            tvSourceLanguage.text =
                tvTargetLanguage.text

            tvTargetLanguage.text =
                tempText

            Toast.makeText(
                requireContext(),
                "${tvSourceLanguage.text} → ${tvTargetLanguage.text}",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnTranslate.setOnClickListener {
            val inputText = etInput.text.toString().trim()


            val wordCount = if (inputText.isEmpty()) 0 else inputText.split("\\s+".toRegex()).size

            if (wordCount > 1500) {
                Toast.makeText(requireContext(), "Teks melebihi batas maksimal 1500 kata!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputText.isNotEmpty()) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build()

                val translator = Translation.getClient(options)
                tvResult.text = "Translating..."

                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(inputText)
                            .addOnSuccessListener { translatedText ->
                                tvResult.text = translatedText
                                saveToHistory(inputText, translatedText, sourceLangCode, targetLangCode)
                            }
                            .addOnFailureListener {
                                tvResult.text = "Gagal menerjemahkan teks."
                            }
                    }
                    .addOnFailureListener {
                        tvResult.text = "Model bahasa gagal diunduh."
                    }
            } else {
                tvResult.text = "Input teks translator kosong."
            }
        }

        btnSpeak.setOnClickListener {
            val text = tvResult.text.toString()
            if (text.isNotEmpty() && text != "Hasil terjemahan teks akan muncul di sini..." && text != "Translating...") {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                Toast.makeText(requireContext(), "Tidak ada teks untuk dibaca", Toast.LENGTH_SHORT).show()
            }
        }

        btnCopy.setOnClickListener {
            val textToCopy = tvResult.text.toString()
            if (textToCopy.isNotEmpty() && textToCopy != "Hasil terjemahan teks akan muncul di sini...") {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Translated Text", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Teks berhasil disalin!", Toast.LENGTH_SHORT).show()
            }
        }

        btnFavorite.setOnClickListener {

            val source = etInput.text.toString()
            val result = tvResult.text.toString()

            if (!isFavorite) {

                if (result.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada hasil untuk difavoritkan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val id = database.favoriteDao().insertFavorite(
                        FavoriteEntity(
                            sourceText = source,
                            translatedText = result,
                            sourceLang = sourceLangCode,
                            targetLang = targetLangCode
                        )
                    )

                    currentFavorite = FavoriteEntity(
                        idFavorite = id,
                        sourceText = source,
                        translatedText = result,
                        sourceLang = "ID",
                        targetLang = "EN"
                    )

                    isFavorite = true
                    btnFavorite.setImageResource(R.drawable.ic_favorite_red)

                    Toast.makeText(requireContext(), "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                }

            } else {

                lifecycleScope.launch {
                    currentFavorite?.idFavorite?.let { id ->
                        database.favoriteDao().deleteFavoriteById(id)
                    }

                    isFavorite = false
                    btnFavorite.setImageResource(R.drawable.ic_favorites_new)

                    Toast.makeText(requireContext(), "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToHistory(inputText: String, translatedText: String, sourceLangCode: String, targetLangCode: String) {
        lifecycleScope.launch {
            val history = HistoryEntity(
                sourceText = inputText,
                translatedText = translatedText,
                sourceLang = sourceLangCode,
                targetLang = targetLangCode
            )
            database.historyDao().insertHistory(history)
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}