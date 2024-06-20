package com.acneappdetections.api

data class UserDataResponse(
    val name: String,
    val email: String,
    val dateOfBirth: String,
    val number: String,
    val region: String,
    val password: String
)
