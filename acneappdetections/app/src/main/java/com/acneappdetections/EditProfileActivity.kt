package com.acneappdetections

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.acneappdetections.api.ApiClient
import com.acneappdetections.api.UpdateProfileRequest
import com.acneappdetections.api.UserApi
import com.acneappdetections.api.UserDataResponse
import com.acneappdetections.databinding.ActivityEditProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.lifecycleOwner = this
        binding.profile = viewModel

        // Fetch data from API
        fetchData()

        // Atur listener untuk button Save
        binding.saveButton.setOnClickListener {
            // Simpan data ke database atau shared preferences
            saveProfileData()

            // Tampilkan pesan konfirmasi
            Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()

            // Arahkan ke halaman utama atau halaman lainnya
            finish() // untuk kembali ke activity sebelumnya
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
                        viewModel.name.value = it.name
                        viewModel.email.value = it.email
                        viewModel.dateOfBirth.value = it.dateOfBirth
                        viewModel.number.value = it.number
                        viewModel.region.value = it.region
                        viewModel.password.value = it.password
                    }
                } else {
                    // Handle error
                    Toast.makeText(this@EditProfileActivity, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
                // Handle failure
                Toast.makeText(this@EditProfileActivity, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val userApi = ApiClient.create(UserApi::class.java)
        val updateProfileRequest = UpdateProfileRequest(
            viewModel.name.value ?: "",
            viewModel.email.value ?: "",
            viewModel.dateOfBirth.value ?: "",
            viewModel.number.value ?: "",
            viewModel.region.value ?: "",
            viewModel.password.value ?: ""
        )

        userApi.updateUserData(updateProfileRequest).enqueue(object : Callback<UpdateProfileRequest> {
            override fun onResponse(call: Call<UpdateProfileRequest>, response: Response<UpdateProfileRequest>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateProfileRequest>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
