package com.example.tradeup.services

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.tradeup.utils.ImageUtils
import java.io.File

class ImageManagerService {
    private val imageUploadService = ImageUploadService()
    
    /**
     * Upload image and update product in local database
     * @param context Android context
     * @param imageUri Image URI to upload
     * @param productId Product ID to update
     * @return Result with success/error
     */
    suspend fun uploadAndUpdateProductImage(
        context: Context,
        imageUri: Uri,
        productId: String
    ): ImageManagerResult = withContext(Dispatchers.IO) {
        try {
            val localImageService = LocalImageService(context)
            
            // Convert URI to compressed File first
            val imageFile = ImageUtils.compressImageFromUri(context, imageUri, 1024, 1024, 80)
                ?: return@withContext ImageManagerResult.Error("Failed to process image")
            
            // Upload image to FreeImage.host
            when (val uploadResult = imageUploadService.uploadImage(imageFile)) {
                is ImageUploadResult.Success -> {
                    // Update local database with new image URL
                    val localSuccess = localImageService.updateProductImage(productId, uploadResult.imageUrl)
                    
                    // Clean up temporary file
                    imageFile.delete()
                    
                    if (localSuccess) {
                        ImageManagerResult.Success(uploadResult.imageUrl, "Product image updated successfully")
                    } else {
                        ImageManagerResult.Error("Image uploaded but failed to update local database")
                    }
                }
                is ImageUploadResult.Error -> {
                    // Clean up temporary file
                    imageFile.delete()
                    ImageManagerResult.Error(uploadResult.error)
                }
            }
        } catch (e: Exception) {
            ImageManagerResult.Error("Failed to upload and update: ${e.message}")
        }
    }
    
    /**
     * Upload image and update user avatar in local database
     * @param context Android context
     * @param imageUri Image URI to upload
     * @param userId User ID to update
     * @return Result with success/error
     */
    suspend fun uploadAndUpdateUserAvatar(
        context: Context,
        imageUri: Uri,
        userId: String
    ): ImageManagerResult = withContext(Dispatchers.IO) {
        try {
            val localImageService = LocalImageService(context)
            
            // Convert URI to compressed File first
            val imageFile = ImageUtils.compressImageFromUri(context, imageUri, 1024, 1024, 80)
                ?: return@withContext ImageManagerResult.Error("Failed to process image")
            
            // Upload image to FreeImage.host
            when (val uploadResult = imageUploadService.uploadImage(imageFile)) {
                is ImageUploadResult.Success -> {
                    // Update local database with new avatar URL
                    val localSuccess = localImageService.updateUserAvatar(userId, uploadResult.imageUrl)
                    
                    // Clean up temporary file
                    imageFile.delete()
                    
                    if (localSuccess) {
                        ImageManagerResult.Success(uploadResult.imageUrl, "User avatar updated successfully")
                    } else {
                        ImageManagerResult.Error("Image uploaded but failed to update local database")
                    }
                }
                is ImageUploadResult.Error -> {
                    // Clean up temporary file
                    imageFile.delete()
                    ImageManagerResult.Error(uploadResult.error)
                }
            }
        } catch (e: Exception) {
            ImageManagerResult.Error("Failed to upload and update: ${e.message}")
        }
    }
    
    /**
     * Upload multiple images and update product in local database
     * @param context Android context
     * @param imageUris List of image URIs to upload
     * @param productId Product ID to update
     * @return Result with success/error
     */
    suspend fun uploadAndUpdateProductImages(
        context: Context,
        imageUris: List<Uri>,
        productId: String
    ): ImageManagerResult = withContext(Dispatchers.IO) {
        try {
            val localImageService = LocalImageService(context)
            val uploadedUrls = mutableListOf<String>()
            val tempFiles = mutableListOf<File>()
            
            // Upload all images
            for (uri in imageUris) {
                // Convert URI to compressed File first
                val imageFile = ImageUtils.compressImageFromUri(context, uri, 1024, 1024, 80)
                if (imageFile != null) {
                    tempFiles.add(imageFile)
                    when (val uploadResult = imageUploadService.uploadImage(imageFile)) {
                        is ImageUploadResult.Success -> {
                            uploadedUrls.add(uploadResult.imageUrl)
                        }
                        is ImageUploadResult.Error -> {
                            // Clean up temp files
                            tempFiles.forEach { it.delete() }
                            return@withContext ImageManagerResult.Error("Failed to upload image: ${uploadResult.error}")
                        }
                    }
                } else {
                    // Clean up temp files
                    tempFiles.forEach { it.delete() }
                    return@withContext ImageManagerResult.Error("Failed to process image")
                }
            }
            
            // Clean up temp files
            tempFiles.forEach { it.delete() }
            
            // Update local database with all image URLs
            val localSuccess = localImageService.updateProductImages(productId, uploadedUrls)
            
            if (localSuccess) {
                ImageManagerResult.Success(
                    uploadedUrls.firstOrNull() ?: "",
                    "All ${uploadedUrls.size} images uploaded and updated successfully"
                )
            } else {
                ImageManagerResult.Error("Images uploaded but failed to update local database")
            }
        } catch (e: Exception) {
            ImageManagerResult.Error("Failed to upload and update: ${e.message}")
        }
    }
    
    /**
     * Upload single image file and update product in local database
     * @param imageFile Image file to upload
     * @param productId Product ID to update
     * @return Result with success/error
     */
    suspend fun uploadAndUpdateProductImageFromFile(
        context: Context,
        imageFile: File,
        productId: String
    ): ImageManagerResult = withContext(Dispatchers.IO) {
        try {
            val localImageService = LocalImageService(context)
            
            // Upload image to FreeImage.host
            when (val uploadResult = imageUploadService.uploadImage(imageFile)) {
                is ImageUploadResult.Success -> {
                    // Update local database with new image URL
                    val localSuccess = localImageService.updateProductImage(productId, uploadResult.imageUrl)
                    
                    if (localSuccess) {
                        ImageManagerResult.Success(uploadResult.imageUrl, "Product image updated successfully")
                    } else {
                        ImageManagerResult.Error("Image uploaded but failed to update local database")
                    }
                }
                is ImageUploadResult.Error -> {
                    ImageManagerResult.Error(uploadResult.error)
                }
            }
        } catch (e: Exception) {
            ImageManagerResult.Error("Failed to upload and update: ${e.message}")
        }
    }
}

sealed class ImageManagerResult {
    data class Success(val imageUrl: String, val message: String) : ImageManagerResult()
    data class Error(val error: String) : ImageManagerResult()
}
