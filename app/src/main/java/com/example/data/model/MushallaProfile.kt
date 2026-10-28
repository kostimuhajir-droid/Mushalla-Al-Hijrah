package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mushalla_profile")
data class MushallaProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Mushalla Ar-Rahman",
    val address: String = "Jl. Khatib Sulaiman No. 45",
    val city: String = "Padang, Sumatera Barat",
    val phone: String = "+62 812-3456-7890",
    val leaderName: String = "H. Syahril Efendi",
    val treasurerName: String = "Ahmad Fauzi, S.E.",
    val bankNagariAccount: String = "71000201021831",
    val bankNagariName: String = "Bank Nagari Syariah",
    val bankBsiAccount: String = "7167978112",
    val bankBsiName: String = "Bank Syariah Indonesia",
    val qrisCode: String = "ID10200304050607",
    val avatarId: Int = 0 // index for mosque icon/avatar
)
