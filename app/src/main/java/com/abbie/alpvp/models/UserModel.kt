package com.abbie.alpvp.models

data class UserResponse (
    val data: UserModel
)

data class UserModel (
    val token: String?
)