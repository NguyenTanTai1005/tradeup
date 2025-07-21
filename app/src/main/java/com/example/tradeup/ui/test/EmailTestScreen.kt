package com.example.tradeup.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.services.EmailService
import com.example.tradeup.services.EmailResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTestScreen() {
    val scope = rememberCoroutineScope()
    val emailService = remember { EmailService() }
    
    var testEmail by remember { mutableStateOf("") }
    var testName by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("123456") }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Test Mailjet API",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Thông tin Mailjet",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("API Key: ff849fc4d097117f99c1e08ef2c45092")
                Text("Secret Key: fd0f659cadc388e72ee355a4fe15a526")
                Text("From Email: luisaccforwork@gmail.com")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = testEmail,
            onValueChange = { testEmail = it },
            label = { Text("Email người nhận") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            placeholder = { Text("example@gmail.com") }
        )

        OutlinedTextField(
            value = testName,
            onValueChange = { testName = it },
            label = { Text("Tên người nhận") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            placeholder = { Text("Tên của bạn") }
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            label = { Text("Mã OTP test") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    resultMessage = ""
                    
                    val result = emailService.testMailjetConnection()
                    when (result) {
                        is EmailResult.Success -> {
                            isSuccess = true
                            resultMessage = "✅ ${result.message}"
                        }
                        is EmailResult.Error -> {
                            isSuccess = false
                            resultMessage = "❌ ${result.error}"
                        }
                    }
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Test Connection")
        }

        Button(
            onClick = {
                if (testEmail.isNotEmpty() && testName.isNotEmpty()) {
                    scope.launch {
                        isLoading = true
                        resultMessage = ""
                        
                        val result = emailService.sendOTPEmail(testEmail, otpCode, testName)
                        when (result) {
                            is EmailResult.Success -> {
                                isSuccess = true
                                resultMessage = "✅ ${result.message}"
                            }
                            is EmailResult.Error -> {
                                isSuccess = false
                                resultMessage = "❌ ${result.error}"
                            }
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading && testEmail.isNotEmpty() && testName.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Gửi Email OTP Test")
        }

        if (resultMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = resultMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSuccess) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Hướng dẫn test:",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("1. Nhấn 'Test Connection' để kiểm tra kết nối API")
                Text("2. Nhập email và tên, sau đó nhấn 'Gửi Email OTP Test'")
                Text("3. Kiểm tra hộp thư email để xem có nhận được không")
                Text("4. Nếu thành công, OTP feature sẽ hoạt động bình thường")
            }
        }
    }
}
