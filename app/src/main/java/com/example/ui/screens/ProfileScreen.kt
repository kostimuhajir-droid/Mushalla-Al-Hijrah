package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MushallaProfile
import com.example.ui.components.BankRekeningCard
import com.example.ui.components.DetailRow
import com.example.ui.components.SecurityBadge
import com.example.ui.viewmodel.MushallaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MushallaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val prof = profile ?: MushallaProfile()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROFIL CARD HEADER DENGAN AVATAR & TOMBOL EDIT ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFD1E7DD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "Foto Profil Mushalla",
                            tint = Color(0xFF0F5132),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = prof.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${prof.address}, ${prof.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kontak Pengurus: ${prof.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SecurityBadge()

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profil & Rekening Mushalla")
                    }
                }
            }
        }

        // --- 2. INFORMASI STRUKTUR PENGURUS TAKMIR ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Struktur Pengurus & Bendahara",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Ketua Pengurus / Takmir", value = prof.leaderName)
                    DetailRow(label = "Bendahara Mushalla", value = prof.treasurerName)
                    DetailRow(label = "Status Verifikasi", value = "Resmi Terdaftar")
                    DetailRow(label = "Nomor Rekening Nagari", value = prof.bankNagariAccount)
                    DetailRow(label = "Nomor Rekening BSI", value = prof.bankBsiAccount)
                }
            }
        }

        // --- 3. REKENING RESMI MUSHALLA ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pengaturan Rekening Donasi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                BankRekeningCard(
                    bankName = prof.bankNagariName,
                    accountNumber = prof.bankNagariAccount,
                    accountHolder = prof.name,
                    gradientColors = listOf(Color(0xFF0A3622), Color(0xFF13795B))
                )

                Spacer(modifier = Modifier.height(10.dp))

                BankRekeningCard(
                    bankName = prof.bankBsiName,
                    accountNumber = prof.bankBsiAccount,
                    accountHolder = prof.name,
                    gradientColors = listOf(Color(0xFF005C8A), Color(0xFF00A39E))
                )
            }
        }

        // --- 4. KEAMANAN & ENKRIPSI DATA ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Keamanan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Keamanan & Enkripsi Data",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aplikasi ini dilengkapi enkripsi SHA-256 integrity hash anti-tamper untuk menjamin bahwa seluruh catatan kas, wakaf, dan inventaris tidak dapat dimanipulasi di luar sistem resmi mushalla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFD1E7DD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF0F5132),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Status Enkripsi: Aktif & Terlindungi (SHA-256)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F5132)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // --- DIALOG EDIT PROFIL & REKENING ---
    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = prof,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updateProfile(updated)
                showEditProfileDialog = false
                Toast.makeText(context, "Profil & Rekening Mushalla berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    profile: MushallaProfile,
    onDismiss: () -> Unit,
    onSave: (MushallaProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var address by remember { mutableStateOf(profile.address) }
    var city by remember { mutableStateOf(profile.city) }
    var phone by remember { mutableStateOf(profile.phone) }
    var leaderName by remember { mutableStateOf(profile.leaderName) }
    var treasurerName by remember { mutableStateOf(profile.treasurerName) }
    var bankNagariAccount by remember { mutableStateOf(profile.bankNagariAccount) }
    var bankBsiAccount by remember { mutableStateOf(profile.bankBsiAccount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profil & Rekening Mushalla") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Mushalla") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Kota / Kabupaten") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Kontak / WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = leaderName,
                        onValueChange = { leaderName = it },
                        label = { Text("Nama Ketua Pengurus") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = treasurerName,
                        onValueChange = { treasurerName = it },
                        label = { Text("Nama Bendahara") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bankNagariAccount,
                        onValueChange = { bankNagariAccount = it },
                        label = { Text("No. Rek Bank Nagari Syariah") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bankBsiAccount,
                        onValueChange = { bankBsiAccount = it },
                        label = { Text("No. Rek Bank Syariah Indonesia (BSI)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            profile.copy(
                                name = name,
                                address = address,
                                city = city,
                                phone = phone,
                                leaderName = leaderName,
                                treasurerName = treasurerName,
                                bankNagariAccount = bankNagariAccount,
                                bankBsiAccount = bankBsiAccount
                            )
                        )
                    }
                }
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
