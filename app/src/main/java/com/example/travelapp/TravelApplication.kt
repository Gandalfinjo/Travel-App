package com.example.travelapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for the Travel app.
 *
 * Initializes Hilt dependency injection and configures WorkManager
 * to support Hilt-injected Workers for background tasks
 * (trip reminders, status updates).
 */
@HiltAndroidApp
class TravelApplication() : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}