package com.example.translateapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.translateapp.data.AppDatabase
import com.example.translateapp.data.FavoriteDao
import com.example.translateapp.model.FavoriteEntity
import com.example.translateapp.model.HistoryEntity
import com.example.translateapp.ui.FavoriteActivity
import com.example.translateapp.ui.DictionaryActivity
import com.example.translateapp.ui.FavoriteAdapter
import com.example.translateapp.ui.HistoryActivity
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var database: AppDatabase
    private var isFavorite = false
    private var currentFavorite: FavoriteEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)

        val etInput = findViewById<EditText>(R.id.etInput)
        val btnTranslate = findViewById<Button>(R.id.btnTranslate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val btnSpeak = findViewById<ImageView>(R.id.btnSpeak)
        val btnCopy = findViewById<ImageView>(R.id.btnCopy)
        val btnFavorite = findViewById<ImageView>(R.id.btnFavorite)
        val btnOpenHistory = findViewById<ImageView>(R.id.btnOpenHistory)
        val btnOpenDictionary = findViewById<ImageView>(R.id.btnOpenDictionary)

        val btnFavoriteDua = findViewById<ImageView>(R.id.btnFavoriteDua)
        isFavorite = false

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.ENGLISH
            }
        }

        btnOpenHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnOpenDictionary.setOnClickListener {
            startActivity(Intent(this, DictionaryActivity::class.java))
        }

        btnTranslate.setOnClickListener {
            val inputText = etInput.text.toString().trim()

            if (inputText.isNotEmpty()) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.INDONESIAN)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()

                val translator = Translation.getClient(options)
                tvResult.text = "Translating..."

                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(inputText)
                            .addOnSuccessListener { translatedText ->
                                tvResult.text = translatedText
                                saveToHistory(inputText, translatedText, "ID", "EN")
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
                Toast.makeText(this, "Tidak ada teks untuk dibaca", Toast.LENGTH_SHORT).show()
            }
        }

        btnCopy.setOnClickListener {
            val textToCopy = tvResult.text.toString()
            if (textToCopy.isNotEmpty() && textToCopy != "Hasil terjemahan teks akan muncul di sini...") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Translated Text", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Teks berhasil disalin!", Toast.LENGTH_SHORT).show()
            }
        }

        btnFavorite.setOnClickListener {

            val source = etInput.text.toString()
            val result = tvResult.text.toString()

            if (!isFavorite) {

                if (result.isEmpty()) {
                    Toast.makeText(this, "Tidak ada hasil untuk difavoritkan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val id = database.favoriteDao().insertFavorite(
                        FavoriteEntity(
                            sourceText = source,
                            translatedText = result,
                            sourceLang = "ID",
                            targetLang = "EN"
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

                    Toast.makeText(this@MainActivity, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                }

            } else {

                lifecycleScope.launch {
                    currentFavorite?.idFavorite?.let { id ->
                        database.favoriteDao().deleteFavoriteById(id)
                    }

                    isFavorite = false
                    btnFavorite.setImageResource(R.drawable.ic_favorites_new)

                    Toast.makeText(this@MainActivity, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToHistory(source: String, result: String, sourceLang: String, targetLang: String) {
        lifecycleScope.launch {
            val history = HistoryEntity(
                sourceText = source,
                translatedText = result,
                sourceLang = sourceLang,
                targetLang = targetLang
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