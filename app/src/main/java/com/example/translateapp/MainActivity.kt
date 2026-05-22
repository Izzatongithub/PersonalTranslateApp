package com.example.translateapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.translateapp.network.RetrofitClient
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch

import android.speech.tts.TextToSpeech
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val etInput = findViewById<EditText>(R.id.etInput)
        val btnTranslate = findViewById<Button>(R.id.btnTranslate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        //dictionary
        val etInputDictionary = findViewById<EditText>(R.id.etInputDictionary)
        val btnDictionary = findViewById<Button>(R.id.btnDictionary)
        val tvResultDictionary = findViewById<TextView>(R.id.tvResultDictionary)

        val btnSpeak = findViewById<Button>(R.id.btnSpeak)

        textToSpeech = TextToSpeech(this) { status ->

            if (status == TextToSpeech.SUCCESS) {

                textToSpeech.language = Locale.getDefault()

            }
        }

        btnTranslate.setOnClickListener {

            val inputText = etInput.text.toString()

            if (inputText.isEmpty()) {
                tvResult.text = "Input tidak boleh kosong"
                return@setOnClickListener
            }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.INDONESIAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()

            val translator = Translation.getClient(options)

            tvResult.text = "Downloading model..."

            translator.downloadModelIfNeeded()
                .addOnSuccessListener {

                    translator.translate(inputText)
                        .addOnSuccessListener { translatedText ->

                            tvResult.text = translatedText

                        }
                        .addOnFailureListener {

                            tvResult.text = "Gagal translate"
                        }
                }
                .addOnFailureListener {

                    tvResult.text = "Model gagal didownload"
                }
        }

        btnDictionary.setOnClickListener {
            val inputDictionary = etInputDictionary.text.toString().trim()

            if (inputDictionary.isEmpty()) {
                tvResultDictionary.text = "Input tidak boleh kosong"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiDictionary.getWordMeaning(inputDictionary)

                    if (response.isNotEmpty()) {
                        val data = response[0]
                        val sb = StringBuilder()

                        // Judul Kata & Fonetik
                        sb.append("Word: ${data.word}\n")
                        if (!data.phonetic.isNullOrEmpty()) {
                            sb.append("Phonetic: ${data.phonetic}\n")
                        }
                        sb.append("====================\n\n")

                        data.meanings?.forEach { meaning ->
                            // Jenis Kata (Noun, Verb, dll)
                            sb.append("Type: [${meaning.partOfSpeech?.uppercase()}]\n")
                            
                            // Definisi
                            meaning.definitions?.take(2)?.forEachIndexed { index, def ->
                                sb.append("${index + 1}. ${def.definition}\n")
                                if (!def.example.isNullOrEmpty()) {
                                    sb.append("   Ex: \"${def.example}\"\n")
                                }
                            }

                            // Sinonim (di level Meaning)
                            if (!meaning.synonyms.isNullOrEmpty()) {
                                sb.append("Synonyms: ${meaning.synonyms.joinToString(", ")}\n")
                            }

                            // Antonim (di level Meaning)
                            if (!meaning.antonyms.isNullOrEmpty()) {
                                sb.append("Antonyms: ${meaning.antonyms.joinToString(", ")}\n")
                            }
                            
                            sb.append("\n")
                        }

                        tvResultDictionary.text = sb.toString()
                    } else {
                        tvResultDictionary.text = "Kata tidak ditemukan"
                    }

                } catch (e: Exception) {
                    tvResultDictionary.text = "Error: Kata tidak ditemukan atau masalah koneksi"
                }
            }
        }

        btnSpeak.setOnClickListener {

            val text = tvResult.text.toString()

            textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }

    }

    override fun onDestroy() {

        textToSpeech.stop()
        textToSpeech.shutdown()

        super.onDestroy()
    }

}