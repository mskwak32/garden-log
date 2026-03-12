package com.mskwak.plant.diary_list

import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.domain.useCase.diary.GetDiariesUseCase
import com.mskwak.domain.useCase.plant.GetPlantsWithSortOrderUseCase
import com.mskwak.plant.model.toDiaryListItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(
    private val getDiariesUseCase: GetDiariesUseCase,
    private val getPlantsWithSortOrderUseCase: GetPlantsWithSortOrderUseCase
) : BaseViewModel<DiaryListState, DiaryListEvent, DiaryListEffect>() {
    override fun setInitialState(): DiaryListState = DiaryListState()

    init {
        observePlants()
        observeDiaries()
    }

    private fun observePlants() {
        getPlantsWithSortOrderUseCase(PlantListSortOrder.CREATED_LATEST)
            .onEach { plants ->
                setState {
                    copy(plantFilterList = plants.map {
                        DiaryListPlantFilterUiModel(
                            it.id,
                            it.name
                        )
                    })
                }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDiaries() {
        viewState
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
            .onEach { diaries ->
                setState {
                    copy(diaries = diaries.map { it.toDiaryListItemUiModel() })
                }
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