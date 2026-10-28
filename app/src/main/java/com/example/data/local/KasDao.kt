package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.KasTransaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface KasDao {
    @Query("SELECT * FROM kas_transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<KasTransaction>>

    @Query("SELECT * FROM kas_transactions WHERE type = :type ORDER BY date DESC, id DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<KasTransaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM kas_transactions WHERE type = 'MASUK'")
    fun getTotalMasuk(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM kas_transactions WHERE type = 'KELUAR'")
    fun getTotalKeluar(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KasTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: KasTransaction)

    @Query("DELETE FROM kas_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT COUNT(*) FROM kas_transactions")
    suspend fun getTransactionCount(): Int
}
