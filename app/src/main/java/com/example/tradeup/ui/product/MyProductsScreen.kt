package com.example.tradeup.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.ProductStatus
import com.example.tradeup.model.User
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    currentUser: User,
    onNavigateBack: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onProductClick: (Product) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var products by remember { mutableStateOf(listOf<Product>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showMarkAsSoldDialog by remember { mutableStateOf(false) }
    var productToMarkSold by remember { mutableStateOf<Product?>(null) }

    // Load user's products
    LaunchedEffect(Unit) {
        products = dbHelper.getUserProducts(currentUser.id)
    }

    fun refreshProducts() {
        products = dbHelper.getUserProducts(currentUser.id)
    }

    fun deleteProduct(product: Product) {
        dbHelper.deleteProduct(product.id)
        refreshProducts()
    }

    fun markProductAsSold(product: Product) {
        dbHelper.markProductAsSold(product.id)
        refreshProducts()
    }

    // Delete confirmation dialog
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm \"${productToDelete!!.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { deleteProduct(it) }
                        showDeleteDialog = false
                        productToDelete = null
                    }
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Mark as sold confirmation dialog
    if (showMarkAsSoldDialog && productToMarkSold != null) {
        AlertDialog(
            onDismissRequest = { showMarkAsSoldDialog = false },
            title = { Text("Đánh dấu đã bán") },
            text = { Text("Bạn có chắc chắn muốn đánh dấu sản phẩm \"${productToMarkSold!!.title}\" là đã bán?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToMarkSold?.let { markProductAsSold(it) }
                        showMarkAsSoldDialog = false
                        productToMarkSold = null
                    }
                ) {
                    Text("Đánh dấu đã bán")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAsSoldDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sản phẩm của tôi") },
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
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bạn chưa đăng sản phẩm nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { onEditProduct(product) },
                            onDelete = {
                                productToDelete = product
                                showDeleteDialog = true
                            },
                            onMarkAsSold = {
                                productToMarkSold = product
                                showMarkAsSoldDialog = true
                            },
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsSold: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            .format(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Status badge
                    StatusBadge(status = product.status)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Action buttons
                    Row {
                        // Only show mark as sold button if product is available
                        if (product.status == "Available") {
                            IconButton(onClick = onMarkAsSold) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Đánh dấu đã bán",
                                    tint = Color(0xFF4CAF50) // Green color for success
                                )
                            }
                        }
                        
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Sửa",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Đăng lúc: ${product.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
