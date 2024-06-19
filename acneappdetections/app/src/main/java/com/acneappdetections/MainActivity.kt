package com.acneappdetections

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Pastikan layout yang benar digunakan

        // Simulasikan penundaan selama 3 detik
        Handler().postDelayed({
            // Mulai aktivitas login setelah penundaan
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Akhiri aktivitas splash screen
        }, 3000)
    }
}
