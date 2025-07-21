package com.example.tradeup.model

data class User(
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String = "",
    val phone: String = "",
    val bio: String = "",
    val firebaseUid: String? = null,
    val avatarUrl: String? = null,
    val isActive: Boolean = true
)
