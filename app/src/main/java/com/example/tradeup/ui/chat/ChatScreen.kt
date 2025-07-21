package com.example.tradeup.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.model.Message
import com.example.tradeup.model.MessageType
import com.example.tradeup.repository.ChatRepository
import com.example.tradeup.repository.PriceOfferRepository
import com.example.tradeup.ui.components.PriceOfferMessageCard
import com.example.tradeup.ui.components.PriceOfferResponseCard
import com.example.tradeup.utils.SessionManager
import com.example.tradeup.database.DatabaseHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    buyerEmail: String,
    sellerEmail: String,
    productTitle: String = "",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val chatRepository = remember { ChatRepository() }
    val databaseHelper = remember { DatabaseHelper(context) }
    val priceOfferRepository = remember { PriceOfferRepository(context) }
    val currentUser = sessionManager.getCurrentUser()
    
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isBlocked by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Determine other user email
    val otherUserEmail = if (currentUser?.email == buyerEmail) sellerEmail else buyerEmail
    
    // Check if user is blocked
    LaunchedEffect(currentUser?.email, otherUserEmail) {
        if (currentUser != null) {
            isBlocked = databaseHelper.isUserBlocked(currentUser.email, otherUserEmail)
        }
    }
    
    // Lắng nghe tin nhắn real-time
    LaunchedEffect(buyerEmail, sellerEmail) {
        chatRepository.getMessages(buyerEmail, sellerEmail).collect { messageList ->
            messages = messageList
            // Tự động scroll xuống tin nhắn mới nhất
            if (messageList.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(messageList.size - 1)
                }
            }
        }
    }
    
    // Gửi tin nhắn
    fun sendMessage() {
        if (messageText.trim().isEmpty() || currentUser == null || isBlocked) return
        
        isLoading = true
        chatRepository.sendMessage(
            buyerEmail = buyerEmail,
            sellerEmail = sellerEmail,
            senderEmail = currentUser.email,
            text = messageText.trim(),
            onSuccess = {
                messageText = ""
                isLoading = false
                errorMessage = null
            },
            onError = { exception ->
                isLoading = false
                errorMessage = "Lỗi gửi tin nhắn: ${exception.message}"
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = if (currentUser?.email == buyerEmail) 
                                "Chat với người bán" else "Chat với người mua",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (productTitle.isNotEmpty()) {
                            Text(
                                text = productTitle,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Thêm tùy chọn")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(if (isBlocked) "Bỏ chặn người dùng" else "Chặn người dùng") 
                                },
                                onClick = {
                                    showMenu = false
                                    if (currentUser != null) {
                                        if (isBlocked) {
                                            databaseHelper.unblockUser(currentUser.email, otherUserEmail)
                                        } else {
                                            databaseHelper.blockUser(currentUser.email, otherUserEmail)
                                        }
                                        isBlocked = !isBlocked
                                    }
                                },
                                leadingIcon = {
                                    Text("🚫")
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = ::sendMessage,
                isLoading = isLoading,
                isBlocked = isBlocked
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hiển thị lỗi nếu có
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Danh sách tin nhắn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageItem(
                        message = message,
                        isCurrentUser = message.sender == currentUser?.email,
                        priceOfferRepository = priceOfferRepository
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    isCurrentUser: Boolean,
    priceOfferRepository: PriceOfferRepository
) {
    val coroutineScope = rememberCoroutineScope()
    
    when (MessageType.fromString(message.messageType)) {
        MessageType.PRICE_OFFER -> {
            // Hiển thị tin nhắn đề nghị giá
            var priceOffer by remember { mutableStateOf<com.example.tradeup.model.PriceOffer?>(null) }
            
            LaunchedEffect(message.priceOfferId) {
                message.priceOfferId?.let { offerId ->
                    priceOffer = priceOfferRepository.getPriceOffer(offerId)
                }
            }
            
            priceOffer?.let { offer ->
                PriceOfferMessageCard(
                    priceOffer = offer,
                    isFromCurrentUser = isCurrentUser,
                    onAcceptOffer = if (!isCurrentUser && offer.status == "PENDING") {
                        {
                            coroutineScope.launch {
                                priceOfferRepository.respondToPriceOffer(
                                    offerId = offer.offerId,
                                    isAccepted = true,
                                    onSuccess = { /* Reload messages will happen automatically */ },
                                    onError = { /* Handle error */ }
                                )
                            }
                        }
                    } else null,
                    onRejectOffer = if (!isCurrentUser && offer.status == "PENDING") {
                        {
                            coroutineScope.launch {
                                priceOfferRepository.respondToPriceOffer(
                                    offerId = offer.offerId,
                                    isAccepted = false,
                                    onSuccess = { /* Reload messages will happen automatically */ },
                                    onError = { /* Handle error */ }
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        MessageType.PRICE_RESPONSE -> {
            // Hiển thị tin nhắn phản hồi đề nghị giá
            var priceOffer by remember { mutableStateOf<com.example.tradeup.model.PriceOffer?>(null) }
            
            LaunchedEffect(message.priceOfferId) {
                message.priceOfferId?.let { offerId ->
                    priceOffer = priceOfferRepository.getPriceOffer(offerId)
                }
            }
            
            val isAccepted = message.metadata?.get("isAccepted") as? Boolean ?: false
            
            priceOffer?.let { offer ->
                PriceOfferResponseCard(
                    priceOffer = offer,
                    isAccepted = isAccepted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        MessageType.TEXT -> {
            // Hiển thị tin nhắn thường
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
            ) {
                if (!isCurrentUser) {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                Card(
                    modifier = Modifier.widthIn(max = 280.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentUser) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                        bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = if (isCurrentUser) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = formatTime(message.timestamp),
                            color = if (isCurrentUser) 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    isBlocked: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        if (isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bạn đã chặn người dùng này. Không thể gửi tin nhắn.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập tin nhắn...") },
                    maxLines = 3,
                    enabled = !isLoading
                )
                
                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Gửi tin nhắn"
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diffInMillis = now.time - timestamp
    val diffInHours = diffInMillis / (1000 * 60 * 60)
    
    return when {
        diffInHours < 24 -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        diffInHours < 24 * 7 -> {
            SimpleDateFormat("E HH:mm", Locale("vi", "VN")).format(date)
        }
        else -> {
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date)
        }
    }
}
