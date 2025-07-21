package com.example.tradeup.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.User
import com.example.tradeup.ui.components.StatusBadge
import com.example.tradeup.ui.filter.FilterCriteria
import com.example.tradeup.ui.filter.FilterSortScreen
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredProductsScreen(
    currentUser: User?,
    onNavigateBack: () -> Unit,
    showOnlyUserProducts: Boolean = false, // true to show only current user's products
    initialFilter: FilterCriteria = FilterCriteria(),
    onProductClick: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    
    var filteredProducts by remember { mutableStateOf(listOf<Product>()) }
    var showFilterScreen by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(initialFilter) }
    var isLoading by remember { mutableStateOf(true) }

    // Load initial products
    LaunchedEffect(Unit) {
        val userId = if (showOnlyUserProducts) currentUser?.id else null
        filteredProducts = dbHelper.getFilteredAndSortedProducts(
            keyword = initialFilter.keyword,
            category = if (initialFilter.category == "Tất cả") null else initialFilter.category,
            condition = if (initialFilter.condition == "Tất cả") null else initialFilter.condition,
            status = if (initialFilter.status == "Tất cả") null else initialFilter.status,
            minPrice = initialFilter.minPrice.toDoubleOrNull(),
            maxPrice = initialFilter.maxPrice.toDoubleOrNull(),
            sortBy = initialFilter.sortBy,
            userId = userId
        )
        isLoading = false
    }

    if (showFilterScreen) {
        FilterSortScreen(
            onNavigateBack = { showFilterScreen = false },
            onApplyFilter = { products ->
                filteredProducts = products
                showFilterScreen = false
            },
            userId = if (showOnlyUserProducts) currentUser?.id else null,
            initialFilter = currentFilter
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            if (showOnlyUserProducts) "Sản phẩm của tôi" 
                            else "Tất cả sản phẩm"
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilterScreen = true }) {
                            Icon(Icons.Default.List, contentDescription = "Lọc & Sắp xếp")
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
            ) {
                // Summary card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tìm thấy ${filteredProducts.size} sản phẩm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (filteredProducts.isNotEmpty()) {
                                val totalValue = filteredProducts.sumOf { it.price }
                                Text(
                                    text = "Tổng giá trị: ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalValue)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        IconButton(onClick = { showFilterScreen = true }) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "Lọc & Sắp xếp",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Products list
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📦",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Không tìm thấy sản phẩm nào",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { product ->
                            FilteredProductCard(
                                product = product,
                                showOwnerInfo = !showOnlyUserProducts,
                                currentUserId = currentUser?.id,
                                onClick = { onProductClick(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilteredProductCard(
    product: Product,
    showOwnerInfo: Boolean = true,
    currentUserId: Long? = null,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var ownerName by remember { mutableStateOf("") }
    
    // Load owner info if needed
    LaunchedEffect(product.userId) {
        if (showOwnerInfo && product.userId != currentUserId) {
            val owner = dbHelper.getUserById(product.userId)
            ownerName = owner?.name ?: "Người dùng ẩn danh"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    // Title
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Category and Condition
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryBadge(category = product.category)
                        ConditionBadge(condition = product.condition)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description
                    if (product.description.isNotBlank()) {
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Price
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            .format(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Status badge
                StatusBadge(status = product.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (showOwnerInfo && ownerName.isNotBlank()) {
                        Text(
                            text = "Người bán: $ownerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Đăng lúc: ${product.createdAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ConditionBadge(condition: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = condition,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 10.sp
        )
    }
}
