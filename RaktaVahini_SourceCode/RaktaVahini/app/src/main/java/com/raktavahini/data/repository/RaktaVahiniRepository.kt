package com.raktavahini.data.repository

import androidx.lifecycle.LiveData
import com.raktavahini.data.dao.RaktaVahiniDao
import com.raktavahini.data.entities.*

class RaktaVahiniRepository(private val dao: RaktaVahiniDao) {

    // Donors
    fun getAllDonors(): LiveData<List<DonorEntity>> = dao.getAllDonors()
    fun getMyProfile(): LiveData<DonorEntity?> = dao.getMyProfile()
    suspend fun getMyProfileOnce(): DonorEntity? = dao.getMyProfileOnce()
    fun getDonorByIdLive(id: Long): LiveData<DonorEntity?> = dao.getDonorByIdLive(id)
    suspend fun getDonorById(id: Long): DonorEntity? = dao.getDonorById(id)
    suspend fun insertDonor(d: DonorEntity): Long = dao.insertDonor(d)
    suspend fun updateDonor(d: DonorEntity) = dao.updateDonor(d)
    suspend fun deleteDonor(d: DonorEntity) = dao.deleteDonor(d)
    suspend fun updateLastDonationDate(id: Long, dateMs: Long) = dao.updateLastDonationDate(id, dateMs)
    suspend fun updateEligibilityToggle(id: Long, eligible: Boolean) = dao.updateEligibilityToggle(id, eligible)
    fun getTotalDonorCount(): LiveData<Int> = dao.getTotalDonorCount()
    fun getEligibleCountForGroup(group: String): LiveData<Int> = dao.getEligibleCountForGroup(group)

    // Search
    suspend fun searchEligible(compatibleGroups: List<String>): List<DonorEntity> =
        dao.searchEligibleDonors(compatibleGroups)

    suspend fun searchEligibleByLocation(compatibleGroups: List<String>, location: String): List<DonorEntity> =
        dao.searchEligibleDonorsByLocation(compatibleGroups, location)

    // Donation log
    suspend fun insertDonationLog(log: DonationLogEntity): Long = dao.insertDonationLog(log)
    suspend fun deleteDonationLog(log: DonationLogEntity) = dao.deleteDonationLog(log)
    fun getDonationLogForDonor(donorId: Long): LiveData<List<DonationLogEntity>> =
        dao.getDonationLogForDonor(donorId)
    fun getTotalDonationsForDonor(donorId: Long): LiveData<Int> = dao.getTotalDonationsForDonor(donorId)
    fun getAllDonationLogs(): LiveData<List<DonationLogEntity>> = dao.getAllDonationLogs()
}
