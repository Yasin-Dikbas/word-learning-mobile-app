package com.nisanurguven.wordleloop

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
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
        // DatabaseHelper'dan ham veriyi al
        val reportData = dbHelper.getFullReportData()

        // KRİTİK KONTROL: Eğer toplam kelime sayısı 0 ise modül aktif değildir mesajı verilir.
        // reportData içinde "totalWords" değerinin döndüğünü varsayıyoruz.
        val categoryList = reportData["categoryProgress"] as? List<String>

        if (categoryList.isNullOrEmpty()) {
            // Eğer veri yoksa görsel olarak kullanıcıyı bilgilendir
            binding.tvOverallPercentage.text = "%0"
            binding.tvCategoryStats.text = "Raporlama modülü henüz aktif değil.\nLütfen önce kelime yüklendiğinden emin olun."
            binding.btnPrintReport.isEnabled = false
            binding.btnPrintReport.alpha = 0.5f
            return
        }

        // Genel başarı yüzdesini yansıt
        binding.tvOverallPercentage.text = "%${reportData["overallPercentage"] ?: 0}"

        // Hata yapılan aşama analizini yansıt
        binding.tvMostFailedStage.text = reportData["mostFailedStage"]?.toString() ?: "Veri Yok"

        // Kategori listesini yüzdeleriyle birlikte formatlayarak göster
        binding.tvCategoryStats.text = categoryList.joinToString("\n")

        // Veri varsa butonu aktif et
        binding.btnPrintReport.isEnabled = true
        binding.btnPrintReport.alpha = 1.0f
    }

    private fun saveReportImage() {
        // Rapor içeriğini barındıran View (ConstraintLayout veya ScrollView)
        val view = binding.reportContent

        if (view.width <= 0 || view.height <= 0) {
            Toast.makeText(this, "Rapor yükleniyor, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            return
        }

        // Bitmap oluştur
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Eğer arka plan şeffaf çıkarsa beyaz ile doldur (Paylaşırken daha iyi görünür)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        try {
            // Galeri yerine uygulama klasörüne kaydeder
            val file = File(getExternalFilesDir(null), "LoopWords_Basari_Raporu.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Toast.makeText(this, "Rapor başarıyla kaydedildi:\n${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Rapor kaydedilirken bir hata oluştu!", Toast.LENGTH_SHORT).show()
        }
    }
}