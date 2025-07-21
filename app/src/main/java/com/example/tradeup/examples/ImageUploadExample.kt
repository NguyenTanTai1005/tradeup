package com.example.tradeup.examples

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.tradeup.services.ImageManagerService
import com.example.tradeup.services.ImageManagerResult
import kotlinx.coroutines.launch

/**
 * Example usage of ImageManagerService
 * This shows how to upload images and update Firebase in different scenarios
 */
class ImageUploadExample {
    
    /**
     * Example: Upload and update product image
     */
    suspend fun uploadProductImage(
        context: Context,
        imageUri: Uri,
        productId: String
    ): String? {
        val imageManager = ImageManagerService()
        
        return when (val result = imageManager.uploadAndUpdateProductImage(context, imageUri, productId)) {
            is ImageManagerResult.Success -> {
                println("✅ Success: ${result.message}")
                println("🔗 Image URL: ${result.imageUrl}")
                result.imageUrl
            }
            is ImageManagerResult.Error -> {
                println("❌ Error: ${result.error}")
                null
            }
        }
    }
    
    /**
     * Example: Upload and update user avatar
     */
    suspend fun uploadUserAvatar(
        context: Context,
        imageUri: Uri,
        userId: String
    ): String? {
        val imageManager = ImageManagerService()
        
        return when (val result = imageManager.uploadAndUpdateUserAvatar(context, imageUri, userId)) {
            is ImageManagerResult.Success -> {
                println("✅ Avatar uploaded: ${result.message}")
                result.imageUrl
            }
            is ImageManagerResult.Error -> {
                println("❌ Avatar upload failed: ${result.error}")
                null
            }
        }
    }
    
    /**
     * Example: Upload multiple product images
     */
    suspend fun uploadMultipleProductImages(
        context: Context,
        imageUris: List<Uri>,
        productId: String
    ): Boolean {
        val imageManager = ImageManagerService()
        
        return when (val result = imageManager.uploadAndUpdateProductImages(context, imageUris, productId)) {
            is ImageManagerResult.Success -> {
                println("✅ All images uploaded: ${result.message}")
                true
            }
            is ImageManagerResult.Error -> {
                println("❌ Failed to upload images: ${result.error}")
                false
            }
        }
    }
}

/**
 * Composable example showing how to use in UI
 */
@Composable
fun ImageUploadComposableExample() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageManager = remember { ImageManagerService() }
    
    var isUploading by remember { mutableStateOf(false) }
    var uploadResult by remember { mutableStateOf<String?>(null) }
    
    // Example function to handle image selection and upload
    fun handleImageUpload(imageUri: Uri, productId: String) {
        scope.launch {
            isUploading = true
            
            when (val result = imageManager.uploadAndUpdateProductImage(context, imageUri, productId)) {
                is ImageManagerResult.Success -> {
                    uploadResult = "Upload successful: ${result.imageUrl}"
                }
                is ImageManagerResult.Error -> {
                    uploadResult = "Upload failed: ${result.error}"
                }
            }
            
            isUploading = false
        }
    }
    
    // UI would go here...
    // You can call handleImageUpload(selectedImageUri, productId) when user selects an image
}
