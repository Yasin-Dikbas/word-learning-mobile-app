package com.nisanurguven.wordleloop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.AddWordActivityBinding
import com.nisanurguven.wordleloop.databinding.ItemSentenceBinding // Dinamik cümle satırı için

class AddWordActivity : AppCompatActivity() {

    private lateinit var binding: AddWordActivityBinding
    private var selectedImageUri: String? = null

    // Galeri açma işlemi için
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

        // Resim seçme alanına tıklama
        binding.frameImagePicker.setOnClickListener {
            getImage.launch("image/*")
        }

        // "+ Yeni Cümle Ekle" butonuna tıklama
        binding.btnAddSentence.setOnClickListener {
            addNewSentenceField()
        }

        // "Kelimeyi Kaydet" butonuna tıklama
        binding.btnSaveWord.setOnClickListener {
            if (validateInputs()) {
                saveWordToDatabase()
            }
        }
    }

    private fun addNewSentenceField() {
        // Her tıklandığında yeni bir cümle satırı oluşturur
        val inflater = LayoutInflater.from(this)
        val sentenceBinding = ItemSentenceBinding.inflate(inflater, binding.containerSentences, false)

        // Silme butonu eklemek istersen:
        sentenceBinding.root.setOnLongClickListener {
            binding.containerSentences.removeView(it)
            true
        }

        binding.containerSentences.addView(sentenceBinding.root)
    }

    private fun validateInputs(): Boolean {
        val eng = binding.etEnglishWord.text.toString()
        val tr = binding.etTurkishWord.text.toString()
        val phon = binding.etPhonetic.text.toString()
        val read = binding.etTurkishReading.text.toString()

        if (eng.isEmpty() || tr.isEmpty() || phon.isEmpty() || read.isEmpty()) {
            Toast.makeText(this, "Lütfen yıldızlı (*) alanları doldurun!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Lütfen bir resim seçin!", Toast.LENGTH_SHORT).show()
            return false
        }

        // Cümlelerin kontrolü
        for (i in 0 until binding.containerSentences.childCount) {
            val view = binding.containerSentences.getChildAt(i)
            val etEngSentence = view.findViewById<EditText>(R.id.etSentenceEng)
            val etTrSentence = view.findViewById<EditText>(R.id.etSentenceTr)

            if (etEngSentence.text.isEmpty() || etTrSentence.text.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm örnek cümleleri ve anlamlarını doldurun!", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun saveWordToDatabase() {
        // Burada veritabanı (Firestore veya MsSQL) işlemlerini yapabilirsin
        Toast.makeText(this, "Kelime başarıyla hazırlandı!", Toast.LENGTH_LONG).show()
        finish() // Ekranı kapatıp ana menüye döner
    }
}