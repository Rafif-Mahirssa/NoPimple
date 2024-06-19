package com.acneappdetections

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.acneappdetections.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.lifecycleOwner = this
        binding.profile = viewModel

        // Set nilai default untuk view
        viewModel.name.value = "Jamelia"
        viewModel.email.value = "jamelia@gmail.com"
        viewModel.mobileNumber.value = "************"
        viewModel.dateOfBirth.value = "31/05/2002"
        viewModel.country.value = "India"

        // Atur listener untuk button Save
        binding.saveButton.setOnClickListener {
            // Simpan data ke database atau shared preferences
            android.widget.Toast.makeText(this, "Data saved successfully!", android.widget.Toast.LENGTH_SHORT).show()

            // Arahkan ke halaman utama atau halaman lainnya
            finish() // untuk kembali ke activity sebelumnya
        }
    }
}
