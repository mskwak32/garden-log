package com.mskwak.plant.setting

import android.content.Context
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<SettingState, SettingEvent, SettingEffect>() {

    init {
        analyticsLogger.log(GardenEvent.ScreenView("setting"))
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }.getOrDefault("")
        setState { copy(versionName = versionName) }
    }

    override fun setInitialState(): SettingState = SettingState()

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? SettingEvent ?: return
        when (event) {
            SettingEvent.UpdateContentClick -> setEffect(SettingEffect.OpenUpdateLog)
            SettingEvent.RateAppClick -> setEffect(SettingEffect.OpenPlayStore)
        }
    }
}
