package com.mskwak.plant.diary_more

import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.Constants
import com.mskwak.domain.model.DiaryListSortOrder
import com.mskwak.domain.usecase.diary.GetDiariesByPlantIdUseCase
import com.mskwak.domain.usecase.plant.GetPlantNameUseCase
import com.mskwak.plant.model.toDiaryListItemUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DiaryMoreViewModel.Factory::class)
class DiaryMoreViewModel @AssistedInject constructor(
    @Assisted navKey: DiaryMoreNavKey,
    private val getDiariesByPlantIdUseCase: GetDiariesByPlantIdUseCase,
    private val getPlantNameUseCase: GetPlantNameUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<DiaryMoreState, DiaryMoreEvent, DiaryMoreEffect>() {

    private val plantId = navKey.plantId
    private var currentPage = 0
    private var loadJob: Job? = null

    override fun setInitialState(): DiaryMoreState = DiaryMoreState()

    init {
        analyticsLogger.log(GardenEvent.ScreenView("diary_more"))
        loadPlantName()
        loadPage(page = 0)
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? DiaryMoreEvent ?: return

        when (event) {
            is DiaryMoreEvent.BackClick -> {
                setEffect(DiaryMoreEffect.Navigation.Back)
            }

            is DiaryMoreEvent.OnDiaryClicked -> {
                setEffect(DiaryMoreEffect.Navigation.ToDiaryDetail(event.diaryId))
            }

            is DiaryMoreEvent.SortOrderChanged -> {
                if (viewState.value.sortOrder != event.sortOrder) {
                    setState {
                        copy(
                            sortOrder = event.sortOrder,
                            diaries = emptyList(),
                            hasMore = true
                        )
                    }
                    currentPage = 0
                    loadPage(page = 0)
                }
            }

            is DiaryMoreEvent.LoadMore -> {
                if (!viewState.value.isLoading && viewState.value.hasMore) {
                    loadPage(currentPage + 1)
                }
            }
        }
    }

    private fun loadPlantName() {
        viewModelScope.launch {
            val name = getPlantNameUseCase.getName(plantId) ?: return@launch
            setState { copy(plantName = name) }
        }
    }

    private fun loadPage(page: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState { copy(isLoading = true) }

            val ascending = viewState.value.sortOrder == DiaryListSortOrder.CREATED_EARLIEST
            val diaries = getDiariesByPlantIdUseCase(
                plantId = plantId,
                limit = Constants.PAGE_SIZE,
                page = page,
                ascending = ascending
            ).firstOrNull() ?: emptyList()
            val uiModels = diaries.map { it.toDiaryListItemUiModel() }
            val newItems = buildDiaryListItems(uiModels)

            currentPage = page
            setState {
                copy(
                    diaries = if (page == 0) newItems else this.diaries + newItems,
                    isLoading = false,
                    hasMore = uiModels.size == Constants.PAGE_SIZE
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: DiaryMoreNavKey): DiaryMoreViewModel
    }
}
