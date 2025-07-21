package com.example.tradeup.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.User
import com.example.tradeup.ui.components.StatusBadge
import com.example.tradeup.ui.components.ProductRatingDisplay
import com.example.tradeup.ui.components.ProductRatingDialog
import com.example.tradeup.ui.components.QuickRatingCard
import com.example.tradeup.ui.components.LocationDisplay
import com.example.tradeup.ui.components.PriceOfferDialog
import com.example.tradeup.utils.SessionManager
import com.example.tradeup.utils.LocationHelper
import com.example.tradeup.repository.PriceOfferRepository
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (buyerEmail: String, sellerEmail: String, productTitle: String) -> Unit = { _, _, _ -> },
    onNavigateToSellerProfile: (Long) -> Unit = { _ -> }
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val sessionManager = remember { SessionManager(context) }
    val priceOfferRepository = remember { PriceOfferRepository(context) }
    val currentUser = sessionManager.getCurrentUser()
    val scope = rememberCoroutineScope()

    var product by remember { mutableStateOf<Product?>(null) }
    var seller by remember { mutableStateOf<User?>(null) }
    var imagePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showPriceOfferDialog by remember { mutableStateOf(false) }
    var isRatingInProgress by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var offerError by remember { mutableStateOf<String?>(null) }

    // Load product details
    LaunchedEffect(productId) {
        try {
            isLoading = true
            hasError = false
            val prod = dbHelper.getProductById(productId)
            if (prod != null) {
                product = prod
                seller = dbHelper.getUserById(prod.userId)

                // Parse image paths from JSON với debug logging
                prod.imagePaths?.let { paths ->
                    println("Debug: Raw imagePaths = $paths")
                    try {
                        if (paths.startsWith("[") && paths.endsWith("]")) {
                            // JSON Array format
                            val jsonArray = JSONArray(paths)
                            val pathsList = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                val path = jsonArray.getString(i)
                                if (path.isNotBlank()) {
                                    pathsList.add(path)
                                }
                            }
                            imagePaths = pathsList
                            println("Debug: Parsed ${pathsList.size} image paths: $pathsList")
                        } else {
                            // Single path or comma-separated paths
                            imagePaths = paths.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            println("Debug: Split paths: $imagePaths")
                        }
                    } catch (e: Exception) {
                        println("Debug: Failed to parse imagePaths: ${e.message}")
                        imagePaths = emptyList()
                    }
                } ?: run {
                    println("Debug: No imagePaths found for product")
                    imagePaths = emptyList()
                }
                isLoading = false
            } else {
                println("Debug: Product not found with ID: $productId")
                hasError = true
                isLoading = false
            }
        } catch (e: Exception) {
            println("Debug: Error loading product details: ${e.message}")
            hasError = true
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            hasError || product == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Không thể tải thông tin sản phẩm",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Quay lại")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Product images
                    if (imagePaths.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                items(imagePaths) { imagePath ->
                                    AsyncImage(
                                        model = imagePath,
                                        contentDescription = "Hình sản phẩm",
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop,
                                        onError = {
                                            // Log error để debug
                                            println("Failed to load image: $imagePath")
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // Hiển thị placeholder khi không có hình
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có hình ảnh",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Product title
                    Text(
                        text = product!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            .format(product!!.price),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trạng thái: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        StatusBadge(status = product!!.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rating Display
                    ProductRatingDisplay(
                        rating = product!!.rating,
                        ratingCount = product!!.ratingCount,
                        modifier = Modifier.fillMaxWidth(),
                        showText = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category and Condition
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            DetailRow("Danh mục", product!!.category)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Tình trạng", product!!.condition)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Seller info with clickable action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Đăng bởi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = seller?.name ?: "Không xác định",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (seller != null && currentUser?.id != seller!!.id) {
                                    TextButton(
                                        onClick = { onNavigateToSellerProfile(seller!!.id) }
                                    ) {
                                        Text("Xem thông tin")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Đăng lúc", formatDate(product!!.createdAt))
                            
                            // Location display
                            if (!product!!.location.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column {
                                    Text(
                                        text = "Vị trí",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LocationDisplay(
                                        location = product!!.location,
                                        latitude = product!!.latitude,
                                        longitude = product!!.longitude,
                                        showDistance = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    if (product!!.description.isNotBlank()) {
                        Text(
                            text = "Mô tả",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = product!!.description,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Nút liên hệ người bán (chỉ hiện khi không phải sản phẩm của mình)
                    if (currentUser != null && seller != null && currentUser.id != seller!!.id) {
                        // Rating Card
                        QuickRatingCard(
                            productTitle = product!!.title,
                            currentRating = product!!.rating,
                            currentRatingCount = product!!.ratingCount,
                            onRatingClick = { showRatingDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Contact seller button
                                Button(
                                    onClick = {
                                        onNavigateToChat(
                                            currentUser.email,
                                            seller!!.email,
                                            product!!.title
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Liên hệ người bán",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Price offer button (only if not own product and available)
                                if (product!!.status == "Available" && currentUser.id != product!!.userId) {
                                    OutlinedButton(
                                        onClick = { showPriceOfferDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Đề nghị giá",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                // Error message
                                offerError?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Debug info for development
                    if (imagePaths.isEmpty() && product!!.imagePaths != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Debug: imagePaths rỗng nhưng product.imagePaths = ${product!!.imagePaths}",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog && product != null) {
        ProductRatingDialog(
            productTitle = product!!.title,
            currentRating = 0f,
            onRatingSubmit = { rating ->
                scope.launch {
                    isRatingInProgress = true
                    try {
                        // Cập nhật rating trong database
                        dbHelper.updateProductRating(product!!.id, rating)

                        // Reload product để cập nhật UI
                        product = dbHelper.getProductById(productId)

                        // TODO: Trigger Firebase sync here
                        // productSyncHelper.syncSingleProduct(product!!.id)

                        showRatingDialog = false
                    } catch (e: Exception) {
                        // Handle error
                        println("Error updating rating: ${e.message}")
                    } finally {
                        isRatingInProgress = false
                    }
                }
            },
            onDismiss = { showRatingDialog = false }
        )
    }
    
    // Price Offer Dialog
    product?.let { prod ->
        PriceOfferDialog(
            isVisible = showPriceOfferDialog,
            onDismiss = { 
                showPriceOfferDialog = false
                offerError = null
            },
            productTitle = prod.title,
            originalPrice = prod.price,
            onSendOffer = { offeredPrice, message ->
                scope.launch {
                    try {
                        offerError = null
                        seller?.let { sellerUser ->
                            priceOfferRepository.createPriceOffer(
                                productId = prod.id,
                                buyerEmail = currentUser?.email ?: "",
                                sellerEmail = sellerUser.email,
                                originalPrice = prod.price,
                                offeredPrice = offeredPrice,
                                message = message,
                                productTitle = prod.title,
                                onSuccess = { offerId ->
                                    showPriceOfferDialog = false
                                    // Có thể chuyển đến chat hoặc hiển thị thông báo thành công
                                    onNavigateToChat(
                                        currentUser?.email ?: "",
                                        sellerUser.email,
                                        prod.title
                                    )
                                },
                                onError = { exception ->
                                    offerError = "Lỗi gửi đề nghị: ${exception.message}"
                                }
                            )
                        } ?: run {
                            offerError = "Không tìm thấy thông tin người bán"
                        }
                    } catch (e: Exception) {
                        offerError = "Lỗi: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}