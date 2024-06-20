package com.acneappdetections

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.acneappdetections.api.ApiClient
import com.acneappdetections.api.LoginRequest
import com.acneappdetections.api.LoginResponse
import com.acneappdetections.api.UserApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewForgotPassword: TextView
    private lateinit var textViewSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        textViewSignUp = findViewById(R.id.textViewSignUp)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            // Validasi input pengguna
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and password must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                // Lakukan autentikasi (misalnya, kirim permintaan ke server)
                val userApi = ApiClient.create(UserApi::class.java)
                val loginRequest = LoginRequest(username, password)

                userApi.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse?.success == true) {
                                // Jika autentikasi berhasil, arahkan ke halaman utama
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish() // Opsional: untuk mencegah pengguna kembali ke halaman login dengan tombol back
                            } else {
                                Toast.makeText(this@LoginActivity, "Login failed: ${loginResponse?.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Login error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
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
