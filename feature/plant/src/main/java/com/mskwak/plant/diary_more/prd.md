# 일기 더보기(DiaryMore)

## 개요

특정 식물의 전체 일기 목록을 무한 스크롤로 탐색하는 화면.
`PlantDetailScreen`에서 일기 섹션의 더보기 버튼으로 진입하며, 해당 식물의 일기만 표시한다.

## 진입점

- **PlantDetailScreen**: 일기 더보기 버튼 → `PlantDetailEffect.Navigation.ToMoreDiaries` →
  `DiaryMoreNavKey(plantId)`

---

## 기능 명세

### TopBar

- 왼쪽: 뒤로가기 버튼
- 가운데: 식물 이름(plantName) 표시
- 오른쪽: 정렬 필터 버튼 (최신순 / 오래된순)
- private `TopBar` 함수로 분리

### 일기 목록

- `PAGE_SIZE` 단위로 페이지 로드, 스크롤 끝 도달 시 다음 페이지 자동 로드
- 정렬 변경 시 목록 초기화 후 첫 페이지부터 재로드
- 항목 간 월이 바뀔 경우 연도.월 구분 헤더(`DiaryMoreListItem.MonthHeader`) 삽입
- 항목 클릭 → `DiaryDetailScreen`으로 이동

### 빈 상태

- Lottie 애니메이션(`lottie_notebook`) + "아직 {식물이름}에 대한 일기가 없어요"

---

## 알려진 이슈

없음

---

## 참고

- `DiaryMoreListItem`: `MonthHeader` / `DiaryItem` sealed interface, `buildDiaryListItems()`로 생성 (
  `DiaryMoreListItem.kt`)
- `GetDiariesByPlantIdUseCase`: `limit`, `page`, `ascending` 파라미터로 페이지네이션 및 정렬 지원
- **Navigation 3**: `savedStateHandle` 사용 금지, `@AssistedInject` +
  `@Assisted navKey: DiaryMoreNavKey`