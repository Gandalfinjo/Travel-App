package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.NotificationDao
import com.example.travelapp.database.models.AppNotification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    suspend fun schedule(notification: AppNotification) =
        notificationDao.insert(notification)

    suspend fun updateNotification(notification: AppNotification) =
        notificationDao.update(notification)

    suspend fun deleteNotification(notification: AppNotification) =
        notificationDao.delete(notification)

    fun getTripNotifications(tripId: Int): Flow<List<AppNotification>> =
        notificationDao.getByTrip(tripId)
}