package com.nisanurguven.wordleloop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nisanurguven.wordleloop.databinding.ActivityLlmBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LLMActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLlmBinding

    // Arkadaşının paylaştığı güncel API Key'i buraya koydum
    private val GEMINI_API_KEY = "AIzaSyB3QMaTEUX01mNsV8B8awfd0p3c38G71I8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLlmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Veritabanı bağlantısına artık gerek yok çünkü kelimeleri kullanıcıdan alıyoruz
        // dbHelper = DatabaseHelper(this)

        binding.btnGenerateStory.setOnClickListener {
            generateStoryFromInputs()
        }

        binding.btnTranslate.setOnClickListener {
            if (binding.tvTurkishStory.visibility == View.GONE) {
                binding.tvTurkishStory.visibility = View.VISIBLE
            } else {
                binding.tvTurkishStory.visibility = View.GONE
            }
        }
    }

    private fun generateStoryFromInputs() {
        // Kullanıcının EditText'lere girdiği kelimeleri alıyoruz
        val w1 = binding.etWord1.text.toString().trim().uppercase()
        val w2 = binding.etWord2.text.toString().trim().uppercase()
        val w3 = binding.etWord3.text.toString().trim().uppercase()
        val w4 = binding.etWord4.text.toString().trim().uppercase()
        val w5 = binding.etWord5.text.toString().trim().uppercase()

        // 5 kelimenin de girildiğinden emin olalım
        if (w1.isEmpty() || w2.isEmpty() || w3.isEmpty() || w4.isEmpty() || w5.isEmpty()) {
            Toast.makeText(this, "Lütfen 5 kelimeyi de girin!", Toast.LENGTH_SHORT).show()
            return
        }

        // Zincir kuralını kontrol edelim (Arkadaşının projesinden gelen özellik)
        if (checkChainLogic(w1, w2) && checkChainLogic(w2, w3) && checkChainLogic(w3, w4) && checkChainLogic(w4, w5)) {
            val wordsList = listOf(w1, w2, w3, w4, w5)
            generateStoryAndImage(wordsList)
        } else {
            Toast.makeText(this, "Kelimeler zincir kuralına uymuyor! Bir kelimenin son harfi, diğerinin ilk harfi olmalı.", Toast.LENGTH_LONG).show()
        }
    }

    // Zincir mantığını kontrol eden fonksiyon
    private fun checkChainLogic(wordA: String, wordB: String): Boolean {
        if (wordA.isEmpty() || wordB.isEmpty()) return false
        return wordA.last() == wordB.first()
    }

    private fun generateStoryAndImage(words: List<String>) {
        val wordListString = words.joinToString(", ")

        // UI Güncellemeleri
        binding.loadingProgress.visibility = View.VISIBLE
        binding.btnGenerateStory.isEnabled = false
        binding.btnGenerateStory.text = "Yapay Zeka Düşünüyor..."
        binding.tvEnglishStory.text = "Hikaye ve görsel yapay zeka tarafından oluşturuluyor...\nLütfen 10-15 saniye bekleyin."
        binding.tvTurkishStory.visibility = View.GONE
        binding.ivGeneratedImage.visibility = View.GONE
        binding.btnTranslate.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. GEMINI İLE HİKAYE ÜRETİMİ
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash", // En stabil model
                    apiKey = GEMINI_API_KEY
                )

                // İstemi (Prompt) güncelledim: İngilizce hikaye yazıp sonra Türkçe çeviri versin
                val textPrompt = "Aşağıdaki 5 İngilizce kelimeyi kullanarak İngilizce kısa ve yaratıcı bir hikaye yaz. " +
                        "İngilizce kelimeleri hikayenin içinde orijinal haliyle ve BÜYÜK HARFLERLE kullan. " +
                        "Sonra metnin altına 'TR:' yazarak Türkçe çevirisini ekle. " +
                        "Kelimeler: $wordListString. En fazla 4-5 cümle olsun."

                val response = generativeModel.generateContent(textPrompt)
                val fullText = response.text ?: ""

                // 2. POLLINATIONS İLE RESİM ÜRETİMİ
                var generatedBitmap: Bitmap? = null
                try {
                    val rawImagePrompt = "A magical cinematic illustration of ${words[0]}, ${words[1]} and ${words[2]}, highly detailed, fantasy art"
                    val encodedPrompt = URLEncoder.encode(rawImagePrompt, "UTF-8")
                    val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt"

                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    generatedBitmap = BitmapFactory.decodeStream(connection.inputStream)
                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 3. EKRANI GÜNCELLEME
                withContext(Dispatchers.Main) {
                    // Gemini'nin metnini İngilizce ve Türkçe olarak ayırma
                    if (fullText.contains("TR:")) {
                        binding.tvEnglishStory.text = fullText.substringBefore("TR:").trim()
                        binding.tvTurkishStory.text = fullText.substringAfter("TR:").trim()
                        binding.btnTranslate.visibility = View.VISIBLE
                    } else {
                        binding.tvEnglishStory.text = fullText
                    }

                    // Resmi gösterme
                    if (generatedBitmap != null) {
                        binding.ivGeneratedImage.visibility = View.VISIBLE
                        binding.ivGeneratedImage.setImageBitmap(generatedBitmap)
                    } else {
                        Toast.makeText(this@LLMActivity, "Resim indirilemedi.", Toast.LENGTH_SHORT).show()
                    }

                    // Butonu eski haline getirme
                    binding.loadingProgress.visibility = View.GONE
                    binding.btnGenerateStory.isEnabled = true
                    binding.btnGenerateStory.text = "✨ YENİDEN ÜRET"
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvEnglishStory.text = "Hikaye üretilirken hata oluştu: ${e.localizedMessage}"
                    binding.loadingProgress.visibility = View.GONE
                    binding.btnGenerateStory.isEnabled = true
                    binding.btnGenerateStory.text = "✨ TEKRAR DENE"
                }
            }
        }
    }
}