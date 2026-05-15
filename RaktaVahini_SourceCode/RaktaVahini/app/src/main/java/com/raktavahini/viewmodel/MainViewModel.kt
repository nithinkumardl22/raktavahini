package com.raktavahini.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.raktavahini.data.database.RaktaVahiniDatabase
import com.raktavahini.data.entities.*
import com.raktavahini.data.repository.RaktaVahiniRepository
import com.raktavahini.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: RaktaVahiniRepository
    private val notifHelper = NotificationHelper(app)

    init {
        val db = RaktaVahiniDatabase.getInstance(app)
        repo = RaktaVahiniRepository(db.dao())
    }

    // ── Core eligibility logic (FR key requirement) ───────────────────────────
    /**
     * Returns true if the donor is eligible to donate:
     *  - Never donated before (lastDonationDate == 0), OR
     *  - More than 90 days have passed since last donation
     * This is the 90-day rule enforced by the spec.
     */
    fun isDonorEligible(lastDonationDateMs: Long): Boolean {
        if (lastDonationDateMs == 0L) return true
        val diffMs   = System.currentTimeMillis() - lastDonationDateMs
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
        return diffDays > 90
    }

    fun daysSinceDonation(lastDonationDateMs: Long): Int {
        if (lastDonationDateMs == 0L) return -1
        return TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - lastDonationDateMs
        ).toInt()
    }

    fun daysUntilEligible(lastDonationDateMs: Long): Int {
        if (isDonorEligible(lastDonationDateMs)) return 0
        val diffDays = daysSinceDonation(lastDonationDateMs)
        return 90 - diffDays
    }

    // ── My profile ────────────────────────────────────────────────────────────
    val myProfile: LiveData<DonorEntity?> = repo.getMyProfile()
    val totalDonorCount: LiveData<Int> = repo.getTotalDonorCount()
    val allDonors: LiveData<List<DonorEntity>> = repo.getAllDonors()
    val allDonationLogs = repo.getAllDonationLogs()

    // ── Register / update donor ───────────────────────────────────────────────
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun registerDonor(
        name: String, phone: String, bloodGroup: String,
        city: String, pincode: String, lastDonationDate: Long
    ) = viewModelScope.launch {
        repo.insertDonor(
            DonorEntity(
                name = name,
                phoneNumber = phone,
                bloodGroup = bloodGroup,
                city = city,
                pincode = pincode,
                lastDonationDate = lastDonationDate
            )
        )
        _saveSuccess.value = true
    }

    fun updateDonor(donor: DonorEntity) = viewModelScope.launch {
        repo.updateDonor(donor)
        _saveSuccess.value = true
    }

    fun clearSaveSuccess() { _saveSuccess.value = false }

    // ── Eligibility toggle (FR-03) ────────────────────────────────────────────
    fun toggleEligibility(donorId: Long, isEligible: Boolean) = viewModelScope.launch {
        repo.updateEligibilityToggle(donorId, isEligible)
    }

    // ── Emergency search (FR-02) ──────────────────────────────────────────────
    private val _searchResults = MutableStateFlow<List<EligibleDonor>>(emptyList())
    val searchResults: StateFlow<List<EligibleDonor>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _searchDone = MutableStateFlow(false)
    val searchDone: StateFlow<Boolean> = _searchDone

    fun searchDonors(bloodGroup: String, location: String) = viewModelScope.launch {
        _isSearching.value = true
        _searchDone.value  = false

        // Get compatible donor groups for the required blood type
        val compatibleGroups = COMPATIBLE_DONORS[bloodGroup] ?: listOf(bloodGroup)

        // Fetch from DB — pre-filtered by group + manual eligibility flag
        val rawList = if (location.isBlank())
            repo.searchEligible(compatibleGroups)
        else
            repo.searchEligibleByLocation(compatibleGroups, location)

        // Apply 90-day rule in Kotlin (fine-grained control)
        val eligible = rawList
            .filter { isDonorEligible(it.lastDonationDate) }
            .map { donor ->
                EligibleDonor(
                    donorId           = donor.donorId,
                    name              = donor.name,
                    phoneNumber       = donor.phoneNumber,
                    bloodGroup        = donor.bloodGroup,
                    city              = donor.city,
                    pincode           = donor.pincode,
                    lastDonationDate  = donor.lastDonationDate,
                    isManuallyEligible = donor.isManuallyEligible,
                    daysSinceLastDonation = daysSinceDonation(donor.lastDonationDate)
                )
            }
            .sortedWith(compareByDescending { it.daysSinceLastDonation }) // longest gap first

        _searchResults.value = eligible
        _isSearching.value   = false
        _searchDone.value    = true
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _searchDone.value    = false
    }

    // ── Log a donation + send Thank You notification (FR-05) ──────────────────
    private val _logSuccess = MutableStateFlow(false)
    val logSuccess: StateFlow<Boolean> = _logSuccess

    fun logDonation(
        donorId: Long,
        hospital: String,
        city: String,
        units: Int,
        recipientNote: String,
        donorName: String
    ) = viewModelScope.launch {
        val nowMs = System.currentTimeMillis()

        // Insert log entry
        repo.insertDonationLog(
            com.raktavahini.data.entities.DonationLogEntity(
                donorId       = donorId,
                donationDate  = nowMs,
                hospital      = hospital,
                city          = city,
                unitsGiven    = units,
                recipientNote = recipientNote
            )
        )

        // Update donor's last donation date → makes them ineligible for 90 days
        repo.updateLastDonationDate(donorId, nowMs)

        // FR-05: Send "Thank You" notification
        notifHelper.sendThankYouNotification(donorName)

        _logSuccess.value = true
    }

    fun clearLogSuccess() { _logSuccess.value = false }

    // ── Donation log for profile ──────────────────────────────────────────────
    fun getDonationLog(donorId: Long) = repo.getDonationLogForDonor(donorId)
    fun getTotalDonations(donorId: Long) = repo.getTotalDonationsForDonor(donorId)
    fun getEligibleCount(group: String) = repo.getEligibleCountForGroup(group)

    // ── Blood group stats for home dashboard ──────────────────────────────────
    val bloodGroupStats: LiveData<Map<String, Int>> = allDonors.map { donors ->
        BloodGroup.values().associate { bg ->
            bg.display to donors.count { it.bloodGroup == bg.display }
        }
    }
}
