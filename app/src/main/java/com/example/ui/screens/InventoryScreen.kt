package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InventoryItem
import com.example.data.model.ItemCondition
import com.example.data.model.ItemSource
import com.example.ui.components.*
import com.example.ui.theme.KasKeluarRed
import com.example.ui.theme.KasMasukGreen
import com.example.ui.viewmodel.MushallaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MushallaViewModel,
    modifier: Modifier = Modifier,
    initialOpenAddDialog: Boolean = false
) {
    val context = LocalContext.current
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val totalTypes by viewModel.totalItemTypes.collectAsStateWithLifecycle()
    val totalUnits by viewModel.totalItemUnits.collectAsStateWithLifecycle()
    val goodUnits by viewModel.goodConditionUnits.collectAsStateWithLifecycle()
    val damagedUnits by viewModel.damagedUnits.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }
    var selectedConditionFilter by remember { mutableStateOf<ItemCondition?>(null) }

    var showAddDialog by remember { mutableStateOf(initialOpenAddDialog) }
    var selectedItemForDetail by remember { mutableStateOf<InventoryItem?>(null) }
    var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

    val categories = listOf("Semua", "Elektronik", "Sound System", "Perlengkapan Ibadah", "Sarana Fisik", "Kebersihan")

    val filteredList = remember(inventoryItems, searchQuery, selectedCategoryFilter, selectedConditionFilter) {
        inventoryItems.filter { item ->
            val matchCat = selectedCategoryFilter == "Semua" || item.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchCond = selectedConditionFilter == null || item.condition == selectedConditionFilter
            val matchQuery = searchQuery.isBlank() ||
                item.itemName.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true)
            matchCat && matchCond && matchQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_inventory_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Barang Inventaris"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TOP HEADER: STATUS INVENTARIS CARDS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Status Inventaris Sarana Mushalla",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Jenis Barang",
                        value = "$totalTypes Jenis",
                        icon = Icons.Default.Category,
                        iconTint = Color(0xFF0284C7),
                        containerColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Kondisi Baik",
                        value = "$goodUnits Unit",
                        icon = Icons.Default.CheckCircle,
                        iconTint = KasMasukGreen,
                        containerColor = Color(0xFFD1E7DD),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Perlu Servis",
                        value = "$damagedUnits Unit",
                        icon = Icons.Default.Warning,
                        iconTint = if (damagedUnits > 0) Color(0xFFD97706) else Color.Gray,
                        containerColor = if (damagedUnits > 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_inventory_input"),
                    placeholder = { Text("Cari barang atau lokasi...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // --- SUBHEADER ---
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Inventaris (${filteredList.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: $totalUnits Unit Fisik",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- INVENTORY LIST ---
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Barang tidak ditemukan" else "Belum ada data inventaris",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol + untuk mencatat barang atau sarana mushalla baru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        InventoryRowItem(
                            item = item,
                            onClick = { selectedItemForDetail = item }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH BARANG INVENTARIS ---
    if (showAddDialog) {
        AddEditInventoryDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, cat, qty, unit, cond, src, estVal, loc, notes ->
                viewModel.addInventoryItem(
                    itemName = name,
                    category = cat,
                    quantity = qty,
                    unit = unit,
                    condition = cond,
                    source = src,
                    estimatedValue = estVal,
                    location = loc,
                    notes = notes
                )
                showAddDialog = false
                Toast.makeText(context, "Barang $name berhasil disimpan ke inventaris!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- DIALOG EDIT BARANG INVENTARIS ---
    itemToEdit?.let { existingItem ->
        AddEditInventoryDialog(
            item = existingItem,
            onDismiss = { itemToEdit = null },
            onSave = { name, cat, qty, unit, cond, src, estVal, loc, notes ->
                viewModel.updateInventoryItem(
                    existingItem.copy(
                        itemName = name,
                        category = cat,
                        quantity = qty,
                        unit = unit,
                        condition = cond,
                        source = src,
                        estimatedValue = estVal,
                        location = loc,
                        notes = notes
                    )
                )
                itemToEdit = null
                Toast.makeText(context, "Data inventaris diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- DIALOG DETAIL INVENTARIS ---
    selectedItemForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItemForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detail Inventaris")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF0284C7)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Kategori", value = item.category)
                    DetailRow(label = "Kondisi", value = item.condition.name.replace("_", " "))
                    DetailRow(label = "Sumber Perolehan", value = item.source.name.replace("_", " "))
                    DetailRow(label = "Lokasi Penempatan", value = item.location)
                    if (item.estimatedValue > 0) {
                        DetailRow(label = "Taksiran Nilai", value = formatRupiah(item.estimatedValue))
                    }
                    if (item.notes.isNotEmpty()) {
                        DetailRow(label = "Keterangan / Catatan", value = item.notes)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SecurityBadge(hash = item.securityHash)
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            val target = item
                            selectedItemForDetail = null
                            itemToEdit = target
                        }
                    ) {
                        Text("Edit")
                    }
                    TextButton(onClick = { selectedItemForDetail = null }) {
                        Text("Tutup")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val target = item
                        selectedItemForDetail = null
                        itemToDelete = target
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            }
        )
    }

    // --- CONFIRM DELETE INVENTORY ---
    itemToDelete?.let { toDel ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Barang Inventaris?") },
            text = { Text("Apakah Anda yakin ingin menghapus data inventaris '${toDel.itemName}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInventoryItem(toDel.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun InventoryRowItem(
    item: InventoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conditionColor = when (item.condition) {
        ItemCondition.BAIK -> KasMasukGreen
        ItemCondition.RUSAK_RINGAN -> Color(0xFFD97706)
        ItemCondition.RUSAK_BERAT -> KasKeluarRed
    }

    val conditionBg = when (item.condition) {
        ItemCondition.BAIK -> Color(0xFFD1E7DD)
        ItemCondition.RUSAK_RINGAN -> Color(0xFFFEF3C7)
        ItemCondition.RUSAK_BERAT -> Color(0xFFF8D7DA)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFE0F2FE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.category} • Lokasi: ${item.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sumber: ${item.source.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = conditionBg
                ) {
                    Text(
                        text = item.condition.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = conditionColor
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditInventoryDialog(
    item: InventoryItem?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        quantity: Int,
        unit: String,
        condition: ItemCondition,
        source: ItemSource,
        estimatedValue: Long,
        location: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(item?.itemName ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Perlengkapan Ibadah") }
    var quantityText by remember { mutableStateOf(item?.quantity?.toString() ?: "1") }
    var unit by remember { mutableStateOf(item?.unit ?: "Unit") }
    var condition by remember { mutableStateOf(item?.condition ?: ItemCondition.BAIK) }
    var source by remember { mutableStateOf(item?.source ?: ItemSource.BELI_KAS) }
    var valueText by remember { mutableStateOf(item?.estimatedValue?.takeIf { it > 0 }?.toString() ?: "") }
    var location by remember { mutableStateOf(item?.location ?: "Ruang Sholat Utama") }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    var hasError by remember { mutableStateOf(false) }

    val categories = listOf("Elektronik", "Sound System", "Perlengkapan Ibadah", "Sarana Fisik", "Kebersihan")
    val units = listOf("Unit", "Buah", "Gulung", "Set", "Lembar")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Tambah Barang Inventaris" else "Edit Barang Inventaris") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Barang *") },
                        placeholder = { Text("Contoh: Karpet Sajadah Turki") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_name_input"),
                        isError = hasError && name.isBlank(),
                        singleLine = true
                    )
                }

                // Kategori
                item {
                    Text("Kategori Barang", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Kuantitas & Satuan
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { input -> if (input.all { it.isDigit() }) quantityText = input },
                            label = { Text("Kuantitas *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Satuan") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Kondisi
                item {
                    Text("Kondisi Barang", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ItemCondition.values().forEach { cond ->
                            FilterChip(
                                selected = condition == cond,
                                onClick = { condition = cond },
                                label = { Text(cond.name.replace("_", " "), fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Sumber Perolehan
                item {
                    Text("Sumber Perolehan", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ItemSource.values().forEach { src ->
                            FilterChip(
                                selected = source == src,
                                onClick = { source = src },
                                label = { Text(src.name.replace("_", " "), fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Lokasi Penempatan
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi Penempatan") },
                        placeholder = { Text("Contoh: Ruang Utama, Gudang") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Taksiran Nilai
                item {
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { input -> if (input.all { it.isDigit() }) valueText = input },
                        label = { Text("Taksiran Nilai (Rp Opsional)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Catatan
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan / Spesifikasi") },
                        placeholder = { Text("Keterangan perawatan atau nomor seri") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 1
                    val estVal = valueText.toLongOrNull() ?: 0L
                    if (name.isNotBlank() && qty > 0) {
                        onSave(name, category, qty, unit, condition, source, estVal, location, notes)
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_inventory_button")
            ) {
                Text("Simpan Barang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
