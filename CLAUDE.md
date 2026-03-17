# CLAUDE.md

## Project Context

Plant management and growth diary Android app.
Compose + Navigation 3 + StateFlow/MVI + Multi-module (Clean Architecture)

## Build Commands

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # install & run
./gradlew build                  # full build
./gradlew test                   # unit tests
./gradlew :domain:test           # per-module test
./gradlew connectedAndroidTest   # instrumented tests
./gradlew clean assembleDebug    # clean + build
```

## Architecture

**Clean Architecture + MVVM/MVI**, multi-module.

```
app/              # Entry point (MainActivity, AppApplication)
domain/           # UseCase, Repository interfaces, Model (pure Kotlin)
data/             # RepositoryImpl, Entity-Domain Mapper
design/           # Compose Theme, Icon, UI Component
feature/plant/    # Screen, ViewModel, State (UI Layer)
core/database/    # Room DB (Entity, DAO, Migration)
core/remote/      # Retrofit/OkHttp
core/file/        # File management
core/common_ui/   # BaseViewModel, ViewState/Event/Effect interfaces
```

**Data Flow:** `Screen → ViewModel → UseCase → Repository(interface) → RepositoryImpl → DAO/API`

**BaseViewModel:** Extend `BaseViewModel<UiState, Event, Effect>` from `core/common_ui`.

- `viewState: StateFlow<UiState>`, `setEvent(event)`, `effect: Flow<Effect>`

**DI:** Hilt, `@HiltViewModel(assistedFactory = VM.Factory::class)` + `@AssistedInject constructor`.

- `data/di/RepositoryModule.kt`, `core/database/di/DatabaseModule.kt`

## Navigation 3 Pattern

**NavKey:** Each screen route is named `...NavKey`, defined in a separate file (`...NavKey.kt`).

- No params: `data object FooNavKey : NavKey`
- With params: `data class FooNavKey(val id: Int) : NavKey`

**ViewModel:** ViewModels needing NavKey data use `@AssistedInject` + `@Assisted navKey: FooNavKey`.

- Do NOT use `SavedStateHandle["key"]` or `savedStateHandle.toRoute<T>()` (incompatible with Nav3)
- Add `@AssistedFactory interface Factory { fun create(navKey: FooNavKey): FooViewModel }` at class
  end

**Screen composable:** Does not receive NavKey/params directly. Only takes ViewModel with default
value.

```kotlin
fun FooScreen(
    viewModel: FooViewModel = hiltViewModel(),
    navigate: (FooEffect.Navigation) -> Unit
)
```

**NavGraph:** Create ViewModel in `entry<FooNavKey>` block and pass to Screen.

```kotlin
entry<FooNavKey> {
    val viewModel = hiltViewModel<FooViewModel, FooViewModel.Factory>(
        creationCallback = { factory -> factory.create(it) }
    )
    FooScreen(viewModel = viewModel, navigate = { ... })
}
```

**hiltViewModel import:** `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`

## Tech Stack

- **Kotlin**: 2.2.21 (JVM 17)
- **Compose BOM**: 2025.11.00, Material 3
- **AGP**: 8.13.0, SDK 36/29
- **Hilt**: 2.57.2, KSP
- **Room**: 2.8.3 (DB v4, migration required)
- **Retrofit**: 3.0.0, OkHttp 5.3.0, Kotlin Serialization
- **Coil**: 3.3.0 (image loading)
- **Timber**: 5.0.1 (logging)
- **Firebase BOM**: 34.5.0, Analytics, Crashlytics (disabled in Debug)

## Key Conventions

- UI models: `feature/.../model/*UiModel.kt`
- Domain↔Entity mapping: `data/mapper/`
- Compose Preview: use `design` module Theme
- Version catalog: `gradle/libs.versions.toml`
- TopBar: Extract as `private fun TopBar(...)` Composable when possible, not inline in Scaffold

## Database

`garden.db` (Room v4) — Entity: `PlantEntity`, `DiaryEntity`
Migration: `core/database/migration/`, schema export enabled

## Strings

When UI strings are needed, search `strings.xml` first for an existing match.
If none exists, add a new entry to `strings.xml` — do not hardcode.