package com.raktavahini.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Channels {
    const val DONATION_THANKS = "donation_thanks"
    const val ELIGIBILITY     = "eligibility_reminder"
}

class NotificationHelper(private val context: Context) {

    init { createChannels() }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Thank you channel
            NotificationChannel(
                Channels.DONATION_THANKS,
                "Donation Thank You",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sent after logging a blood donation"
                manager.createNotificationChannel(this)
            }

            // Eligibility reminder channel
            NotificationChannel(
                Channels.ELIGIBILITY,
                "Eligibility Reminder",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reminds donor when they become eligible again"
                manager.createNotificationChannel(this)
            }
        }
    }

    /** FR-05: Thank You notification after a donation is logged */
    fun sendThankYouNotification(donorName: String) {
        val notification = NotificationCompat.Builder(context, Channels.DONATION_THANKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🩸 Thank You, $donorName!")
            .setContentText("Your donation is a precious gift of life. You are a hero!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Your blood donation has been logged successfully. " +
                        "You will be eligible to donate again in 90 days. " +
                        "Thank you for saving lives, $donorName! 🙏"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — silent fail
        }
    }

    /** Notify donor they are eligible again (90 days passed) */
    fun sendEligibilityNotification(donorName: String) {
        val notification = NotificationCompat.Builder(context, Channels.ELIGIBILITY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ You can donate again, $donorName!")
            .setContentText("90 days have passed. Someone might need your blood group today.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1002, notification)
        } catch (e: SecurityException) { /* silent */ }
    }
}
