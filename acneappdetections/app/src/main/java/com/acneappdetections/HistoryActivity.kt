package com.acneappdetections

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acneappdetections.api.ApiClient
import com.acneappdetections.api.HistoryItem
import com.acneappdetections.api.UserApi
import com.acneappdetections.databinding.ActivityHistoryBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private lateinit var currentPhotoPath: String
    private lateinit var adapter: HistoryAdapter
    private val historyItems = mutableListOf<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter(historyItems)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = adapter

        fetchHistoryItems()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_upload -> {
                    openGallery()
                    true
                }
                R.id.navigation_capture -> {
                    openCamera()
                    true
                }
                R.id.navigation_history -> {
                    // Already on history, do nothing
                    true
                }
                else -> false
            }
        }

        // Setup back button
        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchHistoryItems() {
        val userApi = ApiClient.create(UserApi::class.java)
        userApi.getHistoryItems().enqueue(object : Callback<List<HistoryItem>> {
            override fun onResponse(call: Call<List<HistoryItem>>, response: Response<List<HistoryItem>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        historyItems.clear()
                        historyItems.addAll(it)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@HistoryActivity, "Failed to fetch history items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<HistoryItem>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Error fetching history items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = Uri.fromFile(it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                val filePath = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(selectedImageUri, filePath, null, null, null)
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePath[0])
                val picturePath = columnIndex?.let { cursor.getString(it) }
                cursor?.close()

                if (picturePath != null) {
                    // Process the image path (e.g., upload to server or save to database)
                    Toast.makeText(this, "Image selected: $picturePath", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val file = File(currentPhotoPath)
            if (file.exists()) {
                // Process the captured image (e.g., upload to server or save to database)
                Toast.makeText(this, "Photo captured: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class HistoryItem(val date: String, val description: String, val image: String)

class HistoryAdapter(private val historyItems: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_list_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyItems[position]
        holder.bind(historyItem)
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewHistory)

        fun bind(historyItem: HistoryItem) {
            textViewDate.text = historyItem.date
            textViewDescription.text = historyItem.description
            // Load the image
            // imageView.setImageBitmap() or use a library like Glide/Picasso to load the image
        }
    }
}
