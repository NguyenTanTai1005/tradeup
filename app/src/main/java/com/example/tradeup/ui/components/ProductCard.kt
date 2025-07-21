package com.example.tradeup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.example.tradeup.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.NumberFormat
import java.util.*

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showSellerInfo: Boolean = true
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var sellerName by remember { mutableStateOf("") }
    var firstImageUrl by remember { mutableStateOf<String?>(null) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    // Load seller information and first image
    LaunchedEffect(product.userId, product.imagePaths) {
        // Load seller info
        if (showSellerInfo && product.userId > 0) {
            try {
                val seller = withContext(Dispatchers.IO) {
                    dbHelper.getUserById(product.userId)
                }
                sellerName = seller?.name?.takeIf { it.isNotBlank() } ?: "Người dùng ẩn danh"
            } catch (e: Exception) {
                sellerName = "Người dùng ẩn danh"
            }
        }
        
        // Parse and get first image URL
        product.imagePaths?.let { paths ->
            try {
                if (paths.startsWith("[") && paths.endsWith("]")) {
                    // JSON Array format
                    val jsonArray = JSONArray(paths)
                    if (jsonArray.length() > 0) {
                        firstImageUrl = jsonArray.getString(0)
                    }
                } else {
                    // Single path or comma-separated paths
                    val pathsList = paths.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    firstImageUrl = pathsList.firstOrNull()
                }
            } catch (e: Exception) {
                firstImageUrl = null
            }
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Product image
            firstImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Hình sản phẩm",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    onError = {
                        // Log error if needed
                        println("Failed to load image: $imageUrl")
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
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
                            modifier = Modifier.padding(bottom = 8.dp),
                            maxLines = 2
                        )
                    }
                    
                    Text(
                        text = currencyFormat.format(product.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (showSellerInfo) {
                        Text(
                            text = "Người bán: $sellerName",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // Rating display
                    if (product.ratingCount > 0) {
                        ProductRatingCompact(
                            rating = product.rating,
                            ratingCount = product.ratingCount,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Location display
                    if (!product.location.isNullOrBlank()) {
                        LocationDisplay(
                            location = product.location,
                            latitude = product.latitude,
                            longitude = product.longitude,
                            showDistance = false,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Status badge
                StatusBadge(status = product.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = product.createdAt,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
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
