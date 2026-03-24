# CLAUDE.md

## Project

Plant management & growth diary Android app.
Compose + Navigation 3 + MVI + Multi-module (Clean Architecture)

## Build

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # install
./gradlew test                   # unit tests
./gradlew :domain:test           # per-module test
./gradlew connectedAndroidTest   # instrumented tests
```

## Modules

```
app/              # Entry point, DI (UseCaseModule, RepositoryModule), alarm
domain/           # UseCase, Repository interfaces, Model (pure Kotlin)
data/             # RepositoryImpl, Entity↔Domain Mapper, DI: RepositoryModule
feature/plant/    # All screens, ViewModels, States (UI layer)
core/database/    # Room DB (Entity, DAO, Migration), DI: DatabaseModule
core/remote/      # Retrofit/OkHttp
core/file/        # File management
core/pdf/         # PDF generation (diary export)
core/analytics/   # Firebase Analytics
core/common_ui/   # BaseViewModel, ViewState/Event/Effect, Theme, Icons, UI Components
```

**Data flow:** `Screen → ViewModel → UseCase → Repository(interface) → RepositoryImpl → DAO/API`

## BaseViewModel

Extend `BaseViewModel<UiState, Event, Effect>` from `core/common_ui`.
- `viewState: StateFlow<UiState>`, `setEvent(event)`, `effect: Flow<Effect>`

## DI

Hilt. `@HiltViewModel(assistedFactory = VM.Factory::class)` + `@AssistedInject constructor`.
- UseCase: `app/di/UseCaseModule.kt` (`@Provides`)
- Repository: `data/di/RepositoryModule.kt` (`@Binds`)
- DB: `core/database/di/DatabaseModule.kt`

## Navigation 3

**NavKey:** `...NavKey.kt`, `data object FooNavKey : NavKey` or `data class FooNavKey(val id: Int) : NavKey`

**ViewModel with NavKey:** Use `@AssistedInject` + `@Assisted navKey`. Do NOT use `SavedStateHandle`.
```kotlin
@AssistedFactory interface Factory { fun create(navKey: FooNavKey): FooViewModel }
```

**Screen:** No NavKey param, only ViewModel + navigate callback.

**NavGraph:**
```kotlin
entry<FooNavKey> {
    val viewModel = hiltViewModel<FooViewModel, FooViewModel.Factory>(
        creationCallback = { factory -> factory.create(it) }
    )
    FooScreen(viewModel = viewModel, navigate = { ... })
}
```
**hiltViewModel import:** `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`

## Screens

- **Plant tab:** PlantList → PlantDetail → PlantEdit / DiaryEdit / DiaryMore / DiaryExport
- **Diary tab:** DiaryList → DiaryDetail → DiaryEdit
- **Setting tab:** Setting → ExportedDiaryList
- **Shared:** DiaryDetail (from both Plant and Diary tabs)

## Tech Stack

- Kotlin 2.3.20, JVM 17
- Compose BOM 2026.03.00, Material 3
- AGP 9.1.0, SDK 36/minSdk 29
- Hilt 2.59.2, KSP
- Room 2.8.4 (DB v6, migration required)
- Navigation 3 1.1.0-beta01
- Retrofit 3.0.0, OkHttp 5.3.2, Kotlin Serialization
- Coil 3.4.0, Lottie 6.7.1
- Firebase BOM 34.11.0 (Analytics, Crashlytics — disabled in Debug)

## Conventions

- UI models: `feature/.../model/*UiModel.kt`
- Domain↔Entity mapping: `data/mapper/`
- Preview: `GardenLogTheme` from `core/common_ui`
- Versions: `gradle/libs.versions.toml`
- TopBar: extract as `private fun TopBar(...)`, not inline in Scaffold
- Dialogs: separate file `*Dialog.kt` per screen
- Strings: search `strings.xml` first; add new entry if missing — no hardcoding
- Comments: add for non-obvious logic (alarm branches, complex Flow combinations)

## Database

`garden.db` (Room v6) — `PlantEntity`, `DiaryEntity`, `PictureEntity`, `DiaryPictureCrossRef`
Migrations: `core/database/migration/`, schema export enabled
