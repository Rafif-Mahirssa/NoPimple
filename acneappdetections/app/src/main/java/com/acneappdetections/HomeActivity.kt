package com.acneappdetections

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import android.view.MenuInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.acneappdetections.api.ApiClient
import com.acneappdetections.api.UserApi
import com.acneappdetections.api.UserDataResponse
import com.acneappdetections.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private val PERMISSION_REQUEST_CODE = 3
    private val imageUris = mutableListOf<Uri>()
    private lateinit var adapter: DetectedImageAdapter
    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load TensorFlow Lite model
        try {
            interpreter = Interpreter(loadModelFile())
            labels = loadLabels()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Cek dan minta izin yang diperlukan
        if (!hasPermissions()) {
            requestPermissions()
        }

        // Setup RecyclerView with GridLayoutManager
        adapter = DetectedImageAdapter(imageUris)
        binding.recyclerViewDetectedImages.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewDetectedImages.adapter = adapter

        // Add dummy images to gallery
        addDummyImagesToGallery()

        // Setup startQuestCard click listener
        val startQuestCard: CardView = findViewById(R.id.startQuestCard)
        startQuestCard.setOnClickListener {
            Toast.makeText(this, "Drag and drop an image here to start the quest.", Toast.LENGTH_SHORT).show()
        }

        // Set drag listener for startQuestCard
        startQuestCard.setOnDragListener { view, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        view.invalidate()
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> {
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val item: ClipData.Item = dragEvent.clipData.getItemAt(0)
                    val dragData = item.text.toString()

                    val droppedImageUri = Uri.parse(dragData)
                    processImage(droppedImageUri)

                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    view.invalidate()
                    true
                }
                else -> false
            }
        }

        // Setup BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_upload -> {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                    true
                }
                R.id.navigation_capture -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
                    true
                }
                R.id.navigation_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Setup PopupMenu
        val buttonMenu: ImageButton = findViewById(R.id.buttonMenu)
        buttonMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Setup profile image click listener
        val imageViewProfile: ImageView = findViewById(R.id.imageViewProfile)
        imageViewProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Panggil fetchData untuk mendapatkan data user
        fetchData()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit_profile -> {
                    val intent = Intent(this, EditProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_logout -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Close HomeActivity
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        imageUris.add(it)
                        adapter.notifyDataSetChanged()
                        processImage(it)
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val capturedImage = data?.extras?.get("data") as Bitmap
                    val savedImageUri = saveImageToGallery(capturedImage)
                    savedImageUri?.let {
                        imageUris.add(it)
                        adapter.notifyDataSetChanged()
                        processImage(it)
                    }
                }
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/AcneAppDetections")
        }
        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(uri)
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                outputStream?.close()
            }
        }
        return uri
    }

    private fun addDummyImagesToGallery() {
        val dummyImages = listOf(
            R.drawable.gall1,
            R.drawable.gall2,
            R.drawable.gall3,
            R.drawable.gall4
        )

        for (imageResId in dummyImages) {
            val bitmap = BitmapFactory.decodeResource(resources, imageResId)
            saveImageToGallery(bitmap)?.let {
                imageUris.add(it)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun processImage(imageUri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        val tensorImage = TensorImage.fromBitmap(bitmap)
        val inputBuffer = tensorImage.tensorBuffer.buffer

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), org.tensorflow.lite.DataType.FLOAT32)
        interpreter.run(inputBuffer, outputBuffer.buffer.rewind())

        val scores = outputBuffer.floatArray
        val maxScoreIdx = scores.indices.maxByOrNull { scores[it] } ?: -1
        val detectedLabel = if (maxScoreIdx != -1) labels[maxScoreIdx] else "Unknown"

        val detectedAcneList = arrayListOf(
            DetectedAcne(imageUri.toString(), detectedLabel, "Type", "Description of $detectedLabel", "Treatment for $detectedLabel")
        )

        val intent = Intent(this, DetectedObjectActivity::class.java).apply {
            putParcelableArrayListExtra("DETECTED_ACNE_LIST", detectedAcneList)
        }
        startActivity(intent)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("acne_detector_22kstep.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(): List<String> {
        val labels = mutableListOf<String>()
        assets.open("labelmap.txt").bufferedReader().useLines { lines ->
            lines.forEach { labels.add(it) }
        }
        return labels
    }

    private fun hasPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return cameraPermission == PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan
            } else {
                Toast.makeText(this, "Izin diperlukan untuk menjalankan aplikasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchData() {
        val userApi = ApiClient.create(UserApi::class.java)
        userApi.getUserData().enqueue(object : Callback<UserDataResponse> {
            override fun onResponse(call: Call<UserDataResponse>, response: Response<UserDataResponse>) {
                if (response.isSuccessful) {
                    val userData = response.body()
                    // Update UI dengan data user yang diterima
                    userData?.let {
                        // Misalnya, tampilkan nama user di TextView atau lakukan tindakan lain
                        Log.d("HomeActivity", "User Data: ${it.name}, ${it.email}, ${it.dateOfBirth}, ${it.number}, ${it.region}, ${it.password}")
                    }
                } else {
                    // Handle error
                    Log.e("HomeActivity", "Failed to fetch user data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
                // Handle failure
                Log.e("HomeActivity", "Error fetching user data", t)
            }
        })
    }
}
