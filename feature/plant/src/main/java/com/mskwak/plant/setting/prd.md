# 세팅(Setting)

## 개요

앱 정보 및 부가 기능을 제공하는 설정 화면. 하단 탭(BottomNav)의 세 번째 탭으로 진입.

## 진입점

- **BottomNav**: 세 번째 탭(설정 아이콘) → `SettingNavKey`

---

## 기능 명세

### TopBar

- 타이틀: "설정"
- private `TopBar` 함수로 분리

### 항목 목록

| # | 항목 | 타입 | 동작 |
|---|------|------|------|
| 1 | 버전정보 | 정보 표시 (비클릭) | `versionName` 우측 표시 |
| 2 | 업데이트 내용 | 클릭 가능 | `NotReadyDialog` 표시 |
| 3 | 앱 평가하기 | 클릭 가능 | 인앱 리뷰 시도 → 실패 시 Play Store 폴백 |

### 앱 평가하기 동작 상세

- **Debug 빌드**: Toast 메시지만 표시 (`BuildConfig.DEBUG`)
- **Release 빌드**: `ReviewManagerFactory`로 인앱 리뷰 요청
  - 성공 → `launchReviewFlow()` 실행
  - 실패 또는 Activity를 찾을 수 없는 경우 → Play Store 오픈 (`Context.openPlayStore()`)

---

## 알려진 이슈

없음

---

## 참고

- `SettingViewModel`: `@HiltViewModel` + `@Inject constructor(@ApplicationContext context)`, `AssistedInject` 불필요
- `versionName` 로드: `context.packageManager.getPackageInfo(context.packageName, 0).versionName`
- `NotReadyDialog`: `core/common_ui` — `com.mskwak.common_ui.dialog.NotReadyDialog`
- `Context.findActivity()` / `Context.openPlayStore()`: `core/common_ui` — `com.mskwak.common_ui.util.ContextUtil`