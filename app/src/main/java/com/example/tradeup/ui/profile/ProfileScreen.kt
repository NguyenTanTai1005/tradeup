package com.example.tradeup.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.User
import com.example.tradeup.utils.SessionManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User,
    onNavigateBack: () -> Unit,
    onUserUpdated: (User) -> Unit,
    onDeleteAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf(currentUser.name) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun updateProfile() {
        if (name.isBlank()) {
            errorMessage = "Vui lòng nhập tên hiển thị"
            return
        }

        isLoading = true
        errorMessage = ""

        try {
            val result = dbHelper.updateUserProfile(
                userId = currentUser.id,
                name = name,
                phone = phone,
                bio = bio
            )
            
            if (result > 0) {
                // Update session with new user data
                val updatedUser = currentUser.copy(
                    name = name,
                    phone = phone,
                    bio = bio
                )
                sessionManager.saveUser(updatedUser)
                onUserUpdated(updatedUser)
                
                showSuccessMessage = true
                isEditing = false
                
                // Hide success message after delay
                coroutineScope.launch {
                    delay(2000)
                    showSuccessMessage = false
                }
            } else {
                errorMessage = "Có lỗi xảy ra khi cập nhật hồ sơ"
            }
        } catch (e: Exception) {
            errorMessage = "Có lỗi xảy ra: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun cancelEdit() {
        name = currentUser.name
        phone = currentUser.phone
        bio = currentUser.bio
        isEditing = false
        errorMessage = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ của tôi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (!isEditing) {
                        TextButton(
                            onClick = { isEditing = true }
                        ) {
                            Text("Chỉnh sửa")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (name.isNotBlank()) name else "Người dùng",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile form
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Thông tin cá nhân",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email field (read-only)
                    OutlinedTextField(
                        value = currentUser.email,
                        onValueChange = { },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên hiển thị *") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing && !isLoading,
                        isError = name.isBlank() && errorMessage.isNotEmpty()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Số điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = isEditing && !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bio field
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Mô tả ngắn") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        enabled = isEditing && !isLoading,
                        placeholder = { Text("Giới thiệu về bản thân...") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success message
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Cập nhật hồ sơ thành công!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action buttons
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { cancelEdit() },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = { updateProfile() },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Lưu")
                        }
                    }
                }
            }

            // Delete Account Section (only show when not editing)
            if (!isEditing) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Khu vực nguy hiểm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Xóa tài khoản sẽ xóa vĩnh viễn tất cả dữ liệu của bạn. Hành động này không thể hoàn tác.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xóa tài khoản")
                        }
                    }
                }
            }
        }
        
        // Delete Account Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        "Xác nhận xóa tài khoản",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            "Bạn có chắc chắn muốn xóa tài khoản này không?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "⚠️ Sau khi xóa, bạn sẽ mất:",
                            fontWeight = FontWeight.Medium,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val consequences = listOf(
                            "• Tất cả thông tin cá nhân",
                            "• Các sản phẩm đang bán",
                            "• Lịch sử giao dịch",
                            "• Tin nhắn và đánh giá"
                        )
                        consequences.forEach { item ->
                            Text(
                                item,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Hành động này KHÔNG THỂ HOÀN TÁC!",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Xóa tài khoản")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Hủy bỏ")
                    }
                }
            )
        }
    }
}
