package com.example.tradeup.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.services.OTPService
import com.example.tradeup.services.OTPServiceResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onPasswordResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { DatabaseHelper(context) }
    val otpService = remember { OTPService() }
    
    var currentStep by remember { mutableStateOf("email") } // "email", "otp_verification", "new_password"
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }

    when (currentStep) {
        "email" -> {
            EmailInputStep(
                email = email,
                errorMessage = errorMessage,
                successMessage = successMessage,
                isLoading = isLoading,
                onEmailChange = { email = it },
                onSendOTP = {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        successMessage = ""
                        
                        // Kiểm tra email có tồn tại không
                        val user = dbHelper.getUserByEmail(email.trim())
                        if (user != null) {
                            userName = user.name
                            when (val result = otpService.sendPasswordResetOTP(email.trim(), user.name)) {
                                is OTPServiceResult.Success -> {
                                    successMessage = result.message
                                    currentStep = "otp_verification"
                                }
                                is OTPServiceResult.Error -> {
                                    errorMessage = result.error
                                }
                            }
                        } else {
                            errorMessage = "Email không tồn tại trong hệ thống"
                        }
                        isLoading = false
                    }
                },
                onNavigateBack = onNavigateBack
            )
        }
        
        "otp_verification" -> {
            OTPVerificationScreen(
                email = email,
                purpose = "reset_password",
                onVerificationSuccess = {
                    currentStep = "new_password"
                },
                onResendOTP = {
                    scope.launch {
                        when (val result = otpService.sendPasswordResetOTP(email, userName)) {
                            is OTPServiceResult.Success -> {
                                successMessage = "OTP đã được gửi lại"
                            }
                            is OTPServiceResult.Error -> {
                                errorMessage = result.error
                            }
                        }
                    }
                },
                onNavigateBack = {
                    currentStep = "email"
                }
            )
        }
        
        "new_password" -> {
            NewPasswordStep(
                newPassword = newPassword,
                confirmPassword = confirmPassword,
                errorMessage = errorMessage,
                successMessage = successMessage,
                isLoading = isLoading,
                onNewPasswordChange = { newPassword = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onResetPassword = {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        
                        when {
                            newPassword.length < 6 -> {
                                errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                            }
                            newPassword != confirmPassword -> {
                                errorMessage = "Mật khẩu xác nhận không khớp"
                            }
                            else -> {
                                val success = dbHelper.updatePassword(email, newPassword)
                                if (success) {
                                    successMessage = "Đặt lại mật khẩu thành công!"
                                    onPasswordResetSuccess()
                                } else {
                                    errorMessage = "Đặt lại mật khẩu thất bại"
                                }
                            }
                        }
                        isLoading = false
                    }
                },
                onNavigateBack = {
                    currentStep = "otp_verification"
                }
            )
        }
    }
}

@Composable
private fun EmailInputStep(
    email: String,
    errorMessage: String,
    successMessage: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSendOTP: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Quên mật khẩu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Nhập email của bạn để nhận mã xác thực",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage.isNotEmpty()
        )
        
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        
        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    onSendOTP()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Gửi mã xác thực")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateBack) {
            Text("Quay lại đăng nhập")
        }
    }
}

@Composable
private fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    errorMessage: String,
    successMessage: String,
    isLoading: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPassword: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Đặt mật khẩu mới",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Nhập mật khẩu mới cho tài khoản của bạn",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("Mật khẩu mới") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage.isNotEmpty()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage.isNotEmpty()
        )
        
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        
        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onResetPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Đặt lại mật khẩu")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateBack) {
            Text("Quay lại")
        }
    }
}
