package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wakaf_donations")
data class WakafDonation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val donorName: String, // Nama Donatur
    val address: String, // Alamat Donatur
    val nominal: Long, // Nominal Wakaf
    val date: Long, // Tanggal Wakaf
    val projectId: Long = 1, // Target proyek wakaf
    val note: String = "", // Doa atau peruntukan khusus
    val phoneNumber: String = "",
    val securityHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wakaf_projects")
data class WakafProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1,
    val title: String, // Judul Proyek Wakaf (misal: "Pembangunan Tempat Wudhu & Menara")
    val targetAmount: Long, // Target Nominal (misal: 100.000.000)
    val description: String,
    val deadline: String = "",
    val isActive: Boolean = true
)
