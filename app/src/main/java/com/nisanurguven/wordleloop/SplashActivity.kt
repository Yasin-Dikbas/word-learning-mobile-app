package com.nisanurguven.wordleloop

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000 // 3 saniye bekleme süresi
    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Progress Bar'ı simüle eden Thread
        Thread {
            while (progressStatus < 100) {
                progressStatus += 1
                // UI güncellemeleri Main Thread üzerinden yapılmalı
                handler.post {
                    progressBar.progress = progressStatus
                }
                try {
                    // 3 saniye / 100 adım = 30ms her adım başı bekleme
                    Thread.sleep(30)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // Yükleme bittiğinde ana ekrana geçiş yap
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Splash ekranını geri yığınından kaldır
        }.start()
    }
}