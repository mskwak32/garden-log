package com.mskwak.plant.plant_detail

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.plant.model.DiaryListItemUiModel
import com.mskwak.plant.model.PlantTaskUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Immutable
data class PlantDetailState(
    val plantImagePath: String? = null,
    val plantName: String = "",
    val createdAt: LocalDate? = null,
    val dDays: Int = 0,
    val wateringStatus: WateringStatus = WateringStatus.UPCOMING,
    val lastWateringDate: LocalDate? = null,
    val wateringAlarmTime: LocalTime? = null,
    val isWateringActive: Boolean = false,
    val memo: String? = null,
    val diaries: List<DiaryListItemUiModel> = emptyList(),
    val harvestDate: LocalDate? = null,
    val harvestMemo: String? = null,
    val isHarvestSectionExpanded: Boolean = false,
    val harvestMemoInput: String = "",
    // 물주기·할일 공용 달력. null이면 닫힌 상태
    val calendarTarget: CalendarTarget? = null,
    val calendarMonth: YearMonth = YearMonth.now(),
    val calendarSelectedDate: LocalDate = LocalDate.now(),
    // 물주기·할일 완료 기록 저장 중에는 달력 조작과 버튼 중복 실행을 막는다.
    val isCalendarSaving: Boolean = false,
    // 달력에서 실행한 저장의 실패 메시지
    val calendarError: Int? = null,
    // 모든 이력을 보관해 월 이동과 범위 밖 기존 기록 조회에 함께 사용한다.
    val wateringDates: Set<LocalDate> = emptySet(),
    // 조회 완료 전의 빈 집합은 실제 기록 없음 상태와 구분한다.
    val isWateringLoading: Boolean = true,
    // 달력 대상과 무관하게 유지해 조회 실패 시 물주기 버튼 저장도 막는다.
    val isWateringLoadFailed: Boolean = false,
    val tasks: List<PlantTaskUiModel> = emptyList(),
    // 물주기 달력에 함께 표시할 날짜별 할일 완료 제목
    val plantTaskLogs: Map<LocalDate, List<String>> = emptyMap(),
    // 할일 달력에서 연 할일의 완료 날짜
    val taskLogDates: Set<LocalDate> = emptySet(),
    val isTaskLogLoading: Boolean = false,
    val isTaskLogLoadFailed: Boolean = false,
    val taskDialog: PlantTaskDialogState? = null,
    val isTaskSaving: Boolean = false,
    val today: LocalDate = LocalDate.now()
) : ViewState {
    val isHarvested: Boolean get() = harvestDate != null

    val isCalendarLoading: Boolean
        get() = if (calendarTarget is CalendarTarget.Task) isTaskLogLoading else isWateringLoading

    val isCalendarLoadFailed: Boolean
        get() = if (calendarTarget is CalendarTarget.Task) isTaskLogLoadFailed else isWateringLoadFailed

    // 달력 대상의 저장·취소 판단에 쓰는 기록 날짜
    val calendarRecordDates: Set<LocalDate>
        get() = if (calendarTarget is CalendarTarget.Task) taskLogDates else wateringDates
}

sealed interface CalendarTarget {
    data object Watering : CalendarTarget
    data class Task(val taskId: Int, val title: String, val type: PlantTaskType) : CalendarTarget
}

@Immutable
data class PlantTaskDialogState(
    // null이면 생성, 값이 있으면 수정
    val taskId: Int? = null,
    val title: String = "",
    val content: String = "",
    val type: PlantTaskType = PlantTaskType.ONE_TIME,
    val date: LocalDate = LocalDate.now(),
    // 수정 시 다음 예정일. 날짜를 바꿨을 때만 기준일을 갱신한다.
    val initialDate: LocalDate = date,
    val periodDays: Int = DEFAULT_TASK_PERIOD_DAYS,
    val alarmTime: LocalTime = DEFAULT_TASK_ALARM_TIME
) {
    val isEdit: Boolean get() = taskId != null
    val canSave: Boolean get() = title.isNotBlank()
    val isDateChanged: Boolean get() = date != initialDate

    companion object {
        const val TITLE_MAX_LENGTH = 20
        const val DEFAULT_TASK_PERIOD_DAYS = 7
        val DEFAULT_TASK_ALARM_TIME: LocalTime = LocalTime.of(9, 0)
    }
}

sealed interface PlantDetailEvent : ViewEvent {
    data object OnBackClicked : PlantDetailEvent
    data object OnEditPlantClicked : PlantDetailEvent
    data object OnDeletePlantClicked : PlantDetailEvent
    data object OnDeleteConfirmClicked : PlantDetailEvent
    data class ToggleWateringAlarmActive(val isActive: Boolean) : PlantDetailEvent
    data object OnWateringClicked : PlantDetailEvent
    data class OnDiaryClicked(val diaryId: Int) : PlantDetailEvent
    data object OnNewDiaryClicked : PlantDetailEvent
    data object OnMoreDiaryClicked : PlantDetailEvent
    data object OnHarvestSectionToggled : PlantDetailEvent
    data class OnHarvestMemoChanged(val memo: String) : PlantDetailEvent
    data object OnHarvestClicked : PlantDetailEvent
    data class OnHarvestConfirmed(val date: LocalDate) : PlantDetailEvent
    data object OnCancelHarvestClicked : PlantDetailEvent
    data object OnCancelWateringClicked : PlantDetailEvent
    data object OnExportClicked : PlantDetailEvent

    // 달력
    data object OnWateringCalendarClicked : PlantDetailEvent
    data class OnTaskCalendarClicked(val taskId: Int) : PlantDetailEvent
    data object OnCalendarClosed : PlantDetailEvent
    data class OnCalendarMonthChanged(val month: YearMonth) : PlantDetailEvent
    data class OnCalendarDateSelected(val date: LocalDate) : PlantDetailEvent
    data object OnCalendarSaveClicked : PlantDetailEvent
    data object OnCalendarRetry : PlantDetailEvent

    // 할일
    data object OnAddTaskClicked : PlantDetailEvent
    data class OnTaskMoreClicked(val taskId: Int) : PlantDetailEvent
    data object OnTaskDialogDismissed : PlantDetailEvent
    data class OnTaskTitleChanged(val title: String) : PlantDetailEvent
    data class OnTaskContentChanged(val content: String) : PlantDetailEvent
    data class OnTaskTypeChanged(val type: PlantTaskType) : PlantDetailEvent
    data class OnTaskDateChanged(val date: LocalDate) : PlantDetailEvent
    data class OnTaskPeriodChanged(val periodDays: Int) : PlantDetailEvent
    data class OnTaskAlarmTimeChanged(val time: LocalTime) : PlantDetailEvent
    data object OnTaskSaveClicked : PlantDetailEvent
    data object OnTaskDeleteClicked : PlantDetailEvent
    data class OnTaskDeleteConfirmed(val taskId: Int) : PlantDetailEvent
    data class OnTaskCompleteClicked(val taskId: Int) : PlantDetailEvent
    data class OnOneTimeTaskCompleteConfirmed(val taskId: Int, val date: LocalDate) : PlantDetailEvent
    data class OnTaskCancelClicked(val taskId: Int) : PlantDetailEvent
    data class ToggleTaskAlarm(val taskId: Int, val isActive: Boolean) : PlantDetailEvent
}

sealed interface PlantDetailEffect : ViewEffect {
    sealed interface Navigation : PlantDetailEffect {
        data object Back : Navigation
        data object ToEditPlant : Navigation
        data object ToNewDiary : Navigation
        data class ToDiaryDetail(val diaryId: Int) : Navigation
        data object ToMoreDiaries : Navigation
        data object ToExportDiary : Navigation
    }

    data class ShowSnackbar(val message: Int) : PlantDetailEffect
    data object ShowExactAlarmPermissionDialog : PlantDetailEffect
    data object ShowDeleteConfirmDialog : PlantDetailEffect
    data object ShowHarvestConfirmDialog : PlantDetailEffect
    data class ShowOneTimeTaskCompleteDialog(val taskId: Int, val date: LocalDate) : PlantDetailEffect
    data class ShowTaskDeleteConfirmDialog(val taskId: Int) : PlantDetailEffect
}
