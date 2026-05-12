package com.nisanurguven.wordleloop

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val edtUserName = findViewById<EditText>(R.id.edtUserName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val dbHelper = DatabaseHelper(this)

        btnRegister.setOnClickListener {
            val userName = edtUserName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            val success = dbHelper.addUser(userName, password, email)

            if (success) {
                Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Kayıt başarısız", Toast.LENGTH_SHORT).show()
            }
        }
    }
}