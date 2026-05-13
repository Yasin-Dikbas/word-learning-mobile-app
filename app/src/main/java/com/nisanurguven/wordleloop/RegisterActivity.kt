package com.nisanurguven.wordleloop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val user = etUsername.text.toString()
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (user.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                // Kayıt işlemleri (Database işlemleri buraya gelecek)
                Toast.makeText(this, "Hesap başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                finish() // Kayıt başarılıysa bu ekranı kapatıp giriş ekranına döner
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            // Giriş ekranına geri dön
            finish()
        }
    }
}