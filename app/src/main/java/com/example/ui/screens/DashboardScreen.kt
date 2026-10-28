package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.KasTransaction
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.theme.KasKeluarRed
import com.example.ui.theme.KasMasukGreen
import com.example.ui.viewmodel.MushallaViewModel

@Composable
fun DashboardScreen(
    viewModel: MushallaViewModel,
    onNavigateToKas: () -> Unit,
    onNavigateToWakaf: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onQuickAddKasMasuk: () -> Unit,
    onQuickAddKasKeluar: () -> Unit,
    onQuickAddWakaf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val saldoKas by viewModel.totalSaldo.collectAsStateWithLifecycle()
    val totalMasuk by viewModel.totalMasuk.collectAsStateWithLifecycle()
    val totalKeluar by viewModel.totalKeluar.collectAsStateWithLifecycle()

    val wakafProject by viewModel.wakafProject.collectAsStateWithLifecycle()
    val totalWakaf by viewModel.totalWakaf.collectAsStateWithLifecycle()
    val donorCount by viewModel.donorCount.collectAsStateWithLifecycle()

    val totalItemTypes by viewModel.totalItemTypes.collectAsStateWithLifecycle()
    val totalItemUnits by viewModel.totalItemUnits.collectAsStateWithLifecycle()
    val goodUnits by viewModel.goodConditionUnits.collectAsStateWithLifecycle()
    val damagedUnits by viewModel.damagedUnits.collectAsStateWithLifecycle()

    val recentTransactions by viewModel.transactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HERO HEADER MUSHALLA & PROFILE SHORTCUT ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
                    .testTag("mushalla_profile_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0A3622), Color(0xFF0F5132), Color(0xFF198754))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFFF6C561), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = "Logo Mushalla",
                                    tint = Color(0xFF0A3622),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.name ?: "Mushalla Ar-Rahman",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = profile?.address ?: "Jl. Khatib Sulaiman No. 45, Padang",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Enkripsi",
                                        tint = Color(0xFFFBF0B9),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Keuangan Terenkripsi SHA-256",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFFFBF0B9)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Edit Profil",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. RINGKASAN SALDO KAS UTAMA ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kas_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL SALDO KAS MUSHALLA",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFD1E7DD)
                        ) {
                            Text(
                                text = "Real-Time",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F5132)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatRupiah(saldoKas),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Kas Masuk
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFD1E7DD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Kas Masuk",
                                    tint = KasMasukGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kas Masuk",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatRupiah(totalMasuk),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = KasMasukGreen
                                )
                            }
                        }

                        // Kas Keluar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFF8D7DA), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Kas Keluar",
                                    tint = KasKeluarRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kas Keluar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatRupiah(totalKeluar),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = KasKeluarRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. QUICK ACTIONS (CATAT CEPAT) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aksi Cepat Manajemen",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickActionChip(
                            label = "Kas Masuk",
                            icon = Icons.Default.AddCircle,
                            bgColor = Color(0xFFD1E7DD),
                            iconColor = Color(0xFF0F5132),
                            onClick = onQuickAddKasMasuk
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Kas Keluar",
                            icon = Icons.Default.RemoveCircle,
                            bgColor = Color(0xFFF8D7DA),
                            iconColor = KasKeluarRed,
                            onClick = onQuickAddKasKeluar
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Donasi Wakaf",
                            icon = Icons.Default.VolunteerActivism,
                            bgColor = Color(0xFFFFF3CD),
                            iconColor = Color(0xFFB8860B),
                            onClick = onQuickAddWakaf
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Ekspor Laporan",
                            icon = Icons.Default.PictureAsPdf,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToReports
                        )
                    }
                }
            }
        }

        // --- 4. TARGET & STATISTIK WAKAF PEMBANGUNAN ---
        item {
            val proj = wakafProject
            WakafProgressCard(
                projectTitle = proj?.title ?: "Pembangunan Sarana & Prasarana Mushalla",
                targetAmount = proj?.targetAmount ?: 75000000L,
                collectedAmount = totalWakaf,
                donorCount = donorCount,
                modifier = Modifier.clickable { onNavigateToWakaf() },
                onEditTargetClick = onNavigateToWakaf
            )
        }

        // --- 5. STATUS INVENTARIS REAL-TIME ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToInventory() }
                    .testTag("inventory_summary_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFE0F2FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = "Inventaris",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Status Inventaris Sarana Mushalla",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$totalItemTypes Jenis Barang Terdata",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Buka Inventaris",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Unit
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Total Fisik",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalItemUnits Unit",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Kondisi Baik
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFD1E7DD)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Kondisi Baik",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF0F5132)
                                )
                                Text(
                                    text = "$goodUnits Unit",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F5132)
                                )
                            }
                        }

                        // Perlu Servis / Rusak
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (damagedUnits > 0) Color(0xFFFFF3CD) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Perlu Servis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (damagedUnits > 0) Color(0xFF856404) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$damagedUnits Unit",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (damagedUnits > 0) Color(0xFF856404) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. REKENING RESMI MUSHALLA (NAGARI & BSI) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Nomor Rekening Donasi & Wakaf",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                val prof = profile ?: com.example.data.model.MushallaProfile()

                // Bank Nagari Syariah (71000201021831)
                BankRekeningCard(
                    bankName = prof.bankNagariName,
                    accountNumber = prof.bankNagariAccount,
                    accountHolder = prof.name,
                    gradientColors = listOf(Color(0xFF0A3622), Color(0xFF13795B))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bank Syariah Indonesia / BSI (7167978112)
                BankRekeningCard(
                    bankName = prof.bankBsiName,
                    accountNumber = prof.bankBsiAccount,
                    accountHolder = prof.name,
                    gradientColors = listOf(Color(0xFF005C8A), Color(0xFF00A39E))
                )
            }
        }

        // --- 7. TRANSAKSI TERAKHIR ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Kas Terbaru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToKas) {
                    Text("Lihat Semua")
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Belum ada transaksi kas tercatat.")
                    }
                }
            }
        } else {
            items(recentTransactions.take(5)) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    onClick = onNavigateToKas
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("quick_action_${label.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = iconColor
            )
        }
    }
}
