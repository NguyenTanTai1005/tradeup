package com.example.tradeup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Enhanced search bar với location filter và distance filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    showLocationFilter: Boolean = false,
    onLocationFilterClick: () -> Unit = {},
    hasLocationFilter: Boolean = false,
    distance: Float? = null,
    onDistanceChange: (Float?) -> Unit = {},
    showDistanceFilter: Boolean = true,
    maxDistance: Float = 50f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main search row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tìm kiếm sản phẩm...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSearch,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tìm")
                }
            }

            // Filter row
            if (showDistanceFilter || showLocationFilter) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Distance filter
                    if (showDistanceFilter) {
                        OutlinedTextField(
                            value = distance?.toString() ?: "",
                            onValueChange = { value ->
                                if (value.isEmpty()) {
                                    onDistanceChange(null)
                                } else {
                                    val dist = value.toFloatOrNull()
                                    if (dist != null && dist <= maxDistance) {
                                        onDistanceChange(dist)
                                    }
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            placeholder = { Text("0") },
                            label = { Text("Khoảng cách (km)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Distance",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // Location filter chip
                    if (showLocationFilter) {
                        FilterChip(
                            onClick = onLocationFilterClick,
                            label = {
                                Text(
                                    "Gần tôi",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = hasLocationFilter,
                            leadingIcon = if (hasLocationFilter) {
                                {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove filter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Location filter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    // Distance range indicator
                    if (showDistanceFilter && distance != null) {
                        Text(
                            text = "Tối đa $maxDistance km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick filter chips for categories
 */
@Composable
fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                onClick = { onCategorySelected(null) },
                label = { Text("Tất cả") },
                selected = selectedCategory == null
            )
        }

        // Category chips
        items(categories) { category ->
            FilterChip(
                onClick = { 
                    onCategorySelected(if (selectedCategory == category) null else category)
                },
                label = { Text(category) },
                selected = selectedCategory == category
            )
        }
    }
}

/**
 * Distance slider filter component
 */
@Composable
fun DistanceSliderFilter(
    distance: Float?,
    onDistanceChange: (Float?) -> Unit,
    maxDistance: Float = 50f,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Khoảng cách",
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${distance?.toInt() ?: 0} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(
                    onClick = { onDistanceChange(null) },
                    enabled = enabled && distance != null
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear distance filter",
                        modifier = Modifier.size(16.dp),
                        tint = if (enabled && distance != null) 
                               MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = distance ?: 0f,
            onValueChange = { value ->
                onDistanceChange(if (value > 0) value else null)
            },
            valueRange = 0f..maxDistance,
            steps = (maxDistance.toInt() / 5) - 1, // Steps of 5km
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${maxDistance.toInt()} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Comprehensive product filter with location and distance
 */
@Composable
fun ProductFilterDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    distance: Float?,
    onDistanceChange: (Float?) -> Unit,
    hasLocationFilter: Boolean,
    onLocationFilterChange: (Boolean) -> Unit,
    categories: List<String> = emptyList(),
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    maxDistance: Float = 50f,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Bộ lọc sản phẩm",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
            },
            text = {
                Column {
                    // Location filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tìm kiếm gần vị trí của tôi",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = hasLocationFilter,
                            onCheckedChange = onLocationFilterChange
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Distance filter
                    DistanceSliderFilter(
                        distance = distance,
                        onDistanceChange = onDistanceChange,
                        maxDistance = maxDistance,
                        enabled = hasLocationFilter
                    )
                    
                    if (categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Category filter
                        Text(
                            "Danh mục",
                            style = MaterialTheme.typography.labelMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        CategoryFilterChips(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = onCategorySelected
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onClearFilters) {
                        Text("Xóa bộ lọc")
                    }
                    Button(onClick = {
                        onApplyFilters()
                        onDismiss()
                    }) {
                        Text("Áp dụng")
                    }
                }
            },
            dismissButton = null
        )
    }
}
