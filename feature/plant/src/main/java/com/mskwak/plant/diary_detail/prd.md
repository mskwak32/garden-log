# 일기 상세(DiaryDetail)

## 개요

일기 상세 화면. 선택한 일기의 날짜, 사진, 내용을 보여주며 수정 및 삭제 기능을 제공한다.

## 진입점

- **DiaryListScreen**: 일기 목록 아이템 클릭 시 → `DiaryListEffect.Navigation.GoToDiaryDetail(diaryId)`
- **PlantDetailScreen**: 식물 상세의 diary item 클릭 시 → `PlantDetailEffect.Navigation.ToDiaryDetail(diaryId)`

---

## 기능 명세

### TopBar
- 왼쪽: 뒤로가기(Back) 버튼
- 가운데: 식물 이름(plantName) 표시
- 오른쪽: 더보기 메뉴 아이콘
  - `AppDropDownMenu` (content 람다 오버로드) + `DropdownMenuItem`으로 **수정**, **삭제** 구성
- private `TopBar` 함수로 분리 (`TopAppBar` 사용)

### 날짜 표시
- `design/util/DateTimeTextUtil.kt`의 `toDateString()` 사용

### 사진 목록
- 사진이 있을 경우에만 표시
- `HorizontalPager` 사용 (`contentPadding = 32.dp`, `pageSpacing = 8.dp`, `aspectRatio(1.5f)`, `RoundedCornerShape(12.dp)`)
- dot indicator: 사진이 **2장 이상**일 때만 표시 (`PagerDotIndicator`)
- 사진 클릭 시 전체화면 뷰어 미구현 (TODO 주석)

### 일기 내용
- 메모 텍스트 표시. 비어있을 경우 `R.string.diary_content_empty` 안내 텍스트 표시
- 세로 스크롤 가능 (`verticalScroll`)

### 수정
- TopBar 메뉴 > 수정 → `DiaryEditNavKey(plantId, diaryId)`로 이동

### 삭제
- TopBar 메뉴 > 삭제 → 확인 다이얼로그 표시 (`DeleteConfirmDialog` private 함수 분리)
  - 확인 시: `DeleteDiaryUseCase` 호출 후 뒤로가기
  - 취소 시: 다이얼로그 닫기

---

## 알려진 이슈

- `PlantNavGraph.kt`의 `PlantDetailNavKey` entry에서 `PlantDetailEffect.Navigation.ToMoreDiaries` 처리가 `else -> { /* TODO */ }`로 미구현

---

## 참고

- `DeleteDiaryUseCase`: `Diary` 객체 전체를 파라미터로 받음 (pictureList 포함 삭제 처리) → `currentDiary: Diary?` 보관 필요
- `PagerDotIndicator`: 비활성 dot Row와 슬라이딩 활성 dot(offset 기반)을 Box로 겹쳐 구현
- **Navigation 3**: `savedStateHandle` 사용 금지, NavKey 직접 전달
- **hiltViewModel import**: `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`