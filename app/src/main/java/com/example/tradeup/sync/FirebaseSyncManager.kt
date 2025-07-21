package com.example.tradeup.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.Product
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*

class FirebaseSyncManager(private val context: Context) {
    
    private val dbHelper = DatabaseHelper(context)
    private val firebaseDb = FirebaseDatabase.getInstance()
    private val productsRef = firebaseDb.getReference("products")
    
    companion object {
        private const val TAG = "FirebaseSyncManager"
    }
    
    /**
     * Kiểm tra kết nối internet
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Đồng bộ tất cả sản phẩm chưa được sync lên Firebase
     */
    suspend fun syncUnsyncedProductsToFirebase() = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                Log.d(TAG, "No internet connection. Skipping sync.")
                return@withContext
            }
            
            Log.d(TAG, "Starting sync of unsynced products to Firebase...")
            
            // Lấy danh sách sản phẩm chưa sync
            val unsyncedProducts = dbHelper.getUnsyncedProducts()
            
            if (unsyncedProducts.isEmpty()) {
                Log.d(TAG, "No unsynced products found.")
                return@withContext
            }
            
            Log.d(TAG, "Found ${unsyncedProducts.size} unsynced products")
            
            // Đồng bộ từng sản phẩm
            for (product in unsyncedProducts) {
                try {
                    syncSingleProductToFirebase(product)
                    delay(100) // Tránh spam request
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync product ${product.id}: ${e.message}")
                }
            }
            
            Log.d(TAG, "Sync completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}")
        }
    }
    
    /**
     * Đồng bộ một sản phẩm lên Firebase
     */
    private suspend fun syncSingleProductToFirebase(product: Product) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                // Tạo data object để push lên Firebase
                val firebaseProduct = mapOf(
                    "id" to product.id,
                    "title" to product.title,
                    "description" to product.description,
                    "price" to product.price,
                    "userId" to product.userId,
                    "createdAt" to product.createdAt,
                    "status" to product.status,
                    "category" to product.category,
                    "condition" to product.condition,
                    "createdAtTimestamp" to product.createdAtTimestamp,
                    "imagePaths" to product.imagePaths,
                    "rating" to product.rating,
                    "ratingCount" to product.ratingCount,
                    "location" to product.location,
                    "latitude" to product.latitude,
                    "longitude" to product.longitude,
                    "lastUpdated" to System.currentTimeMillis()
                )
                
                // Push lên Firebase với key là productId
                productsRef.child(product.id.toString())
                    .setValue(firebaseProduct)
                    .addOnSuccessListener {
                        // Đánh dấu đã sync trong SQLite
                        val success = dbHelper.markProductAsSynced(product.id)
                        Log.d(TAG, "Product ${product.id} synced successfully. DB update: $success")
                        continuation.resume(Unit) {}
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to sync product ${product.id}: ${exception.message}")
                        if (continuation.isActive) {
                            continuation.resume(Unit) {}
                        }
                    }
                    
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing product ${product.id} for sync: ${e.message}")
                if (continuation.isActive) {
                    continuation.resume(Unit) {}
                }
            }
        }
    }
    
    /**
     * Xóa sản phẩm khỏi Firebase
     */
    suspend fun deleteProductFromFirebase(productId: Long) = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                Log.d(TAG, "No internet connection. Skipping Firebase delete.")
                return@withContext
            }
            
            suspendCancellableCoroutine<Unit> { continuation ->
                productsRef.child(productId.toString())
                    .removeValue()
                    .addOnSuccessListener {
                        Log.d(TAG, "Product $productId deleted from Firebase successfully")
                        continuation.resume(Unit) {}
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to delete product $productId from Firebase: ${exception.message}")
                        if (continuation.isActive) {
                            continuation.resume(Unit) {}
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product $productId from Firebase: ${e.message}")
        }
    }
    
    /**
     * Sync sản phẩm từ Firebase về SQLite với tối ưu hóa
     */
    suspend fun syncProductsFromFirebaseToLocal() = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                Log.d(TAG, "No internet connection. Skipping Firebase to local sync.")
                return@withContext
            }
            
            Log.d(TAG, "Starting optimized sync from Firebase to local SQLite...")
            
            suspendCancellableCoroutine<Unit> { continuation ->
                productsRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        try {
                            var syncedCount = 0
                            var updatedCount = 0
                            var skippedCount = 0
                            
                            for (productSnapshot in snapshot.children) {
                                try {
                                    val firebaseProduct = productSnapshot.value as? Map<String, Any>
                                    firebaseProduct?.let { productData ->
                                        val productId = (productData["id"] as? Number)?.toLong() ?: 0L
                                        
                                        if (productId > 0) {
                                            // Kiểm tra sản phẩm đã tồn tại trong SQLite chưa
                                            val existingProduct = dbHelper.getProductById(productId.toInt())
                                            val firebaseLastUpdated = (productData["lastUpdated"] as? Number)?.toLong() ?: 0L
                                            
                                            if (existingProduct == null) {
                                                // Sản phẩm mới từ Firebase, thêm vào SQLite
                                                val newProduct = Product(
                                                    id = productId,
                                                    title = productData["title"] as? String ?: "",
                                                    description = productData["description"] as? String ?: "",
                                                    price = (productData["price"] as? Number)?.toDouble() ?: 0.0,
                                                    userId = (productData["userId"] as? Number)?.toLong() ?: 0L,
                                                    createdAt = productData["createdAt"] as? String ?: "",
                                                    status = productData["status"] as? String ?: "available",
                                                    category = productData["category"] as? String ?: "",
                                                    condition = productData["condition"] as? String ?: "",
                                                    createdAtTimestamp = (productData["createdAtTimestamp"] as? Number)?.toLong() ?: 0L,
                                                    imagePaths = productData["imagePaths"] as? String,
                                                    rating = (productData["rating"] as? Number)?.toFloat() ?: 0f,
                                                    ratingCount = (productData["ratingCount"] as? Number)?.toInt() ?: 0,
                                                    location = productData["location"] as? String,
                                                    latitude = (productData["latitude"] as? Number)?.toDouble(),
                                                    longitude = (productData["longitude"] as? Number)?.toDouble(),
                                                    synced = true // Đánh dấu đã sync
                                                )
                                                
                                                dbHelper.addProductFromFirebase(newProduct)
                                                syncedCount++
                                                Log.d(TAG, "Added new product from Firebase: ${newProduct.title}")
                                            } else {
                                                // Chỉ cập nhật nếu Firebase mới hơn và sản phẩm local đã được sync
                                                val localLastUpdated = existingProduct.createdAtTimestamp
                                                
                                                when {
                                                    // Firebase mới hơn và product local đã sync - cập nhật
                                                    firebaseLastUpdated > localLastUpdated && existingProduct.synced -> {
                                                        val updatedProduct = existingProduct.copy(
                                                            title = productData["title"] as? String ?: existingProduct.title,
                                                            description = productData["description"] as? String ?: existingProduct.description,
                                                            price = (productData["price"] as? Number)?.toDouble() ?: existingProduct.price,
                                                            status = productData["status"] as? String ?: existingProduct.status,
                                                            category = productData["category"] as? String ?: existingProduct.category,
                                                            condition = productData["condition"] as? String ?: existingProduct.condition,
                                                            imagePaths = productData["imagePaths"] as? String ?: existingProduct.imagePaths,
                                                            rating = (productData["rating"] as? Number)?.toFloat() ?: existingProduct.rating,
                                                            ratingCount = (productData["ratingCount"] as? Number)?.toInt() ?: existingProduct.ratingCount,
                                                            location = productData["location"] as? String ?: existingProduct.location,
                                                            latitude = (productData["latitude"] as? Number)?.toDouble() ?: existingProduct.latitude,
                                                            longitude = (productData["longitude"] as? Number)?.toDouble() ?: existingProduct.longitude,
                                                            synced = true
                                                        )
                                                        
                                                        dbHelper.updateProductFromFirebase(updatedProduct)
                                                        updatedCount++
                                                        Log.d(TAG, "Updated product from Firebase: ${updatedProduct.title}")
                                                    }
                                                    
                                                    // Product local chưa sync - ưu tiên local
                                                    !existingProduct.synced -> {
                                                        skippedCount++
                                                        Log.d(TAG, "Skipped updating ${existingProduct.title} - local changes not synced yet")
                                                    }
                                                    
                                                    // Firebase cũ hơn - skip
                                                    firebaseLastUpdated <= localLastUpdated -> {
                                                        skippedCount++
                                                        Log.d(TAG, "Skipped ${existingProduct.title} - local version is newer or same")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error processing product from Firebase: ${e.message}")
                                }
                            }
                            
                            Log.d(TAG, "Optimized Firebase to local sync completed. Added: $syncedCount, Updated: $updatedCount, Skipped: $skippedCount")
                            
                            if (continuation.isActive) {
                                continuation.resume(Unit) {}
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing Firebase data: ${e.message}")
                            if (continuation.isActive) {
                                continuation.resume(Unit) {}
                            }
                        }
                    }
                    
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        Log.e(TAG, "Failed to sync from Firebase: ${error.message}")
                        if (continuation.isActive) {
                            continuation.resume(Unit) {}
                        }
                    }
                })
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during Firebase to local sync: ${e.message}")
        }
    }
    
    /**
     * Thực hiện sync nhẹ khi mở app (chỉ sync các items chưa được đồng bộ)
     */
    fun performLightSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!isNetworkAvailable()) {
                    Log.d(TAG, "No internet connection. Skipping sync.")
                    return@launch
                }
                
                delay(2000) // Đợi 2 giây sau khi mở app
                
                // Chỉ sync các sản phẩm local chưa sync lên Firebase
                syncUnsyncedProductsToFirebase()
                
                Log.d(TAG, "Light sync completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during light sync: ${e.message}")
            }
        }
    }
    
    /**
     * Sync toàn bộ từ Firebase về local (chỉ gọi khi cần thiết)
     */
    fun performFullSyncFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!isNetworkAvailable()) {
                    Log.d(TAG, "No internet connection. Skipping full sync.")
                    return@launch
                }
                
                Log.d(TAG, "Starting full sync from Firebase...")
                syncProductsFromFirebaseToLocal()
                Log.d(TAG, "Full sync from Firebase completed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during full sync: ${e.message}")
            }
        }
    }
    
    /**
     * Sync một sản phẩm ngay sau khi thêm/sửa
     */
    fun syncProductImmediately(productId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val product = dbHelper.getProductById(productId.toInt())
                if (product != null && !product.synced) {
                    syncSingleProductToFirebase(product)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing product $productId immediately: ${e.message}")
            }
        }
    }
}
