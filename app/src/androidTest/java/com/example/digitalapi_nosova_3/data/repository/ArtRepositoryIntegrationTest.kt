package com.example.digitalapi_nosova_3.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.AppDatabase
import com.example.digitalapi_nosova_3.data.local.ArtDao
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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

    @Test
    fun `addingArtworkToFavoritesShouldPersistInDatabase`() = runTest {
        assertFalse(repository.isFavorite(testArtwork1.id))

        repository.toggleFavorite(testArtwork1)

        assertTrue(repository.isFavorite(testArtwork1.id))
        
        val favorites = repository.getFavoritesFlow().first()
        assertEquals(1, favorites.size)
        assertEquals(testArtwork1.id, favorites[0].id)
        assertEquals(testArtwork1.title, favorites[0].title)
    }

    @Test
    fun `removingArtworkFromFavoritesShouldDeleteFromDatabase`() = runTest {
        repository.toggleFavorite(testArtwork1)
        assertTrue(repository.isFavorite(testArtwork1.id))

        repository.toggleFavorite(testArtwork1)

        assertFalse(repository.isFavorite(testArtwork1.id))
        
        val favorites = repository.getFavoritesFlow().first()
        assertEquals(0, favorites.size)
    }

    @Test
    fun `multipleFavoritesOperationsShouldMaintainDataIntegrity`() = runTest {
        repository.toggleFavorite(testArtwork1)
        repository.toggleFavorite(testArtwork2)

        var favorites = repository.getFavoritesFlow().first()
        assertEquals(2, favorites.size)
        assertTrue(favorites.any { it.id == testArtwork1.id })
        assertTrue(favorites.any { it.id == testArtwork2.id })

        repository.toggleFavorite(testArtwork1)

        favorites = repository.getFavoritesFlow().first()
        assertEquals(1, favorites.size)
        assertEquals(testArtwork2.id, favorites[0].id)
    }

    @Test
    fun `repositoryShouldIntegrateApiAndRoomCorrectly`() = runTest {
        val artworks = listOf(testArtwork1, testArtwork2)
        val response = ArtworkResponse(
            data = artworks,
            config = ApiConfig(iiifUrl = "https://example.com")
        )
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        val (data, url) = repository.getArtworks()

        assertEquals(2, data.size)

        repository.toggleFavorite(testArtwork1)

        assertTrue(repository.isFavorite(testArtwork1.id))
        assertFalse(repository.isFavorite(testArtwork2.id))
    }
}
