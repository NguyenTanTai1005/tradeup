package com.example.tradeup.utils

/**
 * Utility functions cho Firebase Database
 */
object FirebaseUtils {
    
    /**
     * Làm sạch string để sử dụng làm key/path trong Firebase Database
     * Firebase Database paths không được chứa: . $ # [ ] /
     */
    fun sanitizeForFirebaseKey(input: String): String {
        return input
            .replace(".", "_dot_")
            .replace("$", "_dollar_")
            .replace("#", "_hash_")
            .replace("[", "_lbracket_")
            .replace("]", "_rbracket_")
            .replace("/", "_slash_")
            .replace("@", "_at_")
    }
    
    /**
     * Khôi phục string từ Firebase key về dạng gốc
     */
    fun unsanitizeFromFirebaseKey(input: String): String {
        return input
            .replace("_dot_", ".")
            .replace("_dollar_", "$")
            .replace("_hash_", "#")
            .replace("_lbracket_", "[")
            .replace("_rbracket_", "]")
            .replace("_slash_", "/")
            .replace("_at_", "@")
    }
}
