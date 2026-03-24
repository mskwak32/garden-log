package com.mskwak.plant.diary_export

import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.DiaryExport
import com.mskwak.domain.model.ExportRequest
import com.mskwak.domain.model.PlantDetailExport
import com.mskwak.domain.usecase.diary.GetDiariesForExportUseCase
import com.mskwak.domain.usecase.diary.GetDiaryDateRangeUseCase
import com.mskwak.domain.usecase.export.GenerateExportUseCase
import com.mskwak.domain.usecase.plant.GetPlantUseCase
import com.mskwak.domain.usecase.watering.GetWateringDatesForExportUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = DiaryExportViewModel.Factory::class)
class DiaryExportViewModel @AssistedInject constructor(
    @Assisted navKey: DiaryExportNavKey,
    private val getPlantUseCase: GetPlantUseCase,
    private val getDiaryDateRangeUseCase: GetDiaryDateRangeUseCase,
    private val getDiariesForExportUseCase: GetDiariesForExportUseCase,
    private val getWateringDatesForExportUseCase: GetWateringDatesForExportUseCase,
    private val generateExportUseCase: GenerateExportUseCase
) : BaseViewModel<DiaryExportState, DiaryExportEvent, DiaryExportEffect>() {

    private val plantId = navKey.plantId

    init {
        loadInitialData()
    }

    override fun setInitialState() = DiaryExportState()

    private fun loadInitialData() {
        viewModelScope.launch {
            val plant = getPlantUseCase(plantId).filterNotNull().first()
            val dateRange = getDiaryDateRangeUseCase(plantId)

            setState {
                copy(
                    plantName = plant.name,
                    diaryDateRange = dateRange,
                    startDate = dateRange?.first ?: plant.createdDate,
                    endDate = dateRange?.second ?: plant.createdDate
                )
            }
        }
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? DiaryExportEvent ?: return

        when (event) {
            is DiaryExportEvent.OnBackClicked -> {
                setEffect(DiaryExportEffect.NavigateBack)
            }

            is DiaryExportEvent.OnIncludeFirstPageChanged -> {
                setState { copy(includeFirstPage = event.include) }
            }

            is DiaryExportEvent.OnStartDateChanged -> {
                setState { copy(startDate = event.date) }
            }

            is DiaryExportEvent.OnEndDateChanged -> {
                setState { copy(endDate = event.date) }
            }

            is DiaryExportEvent.OnExportClicked -> {
                exportDiary()
            }
        }
    }

    private fun exportDiary() {
        viewModelScope.launch {
            val state = viewState.value
            setState { copy(isExporting = true) }

            try {
                val diaries = getDiariesForExportUseCase(plantId, state.startDate, state.endDate)

                if (diaries.isEmpty()) {
                    setState { copy(isExporting = false) }
                    setEffect(DiaryExportEffect.ShowNoDiaryInRange)
                    return@launch
                }

                val plant = getPlantUseCase(plantId).filterNotNull().first()
                val wateringDates =
                    getWateringDatesForExportUseCase(plantId, state.startDate, state.endDate)

                val plantDetail = if (state.includeFirstPage) {
                    PlantDetailExport(
                        name = plant.name,
                        imagePath = plant.picture?.path,
                        plantingDate = plant.createdDate,
                        wateringCycle = plant.waterPeriod,
                        memo = plant.memo,
                        harvestDate = plant.harvestDate,
                        harvestMemo = plant.harvestMemo
                    )
                } else null

                val request = ExportRequest(
                    plantName = plant.name,
                    includeFirstPage = state.includeFirstPage,
                    plantDetail = plantDetail,
                    diaries = diaries.map { diary ->
                        DiaryExport(
                            date = diary.createdDate,
                            content = diary.memo,
                            imagePaths = diary.pictureList?.map { it.path } ?: emptyList(),
                            hasWateringRecord = diary.createdDate in wateringDates
                        )
                    }
                )

                generateExportUseCase(request)
                setState { copy(isExporting = false) }
                setEffect(DiaryExportEffect.ShowExportSuccess)
            } catch (e: Exception) {
                Timber.e(e, "PDF 내보내기 실패")
                setState { copy(isExporting = false) }
                setEffect(DiaryExportEffect.ShowExportFail)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: DiaryExportNavKey): DiaryExportViewModel
    }
}
