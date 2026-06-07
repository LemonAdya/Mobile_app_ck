package com.example.digitalapi_nosova_3.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ArtRepository,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val ttlDays = preferencesRepository.cacheTtlDaysFlow.first()
            repository.syncAllArtworks()
            repository.clearExpiredCache(ttlDays)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
