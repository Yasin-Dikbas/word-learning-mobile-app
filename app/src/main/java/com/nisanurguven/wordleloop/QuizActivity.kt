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

        // Veritabanından ayarlara göre filtrelenmiş kelimeleri al
        wordList = dbHelper.getFilteredWords(this).toMutableList()

        if (wordList.isNotEmpty()) {
            showNextQuestion()
        } else {
            Toast.makeText(this, "Seçilen kriterlere uygun kelime bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
        }

        // Pas geçme butonu
        binding.btnPass.setOnClickListener {
            if (currentWordIndex < wordList.size) {
                passedWords.add(wordList[currentWordIndex])
                moveToNext()
            }
        }
    }

    private fun showNextQuestion() {
        if (currentWordIndex >= wordList.size) {
            if (passedWords.isNotEmpty()) {
                wordList = passedWords.toMutableList()
                passedWords.clear()
                currentWordIndex = 0
                showNextQuestion()
            } else {
                Toast.makeText(this, "Quiz Tamamlandı!", Toast.LENGTH_SHORT).show()
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
            // Stil dosyandaki varsayılan rengine dönmesi için sıfırlama
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
            updateWordProgress(currentWord, true)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            // Doğru şıkkı da göstererek kullanıcıya yardımcı olalım
            buttons.forEach {
                if (it.text == currentWord.turkish) it.setBackgroundColor(Color.GREEN)
            }
            updateWordProgress(currentWord, false)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            moveToNext()
        }, 1000)
    }

    private fun updateWordProgress(word: Word, isCorrect: Boolean) {
        // Algoritma: 6 kez üst üste doğru bilme şartı
        if (isCorrect) {
            word.correctCount += 1
            if (word.correctCount >= 6) {
                word.repetitionLevel += 1
                word.correctCount = 0
                if (word.repetitionLevel >= 6) word.isLearned = 1
            }
        } else {
            word.correctCount = 0
        }

        // DatabaseHelper içindeki güncel fonksiyonu çağırıyoruz
        dbHelper.updateWordProgress(word, isCorrect)
    }

    private fun moveToNext() {
        currentWordIndex++
        showNextQuestion()
    }

    private fun generateOptions(correctAnswer: String): List<String> {
        // Geçici olarak sabit seçenekler; ilerde DatabaseHelper'dan rastgele kelime çekebilirsin
        val wrongOptions = listOf("Masa", "Kalem", "Kitap", "Araba", "Ev", "Su").filter { it != correctAnswer }
        val randomOptions = wrongOptions.shuffled().take(3).toMutableList()
        randomOptions.add(correctAnswer)
        return randomOptions.shuffled()
    }
}