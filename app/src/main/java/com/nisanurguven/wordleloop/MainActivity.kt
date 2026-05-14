package com.nisanurguven.wordleloop

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // View Binding ve DatabaseHelper tanımlamaları
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding kurulumu - R.layout.activity_main yerine binding.root kullanılmalı
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Veritabanı yardımcısını başlatıyoruz
        dbHelper = DatabaseHelper(this)

        // Tıklama Olayları
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Kelime Ekleme Sayfasına Geçiş
        binding.cardAddWord.setOnClickListener {
            val intent = Intent(this, AddWordActivity::class.java)
            startActivity(intent)
        }

        // Quiz Ekranına Geçiş
        binding.cardQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
        }

        // Ayarlar Ekranına Geçiş
        binding.cardSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Henüz hazır olmayan modüller için Toast mesajları
        binding.cardWordle.setOnClickListener {
            Toast.makeText(this, "Wordle Oyunu Yakında!", Toast.LENGTH_SHORT).show()
        }

        binding.cardLLM.setOnClickListener {
            Toast.makeText(this, "AI Asistan Hazırlanıyor...", Toast.LENGTH_SHORT).show()
        }

        binding.cardReport.setOnClickListener {
            Toast.makeText(this, "Raporlar Modülü Henüz Aktif Değil", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }
    }

    // Yaşam döngüsü: Kullanıcı her geri döndüğünde (örneğin Quiz bittiğinde) seri sayacı yenilenir
    override fun onResume() {
        super.onResume()
        updateStreakDisplay()
    }

    private fun updateStreakDisplay() {
        // DatabaseHelper üzerinden son 24 saatlik aktivite kontrolü yapılır
        val streak = dbHelper.getUserStreak()

        // Seri yazısını günceller
        binding.tvStreak.text = "Seri: $streak Gün"

        if (streak > 0) {
            // Seri varsa turuncu (vurgulu) renk yapar
            binding.tvStreak.setTextColor(Color.parseColor("#FF9800"))
        } else {
            // Seri yoksa standart koyu mavi renk
            binding.tvStreak.setTextColor(Color.parseColor("#1A4B8F"))
        }
    }
}