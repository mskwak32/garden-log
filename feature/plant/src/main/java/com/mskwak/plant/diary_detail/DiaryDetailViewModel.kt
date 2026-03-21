package com.mskwak.plant.diary_detail

import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.Diary
import com.mskwak.domain.useCase.diary.DeleteDiaryUseCase
import com.mskwak.domain.useCase.diary.GetDiaryUseCase
import com.mskwak.domain.useCase.plant.GetPlantUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DiaryDetailViewModel.Factory::class)
class DiaryDetailViewModel @AssistedInject constructor(
    @Assisted navKey: DiaryDetailNavKey,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val getPlantUseCase: GetPlantUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<DiaryDetailState, DiaryDetailEvent, DiaryDetailEffect>() {

    private val diaryId: Int = navKey.diaryId
    private var currentDiary: Diary? = null
    private var currentPlantId: Int = 0

    init {
        analyticsLogger.log(GardenEvent.ScreenView("diary_detail"))
        loadDiary()
    }

    override fun setInitialState(): DiaryDetailState = DiaryDetailState()

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? DiaryDetailEvent ?: return
        when (event) {
            is DiaryDetailEvent.BackClick -> setEffect(DiaryDetailEffect.Navigation.Back)
            is DiaryDetailEvent.EditClick -> {
                setEffect(DiaryDetailEffect.Navigation.GoToEdit(currentPlantId, diaryId))
            }

            is DiaryDetailEvent.DeleteClick -> setEffect(DiaryDetailEffect.ShowDeleteConfirmDialog)
            is DiaryDetailEvent.DeleteConfirm -> deleteDiary()
        }
    }

    private fun loadDiary() {
        viewModelScope.launch {
            getDiaryUseCase(diaryId).collect { diary ->
                currentDiary = diary
                currentPlantId = diary.plantId
                val plant = getPlantUseCase(diary.plantId).filterNotNull().first()
                setState {
                    copy(
                        plantName = plant.name,
                        diaryDate = diary.createdDate,
                        picturePaths = diary.pictureList?.map { it.path } ?: emptyList(),
                        memo = diary.memo,
                        isHarvested = plant.isHarvested
                    )
                }
            }
        }
    }

    private fun deleteDiary() {
        val diary = currentDiary ?: return
        viewModelScope.launch {
            deleteDiaryUseCase(diary)
            setEffect(DiaryDetailEffect.Navigation.Back)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: DiaryDetailNavKey): DiaryDetailViewModel
    }
}
