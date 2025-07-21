package com.example.tradeup.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    
    /**
     * Convert URI to Base64 string
     * @param context Android context
     * @param uri Image URI
     * @return Base64 encoded string
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null
            
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert Bitmap to Base64 string
     * @param bitmap The bitmap to convert
     * @param quality Compression quality (0-100)
     * @return Base64 encoded string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Convert URI to File (useful for multipart upload)
     * @param context Android context
     * @param uri Image URI
     * @param fileName Desired file name
     * @return File object or null if failed
     */
    fun uriToFile(context: Context, uri: Uri, fileName: String = "temp_image.jpg"): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            file
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Compress and resize image from URI
     * @param context Android context
     * @param uri Image URI
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @param quality Compression quality (0-100)
     * @return Compressed File or null if failed
     */
    fun compressImageFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Calculate new dimensions
            val ratio = minOf(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height
            )
            
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            
            // Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            
            // Save to file
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.close()
            
            // Clean up
            bitmap.recycle()
            resizedBitmap.recycle()
            
            file
        } catch (e: Exception) {
            null
        }
    }
}
