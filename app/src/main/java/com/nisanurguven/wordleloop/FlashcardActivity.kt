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

        loadWordsWithFilter()
        setupCardStack()
        setupButtons()
    }

    private fun loadWordsWithFilter() {
        wordList = dbHelper.getFilteredWords(this).toMutableList()

        if (wordList.isEmpty()) {
            Toast.makeText(this, "Öğrenilecek yeni kelime kalmadı. Raporlarınızı kontrol edebilirsiniz!", Toast.LENGTH_LONG).show()
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
            setCanScrollHorizontal(true)
            setCanScrollVertical(false)
        }

        binding.cardStackView.layoutManager = manager
        binding.cardStackView.adapter = FlashcardAdapter(wordList)
    }

    private fun setupButtons() {
        binding.btnKnow.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding.cardStackView.swipe()
        }

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
        // ÖNEMLİ DÜZELTME: CardStackView swipe olunca topPosition artmış olur,
        // bu yüzden swiped edilen kartın pozisyonu her zaman topPosition - 1'dir.
        val swipedPosition = manager.topPosition - 1

        if (swipedPosition >= 0 && swipedPosition < wordList.size) {
            val currentWord = wordList[swipedPosition]

            if (direction == Direction.Right) {
                // Sağ - Biliyorum: İlerlemeyi kaydet
                dbHelper.updateWordProgress(currentWord, true)
                xpManager.addXP(10)
                Toast.makeText(this, "Harika! +10 XP", Toast.LENGTH_SHORT).show()
            } else if (direction == Direction.Left) {
                // Sol - Bilmiyorum: Aşamayı sıfırla
                dbHelper.updateWordProgress(currentWord, false)

                // Kartı tekrar destenin sonuna ekle
                wordList.add(currentWord)
                binding.cardStackView.adapter?.notifyItemInserted(wordList.size - 1)

                Toast.makeText(this, "Tekrar karşına çıkacak", Toast.LENGTH_SHORT).show()
            }
        }

        // Deste tamamen bittiğinde
        if (manager.topPosition == wordList.size) {
            Toast.makeText(this, "Bugünkü kelimeleri tamamladın!", Toast.LENGTH_LONG).show()
            finish() // Aktiviteyi kapatıp ana menüye dön
        }
    }

    override fun onCardAppeared(view: View?, position: Int) {
        // CardStackListener zorunlu kıldığı için eklendi, işlem yapılmıyor.
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        // CardStackListener zorunlu kıldığı için eklendi, işlem yapılmıyor.
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
        // CardStackListener zorunlu kıldığı için eklendi, işlem yapılmıyor.
    }

    override fun onCardRewound() {
        // CardStackListener zorunlu kıldığı için eklendi, işlem yapılmıyor.
    }

    override fun onCardCanceled() {
        // CardStackListener zorunlu kıldığı için eklendi, işlem yapılmıyor.
    }
}