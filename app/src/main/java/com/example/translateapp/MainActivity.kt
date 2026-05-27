package com.example.translateapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val etInput = findViewById<EditText>(R.id.etInput)
        val btnTranslate = findViewById<Button>(R.id.btnTranslate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val btnSpeak = findViewById<ImageView>(R.id.btnSpeak)
        val btnCopy = findViewById<ImageView>(R.id.btnCopy)
        val btnFavorite = findViewById<ImageView>(R.id.btnFavorite)


        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.ENGLISH
            }
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

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}