package com.nisanurguven.wordleloop

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding kurulumu
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Kelime Yükleme Kontrolü
        checkAndImportCSV()

        // Tıklama Olayları
        setupClickListeners()
    }

    private fun checkAndImportCSV() {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isImported = sharedPref.getBoolean("isImported", false)

        if (!isImported) {
            dbHelper.importCSV(this)
            sharedPref.edit().putBoolean("isImported", true).apply()
            Toast.makeText(this, "Kelimeler başarıyla hazırlandı!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        // --- YENİ EKLENEN/GÜNCELLENEN BUTONLAR ---

        // Sağ Üstteki Ayarlar İkonu
        binding.btnSettingsTop.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Yeni Öğren (Flashcards) Kartı
        // XML'de verdiğimiz yeni ID: cardFlashcards
        binding.cardFlashcards.setOnClickListener {
            val intent = Intent(this, FlashcardActivity::class.java)
            startActivity(intent)
        }

        // --- DİĞER BUTONLAR ---

        binding.cardAddWord.setOnClickListener {
            startActivity(Intent(this, AddWordActivity::class.java))
        }

        binding.cardQuiz.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        binding.cardWordle.setOnClickListener {
            startActivity(Intent(this, WordleActivity::class.java))
        }

        binding.cardLLM.setOnClickListener {
            Toast.makeText(this, "AI Asistan Hazırlanıyor...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LLMActivity::class.java))
        }

        binding.cardReport.setOnClickListener {
            // Rapor modülü artık aktif, bu yüzden uyarıyı sildik
            startActivity(Intent(this, ReportActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Her geri dönüldüğünde seri (streak) bilgisini tazele
        updateStreakDisplay()
    }

    private fun updateStreakDisplay() {
        val streak = dbHelper.getUserStreak()
        binding.tvStreak.text = "🔥 $streak Günlük Seri"

        if (streak > 0) {
            binding.tvStreak.setTextColor(Color.parseColor("#8F1A30")) // Vurgulu Kırmızı/Turuncu
        } else {
            binding.tvStreak.setTextColor(Color.parseColor("#1A4B8F")) // Standart Mavi
        }
    }
}