
Носова Алина Андреевна  
Б9123-09.03.02ацс  

Статистика тестов
Юнит-тесты: 16
- ArtViewModelTest.kt - 10 тестов
- ArtRepositoryTest.kt - 10 тестов (6 основных + 4 дополнительных)
- FlowEmissionTest.kt - 6 тестов на Flow

Интеграционные тесты: 10
- ArtRepositoryIntegrationTest.kt - 5 тестов (Repository + Room)
- NavigationIntegrationTest.kt - 5 тестов (UI интеграция)

Нетривиальные тесты: 6
1. Retry после ошибки инициирует новый запрос (ArtViewModelTest)
2. Пустой результат дает Empty, а не Success (ArtViewModelTest)
3. Повторное добавление в избранное не создает дубль (ArtRepositoryTest)
4. Toggle favorite корректно добавляет/удаляет (ArtRepositoryTest)
5. Переход от ошибки к успешному состоянию (NavigationIntegrationTest)
6. Flow эмиссии при множественных операциях (FlowEmissionTest)

Flow тесты: 7
Все тесты проверяют полную последовательность эмиссий:
1. Последовательность при добавлении элементов
2. Последовательность при удалении элементов
3. Отсутствие лишних эмиссий
4. Корректное поведение при новой подписке
5. Сложные последовательности операций
6. Порядок элементов в эмиссиях
7. Favorites flow в ViewModel
 
Покрытые сценарии
ViewModel (ArtViewModelTest.kt)
Repository (ArtRepositoryTest.kt)
Интеграция Repository + Room (ArtRepositoryIntegrationTest.kt)
Интеграция UI (NavigationIntegrationTest.kt)
Flow эмиссии (FlowEmissionTest.kt)

Последовательности эмиссий Flow
Тест 1: Добавление элементов
```
[] → [entity1] → [entity1, entity2] → [entity1, entity2, entity3]
```

Тест 2: Удаление элементов
```
[entity1, entity2, entity3] → [entity1, entity3] → [entity3] → []
```

Тест 3: Сложные операции
```
[] → [entity1, entity2, entity3] → [entity1, entity3] → [entity1, entity2, entity3] → []
```

Тест 4: Favorites в ViewModel
```
[] → [entity1] → [entity1, entity2]
```

Как запустить тесты
- Java 11 или выше
- Android SDK
- Gradle 8.13+
