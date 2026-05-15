package com.raktavahini.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.raktavahini.data.entities.DonationLogEntity
import com.raktavahini.data.entities.DonorEntity

@Dao
interface RaktaVahiniDao {

    // ── Donor CRUD ────────────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity): Long

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Delete
    suspend fun deleteDonor(donor: DonorEntity)

    @Query("SELECT * FROM donors ORDER BY name ASC")
    fun getAllDonors(): LiveData<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE donorId = :id LIMIT 1")
    suspend fun getDonorById(id: Long): DonorEntity?

    @Query("SELECT * FROM donors WHERE donorId = :id LIMIT 1")
    fun getDonorByIdLive(id: Long): LiveData<DonorEntity?>

    // ── My profile (first registered donor treated as "me") ───────────────────
    @Query("SELECT * FROM donors ORDER BY registeredAt ASC LIMIT 1")
    fun getMyProfile(): LiveData<DonorEntity?>

    @Query("SELECT * FROM donors ORDER BY registeredAt ASC LIMIT 1")
    suspend fun getMyProfileOnce(): DonorEntity?

    // ── Emergency search — filtered by blood group + eligibility ──────────────
    // Returns donors whose blood group is compatible AND who are eligible:
    //   - lastDonationDate = 0 (never donated) OR
    //   - (currentTime - lastDonationDate) > 90 days AND
    //   - isManuallyEligible = 1
    // The 90-day check is done in Kotlin after fetch for simplicity
    @Query("""
        SELECT * FROM donors
        WHERE bloodGroup IN (:compatibleGroups)
        AND isManuallyEligible = 1
        ORDER BY name ASC
    """)
    suspend fun searchEligibleDonors(compatibleGroups: List<String>): List<DonorEntity>

    // Search by city/pincode text
    @Query("""
        SELECT * FROM donors
        WHERE bloodGroup IN (:compatibleGroups)
        AND isManuallyEligible = 1
        AND (city LIKE '%' || :location || '%' OR pincode LIKE '%' || :location || '%')
        ORDER BY name ASC
    """)
    suspend fun searchEligibleDonorsByLocation(
        compatibleGroups: List<String>,
        location: String
    ): List<DonorEntity>

    // All donors for admin/browse view
    @Query("SELECT * FROM donors WHERE bloodGroup = :group ORDER BY name ASC")
    fun getDonorsByGroup(group: String): LiveData<List<DonorEntity>>

    // Update last donation date + toggle
    @Query("UPDATE donors SET lastDonationDate = :dateMs WHERE donorId = :id")
    suspend fun updateLastDonationDate(id: Long, dateMs: Long)

    @Query("UPDATE donors SET isManuallyEligible = :eligible WHERE donorId = :id")
    suspend fun updateEligibilityToggle(id: Long, eligible: Boolean)

    // Count
    @Query("SELECT COUNT(*) FROM donors")
    fun getTotalDonorCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM donors WHERE bloodGroup = :group AND isManuallyEligible = 1")
    fun getEligibleCountForGroup(group: String): LiveData<Int>

    // ── Donation log ──────────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationLog(log: DonationLogEntity): Long

    @Delete
    suspend fun deleteDonationLog(log: DonationLogEntity)

    @Query("SELECT * FROM donation_log WHERE donorId = :donorId ORDER BY donationDate DESC")
    fun getDonationLogForDonor(donorId: Long): LiveData<List<DonationLogEntity>>

    @Query("SELECT COUNT(*) FROM donation_log WHERE donorId = :donorId")
    fun getTotalDonationsForDonor(donorId: Long): LiveData<Int>

    @Query("SELECT * FROM donation_log ORDER BY donationDate DESC")
    fun getAllDonationLogs(): LiveData<List<DonationLogEntity>>
}
