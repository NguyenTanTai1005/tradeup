package com.example.tradeup.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Helper class để xử lý location services
 */
class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    
    /**
     * Kiểm tra quyền truy cập vị trí
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Lấy vị trí hiện tại của người dùng
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(callback: (LocationResult) -> Unit) {
        if (!hasLocationPermission()) {
            callback(LocationResult.Error("Không có quyền truy cập vị trí"))
            return
        }
        
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000L)
                .setMaxUpdateDelayMillis(15000L)
                .build()
            
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    super.onLocationResult(result)
                    result.lastLocation?.let { location ->
                        fusedLocationClient.removeLocationUpdates(this)
                        callback(LocationResult.Success(location))
                    } ?: callback(LocationResult.Error("Không thể lấy vị trí"))
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest, 
                locationCallback, 
                context.mainLooper
            )
            
        } catch (e: Exception) {
            callback(LocationResult.Error("Lỗi khi lấy vị trí: ${e.message}"))
        }
    }
    
    /**
     * Chuyển đổi tọa độ thành địa chỉ
     */
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    buildString {
                        if (!address.thoroughfare.isNullOrBlank()) {
                            append(address.thoroughfare)
                            append(", ")
                        }
                        if (!address.subAdminArea.isNullOrBlank()) {
                            append(address.subAdminArea)
                            append(", ")
                        }
                        if (!address.adminArea.isNullOrBlank()) {
                            append(address.adminArea)
                        }
                        if (!address.countryName.isNullOrBlank()) {
                            append(", ")
                            append(address.countryName)
                        }
                    }.takeIf { it.isNotBlank() }
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Chuyển đổi địa chỉ thành tọa độ  
     */
    suspend fun getLocationFromAddress(address: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                
                val addresses = geocoder.getFromLocationName(address, 1)
                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    Pair(location.latitude, location.longitude)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Tính khoảng cách giữa 2 điểm (km)
     */
    fun calculateDistance(
        lat1: Double, lon1: Double, 
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000f // Convert to km
    }
    
    sealed class LocationResult {
        data class Success(val location: Location) : LocationResult()
        data class Error(val message: String) : LocationResult()
    }
}
