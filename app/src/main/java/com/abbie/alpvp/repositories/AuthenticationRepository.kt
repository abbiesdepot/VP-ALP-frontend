package com.abbie.alpvp.repositories

import com.abbie.alpvp.models.UserResponse
import com.abbie.alpvp.services.AuthenticationAPIService
import retrofit2.Call

interface AuthenticationRepositoryInterface {
    fun register(username: String, email: String, password: String): Call<UserResponse>
    fun login(email: String, password: String): Call<UserResponse>
}

class AuthenticationRepository(
    private val authenticationAPIService: AuthenticationAPIService
): AuthenticationRepositoryInterface {
    override fun register(username: String, email: String, password: String): Call<UserResponse> {
        var registerMap = HashMap<String, String>()

        registerMap["username"] = username
        registerMap["email"] = email
        registerMap["password"] = password

        return authenticationAPIService.register(registerMap)
    }

    override fun login(email: String, password: String): Call<UserResponse> {
        var loginMap = HashMap<String, String>()

        loginMap["email"] = email
        loginMap["password"] = password

        return authenticationAPIService.login(loginMap)
    }
}