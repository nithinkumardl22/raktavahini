package com.raktavahini.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── Blood groups supported ────────────────────────────────────────────────────
enum class BloodGroup(val display: String) {
    A_POS("A+"), A_NEG("A-"),
    B_POS("B+"), B_NEG("B-"),
    AB_POS("AB+"), AB_NEG("AB-"),
    O_POS("O+"), O_NEG("O-");

    companion object {
        fun fromDisplay(s: String) = values().firstOrNull { it.display == s } ?: O_POS
    }
}

// ── Donor profile ─────────────────────────────────────────────────────────────
@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true) val donorId: Long = 0,
    val name: String,
    val phoneNumber: String,
    val bloodGroup: String,          // BloodGroup.display e.g. "O+"
    val city: String,
    val pincode: String,
    val lastDonationDate: Long = 0L, // epoch ms; 0 = never donated
    val isManuallyEligible: Boolean = true,  // FR-03 manual toggle
    val registeredAt: Long = System.currentTimeMillis()
)

// ── Donation log entry ────────────────────────────────────────────────────────
@Entity(tableName = "donation_log")
data class DonationLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val donorId: Long,
    val donationDate: Long = System.currentTimeMillis(),
    val hospital: String = "",
    val city: String = "",
    val unitsGiven: Int = 1,
    val recipientNote: String = ""   // optional note about who received
)

// ── Emergency search result (not a DB table — projection) ─────────────────────
data class EligibleDonor(
    val donorId: Long,
    val name: String,
    val phoneNumber: String,
    val bloodGroup: String,
    val city: String,
    val pincode: String,
    val lastDonationDate: Long,
    val isManuallyEligible: Boolean,
    val daysSinceLastDonation: Int   // computed in ViewModel
)

// ── Compatible blood group map ─────────────────────────────────────────────────
// Key = recipient's group → Value = list of groups that can donate to them
val COMPATIBLE_DONORS: Map<String, List<String>> = mapOf(
    "A+"  to listOf("A+", "A-", "O+", "O-"),
    "A-"  to listOf("A-", "O-"),
    "B+"  to listOf("B+", "B-", "O+", "O-"),
    "B-"  to listOf("B-", "O-"),
    "AB+" to listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
    "AB-" to listOf("A-", "B-", "AB-", "O-"),
    "O+"  to listOf("O+", "O-"),
    "O-"  to listOf("O-")
)
