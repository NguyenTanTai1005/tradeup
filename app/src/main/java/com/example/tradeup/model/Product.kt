package com.example.tradeup.model

data class Product(
    val id: Long = 0,
    val title: String,
    val description: String,
    val price: Double,
    val userId: Long,
    val createdAt: String = "",
    val status: String = "Available", // Available, Sold, Paused
    val category: String = "Khác", // Điện tử, Thời trang, Nội thất, Khác
    val condition: String = "Mới", // Mới, Cũ
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val imagePaths: String? = null, // JSON string containing list of image paths
    val rating: Float = 0f, // Rating từ 0.0 đến 5.0
    val ratingCount: Int = 0, // Số lượng đánh giá
    val location: String? = null, // Vị trí sản phẩm
    val latitude: Double? = null, // Tọa độ vĩ độ
    val longitude: Double? = null, // Tọa độ kinh độ
    val synced: Boolean = false // Đánh dấu đã được đồng bộ với Firebase
)

enum class ProductStatus(val value: String, val displayName: String) {
    AVAILABLE("Available", "Còn hàng"),
    SOLD("Sold", "Đã bán"),
    PAUSED("Paused", "Tạm dừng");

    companion object {
        fun fromString(value: String): ProductStatus {
            return values().find { it.value == value } ?: AVAILABLE
        }
        
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
        
        fun getValueFromDisplayName(displayName: String): String {
            return values().find { it.displayName == displayName }?.value ?: AVAILABLE.value
        }
    }
}

enum class ProductCategory(val value: String, val displayName: String) {
    ELECTRONICS("Điện tử", "Điện tử"),
    FASHION("Thời trang", "Thời trang"),
    FURNITURE("Nội thất", "Nội thất"),
    BOOKS("Sách", "Sách"),
    SPORTS("Thể thao", "Thể thao"),
    BEAUTY("Làm đẹp", "Làm đẹp"),
    OTHER("Khác", "Khác");

    companion object {
        fun fromString(value: String): ProductCategory {
            return values().find { it.value == value } ?: OTHER
        }
        
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
        
        fun getValueFromDisplayName(displayName: String): String {
            return values().find { it.displayName == displayName }?.value ?: OTHER.value
        }
    }
}

enum class ProductCondition(val value: String, val displayName: String) {
    NEW("Mới", "Mới"),
    USED("Cũ", "Cũ");

    companion object {
        fun fromString(value: String): ProductCondition {
            return values().find { it.value == value } ?: NEW
        }
        
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
        
        fun getValueFromDisplayName(displayName: String): String {
            return values().find { it.displayName == displayName }?.value ?: NEW.value
        }
    }
}

enum class SortOption(val value: String, val displayName: String) {
    NEWEST("newest", "Mới nhất"),
    PRICE_LOW_TO_HIGH("price_asc", "Giá thấp đến cao"),
    PRICE_HIGH_TO_LOW("price_desc", "Giá cao đến thấp");

    companion object {
        fun fromString(value: String): SortOption {
            return values().find { it.value == value } ?: NEWEST
        }
        
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
        
        fun getValueFromDisplayName(displayName: String): String {
            return values().find { it.displayName == displayName }?.value ?: NEWEST.value
        }
    }
}
