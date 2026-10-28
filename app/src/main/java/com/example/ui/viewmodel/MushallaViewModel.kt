package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.InventoryItem
import com.example.data.model.ItemCondition
import com.example.data.model.ItemSource
import com.example.data.model.KasTransaction
import com.example.data.model.MushallaProfile
import com.example.data.model.TransactionType
import com.example.data.model.WakafDonation
import com.example.data.model.WakafProject
import com.example.data.repository.MushallaRepository
import com.example.ui.export.ExportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MushallaViewModel(
    private val repository: MushallaRepository
) : ViewModel() {

    // --- KAS STATE ---
    val transactions: StateFlow<List<KasTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMasuk: StateFlow<Long> = repository.totalKasMasuk
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalKeluar: StateFlow<Long> = repository.totalKasKeluar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSaldo: StateFlow<Long> = repository.totalSaldoKas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // --- WAKAF STATE ---
    val wakafDonations: StateFlow<List<WakafDonation>> = repository.allWakafDonations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWakaf: StateFlow<Long> = repository.totalWakafCollected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val donorCount: StateFlow<Int> = repository.donorCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wakafProject: StateFlow<WakafProject?> = repository.currentWakafProject
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- INVENTARIS STATE ---
    val inventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalItemTypes: StateFlow<Int> = repository.totalItemTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalItemUnits: StateFlow<Int> = repository.totalItemUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goodConditionUnits: StateFlow<Int> = repository.goodConditionUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val damagedUnits: StateFlow<Int> = repository.damagedUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- PROFILE STATE ---
    val profile: StateFlow<MushallaProfile?> = repository.mushallaProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Message / Toast feedback state
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- KAS ACTIONS ---
    fun addKasTransaction(
        type: TransactionType,
        category: String,
        amount: Long,
        date: Long,
        description: String,
        recipientOrDonor: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                type = type,
                category = category,
                amount = amount,
                date = date,
                description = description,
                recipientOrDonor = recipientOrDonor,
                paymentMethod = paymentMethod
            )
            _userMessage.value = "Transaksi kas berhasil disimpan dengan verifikasi enkripsi!"
        }
    }

    fun updateKasTransaction(transaction: KasTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            _userMessage.value = "Transaksi kas berhasil diperbarui."
        }
    }

    fun deleteKasTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _userMessage.value = "Transaksi kas berhasil dihapus."
        }
    }

    // --- WAKAF ACTIONS ---
    fun addWakafDonation(
        donorName: String,
        address: String,
        nominal: Long,
        date: Long,
        note: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            repository.addWakafDonation(
                donorName = donorName,
                address = address,
                nominal = nominal,
                date = date,
                note = note,
                phoneNumber = phoneNumber
            )
            _userMessage.value = "Sumbangan wakaf atas nama $donorName berhasil dicatat!"
        }
    }

    fun deleteWakafDonation(id: Long) {
        viewModelScope.launch {
            repository.deleteWakafDonation(id)
            _userMessage.value = "Data sumbangan wakaf dihapus."
        }
    }

    fun updateWakafProjectTarget(
        title: String,
        targetAmount: Long,
        description: String,
        deadline: String
    ) {
        viewModelScope.launch {
            val current = wakafProject.value ?: WakafProject(id = 1, title = title, targetAmount = targetAmount, description = description)
            repository.updateWakafProject(
                current.copy(
                    title = title,
                    targetAmount = targetAmount,
                    description = description,
                    deadline = deadline
                )
            )
            _userMessage.value = "Target wakaf berhasil diperbarui!"
        }
    }

    // --- INVENTARIS ACTIONS ---
    fun addInventoryItem(
        itemName: String,
        category: String,
        quantity: Int,
        unit: String,
        condition: ItemCondition,
        source: ItemSource,
        estimatedValue: Long,
        location: String,
        notes: String
    ) {
        viewModelScope.launch {
            val item = InventoryItem(
                itemName = itemName,
                category = category,
                quantity = quantity,
                unit = unit,
                condition = condition,
                source = source,
                estimatedValue = estimatedValue,
                location = location,
                notes = notes
            )
            repository.addInventoryItem(item)
            _userMessage.value = "Barang inventaris $itemName berhasil ditambahkan!"
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
            _userMessage.value = "Data inventaris diperbarui."
        }
    }

    fun deleteInventoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteInventoryItem(id)
            _userMessage.value = "Barang inventaris dihapus."
        }
    }

    // --- PROFILE ACTIONS ---
    fun updateProfile(updated: MushallaProfile) {
        viewModelScope.launch {
            repository.updateProfile(updated)
            _userMessage.value = "Profil mushalla dan rekening berhasil diperbarui!"
        }
    }

    // --- EXPORT ACTIONS ---
    fun exportToPdf(context: Context) {
        viewModelScope.launch {
            val prof = profile.value ?: MushallaProfile()
            val pdfFile = ExportHelper.generatePdfReport(
                context = context,
                profile = prof,
                transactions = transactions.value,
                wakafDonations = wakafDonations.value,
                wakafProject = wakafProject.value,
                inventoryList = inventoryItems.value,
                totalMasuk = totalMasuk.value,
                totalKeluar = totalKeluar.value,
                saldoKas = totalSaldo.value,
                totalWakaf = totalWakaf.value
            )
            if (pdfFile != null) {
                ExportHelper.shareFile(
                    context,
                    pdfFile,
                    "application/pdf",
                    "Laporan Keuangan & Wakaf ${prof.name}"
                )
            } else {
                _userMessage.value = "Gagal membuat dokumen PDF laporan."
            }
        }
    }

    fun exportToExcelCsv(context: Context) {
        viewModelScope.launch {
            val prof = profile.value ?: MushallaProfile()
            val csvFile = ExportHelper.generateExcelCsv(
                context = context,
                profile = prof,
                transactions = transactions.value,
                wakafDonations = wakafDonations.value,
                inventoryList = inventoryItems.value
            )
            if (csvFile != null) {
                ExportHelper.shareFile(
                    context,
                    csvFile,
                    "text/csv",
                    "Laporan Excel ${prof.name}"
                )
            } else {
                _userMessage.value = "Gagal membuat file Excel/CSV laporan."
            }
        }
    }
}

class MushallaViewModelFactory(
    private val repository: MushallaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MushallaViewModel::class.java)) {
            return MushallaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
