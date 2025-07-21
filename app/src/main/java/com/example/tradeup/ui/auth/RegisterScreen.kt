package com.example.tradeup.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.services.OTPService
import com.example.tradeup.services.OTPServiceResult
import com.example.tradeup.ui.auth.OTPVerificationScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { DatabaseHelper(context) }
    val otpService = remember { OTPService() }
    
    var currentStep by remember { mutableStateOf("register") } // "register" hoặc "otp_verification"
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    when (currentStep) {
        "register" -> {
            RegisterForm(
                name = name,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                errorMessage = errorMessage,
                successMessage = successMessage,
                isLoading = isLoading,
                onNameChange = { name = it },
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onRegisterClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        successMessage = ""
                        
                        // Validation
                        when {
                            name.isBlank() -> {
                                errorMessage = "Vui lòng nhập họ tên"
                                isLoading = false
                                return@launch
                            }
                            email.isBlank() -> {
                                errorMessage = "Vui lòng nhập email"
                                isLoading = false
                                return@launch
                            }
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                errorMessage = "Email không hợp lệ"
                                isLoading = false
                                return@launch
                            }
                            password.length < 6 -> {
                                errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                                isLoading = false
                                return@launch
                            }
                            password != confirmPassword -> {
                                errorMessage = "Mật khẩu xác nhận không khớp"
                                isLoading = false
                                return@launch
                            }
                            dbHelper.isEmailExists(email) -> {
                                errorMessage = "Email đã được sử dụng"
                                isLoading = false
                                return@launch
                            }
                        }
                        
                        // Gửi OTP
                        when (val result = otpService.sendRegistrationOTP(email, name)) {
                            is OTPServiceResult.Success -> {
                                successMessage = result.message
                                currentStep = "otp_verification"
                            }
                            is OTPServiceResult.Error -> {
                                errorMessage = result.error
                            }
                        }
                        isLoading = false
                    }
                },
                onNavigateToLogin = onNavigateToLogin
            )
        }
        
        "otp_verification" -> {
            OTPVerificationScreen(
                email = email,
                purpose = "register",
                onVerificationSuccess = {
                    scope.launch {
                        // Tạo tài khoản sau khi xác thực OTP thành công
                        val userId = dbHelper.registerUser(email, password, name)
                        if (userId > 0) {
                            onRegisterSuccess()
                        } else {
                            errorMessage = "Tạo tài khoản thất bại"
                            currentStep = "register"
                        }
                    }
                },
                onResendOTP = {
                    scope.launch {
                        when (val result = otpService.sendRegistrationOTP(email, name)) {
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
                    currentStep = "register"
                }
            )
        }
    }
}

@Composable
private fun RegisterForm(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    errorMessage: String,
    successMessage: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TradeUp",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Đăng ký tài khoản",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Họ tên") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
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
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Đã có tài khoản? ")
            TextButton(onClick = onNavigateToLogin) {
                Text("Đăng nhập")
            }
        }
    }
}
