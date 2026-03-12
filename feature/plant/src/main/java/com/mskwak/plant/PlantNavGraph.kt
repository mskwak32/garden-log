package com.mskwak.plant

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mskwak.plant.diary_detail.DiaryDetailEffect
import com.mskwak.plant.diary_detail.DiaryDetailNavKey
import com.mskwak.plant.diary_detail.DiaryDetailScreen
import com.mskwak.plant.diary_detail.DiaryDetailViewModel
import com.mskwak.plant.diary_edit.DiaryEditEffect
import com.mskwak.plant.diary_edit.DiaryEditNavKey
import com.mskwak.plant.diary_edit.DiaryEditScreen
import com.mskwak.plant.diary_edit.DiaryEditViewModel
import com.mskwak.plant.diary_list.DiaryListEffect
import com.mskwak.plant.diary_list.DiaryListNavKey
import com.mskwak.plant.diary_list.DiaryListScreen
import com.mskwak.plant.plant_detail.PlantDetailEffect
import com.mskwak.plant.plant_detail.PlantDetailNavKey
import com.mskwak.plant.plant_detail.PlantDetailScreen
import com.mskwak.plant.plant_detail.PlantDetailViewModel
import com.mskwak.plant.plant_edit.PlantEditNavKey
import com.mskwak.plant.plant_edit.PlantEditScreen
import com.mskwak.plant.plant_edit.PlantEditViewModel
import com.mskwak.plant.plant_list.PlantListEffect
import com.mskwak.plant.plant_list.PlantListNavKey
import com.mskwak.plant.plant_list.PlantListScreen
import com.mskwak.plant.setting.SettingNavKey
import com.mskwak.plant.setting.SettingScreen

fun EntryProviderScope<NavKey>.plantNavGraph(
    backStack: NavBackStack<NavKey>
) {
    entry<PlantListNavKey> {
        PlantListScreen(
            navigate = { nav ->
                when (nav) {
                    is PlantListEffect.Navigation.ToAddPlant -> {
                        backStack.add(PlantEditNavKey())
                    }
                    is PlantListEffect.Navigation.ToPlantDetail -> {
                        backStack.add(PlantDetailNavKey(nav.plantId))
                    }
                }
            }
        )
    }

    entry<PlantDetailNavKey> {
        val viewModel = hiltViewModel<PlantDetailViewModel, PlantDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(it) }
        )
        PlantDetailScreen(
            viewModel = viewModel,
            navigate = { nav ->
                when (nav) {
                    is PlantDetailEffect.Navigation.Back -> {
                        backStack.removeLastOrNull()
                    }
                    is PlantDetailEffect.Navigation.ToEditPlant -> {
                        backStack.add(PlantEditNavKey(it.plantId))
                    }
                    is PlantDetailEffect.Navigation.ToNewDiary -> {
                        backStack.add(DiaryEditNavKey(it.plantId))
                    }
                    is PlantDetailEffect.Navigation.ToDiaryDetail -> {
                        backStack.add(DiaryDetailNavKey(nav.diaryId))
                    }
                    else -> { /* TODO */ }
                }
            }
        )
    }

    entry<PlantEditNavKey> {
        val viewModel = hiltViewModel<PlantEditViewModel, PlantEditViewModel.Factory>(
            creationCallback = { factory -> factory.create(it) }
        )
        PlantEditScreen(
            viewModel = viewModel,
            navigate = { backStack.removeLastOrNull() }
        )
    }

    entry<DiaryEditNavKey> {
        val viewModel = hiltViewModel<DiaryEditViewModel, DiaryEditViewModel.Factory>(
            creationCallback = { factory -> factory.create(it) }
        )
        DiaryEditScreen(
            viewModel = viewModel,
            navigate = { backStack.removeLastOrNull() }
        )
    }

    entry<DiaryListNavKey> {
        DiaryListScreen(
            navigate = { nav ->
                when (nav) {
                    is DiaryListEffect.Navigation.GoToDiaryDetail -> {
                        backStack.add(DiaryDetailNavKey(nav.diaryId))
                    }
                }
            }
        )
    }

    entry<DiaryDetailNavKey> {
        val viewModel = hiltViewModel<DiaryDetailViewModel, DiaryDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(it) }
        )
        DiaryDetailScreen(
            viewModel = viewModel,
            navigate = { nav ->
                when (nav) {
                    is DiaryDetailEffect.Navigation.Back -> {
                        backStack.removeLastOrNull()
                    }
                    is DiaryDetailEffect.Navigation.GoToEdit -> {
                        backStack.add(DiaryEditNavKey(nav.plantId, nav.diaryId))
                    }
                }
            }
        )
    }

    entry<SettingNavKey> {
        SettingScreen()
    }
}
