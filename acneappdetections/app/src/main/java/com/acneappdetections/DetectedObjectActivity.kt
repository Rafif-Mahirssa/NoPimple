package com.acneappdetections

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.acneappdetections.databinding.ActivityDetectedObjectBinding

class DetectedObjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetectedObjectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectedObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup back button
        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            onBackPressed()
        }

        // Mengambil daftar jerawat yang terdeteksi dari Intent
        val detectedAcneList = intent.getParcelableArrayListExtra<DetectedAcne>("DETECTED_ACNE_LIST")

        // Mengubah list detectedAcneList menjadi list Uri untuk DetectedImageAdapter
        val imageUris = detectedAcneList?.map { Uri.parse(it.imageUri) } ?: emptyList()

        // Setup RecyclerView with DetectedImageAdapter
        val adapter = DetectedImageAdapter(imageUris)
        binding.recyclerViewDetectedAcne.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewDetectedAcne.adapter = adapter
    }
}
