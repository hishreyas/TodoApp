package com.todopyinsights.todoapp.network

import com.todopyinsights.todoapp.models.ApiTodo
import retrofit2.http.GET

interface ApiService {
    @GET("todos")
    suspend fun getTodos(): List<ApiTodo>
}