package com.nisanurguven.wordleloop

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var wordList = mutableListOf<Word>()
    private var passedWords = mutableListOf<Word>()
    private var currentWordIndex = 0
    private lateinit var currentWord: Word
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Veritabanından ayarlara göre filtrelenmiş kelimeleri yükle
        loadWords()

        // Pas geçme butonu mantığı
        binding.btnPass.setOnClickListener {
            if (currentWordIndex < wordList.size) {
                passedWords.add(wordList[currentWordIndex])
                moveToNext()
            }
        }
    }

    private fun loadWords() {
        wordList = dbHelper.getFilteredWords(this).toMutableList()

        if (wordList.isNotEmpty()) {
            showNextQuestion()
        } else {
            Toast.makeText(this, "Bugünlük tüm kelimeleri bitirdin veya kriterlere uygun kelime yok!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showNextQuestion() {
        // Liste bittiyse pas geçilenlere dön veya quizi bitir
        if (currentWordIndex >= wordList.size) {
            if (passedWords.isNotEmpty()) {
                wordList = passedWords.toMutableList()
                passedWords.clear()
                currentWordIndex = 0
                showNextQuestion()
            } else {
                Toast.makeText(this, "Harika! Quiz tamamlandı.", Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }

        currentWord = wordList[currentWordIndex]
        binding.tvQuestionWord.text = currentWord.english

        val options = generateOptions(currentWord.turkish)
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)

        buttons.forEachIndexed { index, button ->
            button.text = options[index]
            button.setBackgroundColor(Color.WHITE)
            button.isEnabled = true
            button.setOnClickListener {
                checkAnswer(button, options[index] == currentWord.turkish)
            }
        }
    }

    private fun checkAnswer(selectedButton: Button, isCorrect: Boolean) {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach { it.isEnabled = false }

        if (isCorrect) {
            selectedButton.setBackgroundColor(Color.GREEN)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            // Yanlış cevapta doğru olanı yeşil yakarak göster
            buttons.forEach { if (it.text == currentWord.turkish) it.setBackgroundColor(Color.GREEN) }
        }

        // Algoritmayı çalıştır ve veritabanını güncelle
        dbHelper.updateWordProgress(currentWord, isCorrect)

        Handler(Looper.getMainLooper()).postDelayed({
            moveToNext()
        }, 1000)
    }

    private fun moveToNext() {
        currentWordIndex++
        showNextQuestion()
    }

    private fun generateOptions(correctAnswer: String): List<String> {
        val wrongOptions = listOf("Masa", "Kalem", "Kitap", "Araba", "Ev", "Su", "Kedi", "Köpek")
            .filter { it != correctAnswer }
            .shuffled()
            .take(3)
            .toMutableList() // Değişken adı burada tanımlanıyor

        wrongOptions.add(correctAnswer) // Yukarıdaki isimle aynı olmalı
        return wrongOptions.shuffled()
    }
}