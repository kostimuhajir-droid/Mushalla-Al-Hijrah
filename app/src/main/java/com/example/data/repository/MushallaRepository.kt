package com.example.data.repository

import com.example.data.local.InventoryDao
import com.example.data.local.KasDao
import com.example.data.local.ProfileDao
import com.example.data.local.SecurityHelper
import com.example.data.local.WakafDao
import com.example.data.model.InventoryItem
import com.example.data.model.ItemCondition
import com.example.data.model.KasTransaction
import com.example.data.model.MushallaProfile
import com.example.data.model.TransactionType
import com.example.data.model.WakafDonation
import com.example.data.model.WakafProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class MushallaRepository(
    private val kasDao: KasDao,
    private val wakafDao: WakafDao,
    private val inventoryDao: InventoryDao,
    private val profileDao: ProfileDao
) {
    // --- KAS TRANSACTIONS ---
    val allTransactions: Flow<List<KasTransaction>> = kasDao.getAllTransactions()
    val totalKasMasuk: Flow<Long> = kasDao.getTotalMasuk()
    val totalKasKeluar: Flow<Long> = kasDao.getTotalKeluar()

    val totalSaldoKas: Flow<Long> = combine(totalKasMasuk, totalKasKeluar) { masuk, keluar ->
        masuk - keluar
    }

    suspend fun addTransaction(
        type: TransactionType,
        category: String,
        amount: Long,
        date: Long,
        description: String,
        recipientOrDonor: String,
        paymentMethod: String
    ): Long {
        val hash = SecurityHelper.generateIntegrityHash(
            type.name,
            category,
            amount.toString(),
            date.toString(),
            recipientOrDonor
        )
        val transaction = KasTransaction(
            type = type,
            category = category,
            amount = amount,
            date = date,
            description = description,
            recipientOrDonor = recipientOrDonor,
            paymentMethod = paymentMethod,
            securityHash = hash
        )
        return kasDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: KasTransaction) {
        val updatedHash = SecurityHelper.generateIntegrityHash(
            transaction.type.name,
            transaction.category,
            transaction.amount.toString(),
            transaction.date.toString(),
            transaction.recipientOrDonor
        )
        kasDao.updateTransaction(transaction.copy(securityHash = updatedHash))
    }

    suspend fun deleteTransaction(id: Long) {
        kasDao.deleteTransaction(id)
    }

    // --- WAKAF DONATIONS & PROJECT ---
    val allWakafDonations: Flow<List<WakafDonation>> = wakafDao.getAllDonations()
    val totalWakafCollected: Flow<Long> = wakafDao.getTotalWakafCollected()
    val donorCount: Flow<Int> = wakafDao.getDonorCount()
    val currentWakafProject: Flow<WakafProject?> = wakafDao.getProjectById(1)

    suspend fun addWakafDonation(
        donorName: String,
        address: String,
        nominal: Long,
        date: Long,
        projectId: Long = 1,
        note: String,
        phoneNumber: String
    ): Long {
        val hash = SecurityHelper.generateIntegrityHash(
            donorName,
            nominal.toString(),
            projectId.toString(),
            date.toString()
        )
        val donation = WakafDonation(
            donorName = donorName,
            address = address,
            nominal = nominal,
            date = date,
            projectId = projectId,
            note = note,
            phoneNumber = phoneNumber,
            securityHash = hash
        )
        return wakafDao.insertDonation(donation)
    }

    suspend fun deleteWakafDonation(id: Long) {
        wakafDao.deleteDonation(id)
    }

    suspend fun updateWakafProject(project: WakafProject) {
        wakafDao.insertOrUpdateProject(project)
    }

    // --- INVENTORY ---
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    val totalItemTypes: Flow<Int> = inventoryDao.getTotalItemTypes()
    val totalItemUnits: Flow<Int> = inventoryDao.getTotalItemUnits()
    val goodConditionUnits: Flow<Int> = inventoryDao.getGoodConditionUnits()
    val damagedUnits: Flow<Int> = inventoryDao.getDamagedUnits()

    suspend fun addInventoryItem(item: InventoryItem): Long {
        val hash = SecurityHelper.generateIntegrityHash(
            item.itemName,
            item.quantity.toString(),
            item.condition.name,
            item.category
        )
        return inventoryDao.insertItem(item.copy(securityHash = hash, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        val hash = SecurityHelper.generateIntegrityHash(
            item.itemName,
            item.quantity.toString(),
            item.condition.name,
            item.category
        )
        inventoryDao.updateItem(item.copy(securityHash = hash, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteInventoryItem(id: Long) {
        inventoryDao.deleteItem(id)
    }

    // --- MUSHALLA PROFILE ---
    val mushallaProfile: Flow<MushallaProfile?> = profileDao.getProfile()

    suspend fun updateProfile(profile: MushallaProfile) {
        profileDao.insertOrUpdateProfile(profile)
    }
}
