package com.deliverydriver.resources.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deliverydriver.resources.data.repository.ResourceRepository
import com.deliverydriver.resources.ui.components.ExpandableCard
import com.deliverydriver.resources.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceHubScreen() {
    val referenceCategory = ResourceRepository.categories.find { it.id == "reference" }

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
                            text = "Acronyms, FAQs, gear & more",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Everything you need to know — from acronyms to gear checklists. Bookmark this section!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(referenceCategory?.items ?: emptyList()) { item ->
                ExpandableCard(
                    title = item.title,
                    summary = item.summary,
                    details = item.details
                )
            }

            // Bonus: Gear checklist at bottom
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Quick Gear Checklist", icon = Icons.Filled.Backpack)
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val gearItems = listOf(
                            "📱 Charged phone + backup battery",
                            "🔌 USB cables (C + Lightning)",
                            "💧 Large water bottle + electrolytes",
                            "🍎 Snacks / protein bars / lunch",
                            "🧴 Sunscreen + sunglasses",
                            "🧢 Hat + comfortable layers",
                            "👟 Sturdy shoes with grip",
                            "🖊️ Sharpie + pen + notebook",
                            "🔦 Flashlight or headlamp",
                            "🧽 Hand sanitizer + wet wipes",
                            "🔋 Power bank (10,000 mAh+)",
                            "💊 Any personal medication"
                        )
                        gearItems.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text(text = item, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
