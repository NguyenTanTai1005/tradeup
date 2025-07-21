package com.example.tradeup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.model.PriceOffer
import com.example.tradeup.model.PriceOfferStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PriceOfferMessageCard(
    priceOffer: PriceOffer,
    isFromCurrentUser: Boolean,
    onAcceptOffer: (() -> Unit)? = null,
    onRejectOffer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isFromCurrentUser) 48.dp else 0.dp, vertical = 4.dp)
            .then(
                if (isFromCurrentUser) {
                    Modifier.padding(start = 48.dp)
                } else {
                    Modifier.padding(end = 48.dp)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when (priceOffer.status) {
                "PENDING" -> MaterialTheme.colorScheme.primaryContainer
                "ACCEPTED" -> MaterialTheme.colorScheme.tertiaryContainer
                "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFromCurrentUser) "Đề nghị giá của bạn" else "Đề nghị giá",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatusChip(status = priceOffer.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Giá gốc",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = currencyFormat.format(priceOffer.originalPrice),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Giá đề nghị",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = currencyFormat.format(priceOffer.offeredPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (priceOffer.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${priceOffer.message}\"",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = dateFormat.format(Date(priceOffer.createdAt)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
            
            // Nút chấp nhận/từ chối chỉ hiển thị nếu:
            // 1. Không phải từ người dùng hiện tại 
            // 2. Trạng thái đang chờ
            // 3. Có callback functions
            if (!isFromCurrentUser && priceOffer.status == "PENDING" && onAcceptOffer != null && onRejectOffer != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectOffer,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Từ chối")
                    }
                    
                    Button(
                        onClick = onAcceptOffer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Chấp nhận")
                    }
                }
            }
        }
    }
}

@Composable
fun PriceOfferResponseCard(
    priceOffer: PriceOffer,
    isAccepted: Boolean,
    modifier: Modifier = Modifier
) {
    val responseTime = priceOffer.respondedAt?.let { 
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(Date(it))
    } ?: ""
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAccepted) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAccepted) "Đã chấp nhận đề nghị giá" else "Đã từ chối đề nghị giá",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAccepted) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                if (responseTime.isNotBlank()) {
                    Text(
                        text = responseTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            StatusChip(status = if (isAccepted) "ACCEPTED" else "REJECTED")
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "PENDING" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Đang chờ"
        )
        "ACCEPTED" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Đã chấp nhận"
        )
        "REJECTED" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Đã từ chối"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Không rõ"
        )
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
