package com.example.tradeup

import android.app.Application
import android.util.Log
import com.example.tradeup.sync.FirebaseSyncManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class TradeUpApplication : Application() {
    
    companion object {
        private const val TAG = "TradeUpApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "TradeUp Application starting...")
        
        // Khởi tạo Firebase trước tiên
        FirebaseApp.initializeApp(this)
        
        // Bật offline persistence cho Firebase Realtime Database
        // QUAN TRỌNG: Phải gọi trước khi có bất kỳ truy cập nào đến Firebase Database
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            Log.d(TAG, "Firebase persistence enabled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase persistence already enabled or failed: ${e.message}")
        }
        
        // Khởi động sync process khi mở app
        initializeFirebaseSync()
        
        Log.d(TAG, "TradeUp Application initialized successfully")
    }
    
    /**
     * Khởi tạo Firebase sync process (chỉ sync nhẹ)
     */
    private fun initializeFirebaseSync() {
        try {
            val firebaseSyncManager = FirebaseSyncManager(this)
            
            // Chỉ thực hiện sync nhẹ - không tải toàn bộ từ Firebase
            firebaseSyncManager.performLightSync()
            
            Log.d(TAG, "Firebase light sync initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase sync: ${e.message}")
        }
    }
}
