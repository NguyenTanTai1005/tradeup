package com.example.tradeup.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.User
import com.example.tradeup.ui.components.StatusDropdown
import com.example.tradeup.ui.components.CategoryDropdown
import com.example.tradeup.ui.components.ConditionDropdown
import com.example.tradeup.ui.components.LocationInput
import com.example.tradeup.services.ImageManagerService
import com.example.tradeup.services.ImageManagerResult
import org.json.JSONArray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    currentUser: User,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (ProductPreviewData) -> Unit = { _ -> },
    restoreData: ProductPreviewData? = null
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val imageManager = remember { ImageManagerService() }
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Available") }
    var selectedCategory by remember { mutableStateOf("Khác") }
    var selectedCondition by remember { mutableStateOf("Mới") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Location states
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    
    // Image picker states
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImageRequiredError by remember { mutableStateOf(false) }
    
    // Restore data from preview if available
    LaunchedEffect(restoreData) {
        restoreData?.let { data ->
            title = data.title
            description = data.description
            priceText = if (data.price > 0) data.price.toString() else ""
            selectedStatus = data.status
            selectedCategory = data.category
            selectedCondition = data.condition
            location = data.location ?: ""
            latitude = data.latitude
            longitude = data.longitude
            selectedImages = data.imageUris
            // Clear any existing error messages when restoring
            errorMessage = ""
            successMessage = ""
            showImageRequiredError = false
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris.take(10) // Limit to 10 images
            showImageRequiredError = false
            errorMessage = ""
        }
    }
    
    fun showPreview() {
        if (selectedImages.isEmpty()) {
            showImageRequiredError = true
            errorMessage = "Vui lòng chọn ít nhất 1 ảnh sản phẩm để xem trước"
            return
        }
        
        if (title.isBlank()) {
            errorMessage = "Vui lòng nhập tiêu đề sản phẩm để xem trước"
            return
        }
        
        val previewData = ProductPreviewData(
            title = title.trim(),
            description = description.trim(),
            price = priceText.toDoubleOrNull() ?: 0.0,
            status = selectedStatus,
            category = selectedCategory,
            condition = selectedCondition,
            location = if (location.isNotBlank()) location.trim() else null,
            latitude = latitude,
            longitude = longitude,
            imageUris = selectedImages
        )
        
        onNavigateToPreview(previewData)
    }
    
    fun validateAndSubmit() {
        when {
            selectedImages.isEmpty() -> {
                showImageRequiredError = true
                errorMessage = "Vui lòng chọn ít nhất 1 ảnh sản phẩm"
            }
            title.isBlank() -> {
                errorMessage = "Vui lòng nhập tiêu đề sản phẩm"
            }
            priceText.isBlank() -> {
                errorMessage = "Vui lòng nhập giá sản phẩm"
            }
            else -> {
                val price = priceText.toDoubleOrNull()
                if (price == null || price <= 0) {
                    errorMessage = "Giá sản phẩm không hợp lệ"
                } else {
                    scope.launch {
                        isLoading = true
                        try {
                            // First create the product without images
                            val productId = dbHelper.addProduct(
                                title = title.trim(),
                                description = description.trim(),
                                price = price,
                                userId = currentUser.id,
                                imagePaths = null, // We'll update this after upload
                                status = selectedStatus,
                                category = selectedCategory,
                                condition = selectedCondition,
                                location = if (location.isNotBlank()) location.trim() else null,
                                latitude = latitude,
                                longitude = longitude
                            )
                            
                            if (productId > 0) {
                                // Upload images to FreeImage.host
                                when (val result = imageManager.uploadAndUpdateProductImages(context, selectedImages, productId.toString())) {
                                    is ImageManagerResult.Success -> {
                                        successMessage = "Thêm sản phẩm và upload hình ảnh thành công!"
                                        errorMessage = ""
                                        // Reset form
                                        title = ""
                                        description = ""
                                        priceText = ""
                                        location = ""
                                        latitude = null
                                        longitude = null
                                        selectedImages = emptyList()
                                    }
                                    is ImageManagerResult.Error -> {
                                        // Product created but images failed to upload
                                        successMessage = "Sản phẩm đã được tạo nhưng upload hình ảnh thất bại."
                                        errorMessage = "Lỗi upload: ${result.error}"
                                    }
                                }
                            } else {
                                errorMessage = "Có lỗi xảy ra khi thêm sản phẩm"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Có lỗi xảy ra: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Thêm sản phẩm") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Selection Section
            Text(
                text = "Hình ảnh sản phẩm *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (showImageRequiredError) {
                Text(
                    text = "Vui lòng chọn ít nhất 1 ảnh sản phẩm",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Add image button
                item {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        colors = CardDefaults.cardColors(
                            containerColor = if (showImageRequiredError) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        border = if (showImageRequiredError) {
                            androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                MaterialTheme.colorScheme.error
                            )
                        } else null
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Thêm ảnh",
                                    tint = if (showImageRequiredError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = "Thêm ảnh",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (showImageRequiredError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Selected images
                items(selectedImages) { imageUri ->
                    Box(
                        modifier = Modifier.size(100.dp)
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Ảnh sản phẩm",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Remove button
                        IconButton(
                            onClick = {
                                selectedImages = selectedImages.filter { it != imageUri }
                                if (selectedImages.isEmpty()) {
                                    showImageRequiredError = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Xóa ảnh",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "Đã chọn ${selectedImages.size}/10 ảnh",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    errorMessage = ""
                    successMessage = ""
                },
                label = { Text("Tiêu đề sản phẩm *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { 
                    description = it
                    errorMessage = ""
                    successMessage = ""
                },
                label = { Text("Mô tả sản phẩm") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = { 
                    // Only allow numbers and decimal point
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        priceText = it
                        errorMessage = ""
                        successMessage = ""
                    }
                },
                label = { Text("Giá (VNĐ) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                suffix = { Text("VNĐ") }
            )

            // Category dropdown
            CategoryDropdown(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                enabled = !isLoading
            )

            // Condition dropdown
            ConditionDropdown(
                selectedCondition = selectedCondition,
                onConditionSelected = { selectedCondition = it },
                enabled = !isLoading
            )

            // Status dropdown
            StatusDropdownLocal(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                enabled = !isLoading
            )

            // Location input
            LocationInput(
                location = location,
                latitude = latitude,
                longitude = longitude,
                onLocationChange = { newLocation, newLatitude, newLongitude ->
                    location = newLocation
                    latitude = newLatitude
                    longitude = newLongitude
                },
                isEnabled = !isLoading,
                modifier = Modifier.padding(vertical = 8.dp)
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

            // Preview button
            OutlinedButton(
                onClick = { showPreview() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading && selectedImages.isNotEmpty() && title.isNotBlank()
            ) {
                Text("🔍 Xem trước")
            }

            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isLoading && selectedImages.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Đăng sản phẩm")
                }
            }

            if (successMessage.isNotEmpty()) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quay về trang chính")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdownLocal(
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
        onExpandedChange = { expanded = !expanded && enabled },
        modifier = Modifier.padding(bottom = 16.dp)
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
