package com.todopyinsights.todoapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.todopyinsights.todoapp.database.dao.ReminderDao
import com.todopyinsights.todoapp.database.models.Reminder

@Database(entities = [Reminder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}