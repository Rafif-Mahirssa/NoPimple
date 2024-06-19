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
import android.view.DragEvent
import android.view.MenuInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.acneappdetections.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.OutputStream

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private val PERMISSION_REQUEST_CODE = 3
    private val imageUris = mutableListOf<Uri>()
    private lateinit var adapter: DetectedImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    navigateToDetectedObjectActivity(droppedImageUri)

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
                        navigateToDetectedObjectActivity(it)
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val capturedImage = data?.extras?.get("data") as Bitmap
                    val savedImageUri = saveImageToGallery(capturedImage)
                    savedImageUri?.let {
                        imageUris.add(it)
                        adapter.notifyDataSetChanged()
                        navigateToDetectedObjectActivity(it)
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

    private fun navigateToDetectedObjectActivity(imageUri: Uri?) {
        imageUri?.let {
            // Simulate detected acne list
            val detectedAcneList = arrayListOf(
                DetectedAcne(it.toString(), "Pustule", "Inflammatory", "Pustules are small, inflamed, pus-filled, blister-like sores (pimples) on the skin surface.", "Topical antibiotics such as clindamycin, oral antibiotics, and benzoyl peroxide."),
                DetectedAcne(it.toString(), "Papule", "Non-inflammatory", "Papules are small, red bumps that may be tender to the touch.", "Topical treatments including salicylic acid or benzoyl peroxide."),
                // Add more detected acne data
            )

            val intent = Intent(this, DetectedObjectActivity::class.java).apply {
                putParcelableArrayListExtra("DETECTED_ACNE_LIST", detectedAcneList)
            }
            startActivity(intent)
        }
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
}
