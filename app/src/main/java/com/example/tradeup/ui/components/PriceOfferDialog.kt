package com.example.tradeup.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.NumberFormat
import java.util.*

@Composable
fun PriceOfferDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    productTitle: String,
    originalPrice: Double,
    onSendOffer: (Double, String) -> Unit
) {
    if (!isVisible) return
    
    var offeredPrice by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf<String?>(null) }
    
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Đề nghị giá",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = productTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Giá gốc: ${currencyFormat.format(originalPrice)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = offeredPrice,
                    onValueChange = { 
                        offeredPrice = it
                        priceError = null
                    },
                    label = { Text("Giá đề nghị (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it) } }
                )
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Tin nhắn (tùy chọn)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    maxLines = 3,
                    placeholder = { Text("Lý do đề nghị giá này...") }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = {
                            val priceValue = offeredPrice.toDoubleOrNull()
                            when {
                                priceValue == null || priceValue <= 0 -> {
                                    priceError = "Giá phải là số dương"
                                }
                                priceValue >= originalPrice -> {
                                    priceError = "Giá đề nghị phải thấp hơn giá gốc"
                                }
                                else -> {
                                    isLoading = true
                                    onSendOffer(priceValue, message.trim())
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && offeredPrice.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Gửi đề nghị")
                        }
                    }
                }
            }
        }
    }
}
