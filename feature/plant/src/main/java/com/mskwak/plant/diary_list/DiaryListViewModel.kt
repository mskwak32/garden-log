package com.mskwak.plant.diary_list

import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(

) : BaseViewModel<DiaryListState, DiaryListEvent, DiaryListEffect>() {
    override fun setInitialState(): DiaryListState {
        TODO("Not yet implemented")
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        TODO("Not yet implemented")
    }
}