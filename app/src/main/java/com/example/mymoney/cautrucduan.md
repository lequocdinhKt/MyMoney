# 📦 New Project — Project Structure & Database Integration Guide
> Clean Architecture + MVVM + Jetpack Compose  
> Kotlin · Navigation Compose · StateFlow · Manual DI

---

## File
`README.md`

---

## 1. Overview

This document explains the recommended project structure for a new Android app using Clean Architecture and how to add a local database layer (Room). The structure separates responsibilities into **four independent layers**: UI, Presentation, Domain, Data.

- **Domain** is platform-agnostic and contains core business rules.
- **Data** implements Domain interfaces and handles persistence.
- **Presentation** manages state and logic (ViewModels), bridging Domain and UI.
- **UI** is purely declarative — it only renders state and emits events.

---

## 2. Architecture Layers (high level)

### Layer Definitions

| Layer | Contents | Responsibility |
|-------|----------|----------------|
| **UI Layer** | Jetpack Compose Composables | Render state, observe StateFlow, send Events — NO business logic |
| **Presentation Layer** | ViewModels, UiState, UiEvent, NavEvent (Contracts) | Manage UI state & logic, call Use Cases, emit navigation events — NO direct UI interaction |
| **Domain Layer** | Models, Repository Interfaces, Use Cases | Core business rules — pure Kotlin, platform-agnostic |
| **Data Layer** | Repository Implementations, Room DAOs, Data Sources | Implement Domain interfaces, handle local/remote data |

### Dependency Rules (STRICT)

```
UI  →  Presentation  →  Domain  ←  Data
```

- UI depends on Presentation (observes ViewModel state, sends events).
- Presentation depends on Domain (calls Use Cases).
- Data depends on Domain (implements Repository interfaces).
- **Domain MUST NOT depend on Android, UI, or database libraries.**
- **ViewModel MUST NOT know about any Composable or UI component.**
- **UI MUST NOT contain any business logic.**

---

## 3. Recommended Directory Structure (Layer-Based)

```
project-root
│
├── domain/                                    ← Pure Kotlin, no Android dependencies
│   ├── model/
│   │   └── Entity.kt                          ← Domain model (plain data class)
│   ├── repository/
│   │   └── EntityRepository.kt                ← Repository interface (contract)
│   └── usecase/
│       ├── GetEntitiesUseCase.kt
│       ├── GetEntityByIdUseCase.kt
│       ├── AddEntityUseCase.kt
│       ├── UpdateEntityUseCase.kt
│       └── DeleteEntityUseCase.kt
│
├── data/                                      ← Implements domain interfaces
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   └── migrations/
│   │   ├── dao/
│   │   │   └── EntityDao.kt
│   │   ├── entity/
│   │   │   └── EntityRoom.kt                  ← Room entity + toDomain() mapper
│   │   ├── datastore/
│   │   │   └── SettingPreferences.kt          ← DataStore preferences
│   │   └── static/
│   │       └── OnboardingData.kt              ← Static/hardcoded data
│   └── repository/
│       └── EntityRepositoryImpl.kt            ← Implements EntityRepository
│
├── presentation/                              ← ViewModels + Contracts (State, Event, NavEvent)
│   └── viewmodel/
│       ├── onboarding/
│       │   ├── OnboardingViewModel.kt         ← Calls Use Cases, manages state
│       │   └── onboarding/
│       │       └── OnboardingContract.kt      ← OnboardingUiState, OnboardingEvent, OnboardingNavEvent
│       ├── main/
│       │   ├── MainViewModel.kt
│       │   └── main/
│       │       └── MainContract.kt
│       ├── home/
│       │   ├── HomeViewModel.kt
│       │   └── home/
│       │       └── HomeContract.kt
│       ├── budget/
│       │   ├── BudgetViewModel.kt
│       │   └── budget/
│       │       └── BudgetContract.kt
│       ├── saving/
│       │   ├── SavingViewModel.kt
│       │   └── saving/
│       │       └── SavingContract.kt
│       └── other/
│           ├── OtherViewModel.kt
│           └── other/
│               └── OtherContract.kt
│
└── ui/                                        ← Jetpack Compose only — render & observe
    ├── components/                            ← Global reusable UI components
    │   ├── buttons/
    │   │   ├── PrimaryButton.kt
    │   │   └── SecondaryButton.kt
    │   ├── cards/
    │   │   └── EntityCard.kt
    │   ├── dialogs/
    │   │   └── ConfirmDialog.kt
    │   └── loaders/
    │       └── LoadingSpinner.kt
    │
    ├── theme/
    │   ├── Color.kt
    │   ├── Typography.kt
    │   └── Theme.kt
    │
    ├── navigation/
    │   ├── AppNavigation.kt                   ← Main NavHost
    │   └── Screen.kt                          ← Sealed class for routes
    │
    ├── onboarding/                            ← Feature: Onboarding screens
    │   ├── OnboardingScreen.kt                ← Composable only, no logic
    │   └── components/                        ← Feature-specific UI (optional)
    │       └── OnboardingPageLayout.kt
    │
    ├── main/                                  ← Feature: Main shell with BottomNav
    │   ├── MainScreen.kt
    │   └── components/
    │       └── BottomNavBar.kt
    │
    ├── home/                                  ← Feature: Home tab
    │   └── HomeScreen.kt
    │
    ├── budget/                                ← Feature: Budget tab
    │   └── BudgetScreen.kt
    │
    ├── saving/                                ← Feature: Saving tab
    │   └── SavingScreen.kt
    │
    └── other/                                 ← Feature: Other tab
        └── OtherScreen.kt

└── MainActivity.kt                            ← Entry point, reads DataStore, sets startDestination
```

### 3.1 Directory Structure Explanation

#### `domain/` — Business Rules (Pure Kotlin)
- Contains **models**, **repository interfaces**, and **use cases**.
- **MUST NOT** import Android SDK, Room, or any UI library.
- Use cases encapsulate one specific business operation each.
- Repository interfaces define *what* data operations are available — not *how* they are done.

#### `data/` — Data Sources (Implements Domain)
- Implements repository interfaces defined in `domain/`.
- Contains Room entities, DAOs, DataStore helpers, and static local data.
- Mapper functions (`toDomain()`, `fromDomain()`) live here to keep Domain clean.
- **MUST NOT** be accessed directly by ViewModels — only through Domain Use Cases.

#### `presentation/` — ViewModel + State Logic (Bridges Domain ↔ UI)
- Contains all **ViewModels** organized by feature under `presentation/viewmodel/<feature>/`.
- Each feature has a **Contract file** in a sub-folder of the same name:
  - `presentation/viewmodel/onboarding/onboarding/OnboardingContract.kt`
  - Defines `UiState` (immutable data class), `UiEvent` (sealed class), `NavEvent` (sealed class for navigation side-effects via `SharedFlow`).
- **ViewModel rules:**
  - Calls Use Cases to perform operations.
  - Exposes `StateFlow<UiState>` — never exposes raw mutable state.
  - Emits navigation side-effects via `SharedFlow<NavEvent>`.
  - **MUST NOT** import or reference any Composable or Android View.
  - **MUST NOT** create dependencies manually — use DI (Hilt/Koin or manual factory).

#### `ui/` — Composable Screens (Purely Declarative)
- Contains **only Jetpack Compose UI** organized by feature.
- Each screen observes `StateFlow` via `collectAsStateWithLifecycle()`.
- Navigation side-effects are collected via `LaunchedEffect` + `SharedFlow`.
- Sends user interactions to ViewModel using `onEvent(UiEvent)`.
- **MUST NOT** contain business logic, direct database calls, or API calls.
- **Global components** (`ui/components/`): reusable across all features.
- **Feature-local components** (`ui/<feature>/components/`): used only within that feature.

#### Separation Summary

| Rule | Enforced Where |
|------|---------------|
| ViewModel does not know about UI | `presentation/` never imports `ui/` |
| UI has no business logic | `ui/` never imports `domain/` or `data/` directly |
| Domain is platform-free | `domain/` has zero Android imports |
| Data only talks to Domain | `data/` implements `domain/repository/` interfaces only |

### 3.2 Advantages of This Layer-Based Structure

1. **Clear Separation of Concerns**: Each layer has one explicit responsibility.
2. **Testability**: ViewModels and Use Cases can be unit-tested without Android dependencies.
3. **Scalability**: Adding a feature means adding files in each layer — no existing code changes.
4. **Maintainability**: Bugs are easier to isolate — UI bug → `ui/`, logic bug → `presentation/` or `domain/`.
5. **AI-Friendly**: Clear boundaries make it easy for code generation to produce correct, non-leaking code.
6. **Onboarding**: New developers instantly know where to find and place code.

---

## 4. Dependency Graph (manual DI)

- `MainActivity` constructs:
  - `AppDatabase` (Room)
  - `EntityDao` (from database)
  - `EntityRepositoryImpl(dao)` — implements `EntityRepository`
  - UseCases (constructed with `EntityRepositoryImpl`)
  - ViewModels created with factories that receive UseCases

**Flow: `MainActivity` → `ViewModel Factory` → `UseCase` → `RepositoryImpl` → `DAO`**

ViewModels only call UseCases. UseCases only call Repository interfaces. RepositoryImpl uses DAOs and data mappers.

---

## 5. Navigation & MVVM Pattern (Layer-Based)

### 5.1 Route Definition
Define all routes as a `sealed class Screen` in `ui/navigation/Screen.kt` (no raw strings):
```kotlin
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Budget : Screen("budget")
    data object Saving : Screen("saving")
    data object Other : Screen("other")
    data class Detail(val id: String) : Screen("detail/{id}") {
        fun createRoute(id: String) = "detail/$id"
    }
}
```

### 5.2 Feature ViewModel Pattern

Each feature has:
- A **ViewModel** in `presentation/viewmodel/<feature>/`
- A **Contract file** in `presentation/viewmodel/<feature>/<feature>/` defining `UiState`, `UiEvent`, `NavEvent`

**Contract file example: `presentation/viewmodel/onboarding/onboarding/OnboardingContract.kt`**
```kotlin
// UiState: immutable — always updated via copy()
data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val isLoading: Boolean = false
)

// UiEvent: user interactions sent from UI to ViewModel
sealed class OnboardingEvent {
    data object OnNextClicked : OnboardingEvent()
}

// NavEvent: navigation side-effects emitted by ViewModel via SharedFlow
sealed class OnboardingNavEvent {
    data object NavigateToMain : OnboardingNavEvent()
}
```

**ViewModel example: `presentation/viewmodel/onboarding/OnboardingViewModel.kt`**
```kotlin
class OnboardingViewModel(
    private val saveOnboardingCompletedUseCase: SaveOnboardingCompletedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // Navigation side-effects — collected once in LaunchedEffect
    private val _navEvent = MutableSharedFlow<OnboardingNavEvent>()
    val navEvent: SharedFlow<OnboardingNavEvent> = _navEvent.asSharedFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnNextClicked -> handleNext()
        }
    }

    private fun handleNext() {
        val current = _uiState.value
        if (current.currentPage < current.totalPages - 1) {
            // Move to next page — state update only
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        } else {
            // Last page: save completion flag, then navigate
            viewModelScope.launch {
                saveOnboardingCompletedUseCase()       // Domain Use Case
                _navEvent.emit(OnboardingNavEvent.NavigateToMain)
            }
        }
    }
}
```

### 5.3 Feature Screen Composable (Stateless)

Composable file location: `ui/<feature_name>/<FeatureName>Screen.kt`

Rules:
- Observes `StateFlow` using `collectAsStateWithLifecycle()` (lifecycle-aware).
- Collects `SharedFlow` navigation events inside `LaunchedEffect(Unit)`.
- Sends user actions via `viewModel.onEvent(...)` — NO direct logic.
- **MUST NOT** import domain or data classes.

**Example: `ui/onboarding/OnboardingScreen.kt`**
```kotlin
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,          // Injected from NavHost or factory
    onNavigateToMain: () -> Unit             // Navigation callback from AppNavigation
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect navigation side-effects (SharedFlow) — runs once
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                OnboardingNavEvent.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    // Pure UI — renders state, sends events
    OnboardingPageLayout(
        page = pages[state.currentPage],
        onNextClicked = { viewModel.onEvent(OnboardingEvent.OnNextClicked) }
    )
}
```

### 5.4 Main Navigation (NavHost)

`ui/navigation/AppNavigation.kt` — receives `startDestination` from `MainActivity`:
```kotlin
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onNavigateToMain = {
                    onOnboardingFinished()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
    }
}
```

`MainActivity` reads DataStore and determines `startDestination`:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isOnboardingDone by settingPreferences.isOnboardingCompleted
                .collectAsStateWithLifecycle(initialValue = false)

            val startDestination = if (isOnboardingDone)
                Screen.Main.route else Screen.Onboarding.route

            MyMoneyTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    startDestination = startDestination,
                    onOnboardingFinished = { /* handled inside AppNavigation */ }
                )
            }
        }
    }
}
```

### 5.5 Key MVVM Principles

| Principle | Explanation |
|-----------|-------------|
| **State Hoisting** | ViewModel holds all state; UI only observes and renders |
| **Single Source of Truth** | State comes exclusively from `StateFlow` — never duplicated in Composables |
| **Event-Driven (UDF)** | UI sends events → ViewModel updates state → Composables re-render |
| **Stateless Composables** | No `@Composable` holds business state via `mutableStateOf`/`remember` |
| **Navigation via SharedFlow** | Navigation side-effects emitted through `SharedFlow<NavEvent>`, never stored in `UiState` boolean flags |
| **Contract Pattern** | Each feature's `UiState`, `UiEvent`, `NavEvent` are co-located in one Contract file |

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

## 7. Files to Create (Layer-Based Summary)

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
- `data/local/datastore/SettingPreferences.kt`
- `data/local/static/OnboardingData.kt`
- `data/repository/EntityRepositoryImpl.kt`

**Presentation Layer — ViewModels + Contracts:**
- `presentation/viewmodel/onboarding/OnboardingViewModel.kt`
- `presentation/viewmodel/onboarding/onboarding/OnboardingContract.kt`
- `presentation/viewmodel/main/MainViewModel.kt`
- `presentation/viewmodel/main/main/MainContract.kt`
- `presentation/viewmodel/home/HomeViewModel.kt`
- `presentation/viewmodel/home/home/HomeContract.kt`
- `presentation/viewmodel/budget/BudgetViewModel.kt`
- `presentation/viewmodel/budget/budget/BudgetContract.kt`
- `presentation/viewmodel/saving/SavingViewModel.kt`
- `presentation/viewmodel/saving/saving/SavingContract.kt`
- `presentation/viewmodel/other/OtherViewModel.kt`
- `presentation/viewmodel/other/other/OtherContract.kt`

**UI Layer — Global:**
- `ui/theme/Color.kt`
- `ui/theme/Typography.kt`
- `ui/theme/Theme.kt`
- `ui/components/buttons/PrimaryButton.kt`
- `ui/components/buttons/SecondaryButton.kt`
- `ui/components/cards/EntityCard.kt`
- `ui/components/dialogs/ConfirmDialog.kt`
- `ui/components/loaders/LoadingSpinner.kt`
- `ui/navigation/Screen.kt`
- `ui/navigation/AppNavigation.kt`

**UI Layer — Feature Screens:**
- `ui/onboarding/OnboardingScreen.kt`
- `ui/onboarding/components/OnboardingPageLayout.kt` (optional)
- `ui/main/MainScreen.kt`
- `ui/main/components/BottomNavBar.kt` (optional)
- `ui/home/HomeScreen.kt`
- `ui/budget/BudgetScreen.kt`
- `ui/saving/SavingScreen.kt`
- `ui/other/OtherScreen.kt`

**Root:**
- `MainActivity.kt`

---

## 8. Build Order & Dependencies

Build in this order to respect dependency direction:

1. **Domain** — models, repository interfaces, use cases (no dependencies)
2. **Data** — Room entities, DAO, Database, RepositoryImpl (depends on Domain interfaces only)
3. **Presentation** — Contracts (UiState/Event/NavEvent), ViewModels (depends on Domain Use Cases only)
4. **UI** — Composables, Navigation (depends on Presentation ViewModels + UI components only)
5. **MainActivity** — wires everything together (DI: constructs DB → Repository → UseCases → ViewModel factories)

**Dependency Direction:**
```
MainActivity
    ↓
AppNavigation  →  ViewModel (presentation/)
                      ↓
                  UseCase (domain/)
                      ↓
                  RepositoryImpl (data/)
                      ↓
                  DAO / DataStore (data/local/)
```

---

## 9. Checklist

- [ ] Domain layer contains zero Android imports
- [ ] DAO and Room classes isolated under `data/local/`
- [ ] RepositoryImpl maps Room entities to Domain models
- [ ] UseCases contain business rules — not ViewModels
- [ ] ViewModels depend only on Use Cases
- [ ] ViewModels do NOT import any Composable
- [ ] UI Composables do NOT import domain or data packages
- [ ] UiState, UiEvent, NavEvent defined in Contract files
- [ ] Navigation side-effects use `SharedFlow`, not `UiState` boolean flags
- [ ] Database migrations are planned and tested
- [ ] Tests for DAO and Repository (use in-memory DB)
- [ ] Tests for ViewModels (mock Use Cases)

---

## 10. Quick Example Snippets (Mapping Patterns)

- Domain model: `domain/model/Entity.kt` (plain data class, no annotations)
- Room entity: `data/local/entity/EntityRoom.kt` with `fun toDomain()` and companion `fun fromDomain(domain: Entity): EntityRoom`

```kotlin
// domain/model/Entity.kt
data class Entity(val id: String, val name: String)

// data/local/entity/EntityRoom.kt
@Entity(tableName = "entities")
data class EntityRoom(
    @PrimaryKey val id: String,
    val name: String
) {
    fun toDomain() = Entity(id = id, name = name)

    companion object {
        fun fromDomain(domain: Entity) = EntityRoom(id = domain.id, name = domain.name)
    }
}
```

Use these patterns consistently to keep the Domain layer clean and testable.

---
