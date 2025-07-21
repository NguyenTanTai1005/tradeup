package com.example.tradeup.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import com.example.tradeup.ui.components.EnhancedSearchBar
import com.example.tradeup.ui.components.ProductFilterDialog
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onProductClick: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    
    var keyword by remember { mutableStateOf("") }
    var minPriceText by remember { mutableStateOf("") }
    var maxPriceText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    
    // Distance filter states
    var distance by remember { mutableStateOf<Float?>(null) }
    var hasLocationFilter by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    // Available categories
    val categories = remember {
        listOf("Điện tử", "Thời trang", "Xe cộ", "Nhà cửa", "Học tập", "Thể thao")
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Tìm kiếm sản phẩm") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
            },
            actions = {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Bộ lọc")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Enhanced Search Bar with distance filter
            EnhancedSearchBar(
                query = keyword,
                onQueryChange = { keyword = it },
                onSearch = { 
                    performSearch(
                        dbHelper, keyword, minPriceText, maxPriceText, selectedStatus,
                        selectedCategory, hasLocationFilter, distance
                    ) { results ->
                        searchResults = results
                        hasSearched = true
                        isLoading = false
                    }
                    isLoading = true
                },
                showLocationFilter = true,
                onLocationFilterClick = { 
                    hasLocationFilter = !hasLocationFilter
                    if (!hasLocationFilter) {
                        distance = null
                    }
                },
                hasLocationFilter = hasLocationFilter,
                distance = distance,
                onDistanceChange = { newDistance ->
                    distance = newDistance
                    if (newDistance != null && !hasLocationFilter) {
                        hasLocationFilter = true
                    }
                },
                showDistanceFilter = true,
                maxDistance = 50f,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Additional filters form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Bộ lọc bổ sung",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Khoảng giá (VNĐ)",
                        fontSize = 14.sp,
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

                    // Status filter
                    StatusFilterDropdown(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it }
                    )
                }
            }

            // Search results
            if (hasSearched) {
                Text(
                    text = "Kết quả tìm kiếm (${searchResults.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (searchResults.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Không tìm thấy sản phẩm nào",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { product ->
                            SearchResultCard(
                                product = product,
                                onClick = { onProductClick(product) }
                            )
                        }
                    }
                }
            }
        }
        
        // Product Filter Dialog
        ProductFilterDialog(
            showDialog = showFilterDialog,
            onDismiss = { showFilterDialog = false },
            distance = distance,
            onDistanceChange = { newDistance ->
                distance = newDistance
                if (newDistance != null && !hasLocationFilter) {
                    hasLocationFilter = true
                }
            },
            hasLocationFilter = hasLocationFilter,
            onLocationFilterChange = { enabled ->
                hasLocationFilter = enabled
                if (!enabled) {
                    distance = null
                }
            },
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            maxDistance = 50f,
            onApplyFilters = {
                performSearch(
                    dbHelper, keyword, minPriceText, maxPriceText, selectedStatus,
                    selectedCategory, hasLocationFilter, distance
                ) { results ->
                    searchResults = results
                    hasSearched = true
                    isLoading = false
                }
                isLoading = true
            },
            onClearFilters = {
                distance = null
                hasLocationFilter = false
                selectedCategory = null
                keyword = ""
                minPriceText = ""
                maxPriceText = ""
                selectedStatus = ""
            }
        )
    }
}

@Composable
fun SearchResultCard(
    product: Product,
    onClick: () -> Unit = {}
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    if (product.description.isNotEmpty()) {
                        Text(
                            text = product.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    Text(
                        text = currencyFormat.format(product.price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Status badge
                StatusBadge(status = product.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Đăng ngày: ${product.createdAt}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
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
        "" to "Tất cả trạng thái",
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
            value = statusOptions.find { it.first == selectedStatus }?.second ?: "Tất cả trạng thái",
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

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Available" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "Sold" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "Paused" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = when (status) {
                "Available" -> "Còn hàng"
                "Sold" -> "Đã bán"
                "Paused" -> "Tạm dừng"
                else -> status
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

// Helper function to perform search
private fun performSearch(
    dbHelper: DatabaseHelper,
    keyword: String,
    minPriceText: String,
    maxPriceText: String,
    selectedStatus: String,
    selectedCategory: String?,
    hasLocationFilter: Boolean,
    distance: Float?,
    onResults: (List<Product>) -> Unit
) {
    if (keyword.isNotBlank() || minPriceText.isNotBlank() || maxPriceText.isNotBlank() || 
        selectedStatus.isNotBlank() || hasLocationFilter || selectedCategory != null) {
        
        val minPrice = minPriceText.toDoubleOrNull()
        val maxPrice = maxPriceText.toDoubleOrNull()
        val statusFilter = if (selectedStatus.isBlank()) null else selectedStatus
        
        var results = dbHelper.searchProductsAdvanced(
            keyword = if (keyword.isBlank()) "" else keyword.trim(),
            minPrice = minPrice,
            maxPrice = maxPrice,
            status = statusFilter
        )
        
        // Apply category filter
        if (selectedCategory != null) {
            results = results.filter { it.category == selectedCategory }
        }
        
        // TODO: Apply distance filter when location is available
        // if (hasLocationFilter && distance != null) {
        //     results = filterByDistance(results, userLocation, distance)
        // }
        
        onResults(results)
    }
}
