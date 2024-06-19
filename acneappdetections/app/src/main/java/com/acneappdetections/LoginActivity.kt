package com.acneappdetections

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewForgotPassword: TextView
    private lateinit var textViewSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        buttonLogin = findViewById<Button>(R.id.buttonLogin)
        textViewForgotPassword = findViewById<TextView>(R.id.textViewForgotPassword)
        textViewSignUp = findViewById<TextView>(R.id.textViewSignUp)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            // Validasi input pengguna
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and password must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                // Lakukan autentikasi (misalnya, kirim permintaan ke server)
                // ...
                // Jika autentikasi berhasil, arahkan ke halaman utama
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // Opsional: untuk mencegah pengguna kembali ke halaman login dengan tombol back
            }
        }

        textViewForgotPassword.setOnClickListener {
            // Tampilkan halaman lupa kata sandi
            // ...
        }

        textViewSignUp.setOnClickListener {
            // Arahkan ke halaman registrasi
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
