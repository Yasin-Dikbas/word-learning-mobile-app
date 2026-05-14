package com.nisanurguven.wordleloop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nisanurguven.wordleloop.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding kurulumu
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DatabaseHelper başlatılıyor
        dbHelper = DatabaseHelper(this)

        // GİRİŞ YAP BUTONU TIKLAMA OLAYI
        binding.btnLogin.setOnClickListener {
            // Verileri al ve temizle (trim)
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // 1. Validasyon: Boş alan kontrolü
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen kullanıcı adı ve şifre giriniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Veritabanı Kontrolü (Donmayı önlemek için DatabaseHelper'daki güncel checkUser çalışacak)
            try {
                val isUserExist = dbHelper.checkUser(username, password)

                if (isUserExist) {
                    // GİRİŞ BAŞARILI
                    Toast.makeText(this, "Hoş geldin $username!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Giriş sonrası login ekranına dönülmesini engeller
                } else {
                    // GİRİŞ HATALI
                    Toast.makeText(this, "Kullanıcı adı veya şifre hatalı!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Beklenmedik bir veritabanı hatası olursa uygulamayı çökertmek yerine kullanıcıya bildirir
                Toast.makeText(this, "Veritabanı hatası oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // KAYIT OL YAZISI TIKLAMA OLAYI
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}