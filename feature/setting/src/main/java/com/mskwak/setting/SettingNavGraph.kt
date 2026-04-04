package com.mskwak.setting

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mskwak.setting.exported_list.ExportedDiaryListNavKey
import com.mskwak.setting.exported_list.ExportedDiaryListScreen
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
            onNavigateToExportedDiaryList = { backStack.add(ExportedDiaryListNavKey) }
        )
    }

    entry<ExportedDiaryListNavKey> {
        ExportedDiaryListScreen(
            onNavigateBack = { backStack.removeLastOrNull() }
        )
    }
}
