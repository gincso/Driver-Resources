package com.deliverydriver.resources.data.models

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ResourceCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val items: List<ResourceItem>
)

data class ResourceItem(
    val id: String,
    val title: String,
    val summary: String,
    val details: String = "",
    val isExpanded: Boolean = false
)

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Long
)

data class SafetyItem(
    val id: String,
    val title: String,
    val summary: String,
    val details: String,
    val icon: ImageVector,
    val priority: SafetyPriority
)

enum class SafetyPriority {
    CRITICAL, HIGH, MEDIUM, LOW
}

data class AccessCode(
    val id: Int = 0,
    val address: String,
    val code: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChecklistItem(
    val id: String,
    val title: String,
    val category: String
)

data class DailyTip(
    val text: String,
    val category: String
)
