package com.example.kcalci.utils

import android.content.Context
import androidx.work.*
import com.example.kcalci.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class DailyResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(applicationContext)
            dataStoreManager.checkAndResetForNewDay()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        private const val WORK_NAME = "daily_reset_work"
        
        fun schedule(context: Context) {
            // Calculate time until next midnight
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            
            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }
    }
}