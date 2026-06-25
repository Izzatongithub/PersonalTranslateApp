package com.example.translateapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.example.translateapp.model.SharedTranslationViewModel
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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

    private lateinit var translator: Translator

    private var sourceLangCode = "ID"
    private var targetLangCode = "EN"

    private lateinit var etInput: EditText
    private lateinit var tvResult: TextView
    private lateinit var tvSourceLanguage: TextView
    private lateinit var tvTargetLanguage: TextView

    private lateinit var sharedViewModel: SharedTranslationViewModel
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    private lateinit var imageUri: Uri
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher =
            registerForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->

                uri?.let {
                    processImage(it)
                }
            }

        cameraLauncher =
            registerForActivityResult(
                ActivityResultContracts.TakePicture()
            ) { success ->

                if(success){
                    processImage(imageUri)
                }
            }

        cameraPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->

                if (granted) {

                    imageUri = createImageUri()
                    cameraLauncher.launch(imageUri)

                } else {

                    Toast.makeText(
                        requireContext(),
                        "Izin kamera ditolak",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        favoriteDao = database.favoriteDao()
        historyDao = database.historyDao()

        etInput = view.findViewById(R.id.etInput)
        tvResult = view.findViewById(R.id.tvResult)
        val btnTranslate = view.findViewById<Button>(R.id.btnTranslate)

        val btnSpeak = view.findViewById<ImageView>(R.id.btnSpeak)
        val btnCopy = view.findViewById<ImageView>(R.id.btnCopy)
        val btnFavorite = view.findViewById<ImageView>(R.id.btnFavorite)
        val btnSwapLang = view.findViewById<ImageView>(R.id.btnSwapLang)

        tvSourceLanguage = view.findViewById<TextView>(R.id.tvSourceLanguage)
        tvTargetLanguage = view.findViewById<TextView>(R.id.tvTargetLanguage)

        sharedViewModel =
            ViewModelProvider(requireActivity())
                .get(SharedTranslationViewModel::class.java)

        observeTranslation()

        //Inisialisasi text to speech
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (targetLanguage == TranslateLanguage.ENGLISH) {
                textToSpeech.language = Locale.ENGLISH
            } else {
                textToSpeech.language = Locale("id")
            }
        }

        val btnOCR = view.findViewById<ImageView>(R.id.cameraTranslateFragment)
        println("btnOCR = $btnOCR")

        btnOCR.setOnClickListener {
//            imagePickerLauncher.launch("image/*")
            showImageSourceDialog()
        }

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
                        sourceLang = sourceLangCode,
                        targetLang = targetLangCode
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

    private fun observeTranslation() {

        sharedViewModel.sourceText.observe(
            viewLifecycleOwner
        ) {
            etInput.setText(it)
        }

        sharedViewModel.translatedText.observe(
            viewLifecycleOwner
        ) {
            tvResult.text = it
        }

        sharedViewModel.sourceLang.observe(
            viewLifecycleOwner
        ) { lang ->

            sourceLangCode = lang

            sourceLanguage =
                if (lang == "ID")
                    TranslateLanguage.INDONESIAN
                else
                    TranslateLanguage.ENGLISH

            tvSourceLanguage.text =
                if (lang == "ID")
                    "Indonesia"
                else
                    "English"
        }

        sharedViewModel.targetLang.observe(
            viewLifecycleOwner
        ) { lang ->

            targetLangCode = lang

            targetLanguage =
                if (lang == "ID")
                    TranslateLanguage.INDONESIAN
                else
                    TranslateLanguage.ENGLISH

            tvTargetLanguage.text =
                if (lang == "ID")
                    "Indonesia"
                else
                    "English"
        }
    }

    private fun processImage(uri: Uri) {

        val image =
            InputImage.fromFilePath(
                requireContext(),
                uri
            )

        val recognizer =
            TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

        recognizer.process(image)
            .addOnSuccessListener { visionText ->

//                etInput.setText(
//                    visionText.text
//                )

                val extractedText =
                    visionText.text

                etInput.setText(extractedText)

                translateText(extractedText)

            }
            .addOnFailureListener {

                Toast.makeText(
                    requireContext(),
                    "Gagal membaca teks",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun createImageUri(): Uri {

        val file = File(
            requireContext().cacheDir,
            "camera_image.jpg"
        )

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
    }

    private fun showImageSourceDialog() {

        val options = arrayOf(
            "Ambil dari Kamera",
            "Pilih dari Galeri"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
//                        imageUri = createImageUri()
//                        cameraLauncher.launch(imageUri)
                        if (
                            ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {

                            imageUri = createImageUri()
                            cameraLauncher.launch(imageUri)

                        } else {

                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    }

                    1 -> {
                        imagePickerLauncher.launch("image/*")
                    }
                }
            }
            .show()
    }

    private fun translateText(text: String) {

        val options =
            TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

        val translator =
            Translation.getClient(options)

        tvResult.text = "Translating..."

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {

                translator.translate(text)
                    .addOnSuccessListener { result ->

                        tvResult.text = result

                        saveToHistory(
                            text,
                            result,
                            sourceLangCode,
                            targetLangCode
                        )
                    }
            }
    }

}