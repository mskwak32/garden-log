# 일기 상세(DiaryDetail) 기능 구현 계획

## 개요

일기 상세 화면. 선택한 일기의 날짜, 사진, 내용을 보여주며 수정 및 삭제 기능을 제공한다.

## 진입점

- **DiaryListScreen**: 일기 목록 아이템 클릭 시 진입
  - `DiaryListEffect.Navigation.GoToDiaryDetail(diaryId)` 이미 정의됨
  - `PlantNavGraph.kt`의 `DiaryListNavKey` entry에서 navigate 핸들러 연결 필요
- **PlantDetailScreen**: 식물 상세에서 diary item 클릭 시 진입
  - `PlantDetailEffect.Navigation.ToDiaryDetail` 이미 정의됨 (현재 `data object` → `data class ToDiaryDetail(val diaryId: Int)`로 변경 필요)
  - `PlantNavGraph.kt`의 `PlantDetailNavKey` entry에서 `ToDiaryDetail` navigate 핸들러 연결 필요

---

## 기능 명세

### TopBar
- 왼쪽: 뒤로가기(Back) 버튼
- 가운데: 식물 이름(plantName) 표시
- 오른쪽: 더보기 메뉴 아이콘
  - 메뉴 항목: **수정**, **삭제**
  - `AppDropDownMenu` 사용 (content 람다 오버로드 — `AppDropDownMenu(expanded, onDismissRequest) { ... }`)
    - 수정/삭제는 선택 상태가 없으므로 `selectedItem` 버전이 아닌 content 버전 사용
- private TopBar 함수로 따로 분리

### 날짜 표시
- `design/util/DateTimeTextUtil.kt`의 `toDateString()` 사용 (예: "2023. 3. 12.")

### 사진 목록
- 사진이 있을 경우에만 표시
- `HorizontalPager` (`androidx.compose.foundation.pager`) 사용
  - 아래에 dot indicator 표시
  - indicator: `design/ui_component/PagerDotIndicator.kt`로 신규 생성 (custom dot indicator)
- 사진 클릭 시 전체화면 뷰어 (현재 구현 범위 제외, TODO 주석)

### 일기 내용
- 메모 텍스트 표시
- 세로 스크롤 가능

### 수정
- TopBar 메뉴 > 수정 선택 시 `DiaryEditScreen`으로 이동
  - `DiaryEditNavKey(plantId = state.plantId, diaryId = state.diaryId)` 사용

### 삭제
- TopBar 메뉴 > 삭제 선택 시 확인 다이얼로그 표시
  - 확인 시: `DeleteDiaryUseCase` 호출 후 뒤로가기 (SaveComplete 유사 패턴)
  - 취소 시: 다이얼로그 닫기

---

## 생성/수정 파일 목록

| 파일 | 작업 |
|------|------|
| `diary_detail/DiaryDetailNavKey.kt` | 신규 생성 |
| `diary_detail/DiaryDetailState.kt` | 신규 생성 (UiState, Event, Effect) |
| `diary_detail/DiaryDetailViewModel.kt` | 신규 생성 |
| `diary_detail/DiaryDetailScreen.kt` | 신규 생성 |
| `PlantNavGraph.kt` | `DiaryDetailNavKey` entry 추가, `DiaryListNavKey` / `PlantDetailNavKey` entry navigate 핸들러 연결 |
| `plant_detail/PlantDetailState.kt` | `ToDiaryDetail` → `data class ToDiaryDetail(val diaryId: Int)`로 변경 |
| `plant_detail/PlantDetailViewModel.kt` | `OnDiaryClicked` 처리 시 `ToDiaryDetail(event.diaryId)` 전달로 수정 |
| `design/ui_component/PagerDotIndicator.kt` | 신규 생성 (HorizontalPager용 dot indicator 컴포넌트) |

---

## 구현 상세

### DiaryDetailNavKey.kt

```kotlin
@Serializable
data class DiaryDetailNavKey(val diaryId: Int) : NavKey
```

### DiaryDetailState.kt

**UiState (`DiaryDetailState`)**
```
isLoading: Boolean = true
diaryId: Int = 0
plantId: Int = 0
plantName: String = ""
diaryDate: LocalDate = LocalDate.now()
picturePaths: List<String> = emptyList()
memo: String = ""
```

**Event (`DiaryDetailEvent`)**
```
BackClick
EditClick
DeleteClick
DeleteConfirm
```

**Effect (`DiaryDetailEffect`)**
```
Navigation:
  Back
  GoToEdit(plantId: Int, diaryId: Int)
ShowDeleteConfirmDialog
```

### DiaryDetailViewModel.kt

- `@HiltViewModel(assistedFactory = DiaryDetailViewModel.Factory::class)`
- `@AssistedInject constructor(@Assisted navKey: DiaryDetailNavKey, ...)`
- 주입 UseCase:
  - `GetDiaryUseCase` — `Flow<Diary>` 구독으로 일기 데이터 로드
  - `GetPlantNameUseCase` — 식물 이름 로드
  - `DeleteDiaryUseCase` — 일기 삭제
- `init` 블록에서 `loadDiary()` 호출
- private property `currentDiary: Diary?` 보관 — `DeleteDiaryUseCase`가 `Diary` 전체 객체를 요구하므로 Flow 수집 시 저장
- `loadDiary()`: `GetDiaryUseCase(diaryId)`로 수집, `currentDiary`에 저장 후 state 업데이트
  - 수집 후 `getPlantNameUseCase.getName(plantId)`로 식물 이름 로드
- `deleteDiary()`: `currentDiary`로 `DeleteDiaryUseCase` 호출 후 `Navigation.Back` effect emit

### DiaryDetailScreen.kt

```kotlin
@Composable
fun DiaryDetailScreen(
    viewModel: DiaryDetailViewModel = hiltViewModel(),
    navigate: (DiaryDetailEffect.Navigation) -> Unit
)
```

- `viewModel.effect` 수집하여 Navigation 처리, ShowDeleteConfirmDialog 처리
- 삭제 확인 다이얼로그: `AlertDialog` 사용

---

## 단계별 구현 체크리스트

### Step 1: NavKey 및 State 정의
- [x] `DiaryDetailNavKey.kt` 생성
- [x] `DiaryDetailState.kt` 생성 (DiaryDetailState, DiaryDetailEvent, DiaryDetailEffect 정의)

### Step 2: ViewModel 구현
- [x] `DiaryDetailViewModel.kt` 생성
- [x] `@AssistedInject` + `@Assisted navKey` 패턴 적용
- [x] `loadDiary()` 구현 — `GetDiaryUseCase` Flow 수집, plantId 추출 후 `GetPlantNameUseCase` 호출
- [x] `handleEvents()` 구현 — BackClick, EditClick, DeleteClick, DeleteConfirm 처리
- [x] `deleteDiary()` 구현 — `DeleteDiaryUseCase` 호출 후 Back navigation effect emit
- [x] `Factory` interface 추가

### Step 3: Screen UI 구현
- [x] `DiaryDetailScreen.kt` 생성
- [x] `design/ui_component/PagerDotIndicator.kt` 생성 — `PagerState`를 받아 dot indicator를 그리는 컴포저블
- [x] TopBar 구현 (뒤로가기, 식물 이름, 더보기 메뉴) — private TopBar 함수로 분리
- [x] `AppDropDownMenu` content 오버로드로 드롭다운 구현 (수정, 삭제 항목)
- [x] 날짜 표시 영역 구현
- [x] `HorizontalPager` + `PagerDotIndicator` 사진 뷰페이저 구현 (picturePaths가 비어있을 경우 미표시)
- [x] 메모 텍스트 표시 구현
- [x] 삭제 확인 AlertDialog 구현
- [x] effect 수집 및 navigate/다이얼로그 처리 연결

### Step 4: Navigation 연결
- [x] `PlantNavGraph.kt`에 `entry<DiaryDetailNavKey>` 추가
  - `hiltViewModel<DiaryDetailViewModel, DiaryDetailViewModel.Factory>` 사용
  - `GoToEdit` effect → `backStack.add(DiaryEditNavKey(plantId, diaryId))`
  - `Back` effect → `backStack.removeLastOrNull()`
- [x] `PlantNavGraph.kt`의 `DiaryListNavKey` entry에서 `GoToDiaryDetail` navigate 핸들러 연결
  - `backStack.add(DiaryDetailNavKey(nav.diaryId))`
- [x] `plant_detail/PlantDetailState.kt`의 `ToDiaryDetail` → `data class ToDiaryDetail(val diaryId: Int)`로 변경
- [x] `plant_detail/PlantDetailViewModel.kt`의 `OnDiaryClicked` 처리 → `setEffect(PlantDetailEffect.Navigation.ToDiaryDetail(event.diaryId))`로 수정
- [x] `PlantNavGraph.kt`의 `PlantDetailNavKey` entry에서 `ToDiaryDetail` navigate 핸들러 연결
  - `backStack.add(DiaryDetailNavKey(nav.diaryId))`

### Step 5: strings.xml 확인 및 추가
- [x] 삭제 확인 다이얼로그 문자열 추가
  - `message_diary_delete_confirm` — "일기를 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다."
  - 확인/취소 버튼은 기존 `confirm`, `cancel` 재사용

---

## 참고

- **패턴 참고**: `diary_edit/` 패키지 — `@AssistedInject`, Effect 처리, 사진 처리 패턴
- **Navigation 3**: `savedStateHandle` 사용 금지, NavKey 직접 전달
- **hiltViewModel import**: `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`
- `DeleteDiaryUseCase`: `Diary` 객체 전체를 파라미터로 받음 (pictureList 포함 삭제 처리)