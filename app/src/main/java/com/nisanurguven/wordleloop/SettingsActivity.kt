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

        // SeekBar Ayarı
        binding.seekBarDailyGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val goal = if (progress == 0) 1 else progress
                binding.tvDailyGoalLabel.text = "Günlük Kelime Hedefi: $goal"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        val dailyGoal = binding.seekBarDailyGoal.progress
        val difficulty = when (binding.rgDifficulty.checkedRadioButtonId) {
            R.id.rbEasy -> 1
            R.id.rbHard -> 3
            else -> 2
        }

        val selectedCategoryIds = mutableListOf<Int>()
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as Chip
            if (chip.isChecked) {
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

        // --- KAYIT BAŞLIYOR ---
        val sharedPref = getSharedPreferences("LoopWordsSettings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("daily_goal", dailyGoal)
            putInt("difficulty", difficulty)
            // Kategori ID'lerini virgülle ayırarak String olarak kaydediyoruz
            putString("categories", selectedCategoryIds.joinToString(","))
            apply()
        }

        Toast.makeText(this, "Ayarlar kaydedildi!", Toast.LENGTH_SHORT).show()
        finish()
    }
}