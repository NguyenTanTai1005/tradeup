package com.example.tradeup.model

data class PriceOffer(
    val offerId: String = "",
    val productId: Long = 0,
    val buyerEmail: String = "",
    val sellerEmail: String = "",
    val originalPrice: Double = 0.0,
    val offeredPrice: Double = 0.0,
    val message: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val conversationId: String = ""
) {
    constructor() : this("", 0, "", "", 0.0, 0.0, "", "PENDING", 0L, null, "")
}

enum class PriceOfferStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Đang chờ"),
    ACCEPTED("ACCEPTED", "Đã chấp nhận"),
    REJECTED("REJECTED", "Đã từ chối");

    companion object {
        fun fromString(value: String): PriceOfferStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}
