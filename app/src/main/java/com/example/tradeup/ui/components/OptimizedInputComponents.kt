package com.example.tradeup.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradeup.model.ProductCategory
import com.example.tradeup.model.ProductCondition

/**
 * Optimized category dropdown with search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedCategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = ProductCategory.getAllDisplayNames()
    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            categories
        } else {
            categories.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Danh mục") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { 
                expanded = false
                searchQuery = ""
            }
        ) {
            // Search field in dropdown
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm danh mục...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )
            
            // Category options
            filteredCategories.forEach { category ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = category,
                            fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    onClick = {
                        onCategorySelected(ProductCategory.getValueFromDisplayName(category))
                        expanded = false
                        searchQuery = ""
                    }
                )
            }
            
            if (filteredCategories.isEmpty()) {
                Text(
                    text = "Không tìm thấy danh mục",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Quick category selection chips
 */
@Composable
fun QuickCategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val popularCategories = listOf("Điện tử", "Thời trang", "Nội thất", "Sách")
    
    Column(modifier = modifier) {
        Text(
            text = "Danh mục phổ biến",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularCategories) { category ->
                FilterChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    selected = selectedCategory == category,
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

/**
 * Smart condition selector with icons
 */
@Composable
fun SmartConditionSelector(
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val conditions = ProductCondition.getAllDisplayNames()
    
    Column(modifier = modifier) {
        Text(
            text = "Tình trạng",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            conditions.forEach { condition ->
                FilterChip(
                    onClick = { onConditionSelected(ProductCondition.getValueFromDisplayName(condition)) },
                    label = { 
                        Text(
                            condition,
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    selected = selectedCondition == condition,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
