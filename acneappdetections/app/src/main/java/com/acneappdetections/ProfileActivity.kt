package com.acneappdetections

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.acneappdetections.api.ApiClient
import com.acneappdetections.api.UserApi
import com.acneappdetections.api.UserDataResponse
import com.acneappdetections.databinding.ActivityEditProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ProfileViewModel
        val profileViewModel = ProfileViewModel()
        binding.profile = profileViewModel

        // Fetch user data from API
        fetchUserData(profileViewModel)
    }

    private fun fetchUserData(profileViewModel: ProfileViewModel) {
        val userApi = ApiClient.create(UserApi::class.java)
        userApi.getUserData().enqueue(object : Callback<UserDataResponse> {
            override fun onResponse(call: Call<UserDataResponse>, response: Response<UserDataResponse>) {
                if (response.isSuccessful) {
                    val userData = response.body()
                    userData?.let {
                        // Update ProfileViewModel with the fetched user data
                        profileViewModel.name.value = it.name
                        profileViewModel.email.value = it.email
                        profileViewModel.number.value = it.number
                        profileViewModel.dateOfBirth.value = it.dateOfBirth
                        profileViewModel.region.value = it.region
                        profileViewModel.password.value = it.password
                    }
                } else {
                    // Handle error
                    Log.e("ProfileActivity", "Failed to fetch user data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
                // Handle failure
                Log.e("ProfileActivity", "Error fetching user data", t)
            }
        })
    }
}
