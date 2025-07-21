package com.example.tradeup.services

import com.example.tradeup.utils.OTPManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OTPService {
    private val otpManager = OTPManager()
    private val emailService = EmailService()
    
    /**
     * Gửi OTP qua email cho việc đăng ký tài khoản
     */
    suspend fun sendRegistrationOTP(
        email: String,
        userName: String
    ): OTPServiceResult = withContext(Dispatchers.IO) {
        try {
            val otpCode = otpManager.generateOTP()
            
            // Gửi email
            val emailResult = emailService.sendOTPEmail(email, otpCode, userName)
            
            when (emailResult) {
                is EmailResult.Success -> {
                    // Lưu OTP vào database
                    val otpId = otpManager.saveOTP(email, otpCode, "register")
                    OTPServiceResult.Success("OTP đã được gửi đến email $email", otpId)
                }
                is EmailResult.Error -> {
                    OTPServiceResult.Error(emailResult.error)
                }
            }
        } catch (e: Exception) {
            OTPServiceResult.Error("Lỗi khi gửi OTP: ${e.message}")
        }
    }
    
    /**
     * Gửi OTP qua email cho việc reset mật khẩu
     */
    suspend fun sendPasswordResetOTP(
        email: String,
        userName: String
    ): OTPServiceResult = withContext(Dispatchers.IO) {
        try {
            val otpCode = otpManager.generateOTP()
            
            // Gửi email
            val emailResult = emailService.sendOTPEmail(email, otpCode, userName)
            
            when (emailResult) {
                is EmailResult.Success -> {
                    // Lưu OTP vào database
                    val otpId = otpManager.saveOTP(email, otpCode, "reset_password")
                    OTPServiceResult.Success("OTP đã được gửi đến email $email", otpId)
                }
                is EmailResult.Error -> {
                    OTPServiceResult.Error(emailResult.error)
                }
            }
        } catch (e: Exception) {
            OTPServiceResult.Error("Lỗi khi gửi OTP: ${e.message}")
        }
    }
    
    /**
     * Xác thực OTP cho đăng ký
     */
    suspend fun verifyRegistrationOTP(
        email: String,
        inputOTP: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            otpManager.verifyOTP(email, inputOTP, "register")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Xác thực OTP cho reset mật khẩu
     */
    suspend fun verifyPasswordResetOTP(
        email: String,
        inputOTP: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            otpManager.verifyOTP(email, inputOTP, "reset_password")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Dọn dẹp các OTP hết hạn
     */
    suspend fun cleanupExpiredOTPs() {
        try {
            otpManager.cleanupExpiredOTPs()
        } catch (e: Exception) {
            // Log error if needed
        }
    }
}

sealed class OTPServiceResult {
    data class Success(val message: String, val otpId: String) : OTPServiceResult()
    data class Error(val error: String) : OTPServiceResult()
}
