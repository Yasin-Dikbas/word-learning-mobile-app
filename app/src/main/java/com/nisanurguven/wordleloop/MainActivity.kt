package com.nisanurguven.wordleloop

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Yeni tasarladığımız dashboard layout'unu set ediyoruz
        setContentView(R.layout.activity_main)

        // XML'deki Kartları (Butonları) Tanımlıyoruz
        val cardAddWord = findViewById<CardView>(R.id.cardAddWord)
        val cardQuiz = findViewById<CardView>(R.id.cardQuiz)
        val cardWordle = findViewById<CardView>(R.id.cardWordle)
        val cardLLM = findViewById<CardView>(R.id.cardLLM)
        val cardReport = findViewById<CardView>(R.id.cardReport)
        val cardSettings = findViewById<CardView>(R.id.cardSettings)

        // Tıklama Olayları (İleride buralara startActivity(Intent(...)) eklenecek)
        cardAddWord.setOnClickListener {
            Toast.makeText(this, "Kelime Ekleme Sayfası", Toast.LENGTH_SHORT).show()
        }

        cardQuiz.setOnClickListener {
            Toast.makeText(this, "Quiz Başlatılıyor...", Toast.LENGTH_SHORT).show()
        }

        cardWordle.setOnClickListener {
            Toast.makeText(this, "Wordle Oyunu", Toast.LENGTH_SHORT).show()
        }

        cardLLM.setOnClickListener {
            Toast.makeText(this, "AI Asistan (LLM) açılıyor...", Toast.LENGTH_SHORT).show()
        }

        cardReport.setOnClickListener {
            Toast.makeText(this, "Raporlar Hazırlanıyor...", Toast.LENGTH_SHORT).show()
        }

        cardSettings.setOnClickListener {
            Toast.makeText(this, "Ayarlar", Toast.LENGTH_SHORT).show()
        }
    }
}