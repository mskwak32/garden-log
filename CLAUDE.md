# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Build Commands

```bash
# 프로젝트 빌드
./gradlew assembleDebug

# 디바이스에 설치 및 실행
./gradlew installDebug

# 전체 빌드 (release 포함)
./gradlew build

# 단위 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :domain:test
./gradlew :data:test

# Android Instrumented 테스트
./gradlew connectedAndroidTest

# 캐시 정리 후 빌드
./gradlew clean assembleDebug
```

## Architecture

이 프로젝트는 **Clean Architecture + MVVM/MVI** 패턴을 따르는 멀티모듈 구조입니다.

### Module Structure

```
app/                    # 앱 진입점 (MainActivity, AppApplication)
domain/                 # 순수 Kotlin - 비즈니스 로직 (UseCase, Repository 인터페이스, Model)
data/                   # Repository 구현체, Entity-Domain Mapper
design/                 # Compose Theme, Icon, UI Component
feature/plant/          # Plant/Diary 기능의 UI Layer (Screen, ViewModel, State)
core/
  ├── database/         # Room DB (Entity, DAO, Migration)
  ├── remote/           # Retrofit/OkHttp
  ├── file/             # 파일 관리
  └── common_ui/        # BaseViewModel, ViewState/Event/Effect 인터페이스
```

### Data Flow

```
Screen (Compose) → ViewModel → UseCase → Repository (interface) → RepositoryImpl → DAO/API
```

### BaseViewModel Pattern

`core/common_ui`의 `BaseViewModel<UiState, Event, Effect>`를 상속하여 MVI 패턴 구현:

```kotlin
class PlantListViewModel : BaseViewModel<PlantListState, PlantListEvent, PlantListEffect>() {
    // viewState: StateFlow<UiState> - UI 상태
    // setEvent(event) - 이벤트 발행
    // effect: Flow<Effect> - 일회성 Side Effect (Navigation 등)
}
```

State 클래스는 `@Immutable` 어노테이션 필수, `ViewState` 인터페이스 구현 필요.

### Dependency Injection

Hilt 사용. 주요 모듈:

- `data/di/RepositoryModule.kt` - Repository 바인딩
- `core/database/di/DatabaseModule.kt` - Room DB 제공

ViewModel은 `@HiltViewModel` + `@Inject constructor` 패턴.

## Tech Stack

- **Kotlin**: 2.2.21 (JVM 17)
- **Compose BOM**: 2025.11.00, Material 3
- **AGP**: 8.13.0, SDK 36/29
- **Hilt**: 2.57.2, KSP
- **Room**: 2.8.3 (DB 버전 4, migration 필수)
- **Retrofit**: 3.0.0, OkHttp 5.3.0, Kotlin Serialization
- **Coil 3**: 이미지 로딩
- **Timber**: 로깅
- **Firebase**: Analytics, Crashlytics (Debug에서 비활성화)

## Key Conventions

- Feature 모듈의 UI 모델은 `model/` 패키지에 `*UiModel.kt`로 작성
- Domain 모델과 Entity는 `data/mapper/`에서 변환
- Compose Preview는 `design` 모듈의 Theme 사용
- 버전 카탈로그: `gradle/libs.versions.toml`
- Debug 빌드는 `.dev` suffix 적용

## Database

Room DB 파일: `garden.db` (버전 4)

- Entity: `PlantEntity`, `DiaryEntity`
- Migration: `core/database/migration/` 참조
- 스키마 export 활성화됨
