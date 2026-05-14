package com.nisanurguven.wordleloop

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityReportBinding
import java.io.File
import java.io.FileOutputStream

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Verileri yükle
        loadStatistics()

        // Çıktı alma butonu
        binding.btnPrintReport.setOnClickListener {
            saveReportImage()
        }
    }

    private fun loadStatistics() {
        // DatabaseHelper'daki tüm kategorileri ve analizleri içeren fonksiyonu çağırıyoruz
        val reportData = dbHelper.getFullReportData()

        // Genel başarı yüzdesini yansıt
        binding.tvOverallPercentage.text = "%${reportData["overallPercentage"]}"

        // Hata yapılan aşama analizini yansıt
        binding.tvMostFailedStage.text = reportData["mostFailedStage"].toString()

        // Kategori listesini yüzdeleriyle birlikte formatlayarak göster
        val categoryList = reportData["categoryProgress"] as? List<String>
        binding.tvCategoryStats.text = categoryList?.joinToString("\n") ?: "Veri bulunamadı."
    }

    private fun saveReportImage() {
        val view = binding.reportContent

        if (view.width <= 0 || view.height <= 0) {
            Toast.makeText(this, "Rapor hazırlanıyor, lütfen bekleyin.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        try {
            val file = File(getExternalFilesDir(null), "LoopWords_Karne.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Toast.makeText(this, "Rapor kaydedildi:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Kaydetme hatası!", Toast.LENGTH_SHORT).show()
        }
    }
}