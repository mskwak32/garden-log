package com.mskwak.plant.plant_detail

import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(

) : BaseViewModel<PlantDetailState, PlantDetailEvent, PlantDetailEffect>() {

    override fun setInitialState(): PlantDetailState {
        TODO("Not yet implemented")
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        TODO("Not yet implemented")
    }
}