package com.todopyinsights.todoapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.todopyinsights.todoapp.models.RecurrenceType
import com.todopyinsights.todoapp.models.ReminderData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val reminderTime: Long, // Time in milliseconds
    val isLocal: Boolean, // True for local reminders, false for API reminders
){
    fun toReminderData(): ReminderData {
        val formattedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
            Date(reminderTime)
        )
        val recurrence = when {
            reminderTime % (15 * 60 * 1000) == 0L -> RecurrenceType.EVERY_15_MIN
            reminderTime % (60 * 60 * 1000) == 0L -> RecurrenceType.HOURLY
            reminderTime % (24 * 60 * 60 * 1000) == 0L -> RecurrenceType.DAILY
            else -> RecurrenceType.NONE
        }

        return ReminderData(
            id = id.toInt(),
            title = title,
            description = description,
            dateTime = formattedDateTime,
            recurrence = recurrence.name,
            isFromApi = !isLocal,
            recurrenceType = recurrence
        )
    }
}