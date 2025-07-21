package com.example.tradeup.utils

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

data class OTPRecord(
    val email: String = "",
    val otpCode: String = "",
    val expiryTime: Long = 0L,
    val isUsed: Boolean = false,
    val purpose: String = "" // "register" hoặc "reset_password"
)

class OTPManager {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val otpRef = database.child("otps")
    
    companion object {
        private const val OTP_EXPIRY_MINUTES = 5
        private const val OTP_LENGTH = 6
    }
    
    /**
     * Tạo mã OTP ngẫu nhiên 6 chữ số
     */
    fun generateOTP(): String {
        return String.format("%06d", Random.nextInt(0, 999999))
    }
    
    /**
     * Lưu OTP vào Firebase Realtime Database
     * @param email Email người nhận
     * @param otpCode Mã OTP
     * @param purpose Mục đích sử dụng OTP (register/reset_password)
     * @return ID của OTP record
     */
    suspend fun saveOTP(email: String, otpCode: String, purpose: String): String {
        val otpId = otpRef.push().key ?: throw Exception("Không thể tạo OTP ID")
        val expiryTime = System.currentTimeMillis() + (OTP_EXPIRY_MINUTES * 60 * 1000)
        
        val otpRecord = OTPRecord(
            email = email,
            otpCode = otpCode,
            expiryTime = expiryTime,
            isUsed = false,
            purpose = purpose
        )
        
        otpRef.child(otpId).setValue(otpRecord).await()
        return otpId
    }
    
    /**
     * Xác thực mã OTP
     * @param email Email cần xác thực
     * @param inputOTP Mã OTP người dùng nhập
     * @param purpose Mục đích sử dụng OTP
     * @return true nếu OTP hợp lệ, false nếu không
     */
    suspend fun verifyOTP(email: String, inputOTP: String, purpose: String): Boolean {
        try {
            val snapshot = otpRef.orderByChild("email").equalTo(email).get().await()
            
            for (child in snapshot.children) {
                val otpRecord = child.getValue(OTPRecord::class.java)
                if (otpRecord != null && 
                    otpRecord.otpCode == inputOTP && 
                    otpRecord.purpose == purpose &&
                    !otpRecord.isUsed && 
                    System.currentTimeMillis() < otpRecord.expiryTime) {
                    
                    // Đánh dấu OTP đã được sử dụng
                    child.ref.child("isUsed").setValue(true).await()
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Xóa các OTP đã hết hạn
     */
    suspend fun cleanupExpiredOTPs() {
        try {
            val currentTime = System.currentTimeMillis()
            val snapshot = otpRef.get().await()
            
            for (child in snapshot.children) {
                val otpRecord = child.getValue(OTPRecord::class.java)
                if (otpRecord != null && currentTime > otpRecord.expiryTime) {
                    child.ref.removeValue().await()
                }
            }
        } catch (e: Exception) {
            // Log error if needed
        }
    }
    
    /**
     * Đánh dấu OTP đã được sử dụng cho một email và purpose cụ thể
     */
    suspend fun markOTPAsUsed(email: String, purpose: String): Boolean {
        try {
            val snapshot = otpRef.orderByChild("email").equalTo(email).get().await()
            
            for (child in snapshot.children) {
                val otpRecord = child.getValue(OTPRecord::class.java)
                if (otpRecord != null && 
                    otpRecord.purpose == purpose &&
                    !otpRecord.isUsed) {
                    
                    child.ref.child("isUsed").setValue(true).await()
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}
