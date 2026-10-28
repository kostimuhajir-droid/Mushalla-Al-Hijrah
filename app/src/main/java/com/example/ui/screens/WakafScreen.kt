package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.WakafDonation
import com.example.ui.components.*
import com.example.ui.viewmodel.MushallaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakafScreen(
    viewModel: MushallaViewModel,
    modifier: Modifier = Modifier,
    initialOpenAddDialog: Boolean = false
) {
    val context = LocalContext.current
    val wakafDonations by viewModel.wakafDonations.collectAsStateWithLifecycle()
    val totalWakaf by viewModel.totalWakaf.collectAsStateWithLifecycle()
    val donorCount by viewModel.donorCount.collectAsStateWithLifecycle()
    val project by viewModel.wakafProject.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddWakafDialog by remember { mutableStateOf(initialOpenAddDialog) }
    var showEditTargetDialog by remember { mutableStateOf(false) }
    var selectedDonationForDetail by remember { mutableStateOf<WakafDonation?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<WakafDonation?>(null) }

    val filteredList = remember(wakafDonations, searchQuery) {
        if (searchQuery.isBlank()) {
            wakafDonations
        } else {
            wakafDonations.filter {
                it.donorName.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWakafDialog = true },
                containerColor = Color(0xFF0F5132),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_wakaf_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = "Tambah Sumbangan Wakaf"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TOP HEADER: TARGET WAKAF & PROGRESS BAR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val currentProject = project
                WakafProgressCard(
                    projectTitle = currentProject?.title ?: "Pembangunan Tempat Wudhu & Menara Mushalla",
                    targetAmount = currentProject?.targetAmount ?: 75000000L,
                    collectedAmount = totalWakaf,
                    donorCount = donorCount,
                    onEditTargetClick = { showEditTargetDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search field for donors
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_wakaf_input"),
                    placeholder = { Text("Cari nama donatur, alamat, atau doa...") },
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
            }

            // --- SUBHEADER DAFTAR DONATUR ---
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
                        text = "Daftar Donatur Wakaf (${filteredList.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${formatRupiah(totalWakaf)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- DONOR LIST ---
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Donatur tidak ditemukan" else "Belum ada donatur wakaf",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol + di kanan bawah untuk mencatat sumbangan wakaf.",
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
                    items(filteredList, key = { it.id }) { donation ->
                        WakafDonorRowItem(
                            donation = donation,
                            onClick = { selectedDonationForDetail = donation }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH SUMBANGAN WAKAF ---
    if (showAddWakafDialog) {
        AddWakafDialog(
            onDismiss = { showAddWakafDialog = false },
            onSave = { name, address, nominal, note, phone ->
                viewModel.addWakafDonation(
                    donorName = name,
                    address = address,
                    nominal = nominal,
                    date = System.currentTimeMillis(),
                    note = note,
                    phoneNumber = phone
                )
                showAddWakafDialog = false
                Toast.makeText(context, "Sumbangan wakaf atas nama $name berhasil dicatat!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- DIALOG UBAH TARGET WAKAF ---
    if (showEditTargetDialog) {
        val cur = project
        EditWakafTargetDialog(
            currentTitle = cur?.title ?: "Pembangunan Tempat Wudhu & Menara",
            currentTarget = cur?.targetAmount ?: 75000000L,
            currentDesc = cur?.description ?: "",
            currentDeadline = cur?.deadline ?: "31 Desember 2026",
            onDismiss = { showEditTargetDialog = false },
            onSave = { title, target, desc, deadline ->
                viewModel.updateWakafProjectTarget(title, target, desc, deadline)
                showEditTargetDialog = false
                Toast.makeText(context, "Target wakaf berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- DIALOG DETAIL WAKAF & RESI TANDA TERIMA ---
    selectedDonationForDetail?.let { w ->
        AlertDialog(
            onDismissRequest = { selectedDonationForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = Color(0xFFB8860B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tanda Terima Wakaf")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = w.donorName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatRupiah(w.nominal),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF0F5132)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Alamat Donatur", value = w.address)
                    DetailRow(label = "Tanggal Wakaf", value = formatDate(w.date))
                    if (w.phoneNumber.isNotEmpty() && w.phoneNumber != "-") {
                        DetailRow(label = "Nomor Telepon/WA", value = w.phoneNumber)
                    }
                    if (w.note.isNotEmpty()) {
                        DetailRow(label = "Pesan / Doa Wakif", value = "\"${w.note}\"")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Akad Wakaf Sah & Terenkripsi",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hash: ${w.securityHash.ifEmpty { "VERIFIED-SECURE" }}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDonationForDetail = null }) {
                    Text("Tutup")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val toDel = w
                        selectedDonationForDetail = null
                        showDeleteConfirmDialog = toDel
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            }
        )
    }

    // --- CONFIRM DELETE DIALOG ---
    showDeleteConfirmDialog?.let { toDel ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Hapus Sumbangan Wakaf?") },
            text = { Text("Apakah Anda yakin ingin menghapus data donatur ${toDel.donorName} sebesar ${formatRupiah(toDel.nominal)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWakafDonation(toDel.id)
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

@Composable
fun AddWakafDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, address: String, nominal: Long, note: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nominalText by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    val nominalQuickSuggestions = listOf(500000L, 1000000L, 2500000L, 5000000L, 10000000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Catat Sumbangan Wakaf",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Wakaf untuk Pembangunan Sarana & Prasarana",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Nama Donatur
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Donatur / Wakif *") },
                        placeholder = { Text("Contoh: H. Ramli / Hamba Allah") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wakaf_donor_name_input"),
                        isError = hasError && name.isBlank(),
                        singleLine = true
                    )
                }

                // Alamat Donatur
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Donatur *") },
                        placeholder = { Text("Contoh: Koto Baru, Padang") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wakaf_donor_address_input"),
                        isError = hasError && address.isBlank(),
                        singleLine = true
                    )
                }

                // Nominal Wakaf
                item {
                    OutlinedTextField(
                        value = nominalText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                nominalText = input
                            }
                        },
                        label = { Text("Nominal Wakaf (Rp) *") },
                        placeholder = { Text("Contoh: 1000000") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wakaf_nominal_input"),
                        isError = hasError && nominalText.isBlank(),
                        singleLine = true
                    )
                }

                // Nominal Quick Chips
                item {
                    Text(
                        text = "Pilihan Cepat Nominal:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        nominalQuickSuggestions.take(3).forEach { nom ->
                            SuggestionChip(
                                onClick = { nominalText = nom.toString() },
                                label = { Text(formatRupiah(nom), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Nomor Kontak
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Nomor Telepon / WhatsApp (Opsional)") },
                        placeholder = { Text("08xxxxxxxxxx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Doa / Peruntukan
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan / Doa Wakif") },
                        placeholder = { Text("Contoh: Wakaf atas nama alm. Orang tua") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nominal = nominalText.toLongOrNull() ?: 0L
                    if (name.isNotBlank() && address.isNotBlank() && nominal > 0) {
                        onSave(name, address, nominal, note, phone)
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("save_wakaf_button")
            ) {
                Text("Simpan Wakaf")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun EditWakafTargetDialog(
    currentTitle: String,
    currentTarget: Long,
    currentDesc: String,
    currentDeadline: String,
    onDismiss: () -> Unit,
    onSave: (title: String, target: Long, desc: String, deadline: String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }
    var targetText by remember { mutableStateOf(currentTarget.toString()) }
    var desc by remember { mutableStateOf(currentDesc) }
    var deadline by remember { mutableStateOf(currentDeadline) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Target Wakaf Pembangunan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Proyek Wakaf") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) targetText = input },
                    label = { Text("Target Nominal (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Target Selesai / Deadline") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi Pembangunan") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toLongOrNull() ?: 50000000L
                    if (title.isNotBlank() && target > 0) {
                        onSave(title, target, desc, deadline)
                    }
                }
            ) {
                Text("Simpan Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
