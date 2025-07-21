package com.example.tradeup.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.User
import com.example.tradeup.ui.components.ProductRatingCompact
import com.example.tradeup.ui.components.PriceOfferDialog
import com.example.tradeup.utils.SessionManager
import com.example.tradeup.repository.PriceOfferRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentUser: User,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFilteredProducts: () -> Unit,
    onNavigateToMyProducts: () -> Unit,
    onNavigateToSoldProducts: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onProductClick: (Product) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var availableProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Search states
    var searchQuery by remember { mutableStateOf("") }
    var filteredProducts by remember { mutableStateOf<List<Product>>(emptyList()) }

    // Load all available products
    LaunchedEffect(Unit) {
        availableProducts = dbHelper.getAllAvailableProducts()
        filteredProducts = availableProducts
        isLoading = false
    }
    
    // Filter products when search changes
    LaunchedEffect(searchQuery) {
        filteredProducts = if (searchQuery.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter { product ->
                product.title.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("TradeUp - Dashboard") },
            actions = {
                IconButton(onClick = onNavigateToConversations) {
                    Icon(Icons.Default.Email, contentDescription = "Tin nhắn")
                }
                IconButton(onClick = onNavigateToProfile) {
                    Icon(Icons.Default.Person, contentDescription = "Hồ sơ")
                }
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Welcome message
            Text(
                text = "Xin chào, ${currentUser.name.ifEmpty { currentUser.email }}!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Simple Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Tìm kiếm sản phẩm...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else null,
                singleLine = true
            )

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToAddProduct,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm SP", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tìm kiếm", fontSize = 12.sp)
                }
            }

            // Browse and filter buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToFilteredProducts,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lọc SP", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToMyProducts,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SP của tôi", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToSoldProducts,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Đã bán", fontSize = 12.sp)
                }
            }

            // Products section
            Text(
                text = "Sản phẩm đang bán (${filteredProducts.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredProducts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) 
                                "Không tìm thấy sản phẩm nào khớp với từ khóa \"$searchQuery\""
                                else "Hiện tại chưa có sản phẩm nào đang bán",
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onNavigateToAddProduct) {
                                Text("Đăng sản phẩm đầu tiên")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            currentUser = currentUser,
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
    currentUser: User,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val priceOfferRepository = remember { PriceOfferRepository(context) }
    var sellerName by remember { mutableStateOf("") }
    var showPriceOfferDialog by remember { mutableStateOf(false) }
    var isLoadingOffer by remember { mutableStateOf(false) }
    var offerError by remember { mutableStateOf<String?>(null) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    // Check if current user is the seller
    val isOwnProduct = currentUser.id == product.userId
    
    // Load seller information
    LaunchedEffect(product.userId) {
        val seller = dbHelper.getUserById(product.userId)
        sellerName = seller?.name ?: "Người dùng ẩn danh"
    }
    
    // Handle price offer submission
    fun handlePriceOffer(offeredPrice: Double, message: String) {
        isLoadingOffer = true
        offerError = null
        
        val seller = dbHelper.getUserById(product.userId)
        if (seller == null) {
            offerError = "Không tìm thấy thông tin người bán"
            isLoadingOffer = false
            return
        }
        
        // Use coroutine scope to handle suspend function
        CoroutineScope(Dispatchers.Main).launch {
            priceOfferRepository.createPriceOffer(
                productId = product.id,
                buyerEmail = currentUser.email,
                sellerEmail = seller.email,
                originalPrice = product.price,
                offeredPrice = offeredPrice,
                message = message,
                productTitle = product.title,
                onSuccess = { offerId ->
                    isLoadingOffer = false
                    showPriceOfferDialog = false
                    // Có thể thêm thông báo thành công ở đây
                },
                onError = { exception ->
                    isLoadingOffer = false
                    offerError = "Lỗi gửi đề nghị: ${exception.message}"
                }
            )
        }
    }
    
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Người bán: $sellerName",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Rating display
                    ProductRatingCompact(
                        rating = product.rating,
                        ratingCount = product.ratingCount,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Status badge
                StatusBadge(status = product.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.createdAt,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                
                // Price offer button (only show if not own product and product is available)
                if (!isOwnProduct && product.status == "Available") {
                    OutlinedButton(
                        onClick = { showPriceOfferDialog = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đề nghị giá", fontSize = 12.sp)
                    }
                }
            }
            
            // Error message
            offerError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
    
    // Price Offer Dialog
    PriceOfferDialog(
        isVisible = showPriceOfferDialog,
        onDismiss = { showPriceOfferDialog = false },
        productTitle = product.title,
        originalPrice = product.price,
        onSendOffer = ::handlePriceOffer
    )
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
