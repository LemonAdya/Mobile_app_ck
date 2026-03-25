# Домашнее задание 5 - Тестирование

**ФИО:** Носова Алина Андреевна  
**Группа:** Б9123-09.03.02ацс  
**Дата:** 25.03.2026

## Описание

Добавлено полное покрытие тестами для проекта из ДЗ 4. Проект остался рабочим, все функции сохранены.

## Статистика тестов

### Юнит-тесты: 16
- **ArtViewModelTest.kt** - 10 тестов
- **ArtRepositoryTest.kt** - 10 тестов (6 основных + 4 дополнительных)
- **FlowEmissionTest.kt** - 6 тестов на Flow

### Интеграционные тесты: 10
- **ArtRepositoryIntegrationTest.kt** - 5 тестов (Repository + Room)
- **NavigationIntegrationTest.kt** - 5 тестов (UI интеграция)

### Нетривиальные тесты: 6+
1. Retry после ошибки инициирует новый запрос (ArtViewModelTest)
2. Пустой результат дает Empty, а не Success (ArtViewModelTest)
3. Повторное добавление в избранное не создает дубль (ArtRepositoryTest)
4. Toggle favorite корректно добавляет/удаляет (ArtRepositoryTest)
5. Переход от ошибки к успешному состоянию (NavigationIntegrationTest)
6. Flow эмиссии при множественных операциях (FlowEmissionTest)

### Flow тесты: 7
Все тесты проверяют полную последовательность эмиссий:
1. Последовательность при добавлении элементов
2. Последовательность при удалении элементов
3. Отсутствие лишних эмиссий
4. Корректное поведение при новой подписке
5. Сложные последовательности операций
6. Порядок элементов в эмиссиях
7. Favorites flow в ViewModel

## Покрытые сценарии

### ViewModel (ArtViewModelTest.kt)
- ✅ Успешная загрузка данных
- ✅ Обработка ошибки загрузки
- ✅ Retry после ошибки (нетривиальный)
- ✅ Корректное начальное состояние
- ✅ Пустой результат → Empty state (нетривиальный)
- ✅ Поиск с пустым запросом
- ✅ Поиск с непустым запросом
- ✅ Загрузка деталей артворка
- ✅ Toggle favorite обновляет состояние
- ✅ Flow последовательность эмиссий favorites

### Repository (ArtRepositoryTest.kt)
- ✅ Успешная загрузка артворков
- ✅ Кеширование работает корректно
- ✅ ForceRefresh игнорирует кеш
- ✅ Поиск артворков
- ✅ Получение деталей артворка
- ✅ Проверка избранного
- ✅ Добавление в избранное (нетривиальный)
- ✅ Удаление из избранного (нетривиальный)
- ✅ Повторное добавление не создает дубль (нетривиальный)
- ✅ getFavoritesFlow возвращает Flow

### Интеграция Repository + Room (ArtRepositoryIntegrationTest.kt)
- ✅ Добавление артворка сохраняется в БД
- ✅ Удаление артворка удаляется из БД
- ✅ Множественные операции сохраняют целостность данных
- ✅ Flow эмиссии при изменениях в БД
- ✅ Интеграция API + Room

### Интеграция UI (NavigationIntegrationTest.kt)
- ✅ Навигация от списка к деталям
- ✅ Отображение состояния Loading
- ✅ Отображение ошибки и кнопки Retry
- ✅ Переход от ошибки к успешному состоянию (нетривиальный)
- ✅ Отображение пустого состояния

### Flow эмиссии (FlowEmissionTest.kt)
- ✅ Полная последовательность при добавлении
- ✅ Полная последовательность при удалении
- ✅ Отсутствие лишних эмиссий
- ✅ Корректное поведение при новой подписке
- ✅ Сложные последовательности операций
- ✅ Порядок элементов в эмиссиях

## Последовательности эмиссий Flow

### Тест 1: Добавление элементов
```
[] → [entity1] → [entity1, entity2] → [entity1, entity2, entity3]
```

### Тест 2: Удаление элементов
```
[entity1, entity2, entity3] → [entity1, entity3] → [entity3] → []
```

### Тест 3: Сложные операции
```
[] → [entity1, entity2, entity3] → [entity1, entity3] → [entity1, entity2, entity3] → []
```

### Тест 4: Favorites в ViewModel
```
[] → [entity1] → [entity1, entity2]
```

## Добавленные файлы

### Тестовые файлы
1. `app/src/test/java/com/example/digitalapi_nosova_3/ui/viewmodel/ArtViewModelTest.kt`
2. `app/src/test/java/com/example/digitalapi_nosova_3/data/repository/ArtRepositoryTest.kt`
3. `app/src/test/java/com/example/digitalapi_nosova_3/data/flow/FlowEmissionTest.kt`
4. `app/src/androidTest/java/com/example/digitalapi_nosova_3/data/repository/ArtRepositoryIntegrationTest.kt`
5. `app/src/androidTest/java/com/example/digitalapi_nosova_3/ui/NavigationIntegrationTest.kt`

### Измененные файлы
1. `app/build.gradle.kts` - добавлены зависимости для тестирования

## Зависимости для тестирования

```kotlin
// Unit testing
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("app.cash.turbine:turbine:1.0.0")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("com.google.dagger:hilt-android-testing:2.51.1")

// Android instrumentation tests
androidTestImplementation("androidx.room:room-testing:2.6.1")
androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
androidTestImplementation("androidx.test:runner:1.5.2")
androidTestImplementation("androidx.test:rules:1.5.0")
```

## Как запустить тесты

### Предварительные требования
- Java 11 или выше
- Android SDK
- Gradle 8.13+

### Команды для запуска

#### 1. Запуск всех юнит-тестов
```bash
./gradlew test
```

#### 2. Запуск конкретного теста
```bash
./gradlew test --tests ArtViewModelTest
./gradlew test --tests ArtRepositoryTest
./gradlew test --tests FlowEmissionTest
```

#### 3. Запуск интеграционных тестов (требуется эмулятор или устройство)
```bash
./gradlew connectedAndroidTest
```

#### 4. Запуск конкретного интеграционного теста
```bash
./gradlew connectedAndroidTest --tests ArtRepositoryIntegrationTest
./gradlew connectedAndroidTest --tests NavigationIntegrationTest
```

#### 5. Запуск всех тестов с отчетом
```bash
./gradlew test connectedAndroidTest
```

### Просмотр отчетов
После выполнения тестов отчеты доступны по адресу:
- Юнит-тесты: `app/build/reports/tests/testDebugUnitTest/index.html`
- Интеграционные: `app/build/reports/androidTests/connected/index.html`

## Теоретическая информация

### Что такое юнит-тесты?
Юнит-тесты проверяют отдельные компоненты (классы, функции) в изоляции от других частей системы. Используют моки (mockk) для имитации зависимостей.

### Что такое интеграционные тесты?
Интеграционные тесты проверяют взаимодействие нескольких компонентов вместе. Например, Repository + Room проверяет, что данные корректно сохраняются и читаются из реальной БД.

### Что такое нетривиальные тесты?
Нетривиальные тесты проверяют не только финальное значение, но и контракт поведения:
- Retry действительно делает новый запрос
- Дубли не создаются при повторном добавлении
- Состояния корректно переходят друг в друга

### Что такое Flow тесты?
Flow тесты проверяют последовательность эмиссий реактивных потоков данных. Используется библиотека Turbine для удобной проверки эмиссий.

### Основные инструменты

#### MockK
Библиотека для создания моков в Kotlin:
```kotlin
val repository = mockk<ArtRepository>()
coEvery { repository.getArtworks() } returns Pair(artworks, url)
coVerify { repository.getArtworks() }
```

#### Turbine
Библиотека для тестирования Flow:
```kotlin
flow.test {
    assertEquals(expected1, awaitItem())
    assertEquals(expected2, awaitItem())
    cancelAndIgnoreRemainingEvents()
}
```

#### Coroutines Test
Библиотека для тестирования корутин:
```kotlin
@Test
fun myTest() = runTest {
    // тестовый код с корутинами
    advanceUntilIdle() // ждем завершения всех корутин
}
```

#### Room Testing
In-memory база данных для тестирования:
```kotlin
database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
    .allowMainThreadQueries()
    .build()
```

## Особенности реализации

### 1. Детерминированность
Все тесты детерминированы - используется `StandardTestDispatcher` для контроля выполнения корутин.

### 2. Изоляция
Каждый тест независим, использует свои моки и не влияет на другие тесты.

### 3. Читаемость
Тесты следуют паттерну Given-When-Then для лучшей читаемости.

### 4. Полнота
Покрыты все основные сценарии использования, включая граничные случаи.

## Проверка работоспособности

### Что проверяется
1. ✅ Проект компилируется
2. ✅ Все тесты написаны корректно
3. ✅ Покрыты все требуемые сценарии
4. ✅ Тесты детерминированы
5. ✅ Проверяются последовательности эмиссий Flow

### Минимальные требования выполнены
- ✅ 6+ юнит-тестов (написано 16)
- ✅ 3+ интеграционных теста (написано 10)
- ✅ 2+ нетривиальных теста (написано 6+)
- ✅ 2+ теста на Flow (написано 7)
- ✅ Проект собирается и запускается

## Итоги

Проект полностью покрыт тестами согласно требованиям ДЗ 5. Все тесты написаны с учетом лучших практик:
- Используются моки для изоляции
- Проверяются последовательности эмиссий Flow
- Покрыты нетривиальные сценарии
- Тесты детерминированы и читаемы
- Интеграционные тесты проверяют реальное взаимодействие компонентов
