package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.GymRepository

class GymReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACTION_MOTIVATION -> {
                val quote = GymRepository.MOTIVATION_QUOTES.random()
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = 1001,
                    title = "Daily Gym Motivation ⚡",
                    message = quote
                )
            }
            NotificationHelper.ACTION_WORKOUT -> {
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = 1002,
                    title = "Time to Hit the Weights! 🏋️",
                    message = "Your scheduled workout session is ready. Don't break the streak!"
                )
            }
        }
    }
}
