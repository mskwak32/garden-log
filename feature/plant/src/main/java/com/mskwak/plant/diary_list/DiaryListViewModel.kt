package com.mskwak.plant.diary_list

import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.domain.usecase.diary.GetDiariesUseCase
import com.mskwak.domain.usecase.plant.GetPlantsWithSortOrderUseCase
import com.mskwak.plant.model.toDiaryListItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(
    private val getDiariesUseCase: GetDiariesUseCase,
    private val getPlantsWithSortOrderUseCase: GetPlantsWithSortOrderUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<DiaryListState, DiaryListEvent, DiaryListEffect>() {
    override fun setInitialState(): DiaryListState = DiaryListState()

    init {
        analyticsLogger.log(GardenEvent.ScreenView("diary_list"))
        observePlants()
        observeDiaries()
    }

    private fun observePlants() {
        getPlantsWithSortOrderUseCase(PlantListSortOrder.CREATED_LATEST)
            .onEach { plants ->
                setState {
                    copy(plantFilterList = plants.map {
                        DiaryListPlantFilterUiModel(
                            plantId = it.id,
                            plantName = it.name,
                            isHarvested = it.isHarvested
                        )
                    })
                }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDiaries() {
        val diariesFlow = viewState
            .map { Triple(it.currentYearMonth, it.sortOder, it.currentPlant) }
            .distinctUntilChanged()
            .flatMapLatest { (yearMonth, sortOrder, currentPlant) ->
                getDiariesUseCase(
                    year = yearMonth.year,
                    month = yearMonth.monthValue,
                    plantId = currentPlant?.plantId,
                    sortOder = sortOrder
                )
            }

        // plantFilterList가 나중에 로드되더라도 식물 이름이 반영되도록 combine 사용
        combine(
            diariesFlow,
            viewState.map { it.plantFilterList }.distinctUntilChanged()
        ) { diaries, plantFilterList ->
            val plantMap = plantFilterList.associateBy { it.plantId }
            diaries.map { diary ->
                val plant = plantMap[diary.plantId]
                diary.toDiaryListItemUiModel(
                    plantName = plant?.plantName,
                    isHarvested = plant?.isHarvested ?: false
                )
            }
        }
            .onEach { mappedDiaries ->
                setState { copy(diaries = mappedDiaries) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? DiaryListEvent ?: return

        when (event) {
            is DiaryListEvent.SelectMonth -> {
                setEffect(DiaryListEffect.ShowSelectYearMonth(viewState.value.currentYearMonth))
            }

            is DiaryListEvent.YearMonthChanged -> {
                yearMonthChange(event.yearMonth)
            }

            is DiaryListEvent.OnPreviousMonthClicked -> {
                yearMonthChange(viewState.value.currentYearMonth.minusMonths(1))
            }

            is DiaryListEvent.OnNextMonthClicked -> {
                yearMonthChange(viewState.value.currentYearMonth.plusMonths(1))
            }

            is DiaryListEvent.OnThisMonthClicked -> {
                yearMonthChange(YearMonth.now())
            }

            is DiaryListEvent.PlantFilterChanged -> {
                setState { copy(currentPlant = event.plantFilter) }
            }

            is DiaryListEvent.SortOderChanged -> {
                setState { copy(sortOder = event.sortOder) }
            }

            is DiaryListEvent.OnDiaryClicked -> {
                setEffect(DiaryListEffect.Navigation.GoToDiaryDetail(event.diaryId))
            }
        }
    }

    private fun yearMonthChange(yearMonth: YearMonth) {
        setState {
            copy(currentYearMonth = yearMonth)
        }
    }

}