package com.nisanurguven.wordleloop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // Veritabanı yardımcısını tanımla
        val dbHelper = DatabaseHelper(this)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString()
            val pass = etPassword.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                // Veritabanından kullanıcıyı kontrol et
                val isUserExist = dbHelper.checkUser(user, pass)

                if (isUserExist) {
                    Toast.makeText(this, "Hoş geldin, $user!", Toast.LENGTH_SHORT).show()
                    // Giriş başarılıysa Ana Sayfaya (MainActivity) git
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Giriş yaptıktan sonra geri dönülmesin diye Login ekranını kapat
                } else {
                    Toast.makeText(this, "Hatalı kullanıcı adı veya şifre!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        // Kayıt Ol yazısına tıklanınca RegisterActivity'ye git
        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}