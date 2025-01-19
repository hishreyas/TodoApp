package com.todopyinsights.todoapp.di

import android.content.Context
import androidx.room.Room
import com.todopyinsights.todoapp.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "app_database_reminder"
    ).build()

    @Singleton
    @Provides
    fun provideYourDao(db: AppDatabase) = db.reminderDao()
}