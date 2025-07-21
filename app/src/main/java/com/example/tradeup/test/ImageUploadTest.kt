package com.example.tradeup.test

import android.content.Context
import com.example.tradeup.services.ImageUploadService
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

/**
 * Test class for FreeImage.host API upload
 */
class ImageUploadTest {
    
    fun testApiUpload(context: Context) {
        runBlocking {
            // Create a small test image file
            val testImageFile = createTestImageFile(context)
            
            val imageUploadService = ImageUploadService()
            val result = imageUploadService.uploadImage(testImageFile)
            
            when (result) {
                is com.example.tradeup.services.ImageUploadResult.Success -> {
                    println("✅ Test upload successful!")
                    println("Image URL: ${result.imageUrl}")
                }
                is com.example.tradeup.services.ImageUploadResult.Error -> {
                    println("❌ Test upload failed!")
                    println("Error: ${result.error}")
                }
            }
            
            // Clean up
            testImageFile.delete()
        }
    }
    
    private fun createTestImageFile(context: Context): File {
        // Create a simple test image (1x1 pixel PNG)
        val testFile = File(context.cacheDir, "test_image.png")
        val pngData = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00, 0x90.toByte(), 0x77, 0x53.toByte(),
            0xDE.toByte(), 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54,
            0x08, 0xD7.toByte(), 0x63, 0xF8.toByte(), 0x0F, 0x00, 0x00, 0x01,
            0x00, 0x01, 0x5C.toByte(), 0xC7.toByte(), 0x8D.toByte(), 0xB4.toByte(),
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
            0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
        )
        
        FileOutputStream(testFile).use { output ->
            output.write(pngData)
        }
        
        return testFile
    }
}
