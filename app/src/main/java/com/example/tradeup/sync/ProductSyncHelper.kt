package com.example.tradeup.sync

import android.content.Context
import android.util.Log
import com.example.tradeup.database.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class để tích hợp Firebase sync vào các thao tác với sản phẩm
 * Đây là lớp trung gian giữa UI và database để đảm bảo sync tự động
 */
class ProductSyncHelper(private val context: Context) {
    
    private val dbHelper = DatabaseHelper(context)
    private val firebaseSyncManager = FirebaseSyncManager(context)
    
    companion object {
        private const val TAG = "ProductSyncHelper"
    }
    
    /**
     * Thêm sản phẩm mới và tự động sync
     */
    fun addProduct(
        title: String,
        description: String,
        price: Double,
        userId: Long,
        imagePaths: String? = null,
        status: String = "Available",
        category: String = "Khác",
        condition: String = "Mới",
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            // Lưu vào SQLite trước
            val productId = dbHelper.addProduct(title, description, price, userId, imagePaths, status, category, condition)
            
            if (productId != -1L) {
                Log.d(TAG, "Product added to SQLite with ID: $productId")
                onSuccess(productId)
                
                // Sync lên Firebase trong background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firebaseSyncManager.syncProductImmediately(productId)
                        Log.d(TAG, "Product $productId sync initiated")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync product $productId: ${e.message}")
                    }
                }
            } else {
                onError("Failed to add product to database")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product: ${e.message}")
            onError("Error adding product: ${e.message}")
        }
    }
    
    /**
     * Cập nhật sản phẩm và tự động sync
     */
    fun updateProduct(
        productId: Long,
        title: String,
        description: String,
        price: Double,
        status: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            // Cập nhật trong SQLite
            val rowsAffected = dbHelper.updateProduct(productId, title, description, price, status)
            
            if (rowsAffected > 0) {
                Log.d(TAG, "Product $productId updated in SQLite")
                onSuccess()
                
                // Sync lên Firebase trong background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firebaseSyncManager.syncProductImmediately(productId)
                        Log.d(TAG, "Product $productId update sync initiated")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync updated product $productId: ${e.message}")
                    }
                }
            } else {
                onError("Failed to update product")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}")
            onError("Error updating product: ${e.message}")
        }
    }
    
    /**
     * Xóa sản phẩm và đồng bộ với Firebase
     */
    fun deleteProduct(
        productId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            // Xóa khỏi SQLite
            val rowsAffected = dbHelper.deleteProduct(productId)
            
            if (rowsAffected > 0) {
                Log.d(TAG, "Product $productId deleted from SQLite")
                onSuccess()
                
                // Xóa khỏi Firebase trong background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firebaseSyncManager.deleteProductFromFirebase(productId)
                        Log.d(TAG, "Product $productId deletion from Firebase initiated")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete product $productId from Firebase: ${e.message}")
                    }
                }
            } else {
                onError("Failed to delete product")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product: ${e.message}")
            onError("Error deleting product: ${e.message}")
        }
    }
    
    /**
     * Cập nhật trạng thái sản phẩm và sync
     */
    fun updateProductStatus(
        productId: Long,
        status: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            val rowsAffected = dbHelper.updateProductStatus(productId, status)
            
            if (rowsAffected > 0) {
                Log.d(TAG, "Product $productId status updated to $status")
                onSuccess()
                
                // Sync trong background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firebaseSyncManager.syncProductImmediately(productId)
                        Log.d(TAG, "Product $productId status sync initiated")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync product $productId status: ${e.message}")
                    }
                }
            } else {
                onError("Failed to update product status")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product status: ${e.message}")
            onError("Error updating product status: ${e.message}")
        }
    }
    
    /**
     * Đánh dấu sản phẩm đã bán và sync
     */
    fun markProductAsSold(
        productId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        updateProductStatus(productId, "Sold", onSuccess, onError)
    }
    
    /**
     * Thực hiện sync tất cả sản phẩm chưa được đồng bộ
     */
    fun syncAllUnsyncedProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firebaseSyncManager.syncUnsyncedProductsToFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing all unsynced products: ${e.message}")
            }
        }
    }
    
    /**
     * Lấy số lượng sản phẩm chưa sync để hiển thị UI
     */
    fun getUnsyncedProductsCount(): Int {
        return dbHelper.getUnsyncedProductsCount()
    }
    
    /**
     * Thực hiện sync toàn bộ từ Firebase về local (gọi khi cần thiết)
     */
    fun syncFromFirebaseToLocal(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firebaseSyncManager.performFullSyncFromFirebase()
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing from Firebase: ${e.message}")
                onError("Error syncing from Firebase: ${e.message}")
            }
        }
    }
    
    /**
     * Refresh dữ liệu từ Firebase (chỉ gọi khi user muốn refresh)
     */
    fun refreshDataFromFirebase(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        syncFromFirebaseToLocal(onSuccess, onError)
    }
    
    /**
     * Sync thông minh - simple version
     */
    fun performSmartSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simple sync - just sync unsynced products
                firebaseSyncManager.syncUnsyncedProductsToFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "Error in smart sync: ${e.message}")
            }
        }
    }
    
    /**
     * Sync chỉ khi có dữ liệu cần sync
     */
    fun syncIfNeeded() {
        performSmartSync()
    }
    
    /**
     * Lấy thông tin trạng thái sync (simple version)
     */
    fun getSyncStatus(): Map<String, Any> {
        return mapOf(
            "unsyncedCount" to getUnsyncedProductsCount(),
            "lastSync" to System.currentTimeMillis()
        )
    }
}
