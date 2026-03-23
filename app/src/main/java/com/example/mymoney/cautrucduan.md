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

## 3. Recommended Directory Structure (Feature-Based)

```
project-root
├── domain/
│   ├── model/
│   │   └── Entity.kt
│   ├── repository/
│   │   └── EntityRepository.kt
│   └── usecase/
│       ├── GetEntitiesUseCase.kt
│       ├── GetEntityByIdUseCase.kt
│       ├── AddEntityUseCase.kt
│       ├── UpdateEntityUseCase.kt
│       └── DeleteEntityUseCase.kt
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   └── migrations/
│   │   ├── dao/
│   │   │   └── EntityDao.kt
│   │   └── entity/
│   │       └── EntityRoom.kt
│   └── repository/
│       └── EntityRepositoryImpl.kt
│
├── ui/
│   ├── components/                    ← Global, reusable UI components
│   │   ├── buttons/
│   │   │   ├── PrimaryButton.kt
│   │   │   └── SecondaryButton.kt
│   │   ├── cards/
│   │   │   └── EntityCard.kt
│   │   ├── dialogs/
│   │   │   └── ConfirmDialog.kt
│   │   └── loaders/
│   │       └── LoadingSpinner.kt
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   └── Theme.kt
│   │
│   ├── navigation/
│   │   ├── AppNavigation.kt           ← Main NavHost
│   │   └── Screen.kt                  ← Sealed class for routes
│   │
│   ├── onboarding/                    ← Feature 1: Onboarding
│   │   ├── presentation/
│   │   │   ├── OnboardingScreen.kt    ← Main Composable
│   │   │   ├── OnboardingViewModel.kt
│   │   │   ├── OnboardingUiState.kt   ← data class
│   │   │   └── OnboardingUiEvent.kt   ← sealed class
│   │   └── components/                ← Feature-specific UI (optional)
│   │       └── OnboardingStep.kt
│   │
│   ├── main/                          ← Feature 2: Main/Dashboard
│   │   ├── presentation/
│   │   │   ├── MainScreen.kt
│   │   │   ├── MainViewModel.kt
│   │   │   ├── MainUiState.kt
│   │   │   └── MainUiEvent.kt
│   │   └── components/
│   │       └── SummaryCard.kt
│   │
│   ├── list/                          ← Feature 3: List View
│   │   ├── presentation/
│   │   │   ├── ListScreen.kt
│   │   │   ├── ListViewModel.kt
│   │   │   ├── ListUiState.kt
│   │   │   └── ListUiEvent.kt
│   │   └── components/
│   │       └── ListItem.kt
│   │
│   ├── detail/                        ← Feature 4: Detail View
│   │   ├── presentation/
│   │   │   ├── DetailScreen.kt
│   │   │   ├── DetailViewModel.kt
│   │   │   ├── DetailUiState.kt
│   │   │   └── DetailUiEvent.kt
│   │   └── components/
│   │       └── DetailHeader.kt
│   │
│   └── form/                          ← Feature 5: Form (Create/Edit)
│       ├── presentation/
│       │   ├── FormScreen.kt
│       │   ├── FormViewModel.kt
│       │   ├── FormUiState.kt
│       │   └── FormUiEvent.kt
│       └── components/
│           ├── FormField.kt
│           └── FormActions.kt
│
└── MainActivity.kt
```

### 3.1 Directory Structure Explanation

**Feature-Based Architecture:**
- Each feature folder (`onboarding/`, `main/`, `list/`, `detail/`, `form/`) is self-contained
- All related logic (Screen, ViewModel, State, Event) lives together
- Reduces cross-feature dependencies, improves scalability and maintainability

**Global vs Local Components:**
- `ui/components/`: Reusable components across multiple features (buttons, cards, dialogs, loaders)
- `feature/components/`: Feature-specific UI components used only within that feature
  - Example: `onboarding/components/OnboardingStep.kt` is only used in onboarding feature
  - Example: `ui/components/PrimaryButton.kt` is used across all features

**Each Feature Contains:**
- `presentation/`: Screen Composable, ViewModel, State, Event
- `components/`: (Optional) Local UI components specific to this feature

### 3.2 Advantages of Feature-Based Structure

1. **Scalability**: Easy to add new features without affecting existing ones
2. **Modularity**: Each feature is independent; can be developed/tested in isolation
3. **Team Collaboration**: Different team members can work on different features without conflicts
4. **Code Organization**: Related code is grouped together (no scattered files)
5. **Easier Refactoring**: Moving/removing a feature affects only its own folder
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

## 5. Navigation & MVVM Pattern (Feature-Based)

### 5.1 Route Definition
- Define all routes as a `sealed class Screen` in `ui/navigation/Screen.kt` (no raw strings):
  ```kotlin
  sealed class Screen(val route: String) {
      data object Onboarding : Screen("onboarding")
      data object Main : Screen("main")
      data object List : Screen("list")
      data class Detail(val id: String) : Screen("detail/{id}") {
          fun createRoute(id: String) = "detail/$id"
      }
      data object Form : Screen("form")
  }
  ```

### 5.2 Feature ViewModel Pattern
Each feature has a dedicated ViewModel that:
- Exposes `StateFlow<UiState>` (immutable state holder)
- Receives user actions via `fun onEvent(event: UiEvent)`
- Manages navigation state within UiState
- Only calls UseCases; never touches UI directly

**Example: `onboarding/presentation/OnboardingViewModel.kt`**
```kotlin
data class OnboardingUiState(
    val currentStep: Int = 0,
    val navigateToNext: Boolean = false  // Navigation flag
)

sealed class OnboardingUiEvent {
    data object NextClicked : OnboardingUiEvent()
    data object SkipClicked : OnboardingUiEvent()
}

class OnboardingViewModel(
    private val someUseCase: SomeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.NextClicked -> {
                // Update state, ViewModel decides navigation
                _uiState.update { it.copy(navigateToNext = true) }
            }
            // ...
        }
    }
}
```

### 5.3 Feature Screen Composable (Stateless)
- Observes StateFlow using `collectAsState()`
- Sends user actions to ViewModel via events
- Does NOT contain business logic
- Triggers navigation based on state changes

**Example: `onboarding/presentation/OnboardingScreen.kt`**
```kotlin
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    navController: NavController
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate when state indicates it
    LaunchedEffect(state.navigateToNext) {
        if (state.navigateToNext) {
            navController.navigate(Screen.Main.route)
        }
    }

    // UI only renders based on state
    Column {
        Text("Onboarding Step: ${state.currentStep}")
        PrimaryButton(
            text = "Next",
            onClick = { viewModel.onEvent(OnboardingUiEvent.NextClicked) }
        )
    }
}
```

### 5.4 Main Navigation (NavHost)
Create `ui/navigation/AppNavigation.kt`:
```kotlin
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.List.route) {
            ListScreen(navController = navController)
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailScreen(id = id, navController = navController)
        }
        composable(Screen.Form.route) {
            FormScreen(navController = navController)
        }
    }
}
```

### 5.5 Key MVVM Principles

| Principle | Explanation |
|-----------|-------------|
| **State Hoisting** | ViewModel holds state, UI observes and renders |
| **Single Source of Truth** | State comes only from StateFlow, never duplicated in Composables |
| **Event-Driven** | UI sends events to ViewModel; ViewModel updates state; Composables react to state changes |
| **Stateless Composables** | No @Composable uses `mutableStateOf` or `remember` for business state |
| **Navigation via State** | Navigation happens when ViewModel updates state, not directly in UI |

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

## 7. Files to Create (Feature-Based Summary)

**Domain Layer:**
- `domain/model/Entity.kt`
- `domain/repository/EntityRepository.kt`
- `domain/usecase/GetEntitiesUseCase.kt`
- `domain/usecase/GetEntityByIdUseCase.kt`
- `domain/usecase/AddEntityUseCase.kt`
- `domain/usecase/UpdateEntityUseCase.kt`
- `domain/usecase/DeleteEntityUseCase.kt`

**Data Layer:**
- `data/local/db/AppDatabase.kt`
- `data/local/dao/EntityDao.kt`
- `data/local/entity/EntityRoom.kt`
- `data/local/db/migrations/*`
- `data/repository/EntityRepositoryImpl.kt`

**UI Layer - Global:**
- `ui/theme/Color.kt`
- `ui/theme/Typography.kt`
- `ui/theme/Theme.kt`
- `ui/components/buttons/PrimaryButton.kt`
- `ui/components/cards/EntityCard.kt`
- `ui/components/dialogs/ConfirmDialog.kt`
- `ui/components/loaders/LoadingSpinner.kt`
- `ui/navigation/Screen.kt`
- `ui/navigation/AppNavigation.kt`

**UI Layer - Features:**
- `ui/onboarding/presentation/OnboardingScreen.kt`
- `ui/onboarding/presentation/OnboardingViewModel.kt`
- `ui/onboarding/presentation/OnboardingUiState.kt`
- `ui/onboarding/presentation/OnboardingUiEvent.kt`
- `ui/onboarding/components/OnboardingStep.kt` (optional)

- `ui/main/presentation/MainScreen.kt`
- `ui/main/presentation/MainViewModel.kt`
- `ui/main/presentation/MainUiState.kt`
- `ui/main/presentation/MainUiEvent.kt`
- `ui/main/components/SummaryCard.kt` (optional)

- `ui/list/presentation/ListScreen.kt`
- `ui/list/presentation/ListViewModel.kt`
- `ui/list/presentation/ListUiState.kt`
- `ui/list/presentation/ListUiEvent.kt`
- `ui/list/components/ListItem.kt` (optional)

- `ui/detail/presentation/DetailScreen.kt`
- `ui/detail/presentation/DetailViewModel.kt`
- `ui/detail/presentation/DetailUiState.kt`
- `ui/detail/presentation/DetailUiEvent.kt`

- `ui/form/presentation/FormScreen.kt`
- `ui/form/presentation/FormViewModel.kt`
- `ui/form/presentation/FormUiState.kt`
- `ui/form/presentation/FormUiEvent.kt`

**Root:**
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
