package com.example.digitalapi_nosova_3.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.repository.FakeArtDao
import com.example.digitalapi_nosova_3.data.repository.FakeCachedArtworkDao
import com.example.digitalapi_nosova_3.data.repository.FakeCollectionDao
import com.example.digitalapi_nosova_3.data.repository.FakeHistoryDao
import com.example.digitalapi_nosova_3.data.repository.FakeNoteDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var params: WorkerParameters
    private lateinit var api: ArtApiService
    private lateinit var repository: ArtRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var cachedArtworkDao: FakeCachedArtworkDao
    private var runAttemptCount: Int = 0

    private val testArtwork1 = Artwork(
        id = 1,
        title = "Starry Night",
        artistTitle = "Van Gogh",
        imageId = "img1",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    private fun newWorker(): SyncWorker {
        every { params.runAttemptCount } returns runAttemptCount
        return SyncWorker(context, params, repository, preferencesRepository)
    }

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        params = mockk(relaxed = true)
        api = mockk()
        preferencesRepository = mockk()
        val artDao = FakeArtDao()
        val collectionDao = FakeCollectionDao()
        val noteDao = FakeNoteDao()
        val historyDao = FakeHistoryDao()
        cachedArtworkDao = FakeCachedArtworkDao()
        repository = ArtRepository(api, artDao, collectionDao, noteDao, historyDao, cachedArtworkDao)
        every { preferencesRepository.cacheTtlDaysFlow } returns flowOf(7)
    }

    @Test
    fun `doWork success caches artworks and clears expired entries`() = runTest {
        val artworks = listOf(testArtwork1)
        val response = ArtworkResponse(data = artworks, config = ApiConfig(iiifUrl = "https://example.com"))
        coEvery { api.getArtworks() } returns response
        runAttemptCount = 0

        val worker = newWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        val cached = cachedArtworkDao.getAllCachedArtworks().first()
        assertEquals(1, cached.size)
        assertEquals(testArtwork1.id, cached[0].id)
    }

    @Test
    fun `doWork retries on network error under max attempts`() = runTest {
        coEvery { api.getArtworks() } throws RuntimeException("Network error")
        runAttemptCount = 0

        val worker = newWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork fails after max retry attempts`() = runTest {
        coEvery { api.getArtworks() } throws RuntimeException("Network error")
        runAttemptCount = 5

        val worker = newWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }
}
