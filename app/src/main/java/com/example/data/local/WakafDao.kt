package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WakafDonation
import com.example.data.model.WakafProject
import kotlinx.coroutines.flow.Flow

@Dao
interface WakafDao {
    @Query("SELECT * FROM wakaf_donations ORDER BY date DESC, id DESC")
    fun getAllDonations(): Flow<List<WakafDonation>>

    @Query("SELECT * FROM wakaf_donations WHERE projectId = :projectId ORDER BY date DESC")
    fun getDonationsByProject(projectId: Long): Flow<List<WakafDonation>>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM wakaf_donations")
    fun getTotalWakafCollected(): Flow<Long>

    @Query("SELECT COUNT(*) FROM wakaf_donations")
    fun getDonorCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: WakafDonation): Long

    @Query("DELETE FROM wakaf_donations WHERE id = :id")
    suspend fun deleteDonation(id: Long)

    // Wakaf Project
    @Query("SELECT * FROM wakaf_projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Long = 1): Flow<WakafProject?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProject(project: WakafProject)
}
