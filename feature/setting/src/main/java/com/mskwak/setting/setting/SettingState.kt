package com.mskwak.setting.setting

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState

@Immutable
data class SettingState(
    val versionName: String = "",
    val isFeedbackVisible: Boolean = false
) : ViewState

sealed interface SettingEvent : ViewEvent {
    data object RateAppClick : SettingEvent
    data object ExportedDiaryListClick : SettingEvent
    data object FeedbackClick : SettingEvent
    data object PrivacyPolicyClick : SettingEvent
}

sealed interface SettingEffect : ViewEffect {
    sealed interface Navigation : SettingEffect {
        data object ToExportedDiaryList : Navigation
        data object ToPrivacyPolicy : Navigation
    }

    data object OpenPlayStore : SettingEffect
    data object OpenFeedbackForm : SettingEffect
}
