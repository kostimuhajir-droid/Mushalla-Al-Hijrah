package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MushallaProfile
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.theme.KasKeluarRed
import com.example.ui.theme.KasMasukGreen
import com.example.ui.viewmodel.MushallaViewModel

@Composable
fun ReportsScreen(
    viewModel: MushallaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val totalMasuk by viewModel.totalMasuk.collectAsStateWithLifecycle()
    val totalKeluar by viewModel.totalKeluar.collectAsStateWithLifecycle()
    val saldoKas by viewModel.totalSaldo.collectAsStateWithLifecycle()

    val wakafDonations by viewModel.wakafDonations.collectAsStateWithLifecycle()
    val totalWakaf by viewModel.totalWakaf.collectAsStateWithLifecycle()
    val wakafProject by viewModel.wakafProject.collectAsStateWithLifecycle()

    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val totalTypes by viewModel.totalItemTypes.collectAsStateWithLifecycle()
    val totalUnits by viewModel.totalItemUnits.collectAsStateWithLifecycle()

    var selectedReportTab by remember { mutableStateOf(0) } // 0: Ringkasan & Transparansi, 1: Buku Kas, 2: Wakaf, 3: Inventaris

    val prof = profile ?: MushallaProfile()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEADER EKSPOR DOKUMEN ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_actions_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFD1E7DD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0F5132),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Laporan Transparan & Ekspor",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ekspor ke PDF Resmi atau Excel (CSV)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Data keuangan, wakaf pembangunan, dan inventaris dapat diekspor langsung ke dokumen PDF siap cetak dengan kop surat resmi mushalla atau format Excel untuk audit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportToPdf(context) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5132)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor PDF")
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportToExcelCsv(context) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor Excel")
                        }
                    }
                }
            }
        }

        // --- 2. INFORMASI REKENING DONASI RESMI & QRIS ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Rekening Resmi Penerimaan Kas & Donasi",
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

                Spacer(modifier = Modifier.height(10.dp))

                // QRIS Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QRIS",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "QRIS Standar Pembayaran Nasional",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${prof.qrisCode} • Mendukung BCA, Mandiri, BSI, Nagari, GoPay, OVO, ShopeePay",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 3. TAB RINCIAN LAPORAN TRANSPARAN ---
        item {
            TabRow(
                selectedTabIndex = selectedReportTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedReportTab == 0,
                    onClick = { selectedReportTab = 0 },
                    text = { Text("Ringkasan", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedReportTab == 1,
                    onClick = { selectedReportTab = 1 },
                    text = { Text("Kas (${transactions.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedReportTab == 2,
                    onClick = { selectedReportTab = 2 },
                    text = { Text("Wakaf (${wakafDonations.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedReportTab == 3,
                    onClick = { selectedReportTab = 3 },
                    text = { Text("Inventaris", fontSize = 12.sp) }
                )
            }
        }

        // --- 4. KONTEN TAB LAPORAN ---
        when (selectedReportTab) {
            0 -> {
                // Ringkasan Transparansi
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Pertanggungjawaban Keuangan Publik",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ReportSummaryRow(label = "Total Kas Masuk Terkumpul", value = formatRupiah(totalMasuk), color = KasMasukGreen)
                            ReportSummaryRow(label = "Total Pengeluaran Kas", value = formatRupiah(totalKeluar), color = KasKeluarRed)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            ReportSummaryRow(label = "Saldo Akhir Kas Mushalla", value = formatRupiah(saldoKas), color = Color(0xFF0F5132), isBold = true)
                            Spacer(modifier = Modifier.height(10.dp))
                            ReportSummaryRow(label = "Total Dana Wakaf Terhimpun", value = formatRupiah(totalWakaf), color = Color(0xFFB8860B), isBold = true)
                            val targetWakaf = wakafProject?.targetAmount ?: 1L
                            val pct = ((totalWakaf.toFloat() / targetWakaf.toFloat()) * 100).toInt()
                            ReportSummaryRow(label = "Target Pembangunan Tercapai", value = "$pct%", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            ReportSummaryRow(label = "Total Aset Inventaris", value = "$totalTypes Jenis ($totalUnits Unit)", color = MaterialTheme.colorScheme.onSurface)

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pengurus: ${prof.leaderName} (Ketua) & ${prof.treasurerName} (Bendahara)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Buku Kas
                items(transactions) { tx ->
                    TransactionRowItem(transaction = tx, onClick = {})
                }
            }

            2 -> {
                // Wakaf
                items(wakafDonations) { donation ->
                    WakafDonorRowItem(donation = donation, onClick = {})
                }
            }

            3 -> {
                // Inventaris
                items(inventoryItems) { item ->
                    InventoryRowItem(item = item, onClick = {})
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportSummaryRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
