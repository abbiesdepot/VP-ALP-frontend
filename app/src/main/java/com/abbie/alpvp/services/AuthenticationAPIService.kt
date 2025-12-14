package com.abbie.alpvp.services

import com.abbie.alpvp.models.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface AuthenticationAPIService {
    @POST("api/register")
    fun register(@Body registerMap: HashMap<String, String>): Call<UserResponse>

    @POST("api/login")
    fun login(@Body loginMap: HashMap<String, String>): Call<UserResponse>
}