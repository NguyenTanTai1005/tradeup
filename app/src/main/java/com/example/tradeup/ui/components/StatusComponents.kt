package com.example.tradeup.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeup.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        "Available" to "Còn hàng",
        "Sold" to "Đã bán",
        "Paused" to "Tạm dừng"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled },
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = statusOptions.find { it.first == selectedStatus }?.second ?: selectedStatus,
            onValueChange = { },
            readOnly = true,
            label = { Text("Trạng thái") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statusOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onStatusSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    includeAllOption: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val categoryOptions = if (includeAllOption) {
        listOf("Tất cả" to "Tất cả") + ProductCategory.values().map { it.value to it.displayName }
    } else {
        ProductCategory.values().map { it.value to it.displayName }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled },
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = categoryOptions.find { it.first == selectedCategory }?.second ?: selectedCategory,
            onValueChange = { },
            readOnly = true,
            label = { Text("Danh mục") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categoryOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onCategorySelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionDropdown(
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    includeAllOption: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val conditionOptions = if (includeAllOption) {
        listOf("Tất cả" to "Tất cả") + ProductCondition.values().map { it.value to it.displayName }
    } else {
        ProductCondition.values().map { it.value to it.displayName }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled },
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = conditionOptions.find { it.first == selectedCondition }?.second ?: selectedCondition,
            onValueChange = { },
            readOnly = true,
            label = { Text("Tình trạng") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            conditionOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onConditionSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = SortOption.values().map { it.value to it.displayName }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled },
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = sortOptions.find { it.first == selectedSort }?.second ?: selectedSort,
            onValueChange = { },
            readOnly = true,
            label = { Text("Sắp xếp theo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sortOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSortSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        "Available" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "Sold" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "Paused" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = when (status) {
                "Available" -> "Còn hàng"
                "Sold" -> "Đã bán"
                "Paused" -> "Tạm dừng"
                else -> status
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 10.sp
        )
    }
}
