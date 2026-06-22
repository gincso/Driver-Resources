package com.deliverydriver.resources.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deliverydriver.resources.data.repository.ResourceRepository
import com.deliverydriver.resources.ui.components.ExpandableCard
import com.deliverydriver.resources.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceHubScreen() {
    val categories = ResourceRepository.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reference Hub",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Acronyms, FAQs, scorecards & more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                val refCategory = categories.find { it.id == "reference" }
                if (refCategory != null) {
                    SectionHeader(title = refCategory.title, icon = refCategory.icon)
                    Spacer(modifier = Modifier.height(4.dp))
                    refCategory.items.forEach { item ->
                        ExpandableCard(
                            title = item.title,
                            summary = item.summary,
                            details = item.details
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
