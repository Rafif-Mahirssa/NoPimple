package com.acneappdetections

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val number = MutableLiveData<String>()
    val dateOfBirth = MutableLiveData<String>()
    val region = MutableLiveData<String>()
    val password = MutableLiveData<String>()
}
