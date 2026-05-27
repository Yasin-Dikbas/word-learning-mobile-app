package com.nisanurguven.wordleloop

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nisanurguven.wordleloop.databinding.ActivityLlmBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LLMActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLlmBinding
    private val GEMINI_API_KEY = ApiKeys.GEMINI

    // Üretilen son resmi bellekte tutmak için değişken
    private var currentGeneratedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLlmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerateStory.setOnClickListener {
            generateStoryFromInputs()
        }

        binding.btnTranslate.setOnClickListener {
            binding.tvTurkishStory.visibility = if (binding.tvTurkishStory.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // RESMİ KAYDET BUTONU
        binding.btnSaveImage.setOnClickListener {
            currentGeneratedBitmap?.let { bitmap ->
                saveImageToGallery(bitmap)
            } ?: run {
                Toast.makeText(this, "Önce bir resim üretmelisiniz!", Toast.LENGTH_SHORT).show()
            }
        }

        // GALERİYİ AÇ BUTONU
        binding.btnViewGallery.setOnClickListener {
            openDeviceGallery()
        }
    }

    private fun generateStoryFromInputs() {
        val w1 = binding.etWord1.text.toString().trim().uppercase()
        val w2 = binding.etWord2.text.toString().trim().uppercase()
        val w3 = binding.etWord3.text.toString().trim().uppercase()
        val w4 = binding.etWord4.text.toString().trim().uppercase()
        val w5 = binding.etWord5.text.toString().trim().uppercase()

        if (w1.isEmpty() || w2.isEmpty() || w3.isEmpty() || w4.isEmpty() || w5.isEmpty()) {
            Toast.makeText(this, "Lütfen 5 kelimeyi de girin!", Toast.LENGTH_SHORT).show()
            return
        }

        if (checkChainLogic(w1, w2) && checkChainLogic(w2, w3) && checkChainLogic(w3, w4) && checkChainLogic(w4, w5)) {
            val wordsList = listOf(w1, w2, w3, w4, w5)
            generateStoryAndImage(wordsList)
        } else {
            Toast.makeText(this, "Kelimeler zincir kuralına uymuyor! Bir kelimenin son harfi, diğerinin ilk harfi olmalı.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkChainLogic(wordA: String, wordB: String): Boolean {
        if (wordA.isEmpty() || wordB.isEmpty()) return false
        return wordA.last() == wordB.first()
    }

    private fun generateStoryAndImage(words: List<String>) {
        // Kelimeleri aralarında ok işareti olacak şekilde dizdik (Sırayı vurgulamak için)
        val wordSequence = words.joinToString(" -> ")

        binding.loadingProgress.visibility = View.VISIBLE
        binding.btnGenerateStory.isEnabled = false
        binding.btnGenerateStory.text = "Yapay Zeka Düşünüyor..."
        binding.tvEnglishStory.text = "Hikaye ve görsel yapay zeka tarafından oluşturuluyor...\nLütfen 10-15 saniye bekleyin."
        binding.tvTurkishStory.visibility = View.GONE
        binding.ivGeneratedImage.visibility = View.GONE
        binding.btnTranslate.visibility = View.GONE
        binding.btnSaveImage.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = GEMINI_API_KEY
                )

                // ALGORİTMA GÜNCELLEMESİ: Sıra zorunluluğu eklendi
                val textPrompt = """
                    Aşağıdaki 5 İngilizce kelimeyi kullanarak İngilizce kısa ve yaratıcı bir hikaye yaz. 
                    KESİN KURALLAR:
                    1. Kelimeleri KESİNLİKLE sana verdiğim SIRA İLE (1. kelimeden 5. kelimeye doğru) kullanmalısın.
                    2. İngilizce kelimeleri hikayenin içinde orijinal haliyle ve BÜYÜK HARFLERLE yaz.
                    3. Metnin en altına 'TR:' yazarak Türkçe çevirisini ekle.
                    Kelimeler (Sırasıyla): $wordSequence. En fazla 4-5 cümle olsun.
                """.trimIndent()

                val response = generativeModel.generateContent(textPrompt)
                val fullText = response.text ?: ""

                var generatedBitmap: Bitmap? = null
                try {
                    // EKSİK TAMAMLANDI: Sadece ilk 3 kelime yerine 5 kelimenin de görsel sırasına dahil edilmesi sağlandı.
                    val rawImagePrompt = "A magical cinematic illustration of ${words.joinToString(", ")}, highly detailed, fantasy art"
                    val encodedPrompt = URLEncoder.encode(rawImagePrompt, "UTF-8")
                    val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt"

                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 25000
                    connection.readTimeout = 25000
                    connection.connect()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        generatedBitmap = BitmapFactory.decodeStream(connection.inputStream)
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                withContext(Dispatchers.Main) {
                    if (fullText.contains("TR:")) {
                        binding.tvEnglishStory.text = fullText.substringBefore("TR:").trim()
                        binding.tvTurkishStory.text = fullText.substringAfter("TR:").trim()
                        binding.btnTranslate.visibility = View.VISIBLE
                    } else {
                        binding.tvEnglishStory.text = fullText
                    }

                    if (generatedBitmap != null) {
                        currentGeneratedBitmap = generatedBitmap // Resmi kaydetmek için hafızaya aldık
                        binding.ivGeneratedImage.visibility = View.VISIBLE
                        binding.ivGeneratedImage.setImageBitmap(generatedBitmap)
                        binding.btnSaveImage.visibility = View.VISIBLE // Kaydet butonunu görünür yaptık
                    } else {
                        Toast.makeText(this@LLMActivity, "Resim üretilemedi, sadece hikaye gösteriliyor.", Toast.LENGTH_SHORT).show()
                    }

                    binding.loadingProgress.visibility = View.GONE
                    binding.btnGenerateStory.isEnabled = true
                    binding.btnGenerateStory.text = "✨ YENİDEN ÜRET"
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvEnglishStory.text = "Hata Türü: ${e.javaClass.simpleName}\nDetay: ${e.localizedMessage}"
                    binding.tvTurkishStory.visibility = View.GONE
                    binding.loadingProgress.visibility = View.GONE
                    binding.btnGenerateStory.isEnabled = true
                    binding.btnGenerateStory.text = "✨ TEKRAR DENE"
                }
            }
        }
    }

    // GÖRSELİ TELEFONA KAYDETME FONKSİYONU
    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "WordleLoop_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WordleLoop")
                    }
                    val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "WordleLoop")
                if (!appDir.exists()) appDir.mkdir()
                val image = File(appDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(this, "✅ Resim başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Resim kaydedilirken hata oluştu: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // GALERİYİ AÇMA FONKSİYONU
    private fun openDeviceGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Galeri uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}