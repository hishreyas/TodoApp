package com.todopyinsights.todoapp.models

import com.todopyinsights.todoapp.database.models.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReminderData(
    val id: Int,
    val title: String,
    val description: String,
    val dateTime: String,
    val recurrence: String,
    val isFromApi:Boolean = false,
    val recurrenceType: RecurrenceType=RecurrenceType.NONE
){
    fun toReminderData(): Reminder {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val time  = formatter.parse(dateTime)?.time ?: 0L

        return    Reminder(
                id = this.id.toLong(), // Convert Int to Long
                title = this.title,
                description = this.description,
                reminderTime = time, // Convert dateTime string to milliseconds
                isLocal = !this.isFromApi )
    }
}

enum class RecurrenceType {
    NONE, EVERY_15_MIN,HOURLY, DAILY,
}