package com.todopyinsights.todoapp.models

data class ApiTodo(
    val id: Int,
    val title: String,
    val completed: Boolean,
    val reminderTime: Long // Assume this field is added to API data
)