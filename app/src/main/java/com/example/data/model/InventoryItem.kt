package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemCondition {
    BAIK,
    RUSAK_RINGAN,
    RUSAK_BERAT
}

enum class ItemSource {
    WAKAF,
    HIBAH,
    BELI_KAS
}

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val category: String, // Elektronik, Sound System, Perlengkapan Ibadah, Sarana Fisik, Kebersihan
    val quantity: Int,
    val unit: String = "Unit", // Unit, Buah, Gulung, Set, Lembar
    val condition: ItemCondition = ItemCondition.BAIK,
    val acquisitionDate: Long = System.currentTimeMillis(),
    val source: ItemSource = ItemSource.BELI_KAS,
    val estimatedValue: Long = 0L,
    val location: String = "Ruang Utama", // Ruang Utama, Gudang, Mihrab, Tempat Wudhu, Kamar Marbot
    val notes: String = "",
    val securityHash: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
