package com.example.apiconnection.MODELS

data class UserDto(
    val id: Int,
    val name: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)
