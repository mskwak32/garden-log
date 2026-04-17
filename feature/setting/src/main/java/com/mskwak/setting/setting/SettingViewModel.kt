package com.mskwak.setting.setting

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.usecase.config.IsFeedbackVisibleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val analyticsLogger: AnalyticsLogger,
    private val isFeedbackVisibleUseCase: IsFeedbackVisibleUseCase
) : BaseViewModel<SettingState, SettingEvent, SettingEffect>() {

    init {
        analyticsLogger.log(GardenEvent.ScreenView("setting"))
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }.getOrDefault("")
        setState { copy(versionName = versionName) }
        viewModelScope.launch {
            val isVisible = isFeedbackVisibleUseCase()
            setState { copy(isFeedbackVisible = isVisible) }
        }
    }

    override fun setInitialState(): SettingState = SettingState()

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? SettingEvent ?: return
        when (event) {
            SettingEvent.RateAppClick -> setEffect(SettingEffect.OpenPlayStore)
            SettingEvent.ExportedDiaryListClick -> setEffect(SettingEffect.NavigateToExportedDiaryList)
            SettingEvent.FeedbackClick -> setEffect(SettingEffect.OpenFeedbackForm)
        }
    }
}
