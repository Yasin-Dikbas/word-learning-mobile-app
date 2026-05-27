package com.nisanurguven.wordleloop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityReportBinding
import java.io.FileOutputStream

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        loadStatistics()

        binding.btnPrintReport.setOnClickListener {
            printReportAsPdf()
        }
    }

    private fun loadStatistics() {
        val reportData = dbHelper.getFullReportData()
        val categoryList = reportData["categoryProgress"] as? List<String>

        if (categoryList.isNullOrEmpty()) {
            binding.tvOverallPercentage.text = "%0"
            binding.tvCategoryStats.text = "Raporlama modülü henüz aktif değil.\nLütfen önce kelime yüklendiğinden emin olun."
            binding.btnPrintReport.isEnabled = false
            binding.btnPrintReport.alpha = 0.5f
            return
        }

        binding.tvOverallPercentage.text = "%${reportData["overallPercentage"] ?: 0}"
        binding.tvMostFailedStage.text = reportData["mostFailedStage"]?.toString() ?: "Veri Yok"
        binding.tvCategoryStats.text = categoryList.joinToString("\n")

        binding.btnPrintReport.isEnabled = true
        binding.btnPrintReport.alpha = 1.0f
    }

    private fun printReportAsPdf() {
        val view = binding.reportContent

        if (view.width <= 0 || view.height <= 0) {
            Toast.makeText(this, "Rapor yükleniyor, lütfen biraz bekleyip tekrar deneyin.", Toast.LENGTH_SHORT).show()
            return
        }

        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${getString(R.string.app_name)} Basari Raporu"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                pdfDocument = PdfDocument()
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, newAttributes != oldAttributes)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                pdfDocument?.let { doc ->
                    // View boyutlarına göre PDF sayfasını oluştur
                    val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
                    val page = doc.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    // Arka planı beyaz yap (PDF şeffaf çıkmasın diye)
                    val bgDrawable = view.background
                    if (bgDrawable != null) {
                        bgDrawable.draw(canvas)
                    } else {
                        canvas.drawColor(Color.WHITE)
                    }

                    // Görünümü Canvas üzerine çiz
                    view.draw(canvas)
                    doc.finishPage(page)

                    try {
                        doc.writeTo(FileOutputStream(destination.fileDescriptor))
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback?.onWriteFailed(e.toString())
                    } finally {
                        doc.close()
                        pdfDocument = null
                    }
                }
            }
        }, null)
    }
}