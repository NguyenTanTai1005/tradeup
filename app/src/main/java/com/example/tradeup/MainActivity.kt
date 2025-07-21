package com.example.tradeup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.tradeup.ui.auth.LoginScreen
import com.example.tradeup.ui.auth.RegisterScreen
import com.example.tradeup.ui.auth.ForgotPasswordScreen
import com.example.tradeup.ui.chat.ChatScreen
import com.example.tradeup.ui.chat.ConversationsScreen
import com.example.tradeup.ui.main.MainScreen
import com.example.tradeup.ui.product.AddProductScreen
import com.example.tradeup.ui.product.ProductPreviewScreen
import com.example.tradeup.ui.product.ProductPreviewData
import com.example.tradeup.ui.product.EditProductScreen
import com.example.tradeup.ui.product.MyProductsScreen
import com.example.tradeup.ui.product.ProductDetailScreen
import com.example.tradeup.ui.product.SoldProductsScreen
import com.example.tradeup.ui.product.FilteredProductsScreen
import com.example.tradeup.ui.profile.ProfileScreen
import com.example.tradeup.ui.profile.SellerProfileScreen
import com.example.tradeup.ui.account.DeleteAccountScreen
import com.example.tradeup.ui.search.SearchScreen
import com.example.tradeup.ui.theme.TradeUPTheme
import com.example.tradeup.utils.SessionManager
import com.example.tradeup.model.Product
import com.example.tradeup.auth.GoogleSignInManager
import com.example.tradeup.ui.test.EmailTestScreen

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInManager: GoogleSignInManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize GoogleSignInManager in onCreate to avoid lifecycle issues
        googleSignInManager = GoogleSignInManager(this)
        
        enableEdgeToEdge()
        setContent {
            TradeUPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TradeUpApp(googleSignInManager)
                }
            }
        }
    }
}

@Composable
fun TradeUpApp(googleSignInManager: GoogleSignInManager) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var currentScreen by remember { mutableStateOf("splash") }
    var currentUser by remember { mutableStateOf(sessionManager.getCurrentUser()) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productIdToView by remember { mutableStateOf<Int?>(null) }
    var sellerIdToView by remember { mutableStateOf<Long?>(null) }
    var previousScreen by remember { mutableStateOf("main") }
    var productPreviewData by remember { mutableStateOf<ProductPreviewData?>(null) }
    var addProductRestoreData by remember { mutableStateOf<ProductPreviewData?>(null) }
    
    // Chat navigation state
    var chatBuyerEmail by remember { mutableStateOf("") }
    var chatSellerEmail by remember { mutableStateOf("") }
    var chatProductTitle by remember { mutableStateOf("") }

    // Check if user is logged in
    LaunchedEffect(Unit) {
        currentScreen = if (sessionManager.isLoggedIn() && currentUser != null) {
            "main"
        } else {
            "login"
        }
    }

    when (currentScreen) {
        "login" -> {
            LoginScreen(
                googleSignInManager = googleSignInManager,
                onLoginSuccess = {
                    currentUser = sessionManager.getCurrentUser()
                    currentScreen = "main"
                },
                onNavigateToRegister = {
                    currentScreen = "register"
                },
                onNavigateToForgotPassword = {
                    currentScreen = "forgot_password"
                }
            )
        }

        "register" -> {
            RegisterScreen(
                onRegisterSuccess = {
                    currentScreen = "login"
                },
                onNavigateToLogin = {
                    currentScreen = "login"
                }
            )
        }

        "forgot_password" -> {
            ForgotPasswordScreen(
                onPasswordResetSuccess = {
                    currentScreen = "login"
                },
                onNavigateBack = {
                    currentScreen = "login"
                }
            )
        }

        "main" -> {
            currentUser?.let { user ->
                MainScreen(
                    currentUser = user,
                    onNavigateToAddProduct = {
                        currentScreen = "add_product"
                    },
                    onNavigateToSearch = {
                        currentScreen = "search"
                    },
                    onNavigateToFilteredProducts = {
                        currentScreen = "filtered_products"
                    },
                    onNavigateToMyProducts = {
                        currentScreen = "my_products"
                    },
                    onNavigateToSoldProducts = {
                        currentScreen = "sold_products"
                    },
                    onNavigateToProfile = {
                        currentScreen = "profile"
                    },
                    onNavigateToConversations = {
                        currentScreen = "conversations"
                    },
                    onProductClick = { product ->
                        previousScreen = "main"
                        productIdToView = product.id.toInt()
                        currentScreen = "product_detail"
                    },
                    onLogout = {
                        sessionManager.logout()
                        currentUser = null
                        currentScreen = "login"
                    }
                )
            }
        }

        "add_product" -> {
            currentUser?.let { user ->
                AddProductScreen(
                    currentUser = user,
                    onNavigateBack = {
                        addProductRestoreData = null // Clear restore data when going back to main
                        currentScreen = "main"
                    },
                    onNavigateToPreview = { previewData ->
                        productPreviewData = previewData
                        currentScreen = "product_preview"
                    },
                    restoreData = addProductRestoreData
                )
            }
        }

        "product_preview" -> {
            currentUser?.let { user ->
                productPreviewData?.let { previewData ->
                    ProductPreviewScreen(
                        productPreview = previewData,
                        currentUser = user,
                        onNavigateBack = { returnedData ->
                            addProductRestoreData = returnedData
                            currentScreen = "add_product"
                        },
                        onEditProduct = { returnedData ->
                            addProductRestoreData = returnedData
                            currentScreen = "add_product"
                        }
                    )
                }
            }
        }

        "search" -> {
            SearchScreen(
                onNavigateBack = {
                    currentScreen = "main"
                },
                onProductClick = { product ->
                    previousScreen = "search"
                    productIdToView = product.id.toInt()
                    currentScreen = "product_detail"
                }
            )
        }

        "filtered_products" -> {
            currentUser?.let { user ->
                FilteredProductsScreen(
                    currentUser = user,
                    onNavigateBack = {
                        currentScreen = "main"
                    },
                    showOnlyUserProducts = false,
                    onProductClick = { product ->
                        previousScreen = "filtered_products"
                        productIdToView = product.id.toInt()
                        currentScreen = "product_detail"
                    }
                )
            }
        }

        "my_products" -> {
            currentUser?.let { user ->
                MyProductsScreen(
                    currentUser = user,
                    onNavigateBack = {
                        currentScreen = "main"
                    },
                    onEditProduct = { product ->
                        productToEdit = product
                        currentScreen = "edit_product"
                    },
                    onProductClick = { product ->
                        previousScreen = "my_products"
                        productIdToView = product.id.toInt()
                        currentScreen = "product_detail"
                    }
                )
            }
        }

        "edit_product" -> {
            currentUser?.let { user ->
                productToEdit?.let { product ->
                    EditProductScreen(
                        product = product,
                        onNavigateBack = {
                            productToEdit = null
                            currentScreen = "my_products"
                        },
                        onProductUpdated = {
                            // Refresh products list
                        }
                    )
                }
            }
        }

        "product_detail" -> {
            productIdToView?.let { productId ->
                ProductDetailScreen(
                    productId = productId,
                    onNavigateBack = {
                        productIdToView = null
                        currentScreen = when (previousScreen) {
                            "seller_profile" -> "main" // Tránh loop
                            "main", "search", "my_products" -> previousScreen
                            else -> "main"
                        }
                        previousScreen = "main" // Reset previous screen
                    },
                    onNavigateToChat = { buyerEmail, sellerEmail, productTitle ->
                        chatBuyerEmail = buyerEmail
                        chatSellerEmail = sellerEmail
                        chatProductTitle = productTitle
                        previousScreen = "product_detail"
                        currentScreen = "chat"
                    },
                    onNavigateToSellerProfile = { sellerId ->
                        sellerIdToView = sellerId
                        previousScreen = "product_detail"
                        currentScreen = "seller_profile"
                    }
                )
            } ?: run {
                // Fallback nếu productIdToView null
                currentScreen = "main"
            }
        }

        "sold_products" -> {
            currentUser?.let { user ->
                SoldProductsScreen(
                    currentUser = user,
                    onNavigateBack = {
                        currentScreen = "main"
                    },
                    onProductClick = { product ->
                        previousScreen = "sold_products"
                        productIdToView = product.id.toInt()
                        currentScreen = "product_detail"
                    }
                )
            }
        }

        "profile" -> {
            currentUser?.let { user ->
                ProfileScreen(
                    currentUser = user,
                    onNavigateBack = {
                        currentScreen = "main"
                    },
                    onUserUpdated = { updatedUser ->
                        currentUser = updatedUser
                    },
                    onDeleteAccount = {
                        currentScreen = "delete_account"
                    }
                )
            }
        }
        
        "chat" -> {
            ChatScreen(
                buyerEmail = chatBuyerEmail,
                sellerEmail = chatSellerEmail,
                productTitle = chatProductTitle,
                onNavigateBack = {
                    currentScreen = previousScreen
                }
            )
        }
        
        "conversations" -> {
            ConversationsScreen(
                onNavigateBack = {
                    currentScreen = "main"
                },
                onNavigateToChat = { buyerEmail, sellerEmail, productTitle ->
                    chatBuyerEmail = buyerEmail
                    chatSellerEmail = sellerEmail
                    chatProductTitle = productTitle
                    currentScreen = "chat"
                }
            )
        }
        
        "delete_account" -> {
            currentUser?.let { user ->
                DeleteAccountScreen(
                    currentUser = user,
                    onNavigateBack = {
                        currentScreen = "profile"
                    },
                    onAccountDeleted = {
                        // Xóa tài khoản thành công - đăng xuất và về màn hình login
                        sessionManager.logout()
                        currentUser = null
                        currentScreen = "login"
                    }
                )
            }
        }
        
        "seller_profile" -> {
            sellerIdToView?.let { sellerId ->
                SellerProfileScreen(
                    userId = sellerId,
                    onNavigateBack = {
                        sellerIdToView = null
                        currentScreen = when (previousScreen) {
                            "product_detail" -> {
                                // Giữ productIdToView để có thể quay về product detail
                                "product_detail"
                            }
                            else -> "main"
                        }
                        // Không reset previousScreen để maintain navigation context
                    },
                    onProductClick = { product ->
                        productIdToView = product.id.toInt()
                        previousScreen = "seller_profile"
                        currentScreen = "product_detail"
                    },
                    onContactSeller = { user ->
                        // Navigate to chat with seller
                        if (currentUser != null) {
                            chatBuyerEmail = currentUser!!.email
                            chatSellerEmail = user.email
                            chatProductTitle = "Liên hệ từ thông tin người bán"
                            previousScreen = "seller_profile"
                            currentScreen = "chat"
                        }
                    }
                )
            } ?: run {
                // Fallback nếu sellerIdToView null
                currentScreen = "main"
            }
        }
    }
}