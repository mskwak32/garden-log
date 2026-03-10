# CLAUDE.md

## Project Context

기존 프로젝트(`~/Android Projects/gerden-daily-log`, XML/LiveData/단일모듈) → 리뉴얼
신규: Compose + Navigation 3 + StateFlow/MVI + 멀티모듈 (Clean Architecture)
기능 구현 시 기존 코드를 참고하되 새 패턴에 맞게 변환.

## Build Commands

```bash
./gradlew assembleDebug          # 빌드
./gradlew installDebug           # 설치 및 실행
./gradlew build                  # 전체 빌드
./gradlew test                   # 단위 테스트
./gradlew :domain:test           # 모듈별 테스트
./gradlew connectedAndroidTest   # Instrumented 테스트
./gradlew clean assembleDebug    # 캐시 정리 후 빌드
```

## Architecture

**Clean Architecture + MVVM/MVI**, 멀티모듈.

```
app/              # 진입점 (MainActivity, AppApplication)
domain/           # UseCase, Repository 인터페이스, Model (순수 Kotlin)
data/             # RepositoryImpl, Entity-Domain Mapper
design/           # Compose Theme, Icon, UI Component
feature/plant/    # Screen, ViewModel, State (UI Layer)
core/database/    # Room DB (Entity, DAO, Migration)
core/remote/      # Retrofit/OkHttp
core/file/        # 파일 관리
core/common_ui/   # BaseViewModel, ViewState/Event/Effect 인터페이스
```

**Data Flow:** `Screen → ViewModel → UseCase → Repository(interface) → RepositoryImpl → DAO/API`

**BaseViewModel:** `core/common_ui`의 `BaseViewModel<UiState, Event, Effect>` 상속.
- `viewState: StateFlow<UiState>`, `setEvent(event)`, `effect: Flow<Effect>`

**DI:** Hilt, `@HiltViewModel(assistedFactory = VM.Factory::class)` + `@AssistedInject constructor`.
- `data/di/RepositoryModule.kt`, `core/database/di/DatabaseModule.kt`

## Navigation 3 패턴

**NavKey:** 각 화면의 route는 `...NavKey` 이름으로, 별도 파일(`...NavKey.kt`)에 분리.
- 파라미터 없음: `data object FooNavKey : NavKey`
- 파라미터 있음: `data class FooNavKey(val id: Int) : NavKey`

**ViewModel:** NavKey 데이터가 필요한 ViewModel은 `@AssistedInject` + `@Assisted navKey: FooNavKey` 사용.
- `SavedStateHandle["key"]` 또는 `savedStateHandle.toRoute<T>()` 사용 금지 (Navigation 3 비호환)
- 클래스 끝에 `@AssistedFactory interface Factory { fun create(navKey: FooNavKey): FooViewModel }` 추가

**Screen 컴포저블:** NavKey/파라미터를 직접 받지 않음. ViewModel만 파라미터로 받고 기본값 유지.
```kotlin
fun FooScreen(
    viewModel: FooViewModel = hiltViewModel(),
    navigate: (FooEffect.Navigation) -> Unit
)
```

**NavGraph:** `entry<FooNavKey>` 블록에서 ViewModel을 생성해 Screen에 전달.
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
- **Room**: 2.8.3 (DB 버전 4, migration 필수)
- **Retrofit**: 3.0.0, OkHttp 5.3.0, Kotlin Serialization
- **Coil**: 3.3.0 (이미지 로딩)
- **Timber**: 5.0.1 (로깅)
- **Firebase BOM**: 34.5.0, Analytics, Crashlytics (Debug 비활성화)

## Key Conventions

- UI 모델: `feature/.../model/*UiModel.kt`
- Domain↔Entity 변환: `data/mapper/`
- Compose Preview: `design` 모듈 Theme 사용
- 버전 카탈로그: `gradle/libs.versions.toml`

## Database

`garden.db` (Room v4) — Entity: `PlantEntity`, `DiaryEntity`
Migration: `core/database/migration/`, 스키마 export 활성화

## string
ui string이 필요할 경우 strings.xml 먼저 탐색하여 적절한 string을 찾을 것.
없을 경우 hard coding하지 말고 strings.xml에 추가할 것