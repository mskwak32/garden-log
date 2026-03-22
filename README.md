# 텃밭 일기 (Garden Log)

텃밭에서 키우는 식물을 관리하고, 성장 일기를 기록하는 Android 앱입니다.

> 기존 `garden-daily-log` 프로젝트를 Clean Architecture + Jetpack Compose 기반으로 리뉴얼한 버전입니다.

[![Google Play](https://img.shields.io/badge/Google%20Play-다운로드-green?logo=google-play)](https://play.google.com/store/apps/details?id=com.mskwak.gardendailylog)

## 주요 기능

### 식물 관리
- 텃밭에 등록된 식물 목록 조회
- 식물별 상세 정보 (심은 날짜, 급수 주기, 마지막 물준 날짜)
- 식물 추가/수정/삭제
- 카메라 또는 갤러리에서 식물 사진 등록

### 일기 기록
- 식물별 성장 일기 작성 및 조회
- 사진 첨부 지원
- 날짜/식물별 정렬 및 필터링

### 물주기 알람
- 식물별 급수 주기 설정
- 물주기 알람 등록 및 관리
- 부팅 시 알람 자동 복구

## 스크린샷

<p align="center">
  <img src="playstore/playstore%20images/슬라이드1.PNG" width="19%" alt="홈 화면"/>
  <img src="playstore/playstore%20images/슬라이드2.PNG" width="19%" alt="물주기 알림"/>
  <img src="playstore/playstore%20images/슬라이드3.PNG" width="19%" alt="식물 상세"/>
  <img src="playstore/playstore%20images/슬라이드4.PNG" width="19%" alt="일기 작성"/>
  <img src="playstore/playstore%20images/슬라이드5.PNG" width="19%" alt="일기 목록"/>
</p>

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Kotlin 2.3.20 |
| UI | Jetpack Compose (BOM 2026.03.00), Material 3 |
| Architecture | Clean Architecture, MVVM/MVI |
| DI | Hilt 2.59.2 |
| Navigation | Navigation 3 1.1.0-beta01 |
| Database | Room 2.8.4 |
| Network | Retrofit 3.0.0, OkHttp 5.3.2 |
| Image | Coil 3.4.0 |
| Animation | Lottie 6.7.1 |
| Logging | Timber 5.0.1 |
| Monitoring | Firebase Analytics, Crashlytics |

## 아키텍처

Clean Architecture 기반의 멀티모듈 구조로, MVI 패턴을 사용합니다.

### 모듈 구조

```
garden-log/
├── app/                  # 앱 진입점 (MainActivity, AppApplication)
├── feature/
│   └── plant/            # 식물/일기 기능 UI Layer (Screen, ViewModel, State)
├── domain/               # 비즈니스 로직 (UseCase, Repository 인터페이스, Model)
├── data/                 # Repository 구현체, Entity↔Domain 매퍼
├── design/               # Compose Theme, 아이콘, 공통 UI 컴포넌트
└── core/
    ├── database/         # Room DB (Entity, DAO, Migration)
    ├── remote/           # Retrofit/OkHttp
    ├── file/             # 파일 관리
    └── common_ui/        # BaseViewModel, ViewState/Event/Effect 인터페이스
```

### 데이터 흐름

```
Screen (Compose) → ViewModel → UseCase → Repository 인터페이스
                                              ↓
                              RepositoryImpl → DAO / API
```

### BaseViewModel 패턴

`core/common_ui`의 `BaseViewModel<UiState, Event, Effect>`를 상속하여 MVI 패턴을 구현합니다.

```kotlin
class PlantListViewModel : BaseViewModel<PlantListState, PlantListEvent, PlantListEffect>() {
    // viewState: StateFlow<UiState> — UI 상태
    // setEvent(event)              — 이벤트 발행
    // effect: Flow<Effect>         — 일회성 Side Effect (Navigation 등)
}
```

### Navigation 구조

Navigation 3 기반 백스택 시스템으로, 하단 탭 3개를 제공합니다.

| 탭 | Screen |
|---|---|
| 홈 | PlantListScreen → PlantDetailScreen → PlantEditScreen |
| 일기 | DiaryListScreen |
| 설정 | SettingScreen |

## 빌드 요구사항

- Android Studio Meerkat 이상
- JDK 17
- Android SDK 36
- 최소 지원 버전: Android 10 (API 29)

## 필요한 권한

| 권한 | 용도 |
|---|---|
| `POST_NOTIFICATIONS` | 물주기 알람 알림 (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | 정확한 시간에 알람 실행 |
| `RECEIVE_BOOT_COMPLETED` | 부팅 후 알람 복구 |

## 의존성 관리

버전 카탈로그(`gradle/libs.versions.toml`)로 중앙 관리합니다.

---
