package com.mskwak.plant.plant_detail

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.analytics.WateringSource
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.usecase.diary.GetDiariesByPlantIdUseCase
import com.mskwak.domain.usecase.plant.DeletePlantUseCase
import com.mskwak.domain.usecase.plant.GetPlantUseCase
import com.mskwak.domain.usecase.plant.HarvestPlantUseCase
import com.mskwak.domain.usecase.watering.GetWateringDatesUseCase
import com.mskwak.domain.usecase.watering.GetWateringDaysUseCase
import com.mskwak.domain.usecase.watering.UpdateWateringAlarmActivationUseCase
import com.mskwak.domain.usecase.watering.UpdateWateringLogUseCase
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import com.mskwak.plant.R
import com.mskwak.plant.model.toDiaryListItemUiModel
import com.mskwak.plant.model.toPlantListItemUiModel
import com.mskwak.plant.util.canScheduleExactAlarms
import com.mskwak.plant.util.wateringErrorResource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<PlantDetailState, PlantDetailEvent, PlantDetailEffect>() {

    private val plantId: Int = navKey.plantId
    private var wateringObserveJob: Job? = null

    init {
        analyticsLogger.log(GardenEvent.ScreenView("plant_detail"))
        observePlant()
        observeWateringDates()
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
                val date = viewState.value.harvestDate ?: LocalDate.now()
                setState {
                    copy(
                        isWateringDateDialogOpen = true,
                        selectedWateringDate = date,
                        wateringMonth = YearMonth.from(date),
                        wateringError = wateringError.takeIf { it == R.string.message_watering_load_failed }
                    )
                }
            }
            PlantDetailEvent.OnWateringCalendarClosed -> {
                if (!viewState.value.isWateringSaving) {
                    setState { copy(isWateringDateDialogOpen = false) }
                }
            }
            is PlantDetailEvent.OnWateringMonthChanged -> {
                if (!viewState.value.isWateringSaving) {
                    setState { copy(wateringMonth = event.month) }
                }
            }
            is PlantDetailEvent.OnWateringDateSelected -> {
                if (!viewState.value.isWateringSaving) {
                    setState {
                        copy(
                            selectedWateringDate = event.date,
                            wateringError = wateringError.takeIf { it == R.string.message_watering_load_failed }
                        )
                    }
                }
            }
            PlantDetailEvent.OnWateringDateSaved -> saveWateringDate()
            PlantDetailEvent.OnWateringRetry -> observeWateringDates()

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
        setState { copy(isWateringLoading = true, wateringError = null) }
        // 최신 물주기 날짜가 그대로인 과거 기록 변경도 달력에는 즉시 반영한다.
        wateringObserveJob = getWateringDatesUseCase(plantId)
            .onEach { dates ->
                setState { copy(wateringDates = dates.toSet(), isWateringLoading = false) }
            }
            .catch {
                setState {
                    copy(
                        isWateringLoading = false,
                        wateringError = R.string.message_watering_load_failed
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun saveWateringDate(
        date: LocalDate = viewState.value.selectedWateringDate,
        closeDialog: Boolean = true,
        isWatered: Boolean = date !in viewState.value.wateringDates
    ) {
        val state = viewState.value
        // 조회 실패를 빈 이력으로 해석하지 않으며 저장 중 중복 실행도 차단한다.
        if (state.isWateringSaving || state.isWateringLoading || state.isHarvested ||
            state.wateringError == R.string.message_watering_load_failed
        ) return
        val add = isWatered
        // 날짜 범위는 추가에만 적용해 기존 범위 밖 기록의 취소는 허용한다.
        if (add && (state.createdAt == null || date < state.createdAt || date > LocalDate.now())) return
        setState { copy(isWateringSaving = true, wateringError = null) }
        viewModelScope.launch {
            try {
                val result = updateWateringLogUseCase(plantId, date, add)
                // 알람 실패도 DB 저장은 완료된 결과이므로 다이얼로그를 닫는다.
                setState {
                    copy(
                        isWateringDateDialogOpen = if (closeDialog) false
                        else isWateringDateDialogOpen
                    )
                }
                if (result == WateringUpdateResult.ALARM_FAILED) {
                    setEffect(PlantDetailEffect.ShowSnackbar(R.string.message_watering_alarm_failed))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // DB 저장 실패는 선택 날짜를 유지해 같은 작업을 다시 시도하게 한다.
                setState { copy(wateringError = e.wateringErrorResource()) }
                if (!closeDialog) {
                    setEffect(PlantDetailEffect.ShowSnackbar(e.wateringErrorResource()))
                }
            } finally {
                setState { copy(isWateringSaving = false) }
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
