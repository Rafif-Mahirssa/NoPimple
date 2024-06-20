package com.acneappdetections.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

data class RegisterRequest(val username: String, val password: String, val email: String)
data class RegisterResponse(val success: Boolean, val message: String)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val success: Boolean, val message: String)

data class UpdateProfileRequest(val name: String, val email: String, val dateOfBirth: String, val number: String, val region: String, val password: String)

data class HistoryItem(val date: String, val description: String, val image: String)

interface UserApi {
    @POST("register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("updateProfile")
    fun updateUserData(@Body request: UpdateProfileRequest): Call<UpdateProfileRequest>

    @GET("getUserData")
    fun getUserData(): Call<UserDataResponse>

    @GET("getHistoryItems")
    fun getHistoryItems(): Call<List<HistoryItem>>
}
