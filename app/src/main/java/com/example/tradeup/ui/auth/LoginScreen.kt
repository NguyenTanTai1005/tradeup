package com.example.tradeup.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.utils.SessionManager
import com.example.tradeup.model.User
import com.example.tradeup.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    googleSignInManager: com.example.tradeup.auth.GoogleSignInManager? = null,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { DatabaseHelper(context) }
    val sessionManager = remember { SessionManager(context) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // Setup Google Sign-In callbacks
    LaunchedEffect(googleSignInManager) {
        googleSignInManager?.setCallbacks(
            onSuccess = { user ->
                scope.launch {
                    // Save Google user to local database if not exists
                    var localUser = dbHelper.getUserByEmail(user.email)
                    if (localUser == null && user.firebaseUid != null) {
                        // Register new user from Google Sign-In
                        val userId = dbHelper.registerUserWithFirebase(
                            email = user.email,
                            password = "", // Google users don't have password
                            name = user.name,
                            firebaseUid = user.firebaseUid,
                            avatarUrl = user.avatarUrl
                        )
                        if (userId > 0) {
                            localUser = dbHelper.getUserByEmail(user.email)
                        }
                    }
                    
                    localUser?.let {
                        sessionManager.saveUserSession(it)
                        isGoogleLoading = false
                        onLoginSuccess()
                    } ?: run {
                        errorMessage = "Không thể tạo tài khoản Google"
                        isGoogleLoading = false
                    }
                }
            },
            onError = { error ->
                errorMessage = error
                isGoogleLoading = false
            }
        )
    }

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
            text = "Đăng nhập",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = ""
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = ""
            },
            label = { Text("Mật khẩu") },
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

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                    return@Button
                }
                
                isLoading = true
                val user = dbHelper.loginUser(email.trim(), password)
                
                if (user != null) {
                    sessionManager.saveUserSession(user)
                    onLoginSuccess()
                } else {
                    errorMessage = "Email hoặc mật khẩu không đúng"
                }
                isLoading = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading && !isGoogleLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Đăng nhập")
            }
        }

        // Divider với text "Hoặc"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Hoặc",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Google Sign-In Button
        googleSignInManager?.let { signInManager ->
            OutlinedButton(
                onClick = {
                    if (!isLoading && !isGoogleLoading) {
                        isGoogleLoading = true
                        errorMessage = ""
                        signInManager.signIn()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isLoading && !isGoogleLoading,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang đăng nhập...")
                } else {
                    // Google Icon
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng nhập bằng Google")
                }
            }
        }

        TextButton(
            onClick = onNavigateToRegister
        ) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }

        TextButton(
            onClick = onNavigateToForgotPassword
        ) {
            Text("Quên mật khẩu?")
        }
    }
}
