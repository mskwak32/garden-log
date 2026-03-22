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
app/              # Entry point (MainActivity, AppApplication, DI: UseCaseModule, RepositoryModule)
domain/           # UseCase, Repository interfaces, Model (pure Kotlin)
data/             # RepositoryImpl, Entity-Domain Mapper, DI: RepositoryModule
feature/plant/    # Screen, ViewModel, State (UI Layer)
core/database/    # Room DB (Entity, DAO, Migration), DI: DatabaseModule
core/remote/      # Retrofit/OkHttp
core/file/        # File management
core/common_ui/   # BaseViewModel, ViewState/Event/Effect, Compose Theme, Icons, UI Components
```

**Data Flow:** `Screen → ViewModel → UseCase → Repository(interface) → RepositoryImpl → DAO/API`

**BaseViewModel:** Extend `BaseViewModel<UiState, Event, Effect>` from `core/common_ui`.

- `viewState: StateFlow<UiState>`, `setEvent(event)`, `effect: Flow<Effect>`

**DI:** Hilt, `@HiltViewModel(assistedFactory = VM.Factory::class)` + `@AssistedInject constructor`.

- UseCase binding: `app/di/UseCaseModule.kt` (`@Provides`)
- Repository binding: `data/di/RepositoryModule.kt` (`@Binds`)
- DB binding: `core/database/di/DatabaseModule.kt`

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

- **Kotlin**: 2.3.20 (JVM 17)
- **Compose BOM**: 2026.03.00, Material 3
- **AGP**: 9.1.0, SDK 36/29
- **Hilt**: 2.59.2, KSP
- **Room**: 2.8.4 (DB v6, migration required)
- **Retrofit**: 3.0.0, OkHttp 5.3.2, Kotlin Serialization
- **Coil**: 3.4.0 (image loading)
- **Timber**: 5.0.1 (logging)
- **Firebase BOM**: 34.11.0, Analytics, Crashlytics (disabled in Debug)

## Key Conventions

- UI models: `feature/.../model/*UiModel.kt`
- Domain↔Entity mapping: `data/mapper/`
- Compose Preview: use `core/common_ui` module `GardenLogTheme`
- Version catalog: `gradle/libs.versions.toml`
- TopBar: Extract as `private fun TopBar(...)` Composable when possible, not inline in Scaffold

## Database

`garden.db` (Room v6) — Entity: `PlantEntity`, `DiaryEntity`, `PictureEntity`,`DiaryPictureCrossRef`
Migration: `core/database/migration/`, schema export enabled

## Strings

When UI strings are needed, search `strings.xml` first for an existing match.
If none exists, add a new entry to `strings.xml` — do not hardcode.

## Code Conventions

- **Dialog files**: Each screen's dialogs must be written in a separate file (`*Dialog.kt`), not
  inline in the screen file.
- **Comments**: Add comments when logic is non-obvious (alarm handling branches, complex Flow
  combinations, etc.).