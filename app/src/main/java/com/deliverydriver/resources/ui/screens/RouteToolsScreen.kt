package com.deliverydriver.resources.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.deliverydriver.resources.data.models.AccessCode
import com.deliverydriver.resources.data.models.ChecklistItem
import com.deliverydriver.resources.data.models.DeliveryStop
import com.deliverydriver.resources.data.models.StopStatus
import com.deliverydriver.resources.data.repository.ResourceRepository
import com.deliverydriver.resources.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteToolsScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Checklist", "Access Codes", "Stops", "Stats", "Guides")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Route Tools",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Checklists, codes, stats & guides",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Filled.Checklist
                                        1 -> Icons.Filled.Lock
                                        2 -> Icons.Filled.Route
                                        3 -> Icons.Filled.BarChart
                                        4 -> Icons.Filled.MenuBook
                                        else -> Icons.Filled.Help
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(title)
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> ChecklistTab()
                1 -> AccessCodesTab()
                2 -> StopsTab()
                3 -> RouteStatsTab()
                4 -> RouteGuidesTab()
            }
        }
    }
}

// ── CHECKLIST TAB ─────────────────────────────────────────────────

@Composable
private fun ChecklistTab() {
    val checklistItems by ResourceRepository.checklistItems.collectAsState()
    val checkedItems = remember { mutableStateListOf<Boolean>().apply {
        repeat(checklistItems.size) { add(false) }
    } }

    val categories = checklistItems.map { it.category }.distinct()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Loadout Checklist",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val checkedCount = checkedItems.count { it }
                        Text(
                            text = "$checkedCount / ${checklistItems.size} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        categories.forEach { category ->
            val categoryItems = checklistItems.filter { it.category == category }

            item {
                SectionHeader(title = category)
                Spacer(modifier = Modifier.height(4.dp))
            }

            itemsIndexed(categoryItems) { _, item ->
                val globalIndex = checklistItems.indexOf(item)
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (checkedItems[globalIndex])
                            Color(0xFF00A67E).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedItems[globalIndex],
                            onCheckedChange = { checkedItems[globalIndex] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF00A67E)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            style = if (checkedItems[globalIndex])
                                MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            else
                                MaterialTheme.typography.bodyMedium,
                            color = if (checkedItems[globalIndex])
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ── ACCESS CODES TAB ─────────────────────────────────────────────

@Composable
private fun AccessCodesTab() {
    var accessCodes by remember {
        mutableStateOf(listOf<AccessCode>())
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCodes = remember(accessCodes, searchQuery) {
        if (searchQuery.isBlank()) accessCodes
        else accessCodes.filter {
            it.address.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search saved codes...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {{
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }} else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Add button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save New Access Code")
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (filteredCodes.isEmpty()) {
            item {
                EmptyState(
                    message = if (accessCodes.isEmpty())
                        "No access codes saved yet.\nTap the button above to add one!"
                    else
                        "No codes match your search.",
                    icon = Icons.Filled.Lock
                )
            }
        }

        items(filteredCodes.sortedByDescending { it.timestamp }) { code ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFB800).copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = code.address,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            accessCodes = accessCodes.filter { it.id != code.id }
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.VpnKey,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Code: ${code.code}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB800)
                        )
                    }
                    if (code.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: ${code.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccessCodeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { code ->
                accessCodes = accessCodes + code
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddAccessCodeDialog(
    onDismiss: () -> Unit,
    onSave: (AccessCode) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Access Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    placeholder = { Text("e.g., 123 Main St gate") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Access Code") },
                    placeholder = { Text("e.g., #1234") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g., call box 204") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address.isNotBlank() && code.isNotBlank()) {
                        val nextId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                        onSave(AccessCode(id = nextId, address = address, code = code, notes = notes))
                    }
                },
                enabled = address.isNotBlank() && code.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── STOPS TAB ────────────────────────────────────────────────────

@Composable
private fun StopsTab() {
    val stops by com.deliverydriver.resources.scanner.RouteViewModel.stops.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStop by remember { mutableStateOf<DeliveryStop?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val sortByAddress = com.deliverydriver.resources.scanner.RouteViewModel.sortByAddress

    val filteredStops = remember(stops, searchQuery) {
        if (searchQuery.isBlank()) com.deliverydriver.resources.scanner.RouteViewModel.getSortedStops()
        else com.deliverydriver.resources.scanner.RouteViewModel.getSortedStops().filter {
            it.address.contains(searchQuery, ignoreCase = true) ||
            it.customerName.contains(searchQuery, ignoreCase = true)
        }
    }

    val (totalStops, deliveredStops, totalPackages) = com.deliverydriver.resources.scanner.RouteViewModel.getStats()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Summary card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delivery Stops",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (totalStops > 0)
                                "$deliveredStops / $totalStops delivered · $totalPackages packages"
                            else
                                "No stops added yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Search + Add + Sort bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search address or name...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {{
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }} else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    com.deliverydriver.resources.scanner.RouteViewModel.toggleSort()
                }) {
                    Icon(
                        imageVector = if (sortByAddress) Icons.Filled.SortByAlpha else Icons.Filled.Numbers,
                        contentDescription = "Toggle sort",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Stop")
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (filteredStops.isEmpty()) {
            item {
                EmptyState(
                    message = if (stops.isEmpty())
                        "No stops yet.\nTap \"Add Stop\" to start building your route!"
                    else
                        "No stops match your search.",
                    icon = Icons.Filled.Route
                )
            }
        }

        // Sort indicator
        if (filteredStops.isNotEmpty()) {
            item {
                Text(
                    text = "Sorted by: ${if (sortByAddress) "Address (A-Z)" else "Stop Order"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        items(filteredStops, key = { it.id }) { stop ->
            val statusColor = when (stop.status) {
                StopStatus.PENDING -> Color(0xFFFFB800)
                StopStatus.DELIVERED -> Color(0xFF00A67E)
                StopStatus.ATTEMPTED -> Color(0xFFFF8C00)
                StopStatus.SKIPPED -> Color(0xFFFF4444)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (stop.status) {
                        StopStatus.DELIVERED -> Color(0xFF00A67E).copy(alpha = 0.08f)
                        StopStatus.SKIPPED -> Color(0xFFFF4444).copy(alpha = 0.08f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
                onClick = { editingStop = stop }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status indicator dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stop.address,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (stop.city.isNotBlank()) {
                                Text(
                                    text = if (stop.state.isNotBlank()) "${stop.city}, ${stop.state}" else stop.city,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = when (stop.status) {
                                    StopStatus.PENDING -> "Pending"
                                    StopStatus.DELIVERED -> "Done"
                                    StopStatus.ATTEMPTED -> "Attempt"
                                    StopStatus.SKIPPED -> "Skip"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Customer info row
                    if (stop.customerName.isNotBlank() || stop.customerPhone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildString {
                                    if (stop.customerName.isNotBlank()) append(stop.customerName)
                                    if (stop.customerName.isNotBlank() && stop.customerPhone.isNotBlank()) append(" · ")
                                    if (stop.customerPhone.isNotBlank()) append(stop.customerPhone)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Package info row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stop.packageCount} package${if (stop.packageCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (stop.deliveryNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.Notes,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (stop.accessCode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.VpnKey,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFB800)
                            )
                        }
                    }
                }
            }
        }

        // Clear all button
        if (stops.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        com.deliverydriver.resources.scanner.RouteViewModel.clearStops()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Stops")
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditStopDialog(
            onDismiss = { showAddDialog = false },
            onSave = { stop ->
                com.deliverydriver.resources.scanner.RouteViewModel.addStop(stop)
                showAddDialog = false
            }
        )
    }

    editingStop?.let { stop ->
        EditStopDialog(
            stop = stop,
            onDismiss = { editingStop = null },
            onUpdate = { updated ->
                com.deliverydriver.resources.scanner.RouteViewModel.updateStop(updated)
                editingStop = null
            },
            onDelete = {
                com.deliverydriver.resources.scanner.RouteViewModel.removeStop(stop.id)
                editingStop = null
            },
            onStatusChange = { status ->
                com.deliverydriver.resources.scanner.RouteViewModel.updateStatus(stop.id, status)
            }
        )
    }
}

@Composable
private fun AddEditStopDialog(
    existingStop: DeliveryStop? = null,
    onDismiss: () -> Unit,
    onSave: (DeliveryStop) -> Unit
) {
    var address by remember { mutableStateOf(existingStop?.address ?: "") }
    var city by remember { mutableStateOf(existingStop?.city ?: "") }
    var state by remember { mutableStateOf(existingStop?.state ?: "") }
    var zip by remember { mutableStateOf(existingStop?.zip ?: "") }
    var customerName by remember { mutableStateOf(existingStop?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(existingStop?.customerPhone ?: "") }
    var packageCount by remember { mutableStateOf(existingStop?.packageCount?.toString() ?: "1") }
    var deliveryNotes by remember { mutableStateOf(existingStop?.deliveryNotes ?: "") }
    var accessCode by remember { mutableStateOf(existingStop?.accessCode ?: "") }

    val isEditing = existingStop != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Stop" else "Add Delivery Stop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    placeholder = { Text("e.g., 123 Main St") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(70.dp)
                    )
                    OutlinedTextField(
                        value = zip,
                        onValueChange = { zip = it },
                        label = { Text("ZIP") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(90.dp)
                    )
                }
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    placeholder = { Text("e.g., John Doe") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer Phone") },
                    placeholder = { Text("e.g., (555) 123-4567") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = packageCount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) packageCount = it },
                    label = { Text("Package Count") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deliveryNotes,
                    onValueChange = { deliveryNotes = it },
                    label = { Text("Delivery Notes") },
                    placeholder = { Text("e.g., Leave at back door") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accessCode,
                    onValueChange = { accessCode = it },
                    label = { Text("Access Code / Gate Code") },
                    placeholder = { Text("e.g., #1234") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address.isNotBlank()) {
                        onSave(DeliveryStop(
                            id = existingStop?.id ?: java.util.UUID.randomUUID().toString(),
                            address = address,
                            city = city,
                            state = state,
                            zip = zip,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            packageCount = packageCount.toIntOrNull() ?: 1,
                            deliveryNotes = deliveryNotes,
                            accessCode = accessCode,
                            status = existingStop?.status ?: StopStatus.PENDING,
                            stopOrder = existingStop?.stopOrder ?: 0,
                            timestamp = existingStop?.timestamp ?: System.currentTimeMillis()
                        ))
                    }
                },
                enabled = address.isNotBlank()
            ) {
                Text(if (isEditing) "Update" else "Add Stop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditStopDialog(
    stop: DeliveryStop,
    onDismiss: () -> Unit,
    onUpdate: (DeliveryStop) -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (StopStatus) -> Unit
) {
    var showEditForm by remember { mutableStateOf(false) }

    if (showEditForm) {
        AddEditStopDialog(
            existingStop = stop,
            onDismiss = { showEditForm = false },
            onSave = { updated ->
                onUpdate(updated)
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stop.address) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Stop details
                if (stop.customerName.isNotBlank() || stop.customerPhone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildString {
                                if (stop.customerName.isNotBlank()) append(stop.customerName)
                                if (stop.customerName.isNotBlank() && stop.customerPhone.isNotBlank()) append(" · ")
                                if (stop.customerPhone.isNotBlank()) append(stop.customerPhone)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${stop.packageCount} package${if (stop.packageCount != 1) "s" else ""}")
                }

                if (stop.deliveryNotes.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stop.deliveryNotes, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (stop.accessCode.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFB800))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Code: ${stop.accessCode}", color = Color(0xFFFFB800), fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider()

                // Status buttons
                Text("Update Status:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SmallStatusButton("Done", Color(0xFF00A67E)) { onStatusChange(StopStatus.DELIVERED) }
                    SmallStatusButton("Attempt", Color(0xFFFF8C00)) { onStatusChange(StopStatus.ATTEMPTED) }
                    SmallStatusButton("Skip", Color(0xFFFF4444)) { onStatusChange(StopStatus.SKIPPED) }
                    SmallStatusButton("Pending", Color(0xFFFFB800)) { onStatusChange(StopStatus.PENDING) }
                }
            }
        },
        confirmButton = {
            Button(onClick = { showEditForm = true }) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Details")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

@Composable
private fun SmallStatusButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ── ROUTE GUIDES TAB ─────────────────────────────────────────────

@Composable
private fun RouteGuidesTab() {
    val toolCategory = ResourceRepository.categories.find { it.id == "tools" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Handyman,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Practical tools to optimize your daily route.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val toolItems = toolCategory?.items ?: emptyList()
        items(toolItems) { item ->
            ExpandableCard(
                title = item.title,
                summary = item.summary,
                details = item.details
            )
        }
    }
}


// ── ROUTE STATS TAB ──────────────────────────────────────────────

@Composable
private fun RouteStatsTab() {
    var totalPackages by remember { mutableStateOf("") }
    var totalStops by remember { mutableStateOf("") }
    var packagesDelivered by remember { mutableStateOf("") }
    var stopsCompleted by remember { mutableStateOf("") }

    var shiftStartTime by remember { mutableStateOf<Long?>(null) }
    var shiftEndTime by remember { mutableStateOf<Long?>(null) }
    var shiftActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }

    // Timer effect
    LaunchedEffect(shiftActive) {
        if (shiftActive && shiftStartTime != null) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - shiftStartTime!!) / 1000
                delay(1000)
            }
        }
    }

    // Reset all inputs
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ── Shift Timer Card ─────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (shiftActive)
                        Color(0xFF00A67E).copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (shiftActive) Icons.Filled.PlayCircle else Icons.Filled.StopCircle,
                        contentDescription = null,
                        tint = if (shiftActive) Color(0xFF00A67E) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (shiftActive) "Shift Active" else "Shift Not Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (shiftActive) Color(0xFF00A67E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Elapsed time display
                    if (shiftActive) {
                        val hours = elapsedSeconds / 3600
                        val minutes = (elapsedSeconds % 3600) / 60
                        val secs = elapsedSeconds % 60
                        Text(
                            text = "%d:%02d:%02d".format(hours, minutes, secs),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!shiftActive && shiftStartTime == null) {
                            Button(
                                onClick = {
                                    shiftStartTime = System.currentTimeMillis()
                                    shiftActive = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00A67E)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Shift")
                            }
                        }
                        if (shiftActive) {
                            Button(
                                onClick = {
                                    shiftEndTime = System.currentTimeMillis()
                                    shiftActive = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF4444)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Shift")
                            }
                        }
                        if (shiftStartTime != null && !shiftActive) {
                            Button(
                                onClick = { showResetDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }

        // ── Route Inputs ─────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Route Numbers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatInputField("Total Packages", totalPackages, { totalPackages = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    StatInputField("Total Stops", totalStops, { totalStops = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    StatInputField("Packages Delivered", packagesDelivered, { packagesDelivered = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    StatInputField("Stops Completed", stopsCompleted, { stopsCompleted = it })
                }
            }
        }

        // ── Calculated Stats ──────────────────────────────────────
        item {
            val totalPkg = totalPackages.toIntOrNull() ?: 0
            val totalStp = totalStops.toIntOrNull() ?: 0
            val delivered = packagesDelivered.toIntOrNull() ?: 0
            val completed = stopsCompleted.toIntOrNull() ?: 0
            val hours = if (shiftStartTime != null && shiftEndTime != null)
                (shiftEndTime!! - shiftStartTime!!) / 1000.0 / 3600.0
            else if (shiftStartTime != null && shiftActive)
                (System.currentTimeMillis() - shiftStartTime!!) / 1000.0 / 3600.0
            else 0.0

            val packagesPerHour = if (hours > 0) "%.1f".format(delivered / hours) else "—"
            val stopsPerHour = if (hours > 0) "%.1f".format(completed / hours) else "—"
            val pkgCompletion = if (totalPkg > 0) (delivered * 100 / totalPkg) else 0
            val stopCompletion = if (totalStp > 0) (completed * 100 / totalStp) else 0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Your Delivery Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Big rate display
                    if (hours > 0 && delivered > 0) {
                        Text(
                            text = "$packagesPerHour",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "packages / hour",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Stats grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MiniStat("Pkgs/hr", packagesPerHour, Color(0xFF00A67E))
                        MiniStat("Stops/hr", stopsPerHour, Color(0xFF00A8E8))
                        MiniStat("Time", if (hours > 0) "%.1fh".format(hours) else "—", Color(0xFFFFB800))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bars
                    if (totalPkg > 0) {
                        Text(
                            text = "Package Completion: $pkgCompletion%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { pkgCompletion / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00A67E),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (totalStp > 0) {
                        Text(
                            text = "Stop Completion: $stopCompletion%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { stopCompletion / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00A8E8),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    if (totalPkg == 0 && totalStp == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter your route numbers above to calculate your delivery rate and completion percentage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Benchmark Comparison ─────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Benchmarks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BenchmarkRow("Residential", "20-25 stops/hr", "25-30 pkgs/hr")
                    BenchmarkRow("Apartments", "8-12 stops/hr", "12-18 pkgs/hr")
                    BenchmarkRow("Rural", "8-12 stops/hr", "10-15 pkgs/hr")
                    BenchmarkRow("Business", "12-16 stops/hr", "15-20 pkgs/hr")
                    BenchmarkRow("Mixed Route", "15-20 stops/hr", "20-25 pkgs/hr")

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Compare your numbers to these benchmarks to see how you're pacing!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Daily Totals Summary ─────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFB800).copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pro Tip",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your first week, focus on accuracy over speed. " +
                                "Track your numbers daily and you'll see natural improvement " +
                                "as you learn your route area. Most drivers hit their stride around week 3-4.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Stats?") },
            text = { Text("This will clear all route numbers and shift data.") },
            confirmButton = {
                Button(onClick = {
                    totalPackages = ""
                    totalStops = ""
                    packagesDelivered = ""
                    stopsCompleted = ""
                    shiftStartTime = null
                    shiftEndTime = null
                    elapsedSeconds = 0L
                    showResetDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { newVal ->
            // Only allow digits
            if (newVal.all { it.isDigit() }) onValueChange(newVal)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BenchmarkRow(routeType: String, stopsPerHour: String, pkgsPerHour: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = routeType,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stopsPerHour,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = pkgsPerHour,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}


