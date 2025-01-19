package com.todopyinsights.todoapp.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.todopyinsights.todoapp.database.models.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders ORDER BY reminderTime ASC")
    fun getPagedReminders(): PagingSource<Int, Reminder>

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}