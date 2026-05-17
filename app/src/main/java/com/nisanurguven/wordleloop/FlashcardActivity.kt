package com.nisanurguven.wordleloop

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityFlashcardBinding
import com.yuyakaido.android.cardstackview.*

class FlashcardActivity : AppCompatActivity(), CardStackListener {

    private lateinit var binding: ActivityFlashcardBinding
    private lateinit var manager: CardStackLayoutManager
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var xpManager: XpManager
    private var wordList = mutableListOf<Word>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        xpManager = XpManager(this)

        // 1. Ayarlardaki filtreye göre kelimeleri yükle
        loadWordsWithFilter()

        // 2. Kart mekanizmasını kur
        setupCardStack()

        // 3. Butonları bağla
        setupButtons()
    }

    private fun loadWordsWithFilter() {
        // DatabaseHelper'daki akıllı filtreleme metodunu kullanıyoruz
        wordList = dbHelper.getFilteredWords(this).toMutableList()

        if (wordList.isEmpty()) {
            Toast.makeText(this, "Seçilen kriterlerde yeni kelime bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupCardStack() {
        manager = CardStackLayoutManager(this, this).apply {
            setStackFrom(StackFrom.None)
            setVisibleCount(3)
            setTranslationInterval(8.0f)
            setScaleInterval(0.95f)
            setSwipeThreshold(0.3f)
            setMaxDegree(20.0f)
            setDirections(Direction.HORIZONTAL)
            setCanScrollHorizontal(true) // Hem kaydırma hem buton aktif
            setCanScrollVertical(false)
        }

        binding.cardStackView.layoutManager = manager
        binding.cardStackView.adapter = FlashcardAdapter(wordList)
    }

    private fun setupButtons() {
        // BİLİYORUM BUTONU (Yeşil - Sağ)
        binding.btnKnow.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding.cardStackView.swipe()
        }

        // BİLMİYORUM BUTONU (Kırmızı - Sol)
        binding.btnDontKnow.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding.cardStackView.swipe()
        }
    }

    override fun onCardSwiped(direction: Direction?) {
        val position = manager.topPosition - 1
        if (position >= 0 && position < wordList.size) {
            val currentWord = wordList[position]

            if (direction == Direction.Right) {
                // BİLİYORUM: 6 tekrarlı döngüde bir üst aşamaya taşı
                dbHelper.updateWordProgress(currentWord, true)
                xpManager.addXP(10)
                Toast.makeText(this, "Harika! +10 XP", Toast.LENGTH_SHORT).show()
            } else {
                // BİLMİYORUM: Döngüyü sıfırla/düşür ve kelimeyi listenin sonuna tekrar ekle
                dbHelper.updateWordProgress(currentWord, false)

                // Kelimeyi öğrenene kadar döngüde tutmak için listenin sonuna ekliyoruz
                wordList.add(currentWord)
                binding.cardStackView.adapter?.notifyItemInserted(wordList.size - 1)

                Toast.makeText(this, "Tekrar karşına çıkacak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kullanılmayan zorunlu metotlar
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
}