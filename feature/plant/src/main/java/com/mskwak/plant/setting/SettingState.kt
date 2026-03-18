package com.mskwak.plant.setting

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState

@Immutable
data class SettingState(
    val versionName: String = ""
) : ViewState

sealed interface SettingEvent : ViewEvent {
    data object UpdateContentClick : SettingEvent
    data object RateAppClick : SettingEvent
}

sealed interface SettingEffect : ViewEffect {
    data object ShowNotReadyDialog : SettingEffect
    data object OpenPlayStore : SettingEffect
}
