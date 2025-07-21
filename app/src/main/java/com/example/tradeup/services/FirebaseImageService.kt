package com.example.tradeup.services

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseImageService {
    private val database = FirebaseDatabase.getInstance().reference
    
    /**
     * Update product image URL in Firebase
     * @param productId The product ID
     * @param imageUrl The new image URL
     */
    suspend fun updateProductImage(productId: String, imageUrl: String): Boolean {
        return try {
            database.child("products").child(productId).child("imageUrl").setValue(imageUrl).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update user avatar URL in Firebase
     * @param userId The user ID
     * @param imageUrl The new avatar URL
     */
    suspend fun updateUserAvatar(userId: String, imageUrl: String): Boolean {
        return try {
            database.child("users").child(userId).child("avatarUrl").setValue(imageUrl).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update product image paths (multiple images) in Firebase
     * @param productId The product ID
     * @param imageUrls List of image URLs
     */
    suspend fun updateProductImages(productId: String, imageUrls: List<String>): Boolean {
        return try {
            val imagePathsString = imageUrls.joinToString(",")
            database.child("products").child(productId).child("imagePaths").setValue(imagePathsString).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
