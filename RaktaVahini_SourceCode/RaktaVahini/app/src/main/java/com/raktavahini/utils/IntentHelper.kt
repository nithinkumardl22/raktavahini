package com.raktavahini.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentHelper {

    /**
     * FR-04: Secure Calling — uses Intent.ACTION_DIAL so the phone number
     * goes to the dialler (user confirms) rather than auto-calling.
     * The number is NOT shown on the public search results screen —
     * only revealed when user taps "Call" on the individual donor card.
     */
    fun dialDonor(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    /** Share emergency request via WhatsApp / other apps */
    fun shareEmergencyRequest(
        context: Context,
        bloodGroup: String,
        city: String,
        hospitalName: String
    ) {
        val message = buildString {
            appendLine("🩸 URGENT BLOOD REQUIRED 🩸")
            appendLine()
            appendLine("Blood Group : $bloodGroup")
            appendLine("Hospital    : $hospitalName")
            appendLine("City        : $city")
            appendLine()
            appendLine("Please contact immediately if you are eligible to donate.")
            appendLine("Shared via Rakta-Vahini App — ರಕ್ತ-ವಾಹಿನಿ")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Share Emergency Request"))
    }

    /** Open WhatsApp directly to a donor's number */
    fun whatsappDonor(context: Context, phone: String, bloodGroup: String) {
        val msg = "Namaskara, I found your contact via Rakta-Vahini. " +
                  "We urgently need $bloodGroup blood. Are you available to donate? 🙏"
        val normalized = if (phone.startsWith("+91")) phone else "+91$phone"
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra("jid", "${normalized.replace("+", "")}@s.whatsapp.net")
                putExtra(Intent.EXTRA_TEXT, msg)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp not installed — fallback to SMS
            val sms = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                putExtra("sms_body", msg)
            }
            context.startActivity(sms)
        }
    }
}
