package com.mskwak.setting.setting

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
    data object ExportedDiaryListClick : SettingEvent
    data object FeedbackClick : SettingEvent
}

sealed interface SettingEffect : ViewEffect {
    data object OpenPlayStore : SettingEffect
    data object OpenUpdateLog : SettingEffect
    data object NavigateToExportedDiaryList : SettingEffect
    data object OpenFeedbackForm : SettingEffect
}
