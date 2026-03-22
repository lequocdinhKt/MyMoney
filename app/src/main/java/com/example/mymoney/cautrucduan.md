# 📦 New Project — Project Structure & Database Integration Guide
> Clean Architecture + MVVM + Jetpack Compose  
> Kotlin · Navigation Compose · StateFlow · Manual DI

---

## File
`README.md`

---

## 1. Overview

This document explains the recommended project structure for a new Android app using Clean Architecture and how to add a local database layer (Room). The structure separates responsibilities into three independent layers: UI, Domain, Data. Domain is platform-agnostic; Data implements Domain interfaces.

---

## 2. Architecture Layers (high level)

- UI Layer (Jetpack Compose): Composables + ViewModels — user interaction and presentation.
- Domain Layer (Pure Kotlin): Models, Repository interfaces, UseCases — business rules.
- Data Layer: Repository implementations, local database (Room) or other sources.

Principle: UI and Data depend on Domain. Domain must not depend on Android or database libraries.

---

## 3. Recommended Directory Structure
project-root │ ├── domain/ │ ├── model/ │ │ └── Entity.kt │ ├── repository/ │ │ └── EntityRepository.kt │ └── usecase/ │ ├── GetEntitiesUseCase.kt │ ├── GetEntityByIdUseCase.kt │ ├── AddEntityUseCase.kt │ ├── UpdateEntityUseCase.kt │ └── DeleteEntityUseCase.kt │ ├── data/ │ ├── local/ │ │ ├── db/ ← Room-related classes │ │ │ ├── AppDatabase.kt │ │ │ └── migrations/ │ │ ├── dao/ │ │ │ └── EntityDao.kt │ │ └── entity/ ← Room @Entity mappings │ │ └── EntityRoom.kt │ └── repository/ │ └── EntityRepositoryImpl.kt │ ├── ui/ │ ├── navigation/ │ │ └── AppNavigation.kt │ ├── screens/ │ │ ├── list/ │ │ ├── detail/ │ │ └── form/ │ └── theme/ │ └── MainActivity.kt
---

## 4. Dependency Graph (manual DI)

- `MainActivity` constructs:
  - `AppDatabase` (Room)
  - `EntityDao` (from database)
  - `EntityRepositoryImpl(dao)` — implements `EntityRepository`
  - UseCases (constructed with `EntityRepositoryImpl`)
  - ViewModels created with factories that receive UseCases

ViewModels only call UseCases. UseCases only call Repository interface. RepositoryImpl uses DAOs and data mappers.

---

## 5. Navigation & ViewModel patterns

- Use a `sealed class Screen` for routes (no raw strings).
- Each screen has a ViewModel exposing `StateFlow<UiState>`.
- UI collects `UiState` and only renders. All input actions delegated to ViewModel.

---

## 6. Detailed: Adding Database (Room) — Steps

1. Add Gradle dependencies
    - In `build.gradle` (module):
        - `implementation "androidx.room:room-runtime:2.x.x"`
        - `kapt "androidx.room:room-compiler:2.x.x"`
        - `implementation "androidx.room:room-ktx:2.x.x"`
    - Enable Kotlin kapt plugin and Kotlin parcelize if needed.

2. Define Room entity mappings (Data -> DB)
    - File: `data/local/entity/EntityRoom.kt`
    - Use `@Entity(tableName = "entities")`
    - Include primary key, columns. Use mapper functions to/from Domain model (`domain/model/Entity.kt`).

3. Create DAO interface
    - File: `data/local/dao/EntityDao.kt`
    - Define suspend functions / Flow:
        - `@Query("SELECT * FROM entities") fun getAll(): Flow<List<EntityRoom>>`
        - `@Query("SELECT * FROM entities WHERE id = :id") suspend fun getById(id: String): EntityRoom?`
        - `@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: EntityRoom)`
        - `@Update suspend fun update(entity: EntityRoom)`
        - `@Delete suspend fun delete(entity: EntityRoom)`

4. Create RoomDatabase
    - File: `data/local/db/AppDatabase.kt`
    - Annotate with `@Database(entities = [EntityRoom::class], version = 1)`
    - Provide abstract `fun entityDao(): EntityDao`
    - Optionally provide migrations folder `data/local/db/migrations/` for schema upgrades.

5. Provide database instance
    - Build the database in `MainActivity` or a `DatabaseModule` (manual DI):
        - `val db = Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()`
        - `val dao = db.entityDao()`

6. Implement Repository using DAO
    - File: `data/repository/EntityRepositoryImpl.kt`
    - Map Room entities to Domain models and expose Domain-friendly APIs:
        - `getAll(): Flow<List<Entity>> = dao.getAll().map { it.map(Room::toDomain) }`
        - CRUD operations call DAO and map inputs/outputs.

7. Handle threading and coroutines
    - DAO suspend functions run on provided coroutine context (use `Dispatchers.IO` for repository operations when needed).
    - Use `flow`/`map` for streams.

8. Migration & Testing
    - Add migration strategies before changing schema (use `migration` objects).
    - Write unit tests for DAO (use in-memory Room) and Repository.

9. Optional: Provide a simple in-memory fallback for debug builds
    - `Room.inMemoryDatabaseBuilder(...)` for tests or quick demo mode.

10. Update UseCases if validation or extra logic depends on persistence details (keep logic in UseCases).

---

## 7. Files to Create (summary)

List of key files to add for DB-enabled project:

- `domain/model/Entity.kt`
- `domain/repository/EntityRepository.kt`
- `domain/usecase/*` (Get/Add/Update/Delete)
- `data/local/entity/EntityRoom.kt`
- `data/local/dao/EntityDao.kt`
- `data/local/db/AppDatabase.kt`
- `data/local/db/migrations/*`
- `data/repository/EntityRepositoryImpl.kt`
- `ui/navigation/AppNavigation.kt`
- `ui/screens/...` (ViewModels + Screens)
- `MainActivity.kt`

---

## 8. Build Order

1. Domain (models, repository interfaces, usecases)
2. Data (Room entities, DAO, Database, RepositoryImpl)
3. UI (navigation, ViewModels, Composables)
4. MainActivity (construct DB, repository, usecases, pass to navigation)

---

## 9. Checklist

- [ ] Domain layer contains no Android imports
- [ ] DAO and Room classes isolated under `data/local`
- [ ] RepositoryImpl maps Room entities to Domain models
- [ ] UseCases validate business rules (not ViewModels)
- [ ] ViewModels depend only on UseCases
- [ ] Database migrations are planned and tested
- [ ] Tests for DAO and Repository (use in-memory DB)

---

## 10. Quick Example snippets (mapping patterns)

- Domain model: `domain/model/Entity.kt` (plain data class)
- Room entity: `data/local/entity/EntityRoom.kt` with `fun toDomain()` and companion `fun fromDomain(domain: Entity): EntityRoom`

Use these patterns consistently to keep domain layer clean and testable.

---
