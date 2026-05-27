package com.nisanurguven.wordleloop

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.nisanurguven.wordleloop.databinding.ActivityWordleBinding
import java.util.*

class WordleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordleBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var xpManager: XpManager

    private var correctWord = ""
    private var currentAttempt = 0
    private var isGameOver = false

    private lateinit var letterBoxes: Array<TextView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        xpManager = XpManager(this)

        letterBoxes = arrayOfNulls<TextView>(30)

        setupWordleBoard()
        loadCorrectWord()
        setupButtons()
        updateXpDisplay() // Bellekteki mevcut XP'yi ekranda göster
    }

    // Ekranda XP bilgisini güncelleyen fonksiyon
    private fun updateXpDisplay() {
        val totalXp = xpManager.getXP()
        binding.tvCurrentXpWordle.text = "Puanınız: $totalXp XP"
    }

    private fun setupWordleBoard() {
        binding.glWordleBoard.removeAllViews()
        for (i in 0 until 30) {
            val textView = TextView(this).apply {
                val params = GridLayout.LayoutParams().apply {
                    width = dpToPx(55)
                    height = dpToPx(60)
                    setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
                }
                layoutParams = params
                setBackgroundResource(R.drawable.wordle_letter_box_default)
                gravity = Gravity.CENTER
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#333333"))
                text = ""
            }
            letterBoxes[i] = textView
            binding.glWordleBoard.addView(textView)
        }
    }

    private fun loadCorrectWord() {
        val wordData = dbHelper.getWordleWord()
        if (wordData != null) {
            correctWord = wordData.english.trim().uppercase(Locale.ROOT)

            if (correctWord.length != 5) {
                Toast.makeText(this, "Hata: Seçilen kelime 5 harfli değil ($correctWord)", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            binding.tvPhoneticWordle.visibility = View.GONE
        } else {
            Toast.makeText(this, "Öğrenilecek 5 harfli kelime bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupButtons() {
        binding.btnWordleGuess.setOnClickListener {
            if (isGameOver) return@setOnClickListener
            val guess = binding.etWordleGuess.text.toString().trim().uppercase(Locale.ROOT)
            if (guess.length != 5) {
                binding.etWordleGuess.error = "Lütfen 5 harfli bir kelime yazın."
                return@setOnClickListener
            }
            processGuess(guess)
        }

        binding.btnWordleHint.setOnClickListener {
            if (isGameOver) return@setOnClickListener
            showHintDialog()
        }
    }

    // Fonksiyon karmaşıklığını (Cognitive Complexity) düşürmek için alt parçalara bölündü
    private fun processGuess(guess: String) {
        val targetLettersCount = getTargetLettersCount()
        val letterColors = evaluateGuessColors(guess, targetLettersCount)

        updateWordleUI(guess, letterColors)
        checkGameState(guess)
    }

    private fun getTargetLettersCount(): MutableMap<Char, Int> {
        val counts = mutableMapOf<Char, Int>()
        for (char in correctWord) {
            counts[char] = counts.getOrDefault(char, 0) + 1
        }
        return counts
    }

    // NullPointerException hatalarını engellemek için !! operatörleri kaldırıldı ve güvenli çağrılar eklendi
    private fun evaluateGuessColors(guess: String, targetLettersCount: MutableMap<Char, Int>): Array<Char> {
        val letterColors = Array(5) { 'R' }

        // 1. Geçiş: Tam doğru yerler (Yeşil)
        for (i in 0 until 5) {
            if (guess[i] == correctWord[i]) {
                letterColors[i] = 'G'
                val currentCount = targetLettersCount.getOrDefault(guess[i], 0)
                targetLettersCount[guess[i]] = currentCount - 1
            }
        }

        // 2. Geçiş: Yanlış yerler (Turuncu)
        for (i in 0 until 5) {
            if (letterColors[i] == 'G') continue
            val char = guess[i]
            val charCount = targetLettersCount.getOrDefault(char, 0)

            if (charCount > 0) {
                letterColors[i] = 'O'
                targetLettersCount[char] = charCount - 1
            }
        }

        return letterColors
    }

    private fun updateWordleUI(guess: String, letterColors: Array<Char>) {
        val startIndex = currentAttempt * 5
        for (i in 0 until 5) {
            val boxIndex = startIndex + i
            val textView = letterBoxes[boxIndex]
            textView?.let {
                // Eğer kutucuk bir ipucu değilse kullanıcının yazdığı harfi koy
                if (it.tag != "isHint") {
                    it.text = guess[i].toString()
                    it.setTextColor(Color.WHITE)
                } else {
                    // İpucu ise harfe dokunma ama rengini beyaz yap
                    it.setTextColor(Color.WHITE)
                }

                when (letterColors[i]) {
                    'G' -> it.setBackgroundResource(R.drawable.wordle_letter_box_correct)
                    'O' -> it.setBackgroundResource(R.drawable.wordle_letter_box_present)
                    else -> it.setBackgroundResource(R.drawable.wordle_letter_box_absent)
                }
            }
        }
    }

    private fun checkGameState(guess: String) {
        if (guess == correctWord) {
            isGameOver = true
            val earnedXp = calculateXp()
            xpManager.addXP(earnedXp) // XP'yi belleğe kaydet
            updateXpDisplay() // Görseli güncelle
            dbHelper.updateWordStatusInDb(correctWord)
            showGameOverDialog(true, correctWord, earnedXp)
        } else {
            currentAttempt++
            binding.etWordleGuess.text.clear()
            if (currentAttempt >= 6) {
                isGameOver = true
                showGameOverDialog(false, correctWord, 0)
            }
        }
    }

    private fun calculateXp(): Int {
        return when (currentAttempt) {
            0 -> 60
            1 -> 50
            2 -> 40
            3 -> 30
            4 -> 20
            5 -> 10
            else -> 0
        }
    }

    private fun showGameOverDialog(isWin: Boolean, word: String, earnedXp: Int) {
        val wordData = dbHelper.getWordByEnglish(word)
        val phonetic = wordData?.phonetic ?: ""
        val title = if (isWin) "TEBRİKLER! 🎉" else "OYUN BİTTİ"
        val message = if (isWin) {
            "Doğru tahmin! +$earnedXp XP kazandın.\n\nKelime: $word\nOkunuşu: $phonetic"
        } else {
            "Hakkın bitti.\n\nDoğru kelime: $word\nOkunuşu: $phonetic"
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Tamam") { _, _ -> finish() }
            .show()
    }

    private fun showHintDialog() {
        val currentTotalXp = xpManager.getXP()

        if (currentTotalXp >= 10) {
            AlertDialog.Builder(this)
                .setTitle("İpucu Kullan")
                .setMessage("Bir harf açmak 10 XP maliyetindedir. Devam etmek istiyor musunuz?")
                .setPositiveButton("Evet") { _, _ ->
                    xpManager.addXP(-10) // Bellekten XP düşür
                    updateXpDisplay() // Ekranı güncelle
                    applyHintLogic() // Harfi yerleştir
                }
                .setNegativeButton("Hayır", null)
                .show()
        } else {
            Toast.makeText(this, "Yetersiz XP! İpucu için en az 10 XP gerekli.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyHintLogic() {
        val randomIndex = (0 until 5).random()
        val hintLetter = correctWord[randomIndex].toString()
        val boxIndex = (currentAttempt * 5) + randomIndex
        val targetBox = letterBoxes[boxIndex]

        targetBox?.let {
            it.text = hintLetter
            it.setTextColor(Color.parseColor("#8E244D")) // Senin logo morun
            it.setTypeface(null, Typeface.BOLD_ITALIC)
            it.tag = "isHint"
            Toast.makeText(this, "İpucu yerleştirildi!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}