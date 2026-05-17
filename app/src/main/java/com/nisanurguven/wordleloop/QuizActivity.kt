package com.nisanurguven.wordleloop

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var dbHelper: DatabaseHelper

    private var wordList = mutableListOf<Word>()
    private var currentWordIndex = 0
    private lateinit var currentWord: Word

    // İstatistikler için sayaçlar
    private var correctCount = 0
    private var wrongCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Veritabanından quiz için kelimeleri yükle
        loadWords()

        // Yanlış cevap ekranındaki "Devam Et" butonu
        binding.btnContinue.setOnClickListener {
            binding.layoutErrorDetail.visibility = View.GONE
            binding.layoutQuestion.visibility = View.VISIBLE
            moveToNext()
        }
    }

    private fun loadWords() {
        // Sadece öğrenilmiş veya öğrenme sürecindeki kelimeleri çeken fonksiyonu kullanıyoruz
        wordList = dbHelper.getQuizWords(10).toMutableList()

        if (wordList.isNotEmpty()) {
            showNextQuestion()
        } else {
            Toast.makeText(this, "Quiz için yeterli kelime yok. Biraz kelime öğrenmelisin!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showNextQuestion() {
        if (currentWordIndex >= wordList.size) {
            showFinalResults()
            return
        }

        currentWord = wordList[currentWordIndex]

        // Premium İlerleme Çubuğu/Yazısı
        binding.tvProgress.text = "${currentWordIndex + 1} / ${wordList.size}"
        binding.tvQuestionWord.text = currentWord.english

        // Şıkları hazırla ve karıştır
        val options = generateOptions(currentWord.turkish)
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)

        buttons.forEachIndexed { index, button ->
            button.text = options[index]
            // Tasarımı sıfırla
            button.setBackgroundResource(R.drawable.edittext_background)
            button.setTextColor(Color.parseColor("#1A4B8F"))
            button.isEnabled = true
            button.setOnClickListener {
                checkAnswer(button, options[index] == currentWord.turkish)
            }
        }
    }

    private fun checkAnswer(selectedButton: Button, isCorrect: Boolean) {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)

        // Kullanıcının art arda basmasını engellemek için tüm butonları kilitliyoruz
        buttons.forEach { it.isEnabled = false }

        if (isCorrect) {
            // 1. İstatistiği artır
            correctCount++

            // 2. Görsel geri bildirim (Yeşil Premium)
            selectedButton.setBackgroundColor(Color.parseColor("#4CAF50"))
            selectedButton.setTextColor(Color.WHITE)

            // 3. VERİTABANI GÜNCELLEME: Aşamayı artır ve 6 aşamalı takvime göre ileri at
            dbHelper.updateWordProgress(currentWord, true)

            // 4. Kısa bir bekleme sonrası sonraki soruya geç
            Handler(Looper.getMainLooper()).postDelayed({
                moveToNext()
            }, 800)

        } else {
            // 1. İstatistiği artır
            wrongCount++

            // 2. Görsel geri bildirim (Kırmızı Premium)
            selectedButton.setBackgroundColor(Color.parseColor("#F44336"))
            selectedButton.setTextColor(Color.WHITE)

            // 3. Doğru olan şıkkı da yeşil yakarak kullanıcıya hatasını göster
            buttons.forEach {
                if (it.text == currentWord.turkish) {
                    it.setBackgroundColor(Color.parseColor("#4CAF50"))
                    it.setTextColor(Color.WHITE)
                }
            }

            // 4. VERİTABANI SIFIRLAMA: 6 aşamalı sistemi bu kelime için Stage 0'a çek
            dbHelper.resetWordProgress(currentWord.id)

            // 5. Kartın arkasını (Örnek cümle, okunuş vb.) gösteren ekranı aç
            Handler(Looper.getMainLooper()).postDelayed({
                showErrorDetail()
            }, 1000)
        }
    }

    private fun showErrorDetail() {
        binding.layoutQuestion.visibility = View.GONE
        binding.layoutErrorDetail.visibility = View.VISIBLE

        binding.tvCorrectMeaning.text = currentWord.turkish
        binding.tvPhoneticAndReading.text = "${currentWord.phonetic ?: ""} (${currentWord.turkishReading ?: ""})"

        val sentences = currentWord.sampleSentence.split("|")
        if (sentences.size >= 2) {
            binding.tvSampleEn.text = sentences[0].trim()
            // Parantez içine alarak yazdırıyoruz
            binding.tvSampleTr.text = "(${sentences[1].trim()})"
        } else {
            binding.tvSampleEn.text = currentWord.sampleSentence
            binding.tvSampleTr.text = ""
        }
    }

    private fun moveToNext() {
        currentWordIndex++
        showNextQuestion()
    }

    private fun generateOptions(correctAnswer: String): List<String> {
        // Havuzdan rastgele 3 yanlış şık çek (dummy veri yerine veritabanından çekmek daha iyidir)
        val dummyDistractors = listOf("Kalem", "Kitap", "Şehir", "Araba", "Gözlük", "Masa", "Dünya", "Zaman")

        val options = dummyDistractors
            .filter { it != correctAnswer }
            .shuffled()
            .take(3)
            .toMutableList()

        options.add(correctAnswer)
        return options.shuffled()
    }

    private fun showFinalResults() {
        // İstatistikleri bir Toast veya özel bir Alert ile göster
        val summary = "Quiz Tamamlandı!\n\n✅ Doğru: $correctCount\n❌ Yanlış: $wrongCount"
        Toast.makeText(this, summary, Toast.LENGTH_LONG).show()

        // Veritabanına quiz sonuçlarını kaydeden bir fonksiyonun varsa buraya ekleyebilirsin
        finish()
    }
}