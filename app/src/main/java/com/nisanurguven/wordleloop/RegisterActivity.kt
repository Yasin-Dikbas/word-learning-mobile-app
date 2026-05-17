package com.nisanurguven.wordleloop

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)

        // YENİ ALANLAR (XML'e eklemen gerekenler)
        val etQuestion = findViewById<EditText>(R.id.etRegQuestion)
        val etAnswer = findViewById<EditText>(R.id.etRegAnswer)
        val etHint = findViewById<EditText>(R.id.etRegHint)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val question = etQuestion.text.toString().trim()
            val answer = etAnswer.text.toString().trim()
            val hint = etHint.text.toString().trim()

            if (user.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() &&
                question.isNotEmpty() && answer.isNotEmpty()) {

                val db = dbHelper.writableDatabase

                val values = ContentValues().apply {
                    put("userName", user) // DatabaseHelper ile tam uyumlu (N büyük)
                    put("email", email)
                    put("password", pass)
                    put("securityQuestion", question)
                    put("securityAnswer", answer)
                    put("securityHint", hint)
                    put("createdAt", System.currentTimeMillis().toString())
                }

                val success = db.insert("Users", null, values)

                if (success != -1L) {
                    Toast.makeText(this, "Hesap başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Kayıt sırasında bir hata oluştu (Kullanıcı adı alınmış olabilir)!", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Lütfen gerekli alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}