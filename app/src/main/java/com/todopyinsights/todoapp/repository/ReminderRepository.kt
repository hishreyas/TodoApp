package com.todopyinsights.todoapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.todopyinsights.todoapp.database.dao.ReminderDao
import com.todopyinsights.todoapp.database.models.Reminder
import com.todopyinsights.todoapp.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReminderRepository @Inject constructor( val reminderDao: ReminderDao,val apiService:ApiService) {

    val _uiState = MutableLiveData<UiState>()

    data class UiState(
        val isLoading: Boolean = false,
        val success: Boolean = true,
        val errorMessage: String? = null
    )

    init {
        _uiState.postValue(UiState())
    }
    fun getPagedReminders(): Pager<Int, Reminder> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = true
            ),
            pagingSourceFactory = { reminderDao.getPagedReminders() }
        )
    }

    suspend fun addReminder(reminder: Reminder) = reminderDao.insertReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)


    suspend fun syncApiDataWithLocal() {
        try {
            _uiState.postValue(UiState(isLoading = true))
            val apiTodos = apiService.getTodos()
            apiTodos.forEach { todo ->
                reminderDao.insertReminder(  Reminder(
                    id = todo.id.toLong(),
                    title = todo.title,
                    description = if (todo.completed) "Completed" else "Pending",
                    reminderTime = System.currentTimeMillis() + (1..10).random() * 60 * 60 * 1000,
                    isLocal = false
                ))
            }
            _uiState.postValue(UiState(isLoading = false,  success = true))
        }catch (e:Exception){
            _uiState.postValue(UiState(isLoading = false, errorMessage = e.message.toString(), success = false))
        }
    }
}