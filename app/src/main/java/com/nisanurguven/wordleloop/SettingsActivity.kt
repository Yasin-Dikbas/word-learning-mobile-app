package com.nisanurguven.wordleloop

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.nisanurguven.wordleloop.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var xpManager: XpManager // XP'yi çekmek için eklendi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        xpManager = XpManager(this)

        // Mevcut ayarları ve XP durumunu yükle
        loadCurrentSettings()

        // Günlük Kelime Hedefi SeekBar
        binding.seekBarDailyGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val goal = if (progress == 0) 1 else progress
                binding.tvDailyGoalLabel.text = "Günlük Kelime Hedefi: $goal"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Kaydırma işlemi başladığında özel bir işlem yapılmasına gerek yok
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Kaydırma işlemi bittiğinde özel bir işlem yapılmasına gerek yok
            }
        })

        // Zorluk Seviyesi SeekBar
        binding.seekBarDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val level = progress + 1
                binding.tvDifficultyLabel.text = "Zorluk Seviyesi: $level"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Kaydırma işlemi başladığında özel bir işlem yapılmasına gerek yok
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Kaydırma işlemi bittiğinde özel bir işlem yapılmasına gerek yok
            }
        })

        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        // XP Görüntüleme: tvTotalXP ID'li bir TextView'ın olduğunu varsayıyoruz
        binding.tvTotalXP?.text = "Toplam Puanınız: ${xpManager.getXP()} XP"

        val sharedPref = getSharedPreferences("LoopWordsSettings", MODE_PRIVATE)
        val currentGoal = sharedPref.getInt("daily_goal", 10)
        val currentDiff = sharedPref.getInt("difficulty", 2)
        val savedCategories = sharedPref.getString("categories", "")?.split(",") ?: listOf()

        // SeekBar durumlarını yükle
        binding.seekBarDailyGoal.progress = currentGoal
        binding.tvDailyGoalLabel.text = "Günlük Kelime Hedefi: $currentGoal"

        binding.seekBarDifficulty.progress = currentDiff - 1
        binding.tvDifficultyLabel.text = "Zorluk Seviyesi: $currentDiff"

        // Kategorileri (Chip) otomatik seçili getir
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as Chip
            val dbId = getDbIdFromChip(chip.id).toString()
            if (savedCategories.contains(dbId)) {
                chip.isChecked = true
            }
        }
    }

    private fun saveSettings() {
        val dailyGoal = if (binding.seekBarDailyGoal.progress == 0) 1 else binding.seekBarDailyGoal.progress
        val difficulty = binding.seekBarDifficulty.progress + 1

        val selectedCategoryIds = mutableListOf<Int>()
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as Chip
            if (chip.isChecked) {
                val dbId = getDbIdFromChip(chip.id)
                if (dbId != 0) selectedCategoryIds.add(dbId)
            }
        }

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

    // Chip ID'sini Veritabanı ID'sine çeviren yardımcı fonksiyon
    private fun getDbIdFromChip(chipId: Int): Int {
        return when (chipId) {
            binding.chip1.id -> 1
            binding.chip2.id -> 2
            binding.chip3.id -> 3
            binding.chip4.id -> 4
            binding.chip5.id -> 5
            binding.chip6.id -> 6
            binding.chip7.id -> 7
            binding.chip8.id -> 8
            else -> 0
        }
    }
}