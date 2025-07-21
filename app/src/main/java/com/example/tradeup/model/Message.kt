package com.example.tradeup.model

data class Message(
    val messageId: String = "",
    val sender: String = "", // email của người gửi
    val receiver: String = "", // email của người nhận
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "TEXT", // TEXT, PRICE_OFFER, PRICE_RESPONSE
    val priceOfferId: String? = null, // ID của đề nghị giá nếu là tin nhắn đề nghị giá
    val metadata: Map<String, Any>? = null // Metadata cho các loại tin nhắn đặc biệt
) {
    // Constructor không tham số cho Firebase
    constructor() : this("", "", "", "", 0L, "TEXT", null, null)
}

enum class MessageType(val value: String) {
    TEXT("TEXT"),
    PRICE_OFFER("PRICE_OFFER"),
    PRICE_RESPONSE("PRICE_RESPONSE");

    companion object {
        fun fromString(value: String): MessageType {
            return values().find { it.value == value } ?: TEXT
        }
    }
}
