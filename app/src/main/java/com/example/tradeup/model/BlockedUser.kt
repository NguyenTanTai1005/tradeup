package com.example.tradeup.model

data class BlockedUser(
    val id: Long = 0,
    val blockerEmail: String, // Email của người chặn
    val blockedEmail: String, // Email của người bị chặn
    val blockedAt: Long = System.currentTimeMillis() // Thời gian chặn
)
