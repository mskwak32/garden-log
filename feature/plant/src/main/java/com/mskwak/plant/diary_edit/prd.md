# 일기 추가/수정(DiaryEdit)

## 개요

일기 추가 및 수정 화면. 사진, 날짜, 메모를 입력/편집하고 저장한다.
`DiaryEditNavKey(plantId, diaryId = null)` → 추가 모드 / `diaryId != null` → 수정 모드.

## 진입점

- **PlantDetailScreen**: 일기 쓰기 버튼 → `DiaryEditNavKey(plantId)`
- **DiaryDetailScreen**: 메뉴 > 수정 → `DiaryEditNavKey(plantId, diaryId)`

---

## 기능 명세

### TopBar
- 가운데: 식물 이름(plantName) 표시
- 왼쪽: X(닫기) 버튼 (`CloseBlack` 아이콘)
- 오른쪽: `TextButton` — 추가 모드 `저장`, 수정 모드 `수정`
  - 저장 진행 중 중복 클릭 방지 (`isSaveEnabled` state)

### 사진 추가
- 추가 버튼(80dp 정사각형) + 추가된 사진 목록(`LazyRow`, 가로 스크롤) 구성
- 사진 선택: `SelectPhotoDialog` 사용 (갤러리 / 카메라, 삭제 버튼 없음)
  - 갤러리: `PickVisualMedia` API (권한 불필요)
  - 카메라: CAMERA 권한 체크 → 거부 시 Toast (`R.string.message_photo_camera_permission`)
- 사진 최대 5장 (`Constants.MAX_PICTURE_PER_DIARY`) — 초과 시 Snackbar (`R.string.message_no_more_picture`)
- 추가된 사진 썸네일: 80dp × 80dp, `RoundedCornerShape(12.dp)`
- 사진 우상단 X 아이콘 클릭 시 해당 사진 삭제

### 날짜 표시
- `LabeledClickableField`로 날짜 표시, 클릭 시 `DatePickerDialog` (Material3) 표시
- 기본값: 오늘 날짜, 미래 날짜 선택 가능

### 일기 입력
- `OutlinedTextField`, placeholder: `R.string.diary_content_hint`
- `weight(1f)`으로 사진/날짜 섹션 아래 나머지 높이 차지

### 유효성 검사
- 사진, 메모 둘 다 없을 때 저장 시 Snackbar (`R.string.message_input_picture_or_content`)

### 저장
- 추가: `AddDiaryUseCase`, 수정: `UpdateDiaryUseCase`
- 성공 시 `DiaryEditEffect.Navigation.SaveComplete` (backStack pop)
- 실패 시 Snackbar (`R.string.message_save_failed`)
- 수정 모드: 삭제된 원본 사진 파일도 `DeletePictureUseCase`로 제거

### 뒤로가기
- **추가 모드**에서 사진 또는 메모가 있을 경우 DiscardConfirmDialog 표시
  - 확인 시 새로 추가한 사진 파일 삭제 후 뒤로가기
- **수정 모드**에서는 다이얼로그 없이 바로 뒤로가기

---

## 사진 관리 설계

사진을 세 개 리스트로 분리 관리:
- `originalPictures`: 수정 모드에서 기존 저장된 사진
- `newPictures`: 현재 세션에서 새로 추가한 사진
- `removedOriginalPictures`: 수정 모드에서 삭제한 원본 사진

저장 시 `buildCurrentPictureList()`로 현재 `picturePaths` 순서 기준으로 두 목록을 합산.
뒤로가기/폐기 시 `newPictures`의 파일만 `DeletePictureUseCase`로 제거.

---

## 알려진 이슈

없음

---

## 참고

- 카메라 촬영 시 `createCameraUri(application)` (`plant/util/`)로 임시 URI 생성
- `isSaveEnabled`: 저장 요청 시 `false` → 완료/실패 후 `true` 복구 (중복 저장 방지)
- **Navigation 3**: `savedStateHandle` 사용 금지, NavKey 직접 전달
- **hiltViewModel import**: `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`