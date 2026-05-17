package com.nisanurguven.wordleloop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    // Binding'i güvenli bir şekilde başlatalım
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var dbHelper: DatabaseHelper
    private var correctUserName: String = ""
    private var correctAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
            setContentView(binding.root)

            dbHelper = DatabaseHelper(this)

            // Başlangıçta güvenlik kısmını gizle
            binding.layoutSecurity.visibility = View.GONE

            // 1. KULLANICI BULMA
            binding.btnFindUser.setOnClickListener {
                val username = binding.etForgotUsername.text.toString().trim()

                if (username.isEmpty()) {
                    Toast.makeText(this, "Kullanıcı adı boş olamaz", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Veritabanından veriyi çekiyoruz
                val securityData = dbHelper.getSecurityData(username)

                if (securityData != null) {
                    correctUserName = username

                    // Verileri TextView'lara basarken null kontrolü yapıyoruz
                    binding.tvDisplayQuestion.text = "Soru: ${securityData.first}"
                    correctAnswer = securityData.second

                    // İpucu varsa göster, yoksa gizle veya belirt
                    val hint = securityData.third
                    binding.tvDisplayHint.text = if (hint.isNullOrEmpty()) "İpucu bulunmuyor." else "İpucu: $hint"

                    // Görünürlük ayarları
                    binding.layoutSecurity.visibility = View.VISIBLE
                    binding.btnFindUser.visibility = View.GONE
                    binding.etForgotUsername.isEnabled = false
                } else {
                    Toast.makeText(this, "Kullanıcı adı sistemde kayıtlı değil!", Toast.LENGTH_SHORT).show()
                }
            }

            // 2. CEVAP DOĞRULAMA
            binding.btnVerifyAnswer.setOnClickListener {
                val userResponse = binding.etForgotAnswer.text.toString().trim()

                if (userResponse.isEmpty()) {
                    Toast.makeText(this, "Lütfen cevabınızı yazın", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (userResponse.equals(correctAnswer, ignoreCase = true)) {
                    // Cevap doğru! Şifre sıfırlama ekranına git
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("userName", correctUserName)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Cevap yanlış! Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            // Eğer binding veya başlatma sırasında bir hata olursa uygulamayı kapatmadan logla
            android.util.Log.e("FORGOT_PASSWORD_ERROR", "Başlatma hatası: ${e.message}")
            Toast.makeText(this, "Ekran yüklenirken bir hata oluştu.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}