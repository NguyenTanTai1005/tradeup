package com.example.tradeup.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class EmailService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    companion object {
        private const val MAILJET_API_URL = "https://api.mailjet.com/v3.1/send"
        private const val API_KEY = "ff849fc4d097117f99c1e08ef2c45092"
        private const val SECRET_KEY = "fd0f659cadc388e72ee355a4fe15a526"
        // Sử dụng email đã xác thực với Mailjet
        private const val FROM_EMAIL = "luisaccforwork@gmail.com"  // Email đã được xác thực
        private const val FROM_NAME = "TradeUp Team"
    }
    
    /**
     * Gửi email OTP sử dụng Mailjet API
     * @param toEmail Email người nhận
     * @param otpCode Mã OTP
     * @param userName Tên người nhận
     * @return Kết quả gửi email
     */
    suspend fun sendOTPEmail(
        toEmail: String,
        otpCode: String,
        userName: String
    ): EmailResult = withContext(Dispatchers.IO) {
        try {
            println("EmailService: Bắt đầu gửi OTP email đến $toEmail")
            
            val emailBody = createEmailBody(toEmail, otpCode, userName)
            val request = createRequest(emailBody)
            
            println("EmailService: Gửi request đến Mailjet API")
            val response = client.newCall(request).execute()
            
            println("EmailService: Response code: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                println("EmailService: Gửi thành công - $responseBody")
                EmailResult.Success("Email OTP đã được gửi thành công đến $toEmail")
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                println("EmailService: Gửi thất bại - ${response.code}: $errorBody")
                EmailResult.Error("Gửi email thất bại (${response.code}): $errorBody")
            }
        } catch (e: IOException) {
            println("EmailService: Lỗi IOException - ${e.message}")
            EmailResult.Error("Lỗi kết nối mạng: ${e.message}")
        } catch (e: Exception) {
            println("EmailService: Lỗi Exception - ${e.message}")
            e.printStackTrace()
            EmailResult.Error("Lỗi không xác định: ${e.message}")
        }
    }
    
    private fun createEmailBody(toEmail: String, otpCode: String, userName: String): String {
        println("EmailService: Tạo email body cho $toEmail với OTP $otpCode")
        
        val emailContent = """
            Xin chào $userName,
            
            Chúng tôi nhận được yêu cầu xác thực tài khoản TradeUp của bạn.
            
            Mã OTP của bạn là: $otpCode
            
            Lưu ý:
            - Mã này có hiệu lực trong 5 phút
            - Không chia sẻ mã này với bất kỳ ai
            - Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này
            
            Trân trọng,
            Đội ngũ TradeUp
        """.trimIndent()
        
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Mã xác thực OTP - TradeUp</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #1976d2, #1565c0); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="margin: 0; font-size: 28px;">TradeUp</h1>
                    <p style="margin: 10px 0 0 0; font-size: 16px;">Nền tảng mua bán trực tuyến</p>
                </div>
                
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #1976d2; margin-top: 0;">Xác thực tài khoản</h2>
                    <p>Xin chào <strong>$userName</strong>,</p>
                    <p>Chúng tôi nhận được yêu cầu xác thực tài khoản TradeUp của bạn.</p>
                    
                    <div style="background: white; border: 2px dashed #1976d2; padding: 20px; text-align: center; margin: 25px 0; border-radius: 8px;">
                        <p style="margin: 0; font-size: 14px; color: #666; margin-bottom: 10px;">Mã OTP của bạn:</p>
                        <h1 style="color: #1976d2; font-size: 36px; margin: 0; letter-spacing: 8px; font-weight: bold;">$otpCode</h1>
                    </div>
                    
                    <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h4 style="margin: 0 0 10px 0; color: #856404;">⚠️ Lưu ý quan trọng:</h4>
                        <ul style="margin: 0; padding-left: 20px; color: #856404;">
                            <li>Mã này có hiệu lực trong <strong>5 phút</strong></li>
                            <li>Không chia sẻ mã này với bất kỳ ai</li>
                            <li>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này</li>
                        </ul>
                    </div>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <div style="text-align: center; color: #666; font-size: 14px;">
                        <p>Trân trọng,<br><strong>Đội ngũ TradeUp</strong></p>
                        <p style="font-size: 12px; margin-top: 20px;">
                            Email này được gửi tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        val jsonBody = JSONObject().apply {
            put("Messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("From", JSONObject().apply {
                        put("Email", FROM_EMAIL)
                        put("Name", FROM_NAME)
                    })
                    put("To", JSONArray().apply {
                        put(JSONObject().apply {
                            put("Email", toEmail)
                            put("Name", userName)
                        })
                    })
                    put("Subject", "Mã xác thực OTP cho tài khoản TradeUp")
                    put("TextPart", emailContent)
                    put("HTMLPart", htmlContent)
                })
            })
        }
        
        val result = jsonBody.toString()
        println("EmailService: JSON Body length: ${result.length}")
        return result
    }
    
    private fun createRequest(emailBody: String): Request {
        println("EmailService: Tạo HTTP request")
        
        val credentials = Credentials.basic(API_KEY, SECRET_KEY)
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = emailBody.toRequestBody(mediaType)
        
        println("EmailService: API Key: ${API_KEY.take(10)}...")
        println("EmailService: URL: $MAILJET_API_URL")
        
        return Request.Builder()
            .url(MAILJET_API_URL)
            .header("Authorization", credentials)
            .header("Content-Type", "application/json")
            .header("User-Agent", "TradeUp-Android/1.0")
            .post(requestBody)
            .build()
    }
    
    /**
     * Test function to validate Mailjet API connection
     */
    suspend fun testMailjetConnection(): EmailResult = withContext(Dispatchers.IO) {
        try {
            println("EmailService: Testing Mailjet connection...")
            
            // Create a simple test request to Mailjet API
            val testBody = JSONObject().apply {
                put("Messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("From", JSONObject().apply {
                            put("Email", FROM_EMAIL)
                            put("Name", FROM_NAME)
                        })
                        put("To", JSONArray().apply {
                            put(JSONObject().apply {
                                put("Email", FROM_EMAIL) // Send to self for testing
                                put("Name", "Test User")
                            })
                        })
                        put("Subject", "TradeUp - Test Connection")
                        put("TextPart", "This is a test email to validate Mailjet API connection.")
                        put("HTMLPart", "<h3>TradeUp Test Email</h3><p>This is a test email to validate Mailjet API connection.</p>")
                    })
                })
            }.toString()
            
            val credentials = Credentials.basic(API_KEY, SECRET_KEY)
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = testBody.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(MAILJET_API_URL)
                .header("Authorization", credentials)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            println("EmailService: Test response code: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                println("EmailService: Test successful - $responseBody")
                EmailResult.Success("Kết nối Mailjet API thành công!")
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                println("EmailService: Test failed - ${response.code}: $errorBody")
                EmailResult.Error("Test thất bại (${response.code}): $errorBody")
            }
        } catch (e: Exception) {
            println("EmailService: Test exception - ${e.message}")
            e.printStackTrace()
            EmailResult.Error("Test thất bại: ${e.message}")
        }
    }
}

sealed class EmailResult {
    data class Success(val message: String) : EmailResult()
    data class Error(val error: String) : EmailResult()
}
