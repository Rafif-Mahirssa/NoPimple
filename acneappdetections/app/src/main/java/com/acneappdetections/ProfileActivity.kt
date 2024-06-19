package com.acneappdetections

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.acneappdetections.databinding.ActivityEditProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ProfileViewModel
        val profileViewModel = ProfileViewModel()
        binding.profile = profileViewModel

        // Example data
        profileViewModel.name.value = "John Doe"
        profileViewModel.email.value = "john.doe@example.com"
        profileViewModel.mobileNumber.value = "+1234567890"
        profileViewModel.dateOfBirth.value = "01/01/1990"
        profileViewModel.country.value = "USA"
    }
}
