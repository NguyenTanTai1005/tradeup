package com.example.tradeup.services

import android.content.Context
import com.example.tradeup.database.DatabaseHelper
import org.json.JSONArray

class LocalImageService(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    
    /**
     * Update product image URL in local database
     * @param productId The product ID
     * @param imageUrl The new image URL
     */
    fun updateProductImage(productId: String, imageUrl: String): Boolean {
        return try {
            val imageUrlsJson = JSONArray().apply { put(imageUrl) }
            dbHelper.updateProductImages(productId.toInt(), imageUrlsJson.toString())
            true
        } catch (e: Exception) {
            println("Debug: Failed to update product image in local DB: ${e.message}")
            false
        }
    }
    
    /**
     * Update product image paths (multiple images) in local database
     * @param productId The product ID
     * @param imageUrls List of image URLs
     */
    fun updateProductImages(productId: String, imageUrls: List<String>): Boolean {
        return try {
            val imageUrlsJson = JSONArray()
            imageUrls.forEach { imageUrlsJson.put(it) }
            dbHelper.updateProductImages(productId.toInt(), imageUrlsJson.toString())
            println("Debug: Successfully updated ${imageUrls.size} images in local DB for product $productId")
            true
        } catch (e: Exception) {
            println("Debug: Failed to update product images in local DB: ${e.message}")
            false
        }
    }
    
    /**
     * Update user avatar URL in local database
     * @param userId The user ID
     * @param imageUrl The new avatar URL
     */
    fun updateUserAvatar(userId: String, imageUrl: String): Boolean {
        return try {
            dbHelper.updateUserAvatar(userId.toInt(), imageUrl)
            true
        } catch (e: Exception) {
            println("Debug: Failed to update user avatar in local DB: ${e.message}")
            false
        }
    }
}
