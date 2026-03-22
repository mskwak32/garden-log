# 물준 날짜 기록하기

## 기능 개요

식물의 물주기를 할 경우 물을 준 날짜를 기록하는 기능.
현재는 마지막으로 물 준 날짜만 DB에 저장하고 이를 토대로 다음 물주기 일정을 계산함.
`lastWateringDate`는 알람 계산에 계속 사용되므로 그대로 유지하고,
이력 기록용 별도 테이블을 추가한다.

## 확정된 요구사항

1. 일기 **상세** 화면 및 일기 **작성/수정** 화면에 물주기 아이콘 표시 (일기 목록은 제외)
2. 물주기 기록 트리거: 물주기 버튼(`WateringNowUseCase`) 실행 시에만 자동 기록
3. 같은 날짜 중복 기록 불가 (날짜 기준 1회만 저장)
4. 마이그레이션 시 기존 `plant.lastWateringDate` 값을 `watering_log` 초기 레코드로 삽입
5. 물주기 이력 조회 화면 없음
6. 물방울 아이콘: `IconPack.WaterDropBlue` 사용

---

## DB 설계

### 신규 테이블: `watering_log`

```sql
CREATE TABLE watering_log (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    plantId INTEGER NOT NULL,
    date    TEXT    NOT NULL   -- LocalDate (yyyy-MM-dd)
)
```

- 중복 방지: `(plantId, date)` UNIQUE 제약 + `INSERT OR IGNORE`
- DB 버전: **7 → 8**

### Migration_7_8

1. `watering_log` 테이블 생성
2. 기존 `plant` 테이블의 `lastWateringDate`를 각 식물 레코드의 초기 watering_log로 삽입

```sql
INSERT OR IGNORE INTO watering_log (plantId, date)
SELECT id, lastWateringDate FROM plant
```

---

## 구현 레이어별 작업

### 1. `core/database`

| 파일 | 작업 |
|---|---|
| `entity/WateringLogEntity.kt` | **신규** — `@Entity(tableName = "watering_log", indices = ...)` |
| `dao/WateringLogDao.kt` | **신규** — `insert (OnConflictStrategy.IGNORE)`, `existsByPlantIdAndDate(plantId, date): Flow<Boolean>` |
| `GardenDatabase.kt` | version 8, entities에 `WateringLogEntity` 추가, `wateringLogDao()` 추가 |
| `migration/Migration_7_8.kt` | **신규** — 테이블 생성 + 초기 데이터 삽입 |

### 2. `domain`

| 파일 | 작업 |
|---|---|
| `repository/WateringLogRepository.kt` | **신규** — `suspend fun addWateringLog(plantId: Int, date: LocalDate)`, `fun hasWateringLog(plantId: Int, date: LocalDate): Flow<Boolean>` |
| `useCase/watering/AddWateringLogUseCase.kt` | **신규** — `WateringLogRepository.addWateringLog` 호출 |
| `useCase/watering/GetWateringLogExistsUseCase.kt` | **신규** — `WateringLogRepository.hasWateringLog` 호출, `Flow<Boolean>` 반환 |
| `useCase/watering/WateringNowUseCase.kt` | **수정** — `AddWateringLogUseCase` 주입 후 물주기 시 함께 호출 |

### 3. `data`

| 파일 | 작업 |
|---|---|
| `repository/WateringLogRepositoryImpl.kt` | **신규** — `WateringLogRepository` 구현 |
| `di/RepositoryModule.kt` | `WateringLogRepository` → `WateringLogRepositoryImpl` 바인딩 추가 |
| `core/database/di/DatabaseModule.kt` | `WateringLogDao` provide 추가 |

### 4. `app/di`

| 파일                    | 작업 |
|-----------------------|---|
| `di/UseCaseModule.kt` | `AddWateringLogUseCase`, `GetWateringLogExistsUseCase` `@Provides` 추가 |

### 5. `feature/plant`

#### DiaryDetailScreen

- `DiaryDetailState`에 `isWatered: Boolean = false` 추가
- `DiaryDetailViewModel`에 `GetWateringLogExistsUseCase` 주입
    - `loadDiary()`에서 `diary.createdDate`로 watering log 존재 여부 조회
    - `setState { copy(isWatered = ...) }`
- `DiaryDetailScreen` Content에서 날짜 텍스트 옆 `IconPack.WaterDropBlue` 아이콘 조건부 표시

#### DiaryEditScreen

- `DiaryEditState`에 `isWatered: Boolean = false` 추가
- `DiaryEditViewModel`에 `GetWateringLogExistsUseCase` 주입
    - 일기 날짜가 변경될 때마다 해당 날짜의 watering log 존재 여부 재조회
    - 기존 `OnDateChanged` 이벤트 처리 시 함께 갱신
- `DiaryEditScreen` 날짜 필드 옆 `IconPack.WaterDropBlue` 아이콘 조건부 표시 (읽기 전용 표시만)

---

## 변경 영향 범위 요약

```
core/database   → WateringLogEntity, WateringLogDao, GardenDatabase(v8), Migration_7_8
domain          → WateringLogRepository, AddWateringLogUseCase, GetWateringLogExistsUseCase,
                  WateringNowUseCase (수정)
data            → WateringLogRepositoryImpl, RepositoryModule (수정), DatabaseModule (수정)
app             → UseCaseModule (수정)
feature/plant   → DiaryDetailState/VM/Screen, DiaryEditState/VM/Screen
```

## 미결 사항

- `strings.xml`에 물주기 아이콘 접근성 description 문자열 추가 필요
