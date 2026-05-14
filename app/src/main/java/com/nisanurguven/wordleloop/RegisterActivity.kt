package com.nisanurguven.wordleloop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    // dbHelper'ı burada tanımlamak diğer fonksiyonlardan erişimi kolaylaştırır
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Veritabanı yardımcısını başlat
        dbHelper = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {

                // --- KRİTİK NOKTA: Veritabanına Yazma İşlemi ---
                val db = dbHelper.writableDatabase // Bu satır veritabanını aktif (Open) hale getirir.

                val values = android.content.ContentValues().apply {
                    put("username", user) // "username" kısmını DatabaseHelper'daki kolon adınla aynı yap
                    put("email", email)    // "email" kısmını DatabaseHelper'daki kolon adınla aynı yap
                    put("password", pass)  // "password" kısmını DatabaseHelper'daki kolon adınla aynı yap
                }

                // Tablo adının "Users" veya senin DatabaseHelper'da verdiğin isim olduğundan emin ol
                val success = db.insert("Users", null, values)

                if (success != -1L) {
                    Toast.makeText(this, "Hesap başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Kayıt sırasında bir hata oluştu!", Toast.LENGTH_SHORT).show()
                }

                // İşlem bittiğinde db'yi kapatabilirsin (isteğe bağlı ama önerilir)
                // db.close()

            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}