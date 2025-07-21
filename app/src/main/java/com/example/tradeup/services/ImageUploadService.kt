package com.example.tradeup.services

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ImageUploadService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    companion object {
        private const val FREEIMAGE_API_URL = "https://freeimage.host/api/1/upload"
        private const val API_KEY = "6d207e02198a847aa98d0a2a901485a5"
    }
    
    /**
     * Upload image file to FreeImage.host
     * @param imageFile The image file to upload
     * @return ImageUploadResult with success/error status
     */
    suspend fun uploadImage(imageFile: File): ImageUploadResult = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart("action", "upload")
                .addFormDataPart("format", "json")
                .addFormDataPart(
                    "source",
                    imageFile.name,
                    imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()
            
            val request = Request.Builder()
                .url(FREEIMAGE_API_URL)
                .post(requestBody)
                .build()
            
            println("Debug: Uploading image file: ${imageFile.name}, size: ${imageFile.length()}")
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    println("Debug: FreeImage API Response: $responseBody") // Debug log
                    val jsonResponse = JSONObject(responseBody)
                    
                    // Check status_code first
                    val statusCode = jsonResponse.optInt("status_code", 0)
                    
                    if (statusCode == 200 && jsonResponse.has("image")) {
                        val imageObject = jsonResponse.getJSONObject("image")
                        val imageUrl = imageObject.getString("url")
                        println("Debug: Successfully got image URL: $imageUrl") // Debug log
                        ImageUploadResult.Success(imageUrl)
                    } else {
                        // Try to get error message
                        val error = jsonResponse.optString("error", "Upload failed - Status: $statusCode")
                        println("Debug: Upload failed - Response: $responseBody") // Debug log
                        ImageUploadResult.Error("Upload failed: $error")
                    }
                } else {
                    ImageUploadResult.Error("Empty response from server")
                }
            } else {
                println("Debug: HTTP Error: ${response.code} - ${response.message}")
                ImageUploadResult.Error("HTTP Error: ${response.code} - ${response.message}")
            }
        } catch (e: Exception) {
            ImageUploadResult.Error("Upload failed: ${e.message}")
        }
    }
    
    /**
     * Upload image from URI using base64 encoding
     * @param context Android context
     * @param imageUri URI of the image
     * @return ImageUploadResult with success/error status
     */
    suspend fun uploadImageFromUri(context: Context, imageUri: Uri): ImageUploadResult = withContext(Dispatchers.IO) {
        try {
            val base64String = uriToBase64(context, imageUri)
            
            println("Debug: Uploading image from URI, base64 length: ${base64String.length}")
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart("action", "upload")
                .addFormDataPart("format", "json")
                .addFormDataPart("source", base64String)
                .build()
            
            val request = Request.Builder()
                .url(FREEIMAGE_API_URL)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    println("Debug: FreeImage API Response (URI): $responseBody") // Debug log
                    val jsonResponse = JSONObject(responseBody)
                    
                    // Check status_code first
                    val statusCode = jsonResponse.optInt("status_code", 0)
                    
                    if (statusCode == 200 && jsonResponse.has("image")) {
                        val imageObject = jsonResponse.getJSONObject("image")
                        val imageUrl = imageObject.getString("url")
                        println("Debug: Successfully got image URL from URI: $imageUrl") // Debug log
                        ImageUploadResult.Success(imageUrl)
                    } else {
                        // Try to get error message
                        val error = jsonResponse.optString("error", "Upload failed - Status: $statusCode")
                        println("Debug: Upload from URI failed - Response: $responseBody") // Debug log
                        ImageUploadResult.Error("Upload failed: $error")
                    }
                } else {
                    ImageUploadResult.Error("Empty response from server")
                }
            } else {
                println("Debug: HTTP Error from URI upload: ${response.code} - ${response.message}")
                ImageUploadResult.Error("HTTP Error: ${response.code} - ${response.message}")
            }
        } catch (e: Exception) {
            ImageUploadResult.Error("Upload failed: ${e.message}")
        }
    }
    
    /**
     * Convert URI to Base64 string
     * @param context Android context
     * @param uri Image URI
     * @return Base64 encoded string
     */
    private fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image URI")
        
        val bytes = inputStream.readBytes()
        inputStream.close()
        
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}

sealed class ImageUploadResult {
    data class Success(val imageUrl: String) : ImageUploadResult()
    data class Error(val error: String) : ImageUploadResult()
}
