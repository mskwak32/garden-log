package com.mskwak.plant.plant_detail

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.analytics.WateringSource
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.usecase.diary.GetDiariesByPlantIdUseCase
import com.mskwak.domain.usecase.plant.DeletePlantUseCase
import com.mskwak.domain.usecase.plant.GetPlantUseCase
import com.mskwak.domain.usecase.plant.HarvestPlantUseCase
import com.mskwak.domain.usecase.task.AddPlantTaskUseCase
import com.mskwak.domain.usecase.task.DeletePlantTaskUseCase
import com.mskwak.domain.usecase.task.GetNextDueDateUseCase
import com.mskwak.domain.usecase.task.GetPlantTaskLogsUseCase
import com.mskwak.domain.usecase.task.GetPlantTasksUseCase
import com.mskwak.domain.usecase.task.GetTaskLogDatesUseCase
import com.mskwak.domain.usecase.task.GetTaskRemainDaysUseCase
import com.mskwak.domain.usecase.task.UpdatePlantTaskAlarmActivationUseCase
import com.mskwak.domain.usecase.task.UpdatePlantTaskLogUseCase
import com.mskwak.domain.usecase.task.UpdatePlantTaskUseCase
import com.mskwak.domain.usecase.watering.GetWateringDatesUseCase
import com.mskwak.domain.usecase.watering.GetWateringDaysUseCase
import com.mskwak.domain.usecase.watering.UpdateWateringAlarmActivationUseCase
import com.mskwak.domain.usecase.watering.UpdateWateringLogUseCase
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import com.mskwak.plant.R
import com.mskwak.plant.model.toDiaryListItemUiModel
import com.mskwak.plant.model.toPlantListItemUiModel
import com.mskwak.plant.model.toPlantTaskUiModel
import com.mskwak.plant.util.canScheduleExactAlarms
import com.mskwak.plant.util.taskErrorResource
import com.mskwak.plant.util.wateringErrorResource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PlantDetailViewModel.Factory::class)
class PlantDetailViewModel @AssistedInject constructor(
    @Assisted navKey: PlantDetailNavKey,
    private val application: Application,
    private val getPlantUseCase: GetPlantUseCase,
    private val getWateringDaysUseCase: GetWateringDaysUseCase,
    private val getDiariesByPlantIdUseCase: GetDiariesByPlantIdUseCase,
    private val updateWateringAlarmActivationUseCase: UpdateWateringAlarmActivationUseCase,
    private val deletePlantUseCase: DeletePlantUseCase,
    private val plantRepository: PlantRepository,
    private val harvestPlantUseCase: HarvestPlantUseCase,
    private val getWateringDatesUseCase: GetWateringDatesUseCase,
    private val updateWateringLogUseCase: UpdateWateringLogUseCase,
    private val getPlantTasksUseCase: GetPlantTasksUseCase,
    private val getNextDueDateUseCase: GetNextDueDateUseCase,
    private val getTaskRemainDaysUseCase: GetTaskRemainDaysUseCase,
    private val addPlantTaskUseCase: AddPlantTaskUseCase,
    private val updatePlantTaskUseCase: UpdatePlantTaskUseCase,
    private val deletePlantTaskUseCase: DeletePlantTaskUseCase,
    private val updatePlantTaskLogUseCase: UpdatePlantTaskLogUseCase,
    private val updatePlantTaskAlarmActivationUseCase: UpdatePlantTaskAlarmActivationUseCase,
    private val getTaskLogDatesUseCase: GetTaskLogDatesUseCase,
    private val getPlantTaskLogsUseCase: GetPlantTaskLogsUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<PlantDetailState, PlantDetailEvent, PlantDetailEffect>() {

    private val plantId: Int = navKey.plantId
    private var wateringObserveJob: Job? = null
    private var taskLogObserveJob: Job? = null
    // 수정 시 UI 모델에 없는 생성일·알람 활성 상태를 유지하기 위해 원본을 보관한다.
    private var domainTasks: Map<Int, PlantTask> = emptyMap()

    init {
        analyticsLogger.log(GardenEvent.ScreenView("plant_detail"))
        observePlant()
        observeWateringDates()
        observeTasks()
        observePlantTaskLogs()
    }

    override fun setInitialState(): PlantDetailState = PlantDetailState()

    private fun observePlant(): Job {
        return combine(
            getPlantUseCase(plantId).filterNotNull(),
            getDiariesByPlantIdUseCase(plantId)
        ) { plant, diaries ->
            plant to diaries
        }.onEach { (plant, diaries) ->
            // 완료 상태와 최근 날짜는 Repository가 실제 이력으로 계산한 값을 사용한다.
            val uiModel = plant.toPlantListItemUiModel {
                getWateringDaysUseCase(it)
            }
            setState {
                copy(
                    today = LocalDate.now(),
                    plantImagePath = uiModel.imagePath,
                    plantName = uiModel.name,
                    createdAt = uiModel.createdAt,
                    dDays = uiModel.dDay,
                    wateringStatus = uiModel.status,
                    lastWateringDate = plant.latestWateringDate,
                    wateringAlarmTime = if (plant.waterPeriod == 0) null else plant.wateringAlarm.time,
                    isWateringActive = plant.wateringAlarm.isActive,
                    memo = plant.memo,
                    diaries = diaries.map { it.toDiaryListItemUiModel() },
                    harvestDate = plant.harvestDate,
                    harvestMemo = plant.harvestMemo
                )
            }
        }
            .launchIn(viewModelScope)
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? PlantDetailEvent ?: return

        when (event) {
            PlantDetailEvent.OnWateringCalendarClicked -> {
                // 수확 식물은 수확일에서 조회를 시작하고 나머지는 오늘을 기본 선택한다.
                openCalendar(CalendarTarget.Watering, viewState.value.harvestDate ?: LocalDate.now())
            }
            is PlantDetailEvent.OnTaskCalendarClicked -> openTaskCalendar(event.taskId)
            PlantDetailEvent.OnCalendarClosed -> {
                if (!viewState.value.isCalendarSaving) closeCalendar()
            }
            is PlantDetailEvent.OnCalendarMonthChanged -> {
                if (!viewState.value.isCalendarSaving) {
                    setState { copy(calendarMonth = event.month) }
                }
            }
            is PlantDetailEvent.OnCalendarDateSelected -> {
                if (!viewState.value.isCalendarSaving) {
                    setState { copy(calendarSelectedDate = event.date, calendarError = null) }
                }
            }
            PlantDetailEvent.OnCalendarSaveClicked -> saveCalendarDate()
            PlantDetailEvent.OnCalendarRetry -> {
                when (val target = viewState.value.calendarTarget) {
                    is CalendarTarget.Task -> observeTaskLogDates(target.taskId)
                    else -> observeWateringDates()
                }
            }

            PlantDetailEvent.OnAddTaskClicked -> {
                if (!viewState.value.isHarvested) {
                    setState { copy(taskDialog = PlantTaskDialogState(date = LocalDate.now())) }
                }
            }
            is PlantDetailEvent.OnTaskMoreClicked -> openTaskEditDialog(event.taskId)
            PlantDetailEvent.OnTaskDialogDismissed -> {
                if (!viewState.value.isTaskSaving) setState { copy(taskDialog = null) }
            }
            is PlantDetailEvent.OnTaskTitleChanged -> updateTaskDialog {
                copy(title = event.title.take(PlantTaskDialogState.TITLE_MAX_LENGTH))
            }
            is PlantDetailEvent.OnTaskContentChanged -> updateTaskDialog { copy(content = event.content) }
            is PlantDetailEvent.OnTaskTypeChanged -> updateTaskDialog {
                // 종류는 생성 시에만 바꿀 수 있다.
                if (isEdit) this else copy(type = event.type)
            }
            is PlantDetailEvent.OnTaskDateChanged -> updateTaskDialog { copy(date = event.date) }
            is PlantDetailEvent.OnTaskPeriodChanged -> updateTaskDialog { copy(periodDays = event.periodDays) }
            is PlantDetailEvent.OnTaskAlarmTimeChanged -> updateTaskDialog { copy(alarmTime = event.time) }
            PlantDetailEvent.OnTaskSaveClicked -> saveTask()
            PlantDetailEvent.OnTaskDeleteClicked -> {
                viewState.value.taskDialog?.taskId?.let {
                    setEffect(PlantDetailEffect.ShowTaskDeleteConfirmDialog(it))
                }
            }
            is PlantDetailEvent.OnTaskDeleteConfirmed -> deleteTask(event.taskId)
            is PlantDetailEvent.OnTaskCompleteClicked -> completeTask(event.taskId)
            is PlantDetailEvent.OnOneTimeTaskCompleteConfirmed -> {
                // 할일 달력에서 확인한 완료는 저장 후 달력도 닫는다.
                val fromCalendar = (viewState.value.calendarTarget as? CalendarTarget.Task)
                    ?.taskId == event.taskId
                saveTaskLog(event.taskId, event.date, isDone = true, fromCalendar = fromCalendar)
            }
            is PlantDetailEvent.OnTaskCancelClicked -> cancelTask(event.taskId)
            is PlantDetailEvent.ToggleTaskAlarm -> toggleTaskAlarm(event.taskId, event.isActive)

            is PlantDetailEvent.OnBackClicked -> {
                setEffect(PlantDetailEffect.Navigation.Back)
            }

            is PlantDetailEvent.OnEditPlantClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToEditPlant)
            }

            is PlantDetailEvent.OnDeletePlantClicked -> {
                setEffect(PlantDetailEffect.ShowDeleteConfirmDialog)
            }

            is PlantDetailEvent.OnDeleteConfirmClicked -> {
                deletePlant()
            }

            is PlantDetailEvent.ToggleWateringAlarmActive -> {
                toggleWateringAlarm(event.isActive)
            }

            is PlantDetailEvent.OnWateringClicked -> {
                waterPlant()
            }

            is PlantDetailEvent.OnDiaryClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToDiaryDetail(event.diaryId))
            }

            is PlantDetailEvent.OnNewDiaryClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToNewDiary)
            }

            is PlantDetailEvent.OnMoreDiaryClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToMoreDiaries)
            }

            is PlantDetailEvent.OnHarvestSectionToggled -> {
                setState { copy(isHarvestSectionExpanded = !isHarvestSectionExpanded) }
            }

            is PlantDetailEvent.OnHarvestMemoChanged -> {
                setState { copy(harvestMemoInput = event.memo) }
            }

            is PlantDetailEvent.OnHarvestClicked -> {
                setEffect(PlantDetailEffect.ShowHarvestConfirmDialog)
            }

            is PlantDetailEvent.OnHarvestConfirmed -> {
                harvestPlant(event.date)
            }

            is PlantDetailEvent.OnCancelHarvestClicked -> {
                cancelHarvest()
            }

            is PlantDetailEvent.OnCancelWateringClicked -> {
                cancelWatering()
            }

            is PlantDetailEvent.OnExportClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToExportDiary)
            }
        }
    }

    private fun observeWateringDates() {
        // 재시도 시 기존 구독을 교체해 같은 이력을 여러 번 수신하지 않는다.
        wateringObserveJob?.cancel()
        setState { copy(isWateringLoading = true, isWateringLoadFailed = false) }
        // 최신 물주기 날짜가 그대로인 과거 기록 변경도 달력에는 즉시 반영한다.
        wateringObserveJob = getWateringDatesUseCase(plantId)
            .onEach { dates ->
                setState { copy(wateringDates = dates.toSet(), isWateringLoading = false) }
            }
            .catch {
                setState { copy(isWateringLoading = false, isWateringLoadFailed = true) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTasks() {
        getPlantTasksUseCase(plantId)
            .onEach { tasks ->
                domainTasks = tasks.associateBy { it.id }
                val today = LocalDate.now()
                setState {
                    copy(
                        tasks = tasks.map {
                            it.toPlantTaskUiModel(
                                dueDate = getNextDueDateUseCase(it),
                                remainDays = getTaskRemainDaysUseCase(it, today)
                            )
                        }
                    )
                }
            }
            .catch { Timber.e(it, "Failed to observe plant tasks") }
            .launchIn(viewModelScope)
    }

    private fun observePlantTaskLogs() {
        getPlantTaskLogsUseCase(plantId)
            .onEach { logs ->
                setState { copy(plantTaskLogs = logs.groupBy({ it.date }, { it.title })) }
            }
            .catch { Timber.e(it, "Failed to observe plant task logs") }
            .launchIn(viewModelScope)
    }

    private fun observeTaskLogDates(taskId: Int) {
        taskLogObserveJob?.cancel()
        setState { copy(isTaskLogLoading = true, isTaskLogLoadFailed = false) }
        taskLogObserveJob = getTaskLogDatesUseCase(taskId)
            .onEach { dates ->
                setState { copy(taskLogDates = dates.toSet(), isTaskLogLoading = false) }
            }
            .catch {
                setState { copy(isTaskLogLoading = false, isTaskLogLoadFailed = true) }
            }
            .launchIn(viewModelScope)
    }

    private fun openCalendar(target: CalendarTarget, date: LocalDate) {
        // 이전 할일 달력의 이력이 다른 대상에 남지 않도록 구독과 값을 정리한다.
        taskLogObserveJob?.cancel()
        taskLogObserveJob = null
        setState {
            copy(
                calendarTarget = target,
                calendarSelectedDate = date,
                calendarMonth = YearMonth.from(date),
                calendarError = null,
                taskLogDates = emptySet(),
                isTaskLogLoading = false,
                isTaskLogLoadFailed = false
            )
        }
    }

    private fun openTaskCalendar(taskId: Int) {
        val state = viewState.value
        if (state.isHarvested) return
        val task = state.tasks.find { it.id == taskId } ?: return
        openCalendar(CalendarTarget.Task(task.id, task.title, task.type), LocalDate.now())
        observeTaskLogDates(taskId)
    }

    private fun closeCalendar() {
        taskLogObserveJob?.cancel()
        taskLogObserveJob = null
        setState {
            copy(
                calendarTarget = null,
                taskLogDates = emptySet(),
                isTaskLogLoading = false,
                isTaskLogLoadFailed = false
            )
        }
    }

    private fun saveCalendarDate() {
        val state = viewState.value
        when (val target = state.calendarTarget) {
            CalendarTarget.Watering -> saveWateringDate()
            is CalendarTarget.Task -> {
                // 조회 전·실패 상태의 빈 이력을 미완료로 해석하지 않는다.
                if (state.isCalendarSaving || state.isTaskLogLoading || state.isTaskLogLoadFailed) return
                val date = state.calendarSelectedDate
                val isDone = date !in state.taskLogDates
                if (isDone && !state.isInRecordRange(date)) return
                // 1회성은 완료하면 할일이 사라지므로 확인 후 저장한다.
                if (isDone && target.type == PlantTaskType.ONE_TIME) {
                    setEffect(PlantDetailEffect.ShowOneTimeTaskCompleteDialog(target.taskId, date))
                } else {
                    saveTaskLog(target.taskId, date, isDone, fromCalendar = true)
                }
            }
            null -> Unit
        }
    }

    private fun PlantDetailState.isInRecordRange(date: LocalDate): Boolean =
        createdAt != null && date >= createdAt && date <= LocalDate.now()

    private fun saveWateringDate(
        date: LocalDate = viewState.value.calendarSelectedDate,
        closeDialog: Boolean = true,
        isWatered: Boolean = date !in viewState.value.wateringDates
    ) {
        val state = viewState.value
        // 조회 실패를 빈 이력으로 해석하지 않으며 저장 중 중복 실행도 차단한다.
        if (state.isCalendarSaving || state.isWateringLoading || state.isHarvested ||
            state.isWateringLoadFailed
        ) return
        val add = isWatered
        // 날짜 범위는 추가에만 적용해 기존 범위 밖 기록의 취소는 허용한다.
        if (add && !state.isInRecordRange(date)) return
        setState { copy(isCalendarSaving = true, calendarError = null) }
        viewModelScope.launch {
            try {
                val result = updateWateringLogUseCase(plantId, date, add)
                // 알람 실패도 DB 저장은 완료된 결과이므로 다이얼로그를 닫는다.
                if (closeDialog) closeCalendar()
                if (result == WateringUpdateResult.ALARM_FAILED) {
                    setEffect(PlantDetailEffect.ShowSnackbar(R.string.message_watering_alarm_failed))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // DB 저장 실패는 선택 날짜를 유지해 같은 작업을 다시 시도하게 한다.
                if (closeDialog) {
                    setState { copy(calendarError = e.wateringErrorResource()) }
                } else {
                    setEffect(PlantDetailEffect.ShowSnackbar(e.wateringErrorResource()))
                }
            } finally {
                setState { copy(isCalendarSaving = false) }
            }
        }
    }

    private fun saveTaskLog(taskId: Int, date: LocalDate, isDone: Boolean, fromCalendar: Boolean) {
        val state = viewState.value
        if (state.isCalendarSaving || state.isHarvested) return
        if (isDone && !state.isInRecordRange(date)) return
        val type = domainTasks[taskId]?.type ?: return
        setState { copy(isCalendarSaving = true, calendarError = null) }
        viewModelScope.launch {
            try {
                val result = updatePlantTaskLogUseCase(taskId, date, isDone)
                if (result != WateringUpdateResult.UNCHANGED) {
                    analyticsLogger.log(
                        if (isDone) GardenEvent.CompleteTask(type.name.lowercase())
                        else GardenEvent.CancelTask
                    )
                }
                if (fromCalendar) closeCalendar()
                if (result == WateringUpdateResult.ALARM_FAILED) {
                    setEffect(PlantDetailEffect.ShowSnackbar(R.string.message_task_alarm_failed))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (fromCalendar) {
                    setState { copy(calendarError = e.taskErrorResource()) }
                } else {
                    setEffect(PlantDetailEffect.ShowSnackbar(e.taskErrorResource()))
                }
            } finally {
                setState { copy(isCalendarSaving = false) }
            }
        }
    }

    private fun completeTask(taskId: Int) {
        val state = viewState.value
        if (state.isHarvested) return
        val task = state.tasks.find { it.id == taskId } ?: return
        val today = LocalDate.now()
        when (task.type) {
            PlantTaskType.ONE_TIME ->
                setEffect(PlantDetailEffect.ShowOneTimeTaskCompleteDialog(taskId, today))
            PlantTaskType.REPEAT ->
                if (!task.isDoneToday) saveTaskLog(taskId, today, isDone = true, fromCalendar = false)
        }
    }

    private fun cancelTask(taskId: Int) {
        val task = viewState.value.tasks.find { it.id == taskId } ?: return
        if (task.type == PlantTaskType.REPEAT && task.isDoneToday) {
            saveTaskLog(taskId, LocalDate.now(), isDone = false, fromCalendar = false)
        }
    }

    private fun openTaskEditDialog(taskId: Int) {
        val state = viewState.value
        if (state.isHarvested) return
        val task = state.tasks.find { it.id == taskId } ?: return
        setState {
            copy(
                taskDialog = PlantTaskDialogState(
                    taskId = task.id,
                    title = task.title,
                    content = task.content.orEmpty(),
                    type = task.type,
                    // 반복성은 기준일 대신 현재 다음 예정일을 보여준다.
                    date = task.dueDate,
                    initialDate = task.dueDate,
                    periodDays = task.periodDays ?: PlantTaskDialogState.DEFAULT_TASK_PERIOD_DAYS,
                    alarmTime = task.alarmTime
                )
            )
        }
    }

    private fun updateTaskDialog(update: PlantTaskDialogState.() -> PlantTaskDialogState) {
        if (viewState.value.isTaskSaving) return
        setState { copy(taskDialog = taskDialog?.update()) }
    }

    private fun saveTask() {
        val state = viewState.value
        val dialog = state.taskDialog ?: return
        if (state.isTaskSaving || state.isHarvested || !dialog.canSave) return
        val periodDays = dialog.periodDays.takeIf { dialog.type == PlantTaskType.REPEAT }
        val title = dialog.title.trim()
        val content = dialog.content.trim().ifBlank { null }
        setState { copy(isTaskSaving = true) }
        viewModelScope.launch {
            try {
                val result = if (dialog.taskId == null) {
                    addPlantTaskUseCase(
                        PlantTask(
                            plantId = plantId,
                            title = title,
                            content = content,
                            type = dialog.type,
                            date = dialog.date,
                            periodDays = periodDays,
                            // 생성 시 알림은 꺼진 상태로 시작한다.
                            alarm = Alarm(dialog.alarmTime, false),
                            createdAt = LocalDateTime.now()
                        )
                    ).also {
                        analyticsLogger.log(GardenEvent.AddTask(dialog.type.name.lowercase(), false))
                    }
                } else {
                    val origin = domainTasks[dialog.taskId]
                    if (origin == null) {
                        setState { copy(taskDialog = null) }
                        return@launch
                    }
                    updatePlantTaskUseCase(
                        origin.copy(
                            title = title,
                            content = content,
                            date = dialog.date,
                            periodDays = periodDays,
                            alarm = origin.alarm.copy(time = dialog.alarmTime)
                        ),
                        isDateChanged = dialog.isDateChanged
                    )
                }
                setState { copy(taskDialog = null) }
                if (result == WateringUpdateResult.ALARM_FAILED) {
                    setEffect(PlantDetailEffect.ShowSnackbar(R.string.message_task_alarm_failed))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setEffect(PlantDetailEffect.ShowSnackbar(e.taskErrorResource()))
            } finally {
                setState { copy(isTaskSaving = false) }
            }
        }
    }

    private fun deleteTask(taskId: Int) {
        if (viewState.value.isTaskSaving) return
        setState { copy(isTaskSaving = true) }
        viewModelScope.launch {
            try {
                deletePlantTaskUseCase(taskId)
                analyticsLogger.log(GardenEvent.DeleteTask)
                setState { copy(taskDialog = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setEffect(PlantDetailEffect.ShowSnackbar(e.taskErrorResource()))
            } finally {
                setState { copy(isTaskSaving = false) }
            }
        }
    }

    private fun toggleTaskAlarm(taskId: Int, isActive: Boolean) {
        val state = viewState.value
        if (state.isHarvested || state.isTaskSaving) return
        if (isActive && !canScheduleExactAlarms(application)) {
            setEffect(PlantDetailEffect.ShowExactAlarmPermissionDialog)
            return
        }
        setState { copy(isTaskSaving = true) }
        viewModelScope.launch {
            try {
                updatePlantTaskAlarmActivationUseCase(taskId, isActive)
                analyticsLogger.log(GardenEvent.TaskAlarmToggle(isActive))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setEffect(PlantDetailEffect.ShowSnackbar(e.taskErrorResource()))
            } finally {
                setState { copy(isTaskSaving = false) }
            }
        }
    }

    private fun toggleWateringAlarm(isActive: Boolean) {
        if (isActive && !canScheduleExactAlarms(application)) {
            setEffect(PlantDetailEffect.ShowExactAlarmPermissionDialog)
            return
        }

        viewModelScope.launch {
            updateWateringAlarmActivationUseCase(plantId, isActive)
            analyticsLogger.log(GardenEvent.WateringAlarmToggle(isActive))
        }
    }

    private fun waterPlant() {
        analyticsLogger.log(GardenEvent.WateringClick(WateringSource.DETAIL))
        if (LocalDate.now() !in viewState.value.wateringDates) {
            saveWateringDate(LocalDate.now(), false, true)
        }
    }

    private fun deletePlant() {
        viewModelScope.launch {
            val plant = plantRepository.getPlant(plantId) ?: return@launch
            deletePlantUseCase(plant)
            setEffect(PlantDetailEffect.Navigation.Back)
        }
    }

    private fun harvestPlant(harvestDate: LocalDate) {
        viewModelScope.launch {
            harvestPlantUseCase.harvest(
                plantId = plantId,
                harvestMemo = viewState.value.harvestMemoInput.ifBlank { null },
                harvestDate = harvestDate
            )
            analyticsLogger.log(GardenEvent.Harvest)
            setState { copy(isHarvestSectionExpanded = false, harvestMemoInput = "") }
        }
    }

    private fun cancelHarvest() {
        viewModelScope.launch {
            harvestPlantUseCase.cancelHarvest(plantId)
            analyticsLogger.log(GardenEvent.CancelHarvest)
        }
    }

    private fun cancelWatering() {
        if (LocalDate.now() in viewState.value.wateringDates) {
            saveWateringDate(LocalDate.now(), false, false)
            analyticsLogger.log(GardenEvent.CancelWatering)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: PlantDetailNavKey): PlantDetailViewModel
    }
}
