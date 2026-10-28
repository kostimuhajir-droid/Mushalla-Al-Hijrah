package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    MASUK,
    KELUAR
}

@Entity(tableName = "kas_transactions")
data class KasTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val category: String,
    val amount: Long,
    val date: Long, // timestamp in ms
    val description: String,
    val recipientOrDonor: String, // Nama Donatur atau Penerima Dana
    val paymentMethod: String = "Tunai", // Tunai, Transfer Bank Nagari Syariah, Transfer BSI, QRIS
    val securityHash: String = "", // Enkripsi SHA-256 integrity hash for anti-tampering
    val createdAt: Long = System.currentTimeMillis()
)
