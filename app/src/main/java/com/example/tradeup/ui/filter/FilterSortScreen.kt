package com.example.tradeup.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.ui.components.*

data class FilterCriteria(
    val keyword: String = "",
    val category: String = "Tất cả",
    val condition: String = "Tất cả",
    val status: String = "Tất cả",
    val minPrice: String = "",
    val maxPrice: String = "",
    val sortBy: String = "newest"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortScreen(
    onNavigateBack: () -> Unit,
    onApplyFilter: (List<Product>) -> Unit,
    userId: Long? = null, // null for all products, specific userId for user's products only
    initialFilter: FilterCriteria = FilterCriteria()
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    
    var keyword by remember { mutableStateOf(initialFilter.keyword) }
    var selectedCategory by remember { mutableStateOf(initialFilter.category) }
    var selectedCondition by remember { mutableStateOf(initialFilter.condition) }
    var selectedStatus by remember { mutableStateOf(initialFilter.status) }
    var minPriceText by remember { mutableStateOf(initialFilter.minPrice) }
    var maxPriceText by remember { mutableStateOf(initialFilter.maxPrice) }
    var selectedSort by remember { mutableStateOf(initialFilter.sortBy) }
    
    var isLoading by remember { mutableStateOf(false) }
    var resultCount by remember { mutableStateOf(0) }

    fun applyFilters() {
        isLoading = true
        
        val minPrice = minPriceText.toDoubleOrNull()
        val maxPrice = maxPriceText.toDoubleOrNull()
        
        val categoryFilter = if (selectedCategory == "Tất cả") null else selectedCategory
        val conditionFilter = if (selectedCondition == "Tất cả") null else selectedCondition
        val statusFilter = if (selectedStatus == "Tất cả") null else selectedStatus
        
        val results = dbHelper.getFilteredAndSortedProducts(
            keyword = keyword,
            category = categoryFilter,
            condition = conditionFilter,
            status = statusFilter,
            minPrice = minPrice,
            maxPrice = maxPrice,
            sortBy = selectedSort,
            userId = userId
        )
        
        resultCount = results.size
        onApplyFilter(results)
        isLoading = false
    }

    fun clearAllFilters() {
        keyword = ""
        selectedCategory = "Tất cả"
        selectedCondition = "Tất cả"
        selectedStatus = "Tất cả"
        minPriceText = ""
        maxPriceText = ""
        selectedSort = "newest"
        resultCount = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lọc & Sắp xếp") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { clearAllFilters() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Xóa bộ lọc")
                    }
                }
            )
        },
        bottomBar = {
            // Fixed bottom action bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { clearAllFilters() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🗑️ Xóa")
                    }
                    
                    Button(
                        onClick = { applyFilters() },
                        modifier = Modifier.weight(2f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang tìm...")
                        } else {
                            Text(
                                text = "🔍 TÌM KIẾM",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Search section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔍 Tìm kiếm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        label = { Text("Từ khóa") },
                        placeholder = { Text("Nhập tên sản phẩm...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Filter section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔧 Bộ lọc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Category filter
                    CategoryDropdown(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        includeAllOption = true
                    )
                    
                    // Condition filter
                    ConditionDropdown(
                        selectedCondition = selectedCondition,
                        onConditionSelected = { selectedCondition = it },
                        includeAllOption = true
                    )
                    
                    // Status filter
                    StatusFilterDropdown(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it }
                    )
                    
                    // Price range
                    Text(
                        text = "Khoảng giá (VNĐ)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minPriceText,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    minPriceText = it
                                }
                            },
                            label = { Text("Từ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = maxPriceText,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    maxPriceText = it
                                }
                            },
                            label = { Text("Đến") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Sort section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Sắp xếp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SortDropdown(
                        selectedSort = selectedSort,
                        onSortSelected = { selectedSort = it }
                    )
                }
            }

            // Result preview
            if (resultCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "📋 Tìm thấy $resultCount sản phẩm",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Add bottom spacing for the fixed bottom bar
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        "Tất cả" to "Tất cả",
        "Available" to "Còn hàng",
        "Sold" to "Đã bán",
        "Paused" to "Tạm dừng"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = statusOptions.find { it.first == selectedStatus }?.second ?: "Tất cả",
            onValueChange = { },
            readOnly = true,
            label = { Text("Trạng thái") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
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
