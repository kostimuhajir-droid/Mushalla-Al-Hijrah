package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.KasTransaction
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.theme.KasKeluarRed
import com.example.ui.theme.KasMasukGreen
import com.example.ui.viewmodel.MushallaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(
    viewModel: MushallaViewModel,
    modifier: Modifier = Modifier,
    initialOpenAddDialog: Boolean = false,
    initialTransactionType: TransactionType = TransactionType.MASUK
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val totalMasuk by viewModel.totalMasuk.collectAsStateWithLifecycle()
    val totalKeluar by viewModel.totalKeluar.collectAsStateWithLifecycle()
    val saldoKas by viewModel.totalSaldo.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Semua, 1: Masuk, 2: Keluar
    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(initialOpenAddDialog) }
    var selectedTypeForAdd by remember { mutableStateOf(initialTransactionType) }
    var selectedTransactionForDetail by remember { mutableStateOf<KasTransaction?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<KasTransaction?>(null) }

    val filteredList = remember(transactions, selectedTab, searchQuery) {
        transactions.filter { tx ->
            val matchTab = when (selectedTab) {
                1 -> tx.type == TransactionType.MASUK
                2 -> tx.type == TransactionType.KELUAR
                else -> true
            }
            val matchQuery = searchQuery.isBlank() ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.description.contains(searchQuery, ignoreCase = true) ||
                tx.recipientOrDonor.contains(searchQuery, ignoreCase = true) ||
                tx.paymentMethod.contains(searchQuery, ignoreCase = true)

            matchTab && matchQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTypeForAdd = if (selectedTab == 2) TransactionType.KELUAR else TransactionType.MASUK
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_kas_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Catat Kas Baru"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TOP SEARCH & TABS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_kas_input"),
                    placeholder = { Text("Cari kategori, donatur, atau keterangan...") },
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

                // Tabs: Semua, Masuk, Keluar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Semua (${transactions.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Masuk",
                                color = if (selectedTab == 1) KasMasukGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Keluar",
                                color = if (selectedTab == 2) KasKeluarRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // --- SALDO BAR ---
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SecurityBadge()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${filteredList.size} Transaksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Saldo: ${formatRupiah(saldoKas)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- LIST OF TRANSACTIONS ---
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Tidak ada transaksi yang cocok" else "Belum ada transaksi kas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol + di pojok kanan bawah untuk mencatat kas masuk atau keluar.",
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
                    items(filteredList, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onClick = { selectedTransactionForDetail = tx }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH TRANSAKSI KAS ---
    if (showAddDialog) {
        AddKasTransactionDialog(
            initialType = selectedTypeForAdd,
            onDismiss = { showAddDialog = false },
            onSave = { type, category, amount, donorOrRecipient, method, desc ->
                viewModel.addKasTransaction(
                    type = type,
                    category = category,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    description = desc,
                    recipientOrDonor = donorOrRecipient,
                    paymentMethod = method
                )
                showAddDialog = false
                Toast.makeText(context, "Kas ${type.name} berhasil dicatat & terenkripsi", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- DIALOG DETAIL TRANSAKSI & VERIFIKASI ENKRIPSI ---
    selectedTransactionForDetail?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTransactionForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (tx.type == TransactionType.MASUK) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.type == TransactionType.MASUK) KasMasukGreen else KasKeluarRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Detail Kas ${tx.type.name}")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = tx.category,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatRupiah(tx.amount),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (tx.type == TransactionType.MASUK) KasMasukGreen else KasKeluarRed
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Tanggal", value = formatDate(tx.date))
                    DetailRow(
                        label = if (tx.type == TransactionType.MASUK) "Penyetor / Donatur" else "Penerima Dana",
                        value = tx.recipientOrDonor
                    )
                    DetailRow(label = "Metode", value = tx.paymentMethod)
                    if (tx.description.isNotEmpty()) {
                        DetailRow(label = "Keterangan", value = tx.description)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Audit Trail",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Anti-Tamper Cryptographic Hash",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SHA-256: ${tx.securityHash}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTransactionForDetail = null }) {
                    Text("Tutup")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val toDelete = tx
                        selectedTransactionForDetail = null
                        showDeleteConfirmDialog = toDelete
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            }
        )
    }

    // --- CONFIRM DELETE DIALOG ---
    showDeleteConfirmDialog?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Hapus Transaksi Kas?") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan kas ${toDelete.category} sebesar ${formatRupiah(toDelete.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteKasTransaction(toDelete.id)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKasTransactionDialog(
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (type: TransactionType, category: String, amount: Long, donor: String, method: String, desc: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var recipientOrDonor by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Tunai") }
    var description by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    val defaultCategoriesMasuk = listOf(
        "Infaq Sholat Jumat",
        "Kotak Amal Tromol Harian",
        "Donasi Operasional",
        "Infaq Kajian & Subuh",
        "Sumbangan Hamba Allah"
    )

    val defaultCategoriesKeluar = listOf(
        "Listrik & Air (PLN & PDAM)",
        "Honor Marbot & Imam",
        "Kebersihan & Perlengkapan",
        "Pemeliharaan Sarana & Alat",
        "Konsumsi Acara / Kajian",
        "Sound System & Kelistrikan"
    )

    val methods = listOf("Tunai", "Transfer Bank Nagari Syariah", "Transfer BSI", "QRIS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Catat Transaksi Kas",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tipe Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == TransactionType.MASUK,
                            onClick = {
                                type = TransactionType.MASUK
                                category = ""
                            },
                            label = { Text("Kas Masuk (+)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD1E7DD),
                                selectedLabelColor = Color(0xFF0F5132)
                            )
                        )
                        FilterChip(
                            selected = type == TransactionType.KELUAR,
                            onClick = {
                                type = TransactionType.KELUAR
                                category = ""
                            },
                            label = { Text("Kas Keluar (-)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF8D7DA),
                                selectedLabelColor = KasKeluarRed
                            )
                        )
                    }
                }

                // Nominal Input
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                amountText = input
                                hasError = false
                            }
                        },
                        label = { Text("Nominal (Rp) *") },
                        placeholder = { Text("Contoh: 500000") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kas_amount_input"),
                        isError = hasError && amountText.isBlank(),
                        singleLine = true
                    )
                }

                // Kategori Rekomendasi
                item {
                    Text(
                        text = "Pilih / Ketik Kategori *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val suggestions = if (type == TransactionType.MASUK) defaultCategoriesMasuk else defaultCategoriesKeluar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            suggestions.take(2).forEach { sug ->
                                SuggestionChip(
                                    onClick = { category = sug },
                                    label = { Text(sug, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori Transaksi") },
                        placeholder = { Text("Nama kategori") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kas_category_input"),
                        singleLine = true
                    )
                }

                // Donatur / Penerima Dana
                item {
                    OutlinedTextField(
                        value = recipientOrDonor,
                        onValueChange = { recipientOrDonor = it },
                        label = { Text(if (type == TransactionType.MASUK) "Nama Penyetor / Donatur" else "Nama Penerima Dana") },
                        placeholder = { Text(if (type == TransactionType.MASUK) "Contoh: Jamaah Jumat" else "Contoh: Petugas PLN / Marbot") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Metode Pembayaran
                item {
                    Text(
                        text = "Metode Pembayaran",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        methods.forEach { m ->
                            FilterChip(
                                selected = paymentMethod == m,
                                onClick = { paymentMethod = m },
                                label = { Text(m.replace("Transfer ", ""), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Keterangan
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Keterangan Tambahan") },
                        placeholder = { Text("Catatan / perincian penggunaan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount > 0 && category.isNotBlank()) {
                        val donorFinal = recipientOrDonor.ifBlank {
                            if (type == TransactionType.MASUK) "Hamba Allah" else "Mushalla"
                        }
                        onSave(type, category, amount, donorFinal, paymentMethod, description)
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_kas_button")
            ) {
                Text("Simpan & Enkripsi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

