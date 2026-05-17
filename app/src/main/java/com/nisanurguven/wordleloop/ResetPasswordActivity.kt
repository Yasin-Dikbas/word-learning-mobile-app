package com.nisanurguven.wordleloop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // ForgotPasswordActivity'den gönderdiğimiz kullanıcı adını alıyoruz
        val userName = intent.getStringExtra("userName") ?: ""

        binding.btnUpdatePassword.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString().trim()
            val confirmPass = binding.etNewPasswordConfirm.text.toString().trim()

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass == confirmPass) {
                // Veritabanında güncelleme yap
                val isUpdated = dbHelper.updatePassword(userName, newPass)

                if (isUpdated) {
                    Toast.makeText(this, "Şifreniz başarıyla güncellendi!", Toast.LENGTH_LONG).show()
                    // Giriş ekranına geri gönder
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Bir hata oluştu!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Şifreler uyuşmuyor!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}