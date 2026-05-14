package com.nisanurguven.wordleloop

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.nisanurguven.wordleloop.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mevcut ayarları yükle (Kullanıcı daha önce ne seçtiyse onu görsün)
        loadCurrentSettings()

        // Günlük Kelime Hedefi SeekBar Dinleyicisi
        binding.seekBarDailyGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val goal = if (progress == 0) 1 else progress
                binding.tvDailyGoalLabel.text = "Günlük Kelime Hedefi: $goal"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // ZORLUK SEVİYESİ SeekBar Dinleyicisi (1-5 Arası)
        binding.seekBarDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // SeekBar 0-4 arası çalışır, biz onu 1-5 yapıyoruz
                val level = progress + 1
                binding.tvDifficultyLabel.text = "Zorluk Seviyesi: $level"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        val sharedPref = getSharedPreferences("LoopWordsSettings", MODE_PRIVATE)
        val currentGoal = sharedPref.getInt("daily_goal", 10)
        val currentDiff = sharedPref.getInt("difficulty", 2)

        binding.seekBarDailyGoal.progress = currentGoal
        binding.tvDailyGoalLabel.text = "Günlük Kelime Hedefi: $currentGoal"

        // Zorluk seviyesini yükle (Veritabanındaki 1-5 değerini SeekBar'ın 0-4 aralığına çeviriyoruz)
        binding.seekBarDifficulty.progress = currentDiff - 1
        binding.tvDifficultyLabel.text = "Zorluk Seviyesi: $currentDiff"
    }

    private fun saveSettings() {
        val dailyGoal = if (binding.seekBarDailyGoal.progress == 0) 1 else binding.seekBarDailyGoal.progress

        // ZORLUK: SeekBar'dan gelen 0-4 değerini veritabanı için 1-5 yapıyoruz
        val difficulty = binding.seekBarDifficulty.progress + 1

        val selectedCategoryIds = mutableListOf<Int>()
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as Chip
            if (chip.isChecked) {
                // Chip ID'lerini doğrudan veritabanı ID'lerine eşliyoruz
                val dbId = when (chip.id) {
                    R.id.chip_1 -> 1
                    R.id.chip_2 -> 2
                    R.id.chip_3 -> 3
                    R.id.chip_4 -> 4
                    R.id.chip_5 -> 5
                    R.id.chip_6 -> 6
                    R.id.chip_7 -> 7
                    R.id.chip_8 -> 8
                    else -> 0
                }
                if (dbId != 0) selectedCategoryIds.add(dbId)
            }
        }

        // Kayıt İşlemi
        val sharedPref = getSharedPreferences("LoopWordsSettings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("daily_goal", dailyGoal)
            putInt("difficulty", difficulty)
            putString("categories", selectedCategoryIds.joinToString(","))
            apply()
        }

        Toast.makeText(this, "Ayarlar başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
        finish()
    }
}