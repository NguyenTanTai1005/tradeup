package com.example.tradeup.ui.offers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.PriceOffer
import com.example.tradeup.model.Product
import com.example.tradeup.repository.PriceOfferRepository
import com.example.tradeup.ui.components.PriceOfferMessageCard
import com.example.tradeup.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val dbHelper = remember { DatabaseHelper(context) }
    val priceOfferRepository = remember { PriceOfferRepository(context) }
    val currentUser = sessionManager.getCurrentUser()
    val scope = rememberCoroutineScope()
    
    var sentOffers by remember { mutableStateOf<List<PriceOffer>>(emptyList()) }
    var receivedOffers by remember { mutableStateOf<List<PriceOffer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Load offers
    LaunchedEffect(currentUser?.email) {
        if (currentUser != null) {
            isLoading = true
            
            try {
                // Get sent offers
                sentOffers = priceOfferRepository.getPriceOffersBySender(currentUser.email)
                
                // Get received offers (offers for user's products)
                val userProducts = dbHelper.getProductsByUserId(currentUser.id)
                receivedOffers = userProducts.flatMap { product: Product ->
                    priceOfferRepository.getPriceOffersForProduct(product.id)
                }
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đề nghị giá của tôi") },
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
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Đã gửi (${sentOffers.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Đã nhận (${receivedOffers.size})") }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        // Sent offers
                        if (sentOffers.isEmpty()) {
                            EmptyOffersMessage("Bạn chưa gửi đề nghị giá nào")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sentOffers) { offer ->
                                    PriceOfferCard(
                                        priceOffer = offer,
                                        isSentOffer = true,
                                        onAcceptOffer = null,
                                        onRejectOffer = null,
                                        priceOfferRepository = priceOfferRepository
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Received offers
                        if (receivedOffers.isEmpty()) {
                            EmptyOffersMessage("Bạn chưa nhận được đề nghị giá nào")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(receivedOffers) { offer ->
                                    PriceOfferCard(
                                        priceOffer = offer,
                                        isSentOffer = false,
                                        onAcceptOffer = if (offer.status == "PENDING") {
                                            {
                                                scope.launch {
                                                    priceOfferRepository.respondToPriceOffer(
                                                        offerId = offer.offerId,
                                                        isAccepted = true,
                                                        onSuccess = {
                                                            // Reload offers
                                                            // Could implement a refresh mechanism here
                                                        },
                                                        onError = { /* Handle error */ }
                                                    )
                                                }
                                            }
                                        } else null,
                                        onRejectOffer = if (offer.status == "PENDING") {
                                            {
                                                scope.launch {
                                                    priceOfferRepository.respondToPriceOffer(
                                                        offerId = offer.offerId,
                                                        isAccepted = false,
                                                        onSuccess = {
                                                            // Reload offers
                                                        },
                                                        onError = { /* Handle error */ }
                                                    )
                                                }
                                            }
                                        } else null,
                                        priceOfferRepository = priceOfferRepository
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyOffersMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun PriceOfferCard(
    priceOffer: PriceOffer,
    isSentOffer: Boolean,
    onAcceptOffer: (() -> Unit)?,
    onRejectOffer: (() -> Unit)?,
    priceOfferRepository: PriceOfferRepository
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var productTitle by remember { mutableStateOf("") }
    
    LaunchedEffect(priceOffer.productId) {
        val product = dbHelper.getProductById(priceOffer.productId.toInt())
        productTitle = product?.title ?: "Sản phẩm không tồn tại"
    }
    
    PriceOfferMessageCard(
        priceOffer = priceOffer,
        isFromCurrentUser = isSentOffer,
        onAcceptOffer = onAcceptOffer,
        onRejectOffer = onRejectOffer
    )
}
