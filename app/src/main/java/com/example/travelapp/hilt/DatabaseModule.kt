package com.example.travelapp.hilt

import android.content.Context
import com.example.travelapp.database.TravelDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        TravelDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideUserDao(database: TravelDatabase) =
        database.userDao()

    @Provides
    @Singleton
    fun provideTripDao(database: TravelDatabase) =
        database.tripDao()

    @Provides
    @Singleton
    fun providePlaceDao(database: TravelDatabase) =
        database.placeDao()

    @Provides
    @Singleton
    fun providePhotoDao(database: TravelDatabase) =
        database.photoDao()

    @Provides
    @Singleton
    fun provideNotificationDao(database: TravelDatabase) =
        database.notificationDao()
}