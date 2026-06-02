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
import com.example.translateapp.model.HistoryEntity
import com.example.translateapp.ui.HistoryActivity
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var database: AppDatabase

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

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.ENGLISH
            }
        }

        btnOpenHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
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
            Toast.makeText(this, "Ditambahkan ke Favorit!", Toast.LENGTH_SHORT).show()
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
