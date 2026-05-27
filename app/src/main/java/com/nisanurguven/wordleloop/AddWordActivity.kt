package com.nisanurguven.wordleloop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.AddWordActivityBinding
import com.nisanurguven.wordleloop.databinding.ItemSentenceBinding

class AddWordActivity : AppCompatActivity() {

    private lateinit var binding: AddWordActivityBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedImageUri: String? = null

    // Galeri açma işlemi
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.ivSelectedImage.setImageURI(it)
            binding.ivSelectedImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            selectedImageUri = it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddWordActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // --- ZORLUK SEVİYESİ SEEKBAR AYARI ---
        binding.seekBarAddWordDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val level = progress + 1
                binding.tvAddWordDifficultyLabel.text = "Kelime Zorluğu: $level"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Resim seçme
        binding.frameImagePicker.setOnClickListener {
            getImage.launch("image/*")
        }

        // Yeni cümle satırı ekle
        binding.btnAddSentence.setOnClickListener {
            addNewSentenceField()
        }

        // KELİMEYİ KAYDET
        binding.btnSaveWord.setOnClickListener {
            if (validateInputs()) {
                saveWordToDatabase()
            }
        }
    }

    private fun addNewSentenceField() {
        val inflater = LayoutInflater.from(this)
        val sentenceBinding = ItemSentenceBinding.inflate(inflater, binding.containerSentences, false)

        // Uzun basınca cümleyi kaldır
        sentenceBinding.root.setOnLongClickListener {
            binding.containerSentences.removeView(it)
            true
        }

        binding.containerSentences.addView(sentenceBinding.root)

        // OTOMATİK KAYDIRMA: Yeni cümle eklendiğinde ScrollView'u en aşağıya getirir
        binding.root.post {
            binding.root.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun validateInputs(): Boolean {
        val eng = binding.etEnglishWord.text.toString().trim()
        val tr = binding.etTurkishWord.text.toString().trim()

        if (eng.isEmpty() || tr.isEmpty()) {
            Toast.makeText(this, "Lütfen İngilizce ve Türkçe karşılıklarını doldurun!", Toast.LENGTH_SHORT).show()
            return false
        }

        // Örnek cümlelerin kontrolü
        for (i in 0 until binding.containerSentences.childCount) {
            val view = binding.containerSentences.getChildAt(i)
            val etEngSentence = view.findViewById<EditText>(R.id.etSentenceEng)
            val etTrSentence = view.findViewById<EditText>(R.id.etSentenceTr)

            if (etEngSentence.text.isEmpty() || etTrSentence.text.isEmpty()) {
                Toast.makeText(this, "Lütfen eklediğiniz tüm örnek cümleleri doldurun!", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun saveWordToDatabase() {
        val eng = binding.etEnglishWord.text.toString().trim()
        val tr = binding.etTurkishWord.text.toString().trim()
        val phonetic = binding.etPhonetic.text.toString().trim()
        val reading = binding.etTurkishReading.text.toString().trim()
        val difficulty = binding.seekBarAddWordDifficulty.progress + 1
        val categoryId = 1

        // Veritabanı işlemleri burada devam edecek...

        Toast.makeText(this, "$eng kelimesi $difficulty zorluk seviyesiyle kaydedildi!", Toast.LENGTH_LONG).show()
        finish()
    }
}