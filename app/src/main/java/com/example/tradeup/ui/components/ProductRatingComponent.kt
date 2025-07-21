package com.example.tradeup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProductRatingDisplay(
    rating: Float,
    ratingCount: Int,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Hiển thị 5 ngôi sao
        repeat(5) { index ->
            val starIcon = when {
                index < rating.toInt() -> "⭐" // Sao đầy
                index == rating.toInt() && rating % 1 != 0f -> "⭐" // Sao nửa (hiển thị đầy cho đơn giản)
                else -> "☆" // Sao rỗng
            }
            
            Text(
                text = starIcon,
                color = if (index < rating.toInt()) Color(0xFFFFD700) else Color.Gray,
                fontSize = 16.sp
            )
        }
        
        if (showText) {
            Text(
                text = if (rating > 0) String.format("%.1f (%d)", rating, ratingCount) else "Chưa có đánh giá",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProductRatingInput(
    currentRating: Float = 0f,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var tempRating by remember { mutableStateOf(currentRating) }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            val starRating = (index + 1).toFloat()
            val isSelected = starRating <= tempRating
            
            Text(
                text = if (isSelected) "⭐" else "☆",
                fontSize = 24.sp,
                color = if (isSelected) Color(0xFFFFD700) else Color.Gray,
                modifier = if (enabled) {
                    Modifier.clickable {
                        tempRating = starRating
                        onRatingChanged(starRating)
                    }
                } else Modifier
            )
        }
    }
}

@Composable
fun ProductRatingCompact(
    rating: Float,
    ratingCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "⭐",
            fontSize = 12.sp,
            color = if (rating > 0) Color(0xFFFFD700) else Color.Gray
        )
        Text(
            text = if (rating > 0) String.format("%.1f", rating) else "0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        if (ratingCount > 0) {
            Text(
                text = "($ratingCount)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}
