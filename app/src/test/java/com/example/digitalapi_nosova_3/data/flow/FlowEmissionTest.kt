package com.example.digitalapi_nosova_3.data.flow

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.digitalapi_nosova_3.data.local.ArtDao
import com.example.digitalapi_nosova_3.data.local.ArtEntity
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.api.ArtApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FlowEmissionTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var api: ArtApiService
    private lateinit var dao: ArtDao
    private lateinit var repository: ArtRepository

    private val entity1 = ArtEntity(1, "Art 1", "Artist 1", "img1")
    private val entity2 = ArtEntity(2, "Art 2", "Artist 2", "img2")
    private val entity3 = ArtEntity(3, "Art 3", "Artist 3", "img3")

    @Before
    fun setup() {
        api = mockk()
        dao = mockk(relaxed = true)
        repository = ArtRepository(api, dao)
    }

    // Flow тест 1: Полная последовательность эмиссий при добавлении в избранное
    @Test
    fun `favorites flow emits complete sequence when adding items`() = runTest {
        // Given
        val favoritesFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        coEvery { dao.getAllFavorites() } returns favoritesFlow

        // When & Then - проверяем полную последовательность
        repository.getFavoritesFlow().test {
            // Эмиссия 1: пустой список
            assertEquals(emptyList(), awaitItem())

            // Добавляем первый элемент
            favoritesFlow.value = listOf(entity1)
            assertEquals(listOf(entity1), awaitItem())

            // Добавляем второй элемент
            favoritesFlow.value = listOf(entity1, entity2)
            val secondEmission = awaitItem()
            assertEquals(2, secondEmission.size)
            assertEquals(entity1, secondEmission[0])
            assertEquals(entity2, secondEmission[1])

            // Добавляем третий элемент
            favoritesFlow.value = listOf(entity1, entity2, entity3)
            val thirdEmission = awaitItem()
            assertEquals(3, thirdEmission.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow тест 2: Последовательность эмиссий при удалении из избранного
    @Test
    fun `favorites flow emits correct sequence when removing items`() = runTest {
        // Given
        val favoritesFlow = MutableStateFlow(listOf(entity1, entity2, entity3))
        coEvery { dao.getAllFavorites() } returns favoritesFlow

        // When & Then
        repository.getFavoritesFlow().test {
            // Эмиссия 1: три элемента
            assertEquals(3, awaitItem().size)

            // Удаляем один элемент
            favoritesFlow.value = listOf(entity1, entity3)
            val afterFirstRemoval = awaitItem()
            assertEquals(2, afterFirstRemoval.size)
            assertEquals(entity1, afterFirstRemoval[0])
            assertEquals(entity3, afterFirstRemoval[1])

            // Удаляем еще один
            favoritesFlow.value = listOf(entity3)
            val afterSecondRemoval = awaitItem()
            assertEquals(1, afterSecondRemoval.size)
            assertEquals(entity3, afterSecondRemoval[0])

            // Удаляем последний
            favoritesFlow.value = emptyList()
            assertEquals(emptyList(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow тест 3: Нет лишних эмиссий при отсутствии изменений
    @Test
    fun `favorites flow should not emit duplicates when data unchanged`() = runTest {
        // Given
        val favoritesFlow = MutableStateFlow(listOf(entity1))
        coEvery { dao.getAllFavorites() } returns favoritesFlow

        // When & Then
        repository.getFavoritesFlow().test {
            // Первая эмиссия
            assertEquals(listOf(entity1), awaitItem())

            // Устанавливаем то же значение - не должно быть новой эмиссии
            favoritesFlow.value = listOf(entity1)
            
            // Изменяем значение - должна быть эмиссия
            favoritesFlow.value = listOf(entity1, entity2)
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow тест 4: Корректное поведение при новой подписке
    @Test
    fun `new subscription to favorites flow receives current state`() = runTest {
        // Given
        val currentFavorites = listOf(entity1, entity2)
        val favoritesFlow = MutableStateFlow(currentFavorites)
        coEvery { dao.getAllFavorites() } returns favoritesFlow

        // When - первая подписка
        repository.getFavoritesFlow().test {
            assertEquals(currentFavorites, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // When - новая подписка должна получить текущее состояние
        repository.getFavoritesFlow().test {
            val received = awaitItem()
            assertEquals(2, received.size)
            assertEquals(currentFavorites, received)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow тест 5: Последовательность эмиссий при множественных операциях
    @Test
    fun `favorites flow emits correct sequence for complex operations`() = runTest {
        // Given
        val favoritesFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        coEvery { dao.getAllFavorites() } returns favoritesFlow

        // When & Then - сложная последовательность операций
        repository.getFavoritesFlow().test {
            // 1. Начальное состояние
            assertEquals(emptyList(), awaitItem())

            // 2. Добавляем три элемента
            favoritesFlow.value = listOf(entity1, entity2, entity3)
            assertEquals(3, awaitItem().size)

            // 3. Удаляем средний элемент
            favoritesFlow.value = listOf(entity1, entity3)
            val afterRemoval = awaitItem()
            assertEquals(2, afterRemoval.size)
            assertEquals(entity1, afterRemoval[0])
            assertEquals(entity3, afterRemoval[1])

            // 4. Добавляем элемент обратно
            favoritesFlow.value = listOf(entity1, entity2, entity3)
            assertEquals(3, awaitItem().size)

            // 5. Очищаем все
            favoritesFlow.value = emptyList()
            assertEquals(emptyList(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow тест 6: Проверка порядка элементов в эмиссиях
    @Test
    fun `favorites flow maintains correct order of items`() = runTest {
        // Given
        val favoritesFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        coEvery { dao.getAllFavorites() } returns favoritesFlow

        // When & Then
        repository.getFavoritesFlow().test {
            assertEquals(emptyList(), awaitItem())

            // Добавляем в определенном порядке
            favoritesFlow.value = listOf(entity3, entity1, entity2)
            val emission = awaitItem()
            assertEquals(3, emission.size)
            assertEquals(entity3, emission[0])
            assertEquals(entity1, emission[1])
            assertEquals(entity2, emission[2])

            // Меняем порядок
            favoritesFlow.value = listOf(entity1, entity2, entity3)
            val reordered = awaitItem()
            assertEquals(entity1, reordered[0])
            assertEquals(entity2, reordered[1])
            assertEquals(entity3, reordered[2])

            cancelAndIgnoreRemainingEvents()
        }
    }
}
