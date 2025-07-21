package com.example.tradeup.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradeup.utils.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationInput(
    location: String,
    latitude: Double?,
    longitude: Double?,
    onLocationChange: (location: String, latitude: Double?, longitude: Double?) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    
    var isLocationLoading by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Permission granted, get current location
                getCurrentLocation(locationHelper, scope) { loc, lat, lon ->
                    onLocationChange(loc, lat, lon)
                    isLocationLoading = false
                }
            }
            else -> {
                locationError = "Cần cấp quyền truy cập vị trí để sử dụng tính năng này"
                isLocationLoading = false
            }
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Vị trí",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = location,
            onValueChange = { newLocation ->
                onLocationChange(newLocation, latitude, longitude)
                locationError = null
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nhập địa chỉ hoặc nhấn nút định vị") },
            enabled = isEnabled,
            isError = locationError != null,
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLocationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (locationHelper.hasLocationPermission()) {
                                    isLocationLoading = true
                                    getCurrentLocation(locationHelper, scope) { loc, lat, lon ->
                                        onLocationChange(loc, lat, lon)
                                        isLocationLoading = false
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            enabled = isEnabled && !isLocationLoading
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Get Current Location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (location.isNotBlank()) {
                        IconButton(
                            onClick = { showLocationDialog = true },
                            enabled = isEnabled
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "View Location",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        )
        
        // Error message
        locationError?.let { error ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // Location info
        if (latitude != null && longitude != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tọa độ: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Location preview dialog
    if (showLocationDialog && location.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Đóng")
                }
            },
            title = { Text("Vị trí sản phẩm") },
            text = {
                Column {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (latitude != null && longitude != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tọa độ: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Tip: Người mua có thể xem vị trí này để ước lượng khoảng cách",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}

/**
 * Compact location display for product cards
 */
@Composable
fun LocationDisplay(
    location: String?,
    latitude: Double? = null,
    longitude: Double? = null,
    currentLatitude: Double? = null,
    currentLongitude: Double? = null,
    modifier: Modifier = Modifier,
    showDistance: Boolean = true
) {
    if (location.isNullOrBlank()) return
    
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    
    // Calculate distance if both coordinates available
    val distance = if (showDistance && 
        latitude != null && longitude != null && 
        currentLatitude != null && currentLongitude != null) {
        locationHelper.calculateDistance(
            currentLatitude, currentLongitude,
            latitude, longitude
        )
    } else null
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Column {
            Text(
                text = location.take(30) + if (location.length > 30) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            distance?.let { dist ->
                Text(
                    text = "Cách ${String.format("%.1f", dist)} km",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getCurrentLocation(
    locationHelper: LocationHelper,
    scope: CoroutineScope,
    onResult: (location: String, latitude: Double?, longitude: Double?) -> Unit
) {
    scope.launch {
        locationHelper.getCurrentLocation { result ->
            when (result) {
                is LocationHelper.LocationResult.Success -> {
                    val location = result.location
                    scope.launch {
                        val address = locationHelper.getAddressFromLocation(
                            location.latitude,
                            location.longitude
                        ) ?: "Vị trí không xác định"
                        
                        onResult(address, location.latitude, location.longitude)
                    }
                }
                is LocationHelper.LocationResult.Error -> {
                    onResult("Không thể xác định vị trí", null, null)
                }
            }
        }
    }
}
