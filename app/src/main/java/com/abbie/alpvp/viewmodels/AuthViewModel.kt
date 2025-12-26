package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbie.alpvp.models.UserResponse
import com.abbie.alpvp.repositories.AuthenticationRepositoryInterface
import com.abbie.alpvp.repositories.UserRepositoryInterface
import com.abbie.alpvp.utils.JwtUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.awaitResponse

class AuthViewModel(
    private val authRepository: AuthenticationRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val call: Call<UserResponse> = authRepository.login(email, pass)
                val response =  withContext(Dispatchers.IO) { call.execute() }
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.data.token ?: ""

                    val userId = JwtUtils.getUserId(token)
                    val username = JwtUtils.getUsername(token)

                    if (userId != -1) {
                        userRepository.saveUserSession(token, username, userId)
                        _uiState.value = AuthUiState.Success
                    } else {
                        _uiState.value = AuthUiState.Error("Invalid Token")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Login failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(username: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val call: Call<UserResponse> = authRepository.register(username, email, pass)
                val response = withContext(Dispatchers.IO) { call.execute() }
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.data.token ?: ""

                    val userId = JwtUtils.getUserId(token)

                    if (userId != -1) {
                        userRepository.saveUserSession(token, username, userId)

                        _uiState.value = AuthUiState.RegisterSuccess
                    } else {
                        _uiState.value = AuthUiState.Error("Invalid Token ID")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Register failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object RegisterSuccess : AuthUiState()
    data class Error(val msg: String) : AuthUiState()
}