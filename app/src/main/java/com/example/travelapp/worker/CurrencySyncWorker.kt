package com.example.travelapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.travelapp.database.repositories.ExpenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CurrencySyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val newCurrency = inputData.getString("NEW_CURRENCY") ?: return Result.failure()

        return try {
            expenseRepository.recalculateGlobalSnapshots(newCurrency)
            Result.success()
        }
        catch (_: Exception) {
            Result.retry()
        }
    }
}