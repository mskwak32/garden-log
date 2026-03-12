package com.mskwak.plant.diary_detail

import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.Diary
import com.mskwak.domain.useCase.diary.DeleteDiaryUseCase
import com.mskwak.domain.useCase.diary.GetDiaryUseCase
import com.mskwak.domain.useCase.plant.GetPlantNameUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DiaryDetailViewModel.Factory::class)
class DiaryDetailViewModel @AssistedInject constructor(
    @Assisted navKey: DiaryDetailNavKey,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val getPlantNameUseCase: GetPlantNameUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : BaseViewModel<DiaryDetailState, DiaryDetailEvent, DiaryDetailEffect>() {

    private val diaryId: Int = navKey.diaryId
    private var currentDiary: Diary? = null
    private var currentPlantId: Int = 0

    init {
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
                val plantName = getPlantNameUseCase.getName(diary.plantId) ?: ""
                setState {
                    copy(
                        plantName = plantName,
                        diaryDate = diary.createdDate,
                        picturePaths = diary.pictureList?.map { it.path } ?: emptyList(),
                        memo = diary.memo
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
