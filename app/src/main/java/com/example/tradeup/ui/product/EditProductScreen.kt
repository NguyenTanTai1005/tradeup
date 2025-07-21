package com.example.tradeup.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.ProductStatus
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    product: Product,
    onNavigateBack: () -> Unit,
    onProductUpdated: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf(product.title) }
    var description by remember { mutableStateOf(product.description) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var selectedStatus by remember { mutableStateOf(product.status) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun updateProduct() {
        if (title.isBlank()) {
            errorMessage = "Vui lòng nhập tiêu đề sản phẩm"
            return
        }
        
        val priceValue = price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            errorMessage = "Vui lòng nhập giá hợp lệ"
            return
        }

        isLoading = true
        errorMessage = ""

        try {
            val result = dbHelper.updateProduct(
                productId = product.id,
                title = title,
                description = description,
                price = priceValue,
                status = selectedStatus
            )
            
            if (result > 0) {
                showSuccessMessage = true
                // Delay navigation to show success message
                coroutineScope.launch {
                    delay(1500)
                    onProductUpdated()
                    onNavigateBack()
                }
            } else {
                errorMessage = "Có lỗi xảy ra khi cập nhật sản phẩm"
            }
        } catch (e: Exception) {
            errorMessage = "Có lỗi xảy ra: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề sản phẩm *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = title.isBlank() && errorMessage.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Price field
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá (VNĐ) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading,
                isError = price.toDoubleOrNull() == null && errorMessage.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status dropdown
            StatusDropdownEdit(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        text = "Cập nhật sản phẩm thành công!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Update button
            Button(
                onClick = { updateProduct() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Cập nhật sản phẩm")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdownEdit(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        "Available" to "Còn hàng",
        "Sold" to "Đã bán",
        "Paused" to "Tạm dừng"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled }
    ) {
        OutlinedTextField(
            value = statusOptions.find { it.first == selectedStatus }?.second ?: selectedStatus,
            onValueChange = { },
            readOnly = true,
            label = { Text("Trạng thái") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statusOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onStatusSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
