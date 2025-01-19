package com.todopyinsights.todoapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.todopyinsights.todoapp.database.models.Reminder
import com.todopyinsights.todoapp.models.ReminderData
import com.todopyinsights.todoapp.repository.ReminderRepository
import com.todopyinsights.todoapp.repository.ReminderRepository.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    val uiState: LiveData<UiState> get() = repository._uiState
    val reminders = repository.getPagedReminders().flow


    init {
        viewModelScope.launch {
            repository.syncApiDataWithLocal()
        }
    }

     fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.addReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
}
