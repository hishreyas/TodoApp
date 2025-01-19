package com.todopyinsights.todoapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.todopyinsights.todoapp.broadcast.NotificationReceiver
import com.todopyinsights.todoapp.models.RecurrenceType
import com.todopyinsights.todoapp.models.ReminderData
import java.text.SimpleDateFormat
import java.util.Locale

object NotificationManager {

    fun scheduleNotification(context: Context, reminderData: ReminderData) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", reminderData.title)
            putExtra("description", reminderData.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderData.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .parse(reminderData.dateTime)?.time ?: return

        Log.d("triggger Time","$triggerTime")

        val interval = when (reminderData.recurrenceType) {
            RecurrenceType.EVERY_15_MIN -> AlarmManager.INTERVAL_FIFTEEN_MINUTES
            RecurrenceType.HOURLY -> AlarmManager.INTERVAL_HOUR
            RecurrenceType.DAILY -> AlarmManager.INTERVAL_DAY
            else -> 0L
        }

        if (interval > 0) {
            // Set a repeating alarm for recurring reminders
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                interval,
                pendingIntent
            )
            Log.d("AlarmManager", "Setting repeating alarm at: $triggerTime with interval: $interval")
        } else {
            // Set a one-time alarm for non-recurring reminders
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("Hello from notification set","Hellp")
        }
        Log.d("Hello from notification set","Hellp")
    }

    fun cancelNotification(context: Context, reminderData: ReminderData) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderData.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }


}