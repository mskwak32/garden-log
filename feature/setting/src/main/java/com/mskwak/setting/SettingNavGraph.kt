package com.mskwak.setting

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mskwak.setting.exported_list.ExportedDiaryListNavKey
import com.mskwak.setting.exported_list.ExportedDiaryListScreen
import com.mskwak.setting.privacy.PrivacyPolicyNavKey
import com.mskwak.setting.privacy.PrivacyPolicyScreen
import com.mskwak.setting.setting.SettingEffect
import com.mskwak.setting.setting.SettingNavKey
import com.mskwak.setting.setting.SettingScreen
import com.mskwak.setting.setting.SettingViewModel

fun EntryProviderScope<NavKey>.settingNavGraph(
    backStack: NavBackStack<NavKey>
) {
    entry<SettingNavKey> {
        val viewModel = hiltViewModel<SettingViewModel>()
        SettingScreen(
            viewModel = viewModel,
            navigate = { nav ->
                when (nav) {
                    is SettingEffect.Navigation.ToExportedDiaryList -> {
                        backStack.add(ExportedDiaryListNavKey)
                    }

                    is SettingEffect.Navigation.ToPrivacyPolicy -> {
                        backStack.add(PrivacyPolicyNavKey)
                    }
                }
            }
        )
    }

    entry<ExportedDiaryListNavKey> {
        ExportedDiaryListScreen(
            onNavigateBack = { backStack.removeLastOrNull() }
        )
    }

    entry<PrivacyPolicyNavKey> {
        PrivacyPolicyScreen(
            onNavigateBack = { backStack.removeLastOrNull() }
        )
    }
}
