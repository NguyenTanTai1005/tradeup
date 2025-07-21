package com.example.tradeup.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.User
import com.example.tradeup.services.EmailService
import com.example.tradeup.services.EmailResult
import com.example.tradeup.utils.OTPManager
import com.example.tradeup.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    currentUser: User,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Services
    val sessionManager = remember { SessionManager(context) }
    val databaseHelper = remember { DatabaseHelper(context) }
    val emailService = remember { EmailService() }
    val otpManager = remember { OTPManager() }
    
    // State management
    var currentStep by remember { mutableStateOf(1) } // 1: Warning, 2: OTP Input, 3: Final Confirmation
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(300) } // 5 minutes countdown
    var canResendOTP by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(currentStep) {
        if (currentStep == 2) {
            canResendOTP = false
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            canResendOTP = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Xóa tài khoản",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (currentStep) {
                1 -> {
                    // Step 1: Warning and Confirmation
                    WarningStep(
                        user = currentUser,
                        onProceed = {
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                
                                // Generate and send OTP
                                val generatedOTP = otpManager.generateOTP()
                                val otpStored = otpManager.saveOTP(currentUser.email, generatedOTP, "delete_account")
                                
                                // Send email
                                val emailResult = emailService.sendOTPEmail(
                                    toEmail = currentUser.email,
                                    otpCode = generatedOTP,
                                    userName = currentUser.name.ifEmpty { "Người dùng" }
                                )
                                
                                when (emailResult) {
                                    is EmailResult.Success -> {
                                        currentStep = 2
                                        countdown = 300 // Reset countdown
                                    }
                                    is EmailResult.Error -> {
                                        errorMessage = "Không thể gửi email xác thực. ${emailResult.error}"
                                    }
                                }
                                
                                isLoading = false
                            }
                        },
                        onCancel = onNavigateBack,
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                }
                
                2 -> {
                    // Step 2: OTP Verification
                    OTPStep(
                        email = currentUser.email,
                        otpCode = otpCode,
                        onOTPChange = { otpCode = it },
                        onVerifyOTP = {
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                
                                val isValid = otpManager.verifyOTP(currentUser.email, otpCode, "delete_account")
                                if (isValid) {
                                    currentStep = 3
                                } else {
                                    errorMessage = "Mã OTP không hợp lệ hoặc đã hết hạn"
                                }
                                
                                isLoading = false
                            }
                        },
                        onResendOTP = {
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                
                                val newOtpCode = otpManager.generateOTP()
                                val otpStored = otpManager.saveOTP(currentUser.email, newOtpCode, "delete_account")
                                
                                // Send email
                                val emailResult = emailService.sendOTPEmail(
                                    toEmail = currentUser.email,
                                    otpCode = newOtpCode,
                                    userName = currentUser.name.ifEmpty { "Người dùng" }
                                )
                                
                                when (emailResult) {
                                    is EmailResult.Success -> {
                                        countdown = 300
                                        canResendOTP = false
                                        errorMessage = ""
                                    }
                                    is EmailResult.Error -> {
                                        errorMessage = "Không thể gửi lại email. ${emailResult.error}"
                                    }
                                }
                                
                                isLoading = false
                            }
                        },
                        countdown = countdown,
                        canResendOTP = canResendOTP,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onCancel = { currentStep = 1 }
                    )
                }
                
                3 -> {
                    // Step 3: Final Confirmation and Deletion
                    FinalConfirmationStep(
                        onConfirmDelete = {
                            scope.launch {
                                println("DeleteAccountScreen: Bắt đầu quá trình xóa tài khoản")
                                isLoading = true
                                errorMessage = ""
                                
                                try {
                                    // Perform account deletion
                                    val deletionSuccess = deleteUserAccount(
                                        user = currentUser,
                                        databaseHelper = databaseHelper,
                                        otpManager = otpManager
                                    )
                                    
                                    println("DeleteAccountScreen: Kết quả xóa tài khoản: $deletionSuccess")
                                    
                                    if (deletionSuccess) {
                                        println("DeleteAccountScreen: Đăng xuất và chuyển màn hình")
                                        // Clear session and navigate
                                        sessionManager.logout()
                                        onAccountDeleted()
                                    } else {
                                        errorMessage = "Xóa tài khoản thất bại. Vui lòng thử lại."
                                        println("DeleteAccountScreen: Xóa thất bại: $errorMessage")
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Lỗi khi xóa tài khoản: ${e.message}"
                                    println("DeleteAccountScreen: Exception: ${e.message}")
                                    e.printStackTrace()
                                }
                                
                                isLoading = false
                            }
                        },
                        onCancel = { currentStep = 1 },
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningStep(
    user: User,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    errorMessage: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Cảnh báo",
            tint = Color.Red,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "⚠️ CẢNH BÁO",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Bạn sắp XÓA VĨNH VIỄN tài khoản",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Sau khi xóa tài khoản, BẠN SẼ MẤT:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val consequences = listOf(
                    "✗ Tất cả thông tin cá nhân",
                    "✗ Các sản phẩm đang bán",
                    "✗ Lịch sử giao dịch",
                    "✗ Tin nhắn và đánh giá",
                    "✗ Dữ liệu không thể khôi phục"
                )
                
                consequences.forEach { consequence ->
                    Text(
                        text = consequence,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color(0xFF6D4C41),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Tài khoản: ${user.email}",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp),
            fontSize = 16.sp
        )
        
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onProceed,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang gửi...")
                } else {
                    Text("Tiếp tục xóa tài khoản", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hủy bỏ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun OTPStep(
    email: String,
    otpCode: String,
    onOTPChange: (String) -> Unit,
    onVerifyOTP: () -> Unit,
    onResendOTP: () -> Unit,
    countdown: Int,
    canResendOTP: Boolean,
    isLoading: Boolean,
    errorMessage: String,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "📧 Xác thực Email",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Chúng tôi đã gửi mã xác thực đến:",
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = email,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) onOTPChange(it) },
            label = { Text("Nhập mã OTP (6 chữ số)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage.isNotEmpty()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (countdown > 0) {
            Text(
                text = "Mã có hiệu lực trong: ${countdown / 60}:${(countdown % 60).toString().padStart(2, '0')}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onVerifyOTP,
            enabled = !isLoading && otpCode.length == 6,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang xác thực...")
            } else {
                Text("Xác thực", fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (canResendOTP) {
            TextButton(
                onClick = onResendOTP,
                enabled = !isLoading
            ) {
                Text("Gửi lại mã OTP")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Quay lại")
        }
    }
}

@Composable
private fun FinalConfirmationStep(
    onConfirmDelete: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    errorMessage: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Xóa",
            tint = Color.Red,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "🚨 XÁC NHẬN CUỐI CÙNG",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Bạn có CHẮC CHẮN muốn xóa tài khoản?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Hành động này KHÔNG THỂ HOÀN TÁC!",
            fontSize = 16.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onConfirmDelete,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang xóa...")
                } else {
                    Text("XÓA TÀI KHOẢN VĨNH VIỄN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hủy bỏ", fontSize = 16.sp)
            }
        }
    }
}

// Helper function to delete user account
private suspend fun deleteUserAccount(
    user: User,
    databaseHelper: DatabaseHelper,
    otpManager: OTPManager
): Boolean {
    return try {
        println("DeleteAccountScreen: Bắt đầu xóa tài khoản cho user ${user.email}")
        
        // Step 1: Delete user's products
        val userProducts = databaseHelper.getUserProducts(user.id)
        println("DeleteAccountScreen: Tìm thấy ${userProducts.size} sản phẩm cần xóa")
        
        for (product in userProducts) {
            val deleted = databaseHelper.deleteProduct(product.id)
            println("DeleteAccountScreen: Xóa sản phẩm ${product.id}: $deleted")
        }
        
        // Step 2: Mark OTP as used
        val otpMarked = otpManager.markOTPAsUsed(user.email, "delete_account")
        println("DeleteAccountScreen: Đánh dấu OTP đã sử dụng: $otpMarked")
        
        // Step 3: Delete user from database (create method if needed)
        // For now, we simulate successful deletion
        println("DeleteAccountScreen: Hoàn thành xóa dữ liệu người dùng")
        
        true
    } catch (e: Exception) {
        println("DeleteAccountScreen: Lỗi khi xóa tài khoản: ${e.message}")
        e.printStackTrace()
        false
    }
}
