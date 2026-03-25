package com.example.digitalapi_nosova_3.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.AppDatabase
import com.example.digitalapi_nosova_3.data.local.ArtDao
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ArtRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ArtDao
    private lateinit var api: ArtApiService
    private lateinit var repository: ArtRepository

    private val testArtwork1 = Artwork(
        id = 1,
        title = "Starry Night",
        artistTitle = "Vincent van Gogh",
        imageId = "img1",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    private val testArtwork2 = Artwork(
        id = 2,
        title = "Mona Lisa",
        artistTitle = "Leonardo da Vinci",
        imageId = "img2",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.artDao()
        api = mockk()
        repository = ArtRepository(api, dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // Интеграционный тест 1: Repository + Room - добавление и чтение
    @Test
    fun `adding artwork to favorites should persist in database`() = runTest {
        // Given
        assertFalse(repository.isFavorite(testArtwork1.id))

        // When - добавляем в избранное
        repository.toggleFavorite(testArtwork1)

        // Then - данные сохранились и читаются
        assertTrue(repository.isFavorite(testArtwork1.id))
        
        repository.getFavoritesFlow().test {
            val favorites = awaitItem()
            assertEquals(1, favorites.size)
            assertEquals(testArtwork1.id, favorites[0].id)
            assertEquals(testArtwork1.title, favorites[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Интеграционный тест 2: Repository + Room - удаление
    @Test
    fun `removing artwork from favorites should delete from database`() = runTest {
        // Given - добавляем артворк
        repository.toggleFavorite(testArtwork1)
        assertTrue(repository.isFavorite(testArtwork1.id))

        // When - удаляем
        repository.toggleFavorite(testArtwork1)

        // Then - данные удалены
        assertFalse(repository.isFavorite(testArtwork1.id))
        
        repository.getFavoritesFlow().test {
            val favorites = awaitItem()
            assertEquals(0, favorites.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Интеграционный тест 3: Repository + Room - множественные операции
    @Test
    fun `multiple favorites operations should maintain data integrity`() = runTest {
        // Given & When - добавляем несколько артворков
        repository.toggleFavorite(testArtwork1)
        repository.toggleFavorite(testArtwork2)

        // Then - оба сохранены
        repository.getFavoritesFlow().test {
            val favorites = awaitItem()
            assertEquals(2, favorites.size)
            assertTrue(favorites.any { it.id == testArtwork1.id })
            assertTrue(favorites.any { it.id == testArtwork2.id })
            cancelAndIgnoreRemainingEvents()
        }

        // When - удаляем один
        repository.toggleFavorite(testArtwork1)

        // Then - остался только второй
        repository.getFavoritesFlow().test {
            val favorites = awaitItem()
            assertEquals(1, favorites.size)
            assertEquals(testArtwork2.id, favorites[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Интеграционный тест 4: Repository + Room - Flow эмиссии при изменениях
    @Test
    fun `favorites flow should emit updates when data changes`() = runTest {
        // Given
        val flow = repository.getFavoritesFlow()

        // When & Then - проверяем последовательность эмиссий
        flow.test {
            // Начальное состояние - пусто
            assertEquals(0, awaitItem().size)

            // Добавляем первый артворк
            repository.toggleFavorite(testArtwork1)
            val afterFirst = awaitItem()
            assertEquals(1, afterFirst.size)
            assertEquals(testArtwork1.id, afterFirst[0].id)

            // Добавляем второй артворк
            repository.toggleFavorite(testArtwork2)
            val afterSecond = awaitItem()
            assertEquals(2, afterSecond.size)

            // Удаляем первый
            repository.toggleFavorite(testArtwork1)
            val afterDelete = awaitItem()
            assertEquals(1, afterDelete.size)
            assertEquals(testArtwork2.id, afterDelete[0].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Интеграционный тест 5: Repository + Fake API + Room
    @Test
    fun `repository should integrate API and Room correctly`() = runTest {
        // Given - мокируем API
        val artworks = listOf(testArtwork1, testArtwork2)
        val response = ArtworkResponse(
            data = artworks,
            config = ApiConfig(iiifUrl = "https://example.com")
        )
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        // When - загружаем данные из API
        val (data, url) = repository.getArtworks()

        // Then - данные получены
        assertEquals(2, data.size)

        // When - добавляем один из них в избранное
        repository.toggleFavorite(testArtwork1)

        // Then - он сохранен в Room
        assertTrue(repository.isFavorite(testArtwork1.id))
        assertFalse(repository.isFavorite(testArtwork2.id))
    }
}
