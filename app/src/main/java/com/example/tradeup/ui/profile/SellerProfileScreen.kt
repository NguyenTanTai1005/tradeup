package com.example.tradeup.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.User
import com.example.tradeup.ui.components.ProductCard
import com.example.tradeup.ui.components.ProductRatingCompact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onContactSeller: (User) -> Unit = {}
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var user by remember { mutableStateOf<User?>(null) }
    var sellerProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var soldProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var averageRating by remember { mutableStateOf(0f) }
    var totalRatings by remember { mutableStateOf(0) }
    
    // Load seller data
    LaunchedEffect(userId) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val sellerUser = dbHelper.getUserById(userId)
                    val activeProducts = dbHelper.getUserProductsByStatus(userId, "Available")
                    val completedProducts = dbHelper.getUserProductsByStatus(userId, "Sold")
                    
                    // Calculate average rating from all user's products
                    val allProducts = dbHelper.getUserProducts(userId)
                    var totalRatingSum = 0f
                    var ratingCount = 0
                    
                    allProducts.forEach { product ->
                        if (product.ratingCount > 0) {
                            totalRatingSum += product.rating * product.ratingCount
                            ratingCount += product.ratingCount
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        user = sellerUser
                        sellerProducts = activeProducts
                        soldProducts = completedProducts
                        averageRating = if (ratingCount > 0) totalRatingSum / ratingCount else 0f
                        totalRatings = ratingCount
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin người bán") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy thông tin người bán",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Profile Section
                item {
                    SellerProfileHeader(
                        user = user!!,
                        averageRating = averageRating,
                        totalRatings = totalRatings,
                        activeProductCount = sellerProducts.size,
                        soldProductCount = soldProducts.size,
                        onContactSeller = { onContactSeller(user!!) }
                    )
                }
                
                // Active Products Section
                if (sellerProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Sản phẩm đang bán (${sellerProducts.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(sellerProducts) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            showSellerInfo = false // Không hiển thị thông tin người bán vì đây là trang của họ
                        )
                    }
                }
                
                // Sold Products Section (Optional - cho thấy track record)
                if (soldProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Sản phẩm đã bán (${soldProducts.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(soldProducts.take(5)) { product -> // Chỉ hiển thị 5 sản phẩm đầu
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        ) {
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product) },
                                showSellerInfo = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    if (soldProducts.size > 5) {
                        item {
                            Text(
                                text = "và ${soldProducts.size - 5} sản phẩm khác...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Empty state
                if (sellerProducts.isEmpty() && soldProducts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Người bán này chưa có sản phẩm nào",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerProfileHeader(
    user: User,
    averageRating: Float,
    totalRatings: Int,
    activeProductCount: Int,
    soldProductCount: Int,
    onContactSeller: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            AsyncImage(
                model = user.avatarUrl ?: "https://via.placeholder.com/100",
                contentDescription = "Avatar của ${user.name}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name
            Text(
                text = user.name.ifEmpty { "Người dùng" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Rating
            if (totalRatings > 0) {
                ProductRatingCompact(
                    rating = averageRating,
                    ratingCount = totalRatings,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Bio
            if (user.bio.isNotEmpty()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "Đang bán",
                    value = activeProductCount.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatisticItem(
                    label = "Đã bán",
                    value = soldProductCount.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StatisticItem(
                    label = "Đánh giá",
                    value = if (totalRatings > 0) String.format("%.1f", averageRating) else "0.0",
                    color = Color(0xFFFFD700)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contact Info (if available)
            if (user.phone.isNotEmpty() || user.email.isNotEmpty()) {
                Column {
                    if (user.phone.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (user.email.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Contact Button
            Button(
                onClick = onContactSeller,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Liên hệ người bán")
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
