package com.acneappdetections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class ProfileViewModel : ViewModel() {
    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val mobileNumber = MutableLiveData<String>()
    val dateOfBirth = MutableLiveData<String>()
    val country = MutableLiveData<String>()
}
