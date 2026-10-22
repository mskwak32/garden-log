# 식물별 커스텀 할일 일정 기능 계획

> 상태: 계획 확정 단계 (구현 전). `[결정]`은 사용자 확인 완료, `[제안]`은 기본안으로 진행할 예정이지만 확인이 필요한 항목, `[미결]`은 사용자 답변이 필요한 항목.

## 1. 목표

- 식물마다 물주기 외에 사용자 정의 할일 일정(이하 "할일")을 여러 개 등록한다.
- 할일은 **1회성**과 **반복성** 두 종류다.
- 완료 기록은 물주기처럼 날짜별 이력으로 남기고, 물주기 기록이 보이는 곳에 함께 표시한다.
- 할일 색상은 종류와 관계없이 **갈색** 하나만 쓴다.
- 할일을 삭제하거나 1회성 할일을 완료해도 **완료 이력은 DB에 남긴다**. 그래야 달력과 PDF에 계속 표시된다.
- 화면을 텍스트로만 채우지 않고 적절히 아이콘을 쓴다.

## 2. 표시 위치 [결정]

| 위치 | 파일 | 현재 | 할일 반영 |
|---|---|---|---|
| 식물 상세 | `plant_detail/PlantDetailScreen.kt` | 물주기 정보·버튼 박스 | 할일 카드 + 추가 버튼 |
| 물주기 달력 다이얼로그 | `plant_detail/PlantDetailWateringDateDialog.kt` | 파란 점, 선택 날짜의 "물주기" 행 | 갈색 점 1개 + 할일 제목 행. 할일별 달력으로도 재사용 |
| PDF 내보내기 | `core/pdf/.../PdfDataSourceImpl.kt` | 날짜 옆 물방울 | 사진 다음, 본문 앞에 `● 제목` 목록 |
| 일기 상세 | `diary_detail/DiaryDetailScreen.kt` | 날짜 옆 물방울 | 날짜 아래, 본문 위에 `● 제목` 목록 |
| 일기 작성/수정 | `diary_edit/DiaryEditScreen.kt` | 날짜 필드 물방울 | 반영 안 함 |
| 식물 목록 | `plant_list/PlantListItem.kt` | 물주기 아이콘 + D-day | 할일 D-day 2번째 줄 추가 |
| 알림 | `app/.../alarm/*` | 물주기 알림, 부팅 재등록 | 할일 알림 추가 |

## 3. 계산 규칙

### 3.1 할일 종류 [결정]

| 항목 | 1회성 (`ONE_TIME`) | 반복성 (`REPEAT`) |
|---|---|---|
| 날짜 필드 의미 | 예정일 | 시작일(첫 예정일) |
| 반복 주기 | 없음 | 1 ~ `Constants.MAX_WATERING_PERIOD`일 |
| 완료 버튼 | 확인 다이얼로그 → 오늘 날짜로 이력 저장 → 카드 사라짐 | 오늘 날짜로 이력 저장 → D-day 갱신 |
| 달력 완료 | 선택 날짜로 완료(확인 다이얼로그) → 카드 사라짐 | 선택 날짜 완료 추가/취소 |
| 취소 버튼 | 없음 | 물주기와 같음: **오늘 완료 이력이 있을 때** 노출 |
| 수정 시 종류 변경 | 불가 | 불가 |

- 1회성은 완료하면 할일이 사라지므로 완료를 되돌릴 수 없다. 완료 이력은 물주기 달력과 PDF에서 조회만 가능하다.

### 3.2 다음 예정일 [결정]

- 1회성: `nextDue = date`
- 반복성: `latest` = 해당 할일의 최신 완료 이력 날짜
  - `latest == null` 또는 `latest < date` → `nextDue = date`
  - 그 외 → `nextDue = latest + periodDays`
  - 수정 다이얼로그의 날짜 초기값은 현재 `nextDue`이다. 날짜를 **변경했을 때만** `date`를 갱신한다.
  - 시작일 이전 날짜로 완료하면(`latest < date`) 예정일은 시작일 그대로다.
- 달력에서 과거 날짜를 추가해도 `latest`보다 이전이면 예정일은 바뀌지 않는다.

### 3.3 D-day 표기 [결정]

물주기 문자열(`watering_d_day_minus_format` / `plus_format`)을 재사용한다.
- 상세 카드: `nextDue > today` → `D-n`, `== today` → `D-0`, `< today` → `D+n`
- 식물 목록: 물주기 표기와 같이 오늘이면 "오늘"로 표시한다.

### 3.4 날짜 제약 [결정]

- 등록/수정 날짜(예정일·시작일): 과거·미래 모두 허용. 과거면 바로 `D+n`이다.
- 완료 날짜: 물주기와 같이 **식물 심은 날 ~ 오늘** 범위만 추가할 수 있다. 범위 밖 기존 이력은 취소만 가능하다.
- 수확한 식물: 할일 카드와 추가 버튼을 숨기고, 완료·수정을 막는다. 알람은 중지하고 수확 취소 시 복원한다. 달력 이력은 유지한다.

## 4. 데이터 설계 (Room v8 → v9)

### 4.1 `plant_task` (진행 중인 할일) [결정]

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `plantId` | INTEGER NOT NULL | index |
| `title` | TEXT NOT NULL | 최대 20자 |
| `content` | TEXT NULL | 내용(선택) |
| `type` | TEXT NOT NULL | `ONE_TIME` / `REPEAT` |
| `date` | TEXT NOT NULL | 1회성: 예정일, 반복성: 기준 예정일 |
| `periodDays` | INTEGER NULL | 반복성만 |
| `alarm_time`, `alarm_isActive` | TEXT, INTEGER | `@Embedded(prefix = "alarm_") AlarmEntity` 재사용 |
| `createdAt` | TEXT NOT NULL | 정렬(생성순) |

- 1회성 완료와 할일 삭제는 모두 **행 삭제**다. 완료/보관 플래그는 두지 않는다.

### 4.2 `plant_task_log` (완료 이력) [결정]

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `plantId` | INTEGER NOT NULL | 달력·PDF·일기 상세 조회 기준 |
| `taskId` | INTEGER NULL | 원본 할일. FK 없음. 할일 삭제 후에도 값 유지(AUTOINCREMENT라 재사용 없음) |
| `title` | TEXT NOT NULL | 완료 시점 제목 스냅샷 |
| `date` | TEXT NOT NULL | 완료 날짜 |

- 인덱스: `(plantId, date)`, UNIQUE `(taskId, date)`.
- FK·CASCADE 없음. 할일을 삭제해도 이력은 남는다.
- 할일 제목을 수정해도 과거 이력 제목은 바뀌지 않는다(이력은 그 시점 기록).

### 4.3 마이그레이션

- `core/database/migration/Migration_8_9.kt`: 두 테이블과 인덱스 생성. 인덱스 이름은 Room 규칙(`index_plant_task_plantId`, `index_plant_task_log_plantId_date`, `index_plant_task_log_taskId_date`).
- `GardenDatabase` version 9, entities·`plantTaskDao()` 추가. `DatabaseModule`에 `MIGRATION_8_9`, DAO provider 추가.
- 스키마 `9.json`은 빌드 결과를 커밋한다.

### 4.4 식물 삭제·수확 연동

- `DeletePlantUseCase`: 할일·이력 삭제, 할일 알람 전부 취소.
- 참고(범위 외): 식물 삭제 시 `watering_log`가 지워지지 않는 기존 문제는 이번 작업에서 다루지 않는다.
- `HarvestPlantUseCase.harvest`: 할일 알람을 물리적으로만 취소(DB `alarm_isActive` 유지).
- `HarvestPlantUseCase.cancelHarvest`: 활성 할일 알람 재등록.

## 5. 레이어별 구현 목록

### 5.1 domain

- `model/PlantTask.kt`
  - `PlantTask(id, plantId, title, content, type, date, periodDays, alarm, createdAt, latestDoneDate, isDoneToday)`
  - `enum class PlantTaskType { ONE_TIME, REPEAT }`
  - `PlantTaskLog(plantId, taskId: Int?, title, date)`
- `repository/PlantTaskRepository.kt`
  - `observeTasks(plantId)`: 생성순, 최신 완료일 포함
  - `observeAllTasks()`: 식물 목록용(plantId별 할일)
  - `getTask`, `addTask(): Int`, `updateTask(task, isDateChanged)`, `deleteTask(taskId)`
  - `setTaskLog(taskId, date, isDone): Boolean`: 물주기 `setWateringLog`와 같은 목표 상태 적용 방식. 1회성 `isDone=true`면 이력 추가와 행 삭제를 한 트랜잭션에서 처리. 수확·범위 검증 포함
  - `observeTaskLogs(taskId)`: 할일별 달력
  - `observePlantTaskLogs(plantId)`: 물주기 달력
  - `getTaskTitlesByDate(plantId, date)`: 일기 상세
  - `getPlantTaskLogsByRange(plantId, start, end)`: PDF
  - `getActiveAlarmTaskIds()`, `updateAlarmActivation(taskId, isActive)`, `deleteByPlantId(plantId)`
- `repository/PlantTaskAlarmRepository.kt`: `setAlarm(taskId, dateTime)`, `cancelAlarm(taskId)`
- `usecase/task/`
  - `GetPlantTasksUseCase`, `GetNextDueDateUseCase`(3.2, 순수 함수), `GetTaskDaysUseCase`(D-day·지남)
  - `GetNearestTaskDueDatesUseCase`: `Flow<Map<plantId, LocalDate>>`. 식물별 할일 `nextDue` 중 가장 이른 날짜(지난 일정 포함). 할일이 없는 식물은 제외
  - `AddPlantTaskUseCase`, `UpdatePlantTaskUseCase`, `DeletePlantTaskUseCase` (알람 재설정/취소 포함)
  - `UpdatePlantTaskLogUseCase(taskId, date, isDone)`: 결과 `WateringUpdateResult` 재사용(CHANGED/UNCHANGED/ALARM_FAILED). 이름 공용화는 구현 시 판단
  - `CompleteTodayTaskUseCase`, `CancelTodayTaskUseCase`: 오늘 날짜 래퍼
  - `SetPlantTaskAlarmUseCase`: `SetWateringAlarmUseCase`의 주기 건너뛰기 로직 재사용. 1회성은 예정 시각이 지났으면 등록하지 않음. 수확한 식물이면 등록하지 않음
  - `UpdatePlantTaskAlarmActivationUseCase`
  - `GetTaskLogsUseCase(taskId)`, `GetPlantTaskLogsUseCase(plantId)`, `GetTaskTitlesByDateUseCase`, `GetPlantTaskLogsForExportUseCase`
- `model/ExportRequest.kt`: `DiaryExport.taskTitles: List<String> = emptyList()`

### 5.2 core/database

- `entity/PlantTaskEntity.kt`, `entity/PlantTaskLogEntity.kt`
- `dao/PlantTaskDao.kt`: 할일 + 할일별 `MAX(date)` 조회, 이력 insert(IGNORE)/delete, 범위·날짜 조회, plantId 일괄 삭제
- `GardenDatabaseConverter`: 필요 시 `PlantTaskType` 변환
- `Migration_8_9.kt`, `GardenDatabase`, `DatabaseModule`

### 5.3 data

- `mapper/DatabaseMapper.kt`, `mapper/ExportMapper.kt`(`taskTitles`)
- `repository/PlantTaskRepositoryImpl.kt`: 수확 식물이면 `WateringValidationException(HARVESTED)`, 범위 밖 추가면 `OUTSIDE_RANGE`. 예외 이름 공용화는 구현 시 판단
- `di/RepositoryModule.kt`: `@Binds`

### 5.4 core/pdf

- `DiaryPdf.taskTitles: List<String>`
- `addDiaryPages`: 날짜(+물방울) → 사진 → **할일 목록** → 본문. 할일마다 갈색 원(`canvas.drawCircle`) + 제목 한 줄, `ensureSpace`로 페이지 넘김 처리.
- 갈색은 PDF 내부 상수로 둔다.
- PDF는 기존처럼 일기가 있는 날짜만 출력한다. 일기가 없는 날의 할일 완료는 PDF에 나오지 않는다.

### 5.5 app (알람)

- `alarm/PlantTaskAlarmReceiver.kt` (신규, `exported=false`, Manifest 등록)
  - extra `taskId`. 수신 시 할일·식물 이름 조회 → 알림 → 다음 알람 재예약
  - ID 충돌 방지: 물주기는 `requestCode = plantId`, `notify(plantId)`, summary 1000을 쓴다.
    - PendingIntent는 receiver 컴포넌트가 달라 `requestCode = taskId`로도 충돌하지 않는다.
    - 알림은 `notify(TAG_TASK, taskId, …)`로 tag를 분리하고, summary ID는 별도 값(2000)을 쓴다.
  - 별도 채널 `plantTaskNotification`("할일 알림")
  - 알림 문구: 제목 "{식물이름} 할일", 본문 "{할일 제목}"
  - 탭하면 `MainActivity` 실행
- `alarm/PlantTaskAlarmRepositoryImpl.kt`: `setAlarmClock`, exact alarm 권한 없으면 skip
- `BootReceiver`: 활성 할일 알람 재등록
- `di/RepositoryModule.kt`(app), `di/UseCaseModule.kt` 갱신

### 5.6 core/common_ui

- `theme/Color.kt`: `TaskBrown`(라이트, 예: `0xFF8D6E63`), `TaskBrownLight`(다크, 예: `0xFFBCAAA4`), `TaskBrownText`(라이트 텍스트, 예: `0xFF6D4C41`)
- 아이콘: 9장 참고

### 5.7 feature/plant — 식물 상세

**화면 배치**
```
Header
WateringInfo            (수확 전)
WateringSection         (달력/물주기/취소 + 메모)
TaskCard × N            (수확 전, 생성순)
AddTaskButton           (수확 전)
HarvestMemoBox
DiarySection
Harvest…
```

**TaskCard** — 할일 1개당 박스 1개. 기존 박스 스타일(`surfaceContainerLowest` + 1dp `surfaceContainerHighest` border, 15dp radius)
```
┌──────────────────────────────────────────┐
│ [Gardening] 제목(Bold) D-3            [⋯] │ 1행: D-day는 제목 바로 오른쪽
│ 내용 텍스트…                               │ 2행: 내용, 없으면 숨김
│                                            │ ← 2·3행 사이 간격 확대(8dp)
│ [Repeat] 7일마다 · 마지막 완료: 3일 전      │ 3행: 반복성 / 1회성은 [Event] 예정일: 2026.10.30
│ [Alarm] 알림: 오전 9:00            [ ○ ]   │ 4행
│ [📅] [ ✓ 완료                ] [취소]      │ 5행: 달력 / 완료 / 취소(반복성 오늘 완료 시)
└──────────────────────────────────────────┘
```
- 1행 [결정]: `Row { Icon; Text(title, Modifier.weight(1f, fill = false), maxLines = 2, ellipsis); Spacer(6dp); Text(dDay); Spacer(Modifier.weight(1f)); MoreButton }`
  - `fill = false`라서 제목이 짧으면 D-day가 제목 바로 뒤에 붙는다.
  - 제목이 길면 제목만 2줄로 줄바꿈되고, D-day는 제목 오른쪽(세로 가운데)에 남는다. ⋯ 버튼은 오른쪽 끝에 고정된다.
- 2행 내용과 3행 사이는 8dp, 나머지 행 사이는 4dp 간격을 둔다.
- 3행 "마지막 완료" 문구는 물주기와 같은 규칙(오늘 / n일 전 / 날짜, 없으면 "기록 없음").
- 5행은 `WateringSection`과 같은 구성이다. 달력 버튼(48dp) → 완료 버튼(`weight(1f)`) → 취소 버튼(`MutableTransitionState` expand/shrink 애니메이션).
  - 달력·완료 버튼 색 [결정]: 물주기 버튼과 같은 `primary` 배경으로 통일한다(달력 아이콘 `onPrimary`, 완료 버튼 `onTertiary` — 물주기 버튼과 동일).
  - 갈색은 점, 할일 아이콘, 달력·PDF·일기 상세 표시에만 쓴다.
- 지난 할일(`D+n`)은 D-day를 error 색으로 표시한다(물주기 OVERDUE와 같음) [제안].
- 1회성 완료 → `PlantDetailTaskCompleteDialog`("할일을 완료하시겠습니까?" / 취소·완료).

**AddTaskButton**
- 전체 너비, 연한 배경(`surfaceContainerLow`) + 1dp border(`outlineVariant`) + `+` 아이콘 + "할일 일정 추가". 진한 채움 배경은 쓰지 않는다.

**생성/수정 다이얼로그 (`PlantDetailTaskDialog.kt`)**
```
할일 일정 추가 / 할일 일정 수정
[제목 _______________ 0/20]       필수
[내용 _______________]            선택, 여러 줄
(●) 1회성   ( ) 반복성             수정 시 비활성
[event]  예정일/시작일  2026.10.22  >    DatePickerDialog
[repeat] 반복 주기      7일         >    반복성만, 주기 휠("없음" 제외)
[alarm]  알림 시간      오전 9:00   >    TimePicker
                    [취소] [만들기]       ← 생성
                    [삭제] [수정]         ← 수정 (삭제는 확인 다이얼로그)
```
- 입력 행은 `LabeledClickableField`를 재사용하고 앞에 아이콘을 둔다.
- 반복 주기: `WateringPeriodDialog`에 "없음" 항목을 숨기는 파라미터를 추가해 재사용한다.
- 제목이 공백이면 만들기/수정 비활성.
- 생성 시 알림은 **꺼짐**, 시간 기본값 09:00. 카드 스위치로 켤 때 `canScheduleExactAlarms` → `ExactAlarmPermissionDialog` 흐름을 탄다.

**달력 다이얼로그 재사용 (`PlantDetailWateringDateDialog.kt` → `PlantDetailRecordCalendarDialog.kt`)**
- 다이얼로그 **제목을 제거**한다(물주기·할일 모두).
- 상태에 `calendarTarget: CalendarTarget?`(`Watering` / `Task(taskId)`)을 두고, 같은 다이얼로그 컴포저블이 대상에 따라 데이터와 버튼을 바꾼다. 기존 `isWateringDateDialogOpen`, `wateringMonth`, `selectedWateringDate`, `isWateringSaving`, `wateringError`는 대상 공용 이름(`calendarMonth`, `calendarSelectedDate`, `isCalendarSaving`, `calendarError`)으로 정리한다.

| 항목 | 물주기 달력 (`Watering`) | 할일 달력 (`Task`) |
|---|---|---|
| 진입 | 물주기 박스 달력 버튼 | 할일 카드 달력 버튼 |
| 날짜 점 | 파란 점 + 갈색 점(그날 할일 이력이 1건 이상이면 1개) | 해당 할일 이력 갈색 점 |
| 선택 날짜 상세 | `● 물주기`(파랑) + 할일 이력마다 `● 제목`(갈색) | `● 제목`(갈색) |
| 저장 버튼 | 물주기 / 기록 취소 (기존) | 완료 / 완료 취소 |
| 1회성 | - | "완료" → 확인 다이얼로그 → 완료되면 달력 닫힘, 카드 사라짐 |
| 월 이동 범위 | 심은 날, 물주기·할일 이력, 수확일, 오늘 | 심은 날, 해당 할일 이력, 오늘 |

- 할일 달력의 이력 구독은 달력이 열릴 때 `taskId`로 시작하고 닫힐 때 취소한다.
- 접근성: 할일 기록이 있는 날짜의 contentDescription에 "할일 n건" 추가.

**상태/이벤트 (`PlantDetailState.kt`)**
- `model/PlantTaskUiModel.kt`: `id, title, content, type, dDay, isOverdue, latestDoneDate, dueDate, periodDays, alarmTime, isAlarmActive, isDoneToday`
- State: `tasks`, `plantTaskLogs: Map<LocalDate, List<String>>`, `calendarTarget`, `taskLogDates: Set<LocalDate>`(할일 달력), `taskDialog: PlantTaskDialogState?`, `isTaskSaving`
- Event: `OnAddTaskClicked`, `OnTaskMoreClicked(id)`, `OnTaskDialogDismissed`, `OnTaskTitleChanged`, `OnTaskContentChanged`, `OnTaskTypeChanged`, `OnTaskDateChanged`, `OnTaskPeriodChanged`, `OnTaskAlarmTimeChanged`, `OnTaskSaveClicked`, `OnTaskDeleteClicked`, `OnTaskDeleteConfirmed`, `OnTaskCompleteClicked(id)`, `OnOneTimeTaskCompleteConfirmed(id, date)`, `OnTaskCancelClicked(id)`, `OnTaskCalendarClicked(id)`, `ToggleTaskAlarm(id, isActive)`
- Effect: `ShowOneTimeTaskCompleteDialog(id, date)`, `ShowTaskDeleteConfirmDialog(id)`, 기존 `ShowExactAlarmPermissionDialog` 재사용

**신규 파일**
- `plant_detail/PlantDetailTaskCard.kt` (TaskCard, AddTaskButton, 프리뷰)
- `plant_detail/PlantDetailTaskDialog.kt`
- `plant_detail/PlantDetailTaskCompleteDialog.kt`
- `plant_detail/PlantDetailTaskDeleteDialog.kt`

### 5.8 feature/plant — 식물 목록 [결정]

- `PlantListViewModel`: `GetNearestTaskDueDatesUseCase`를 `combine`해서 `PlantListItemUiModel.taskDDay: Int?`, `taskStatus`를 채운다. 수확한 식물은 null.
- `PlantListItem` 오른쪽 영역
  - 할일 D-day가 없으면 기존 그대로(물방울 30dp + titleMedium).
  - 있으면 2줄: `[물방울 20dp] D-3` / `[할일 아이콘 20dp] D-1`, 텍스트는 `titleSmall`. 할일 D-day가 지났으면 error 색.

### 5.9 feature/plant — 일기 상세 [결정]

- `DiaryDetailViewModel`: `GetTaskTitlesByDateUseCase(plantId, diary.createdDate)`로 `taskTitles`를 state에 채운다.
- `DiaryDetailScreen`: 날짜 Row 아래, 본문 위에 글머리 목록으로 표시한다. 행마다 갈색 점(6dp) + 8dp 간격 + 제목(`bodyMedium`, `TaskBrownText`/다크 `TaskBrownLight`).

### 5.10 feature/plant — 내보내기

- `DiaryExportViewModel`: `GetPlantTaskLogsForExportUseCase`로 날짜별 제목 목록을 받아 `DiaryExport.taskTitles`에 채운다.

### 5.11 analytics [제안]

- `GardenEvent`: `AddTask(type, alarmEnabled)`, `CompleteTask(type, source)`, `CancelTask`, `DeleteTask`, `TaskAlarmToggle(enabled)`

### 5.12 문자열

`strings.xml` 기존 항목(`cancel`, `delete`, `edit`, `today`, `days_ago_format`, `watering_d_day_*_format`, `watering_no_record` 등)을 먼저 재사용한다. 신규 항목은 **ko / en / ja** 모두 추가한다.
- feature/plant: 할일 일정 추가/수정, 제목, 내용, 1회성, 반복성, 예정일, 시작일, 반복 주기, `%d일마다`, 알림 시간, 만들기, 수정, 완료, 완료 취소, 마지막 완료, 1회성 완료 확인 문구, 삭제 확인 문구, 접근성 문구
- app: 할일 알림 채널 이름, 알림 제목 포맷

## 6. 테스트 계획 (필요한 층에서만)

- domain 단위: `GetNextDueDateUseCase`(이력 없음 / 이력 ≥ 기준일 / 이력 < 기준일 / 지남), 할일 알람 다음 시각(주기 건너뛰기, 1회성 지난 시각 → null), 식물별 가장 이른 예정일
- data Robolectric(`WateringPersistenceTest` 방식) `PlantTaskPersistenceTest`
  - 1회성 완료 → 행 삭제 + 이력 유지
  - 반복성 과거 날짜 완료/취소, 같은 날 중복 완료 무시
  - 반복성 삭제 후 이력 유지(제목 스냅샷)
  - 식물 삭제 시 할일·이력 삭제
  - 수확 식물 완료 거부, 범위 밖 날짜 추가 거부
- 마이그레이션 8→9: `MigrationTestHelper` 1건
- UI: Compose Preview(카드 상태별, 다이얼로그 생성/수정, 달력 물주기/할일 모드, 목록 2줄, 일기 상세)

## 7. 구현 순서

1. DB: Entity, DAO, Migration 8→9, 스키마, 마이그레이션 테스트
2. domain: 모델, Repository 인터페이스, UseCase + 단위 테스트
3. data: RepositoryImpl, Mapper, DI + persistence 테스트
4. app: 알람 receiver/repository, BootReceiver, Manifest, UseCaseModule
5. common_ui: 색상, 아이콘
6. 식물 상세: State/Event/ViewModel → TaskCard → 다이얼로그
7. 달력 다이얼로그 공용화(제목 제거, 할일 모드)
8. 식물 목록 2줄 D-day
9. 일기 상세 할일 목록
10. PDF
11. 문자열 3개 언어, analytics
12. `./gradlew test`, 실기기 확인(알림, 달력, PDF)

## 8. 질문 답변 기록

| # | 내용 | 결정 |
|---|---|---|
| Q1 | 완료 버튼 위치 | 5행. 물주기처럼 달력 버튼 + 완료 버튼 + 취소 버튼. 취소 노출 조건은 물주기와 같음 |
| Q2 | 내용 입력 | 다이얼로그에 선택 입력 필드 추가 |
| Q3 | 1회성 마지막 완료 행 | 예정일 표시(3행) |
| Q4 | 과거 날짜 완료 | 할일별 달력에서 물주기처럼 추가/취소 |
| Q5 | 일기 화면 표시 | 상세만. 날짜 아래, 본문 위, 갈색 점 글머리 목록 |
| Q6 | 수확 식물 | 카드·추가 버튼 숨김, 알람 중지/복원, 이력 유지 |
| Q7 | 종류 변경 | 불가 |
| Q8 | PDF 위치 | 사진 다음, 본문 앞 |
| Q9 | 달력 제목 | 제거 |
| Q10 | 정렬 | 생성순 |
| Q11 | 알림 | 생성 시 꺼짐. 알림 문구 "{식물이름} 할일" / "{제목}" |
| Q12 | 식물 목록 | 가장 이른 할일 D-day를 2번째 줄에 표시. 할일 없으면 기존 표시 |
| Q13 | 제목 길이 | 20자. 제목만 최대 2줄, D-day는 제목 오른쪽에 붙임 |

## 9. 아이콘

기존: 더보기(`MoreHorizBlack`), 알림(`AlarmBlack`), 달력(`Calendar`), 날짜(`EventBlack`), 추가(`AddWhite`, tint 처리).

| 용도 | 아이콘 | 상태 |
|---|---|---|
| 할일 아이콘(카드 1행, 식물 목록) | `IconPack.Gardening` (사용자 제공 이미지를 벡터화, 다색) | 추가 완료 |
| 완료 버튼 | `IconPack.Check` | 추가 완료 |
| 반복 주기(카드 3행, 다이얼로그) | `IconPack.Repeat` | 추가 완료 |
| 추가 버튼 | `AddWhite` tint로 대체 | 기존 |
