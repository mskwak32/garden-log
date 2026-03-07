package com.mskwak.plant

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mskwak.plant.diary_edit.DiaryEditEffect
import com.mskwak.plant.diary_edit.DiaryEditScreen
import com.mskwak.plant.diary_list.DiaryListScreen
import com.mskwak.plant.plant_detail.PlantDetailEffect
import com.mskwak.plant.plant_detail.PlantDetailScreen
import com.mskwak.plant.plant_edit.PlantEditScreen
import com.mskwak.plant.plant_list.PlantListEffect
import com.mskwak.plant.plant_list.PlantListScreen
import com.mskwak.plant.setting.SettingScreen

fun EntryProviderScope<NavKey>.plantNavGraph(
    backStack: NavBackStack<NavKey>
) {
    entry<PlantListScreen> {
        PlantListScreen(
            navigate = { nav ->
                when (nav) {
                    is PlantListEffect.Navigation.ToAddPlant -> {
                        backStack.add(PlantEditScreen())
                    }

                    is PlantListEffect.Navigation.ToPlantDetail -> {
                        backStack.add(PlantDetailScreen(nav.plantId))
                    }
                }
            }
        )
    }

    entry<PlantDetailScreen> {
        PlantDetailScreen(
            plantId = it.plantId,
            navigate = { nav ->
                when (nav) {
                    is PlantDetailEffect.Navigation.Back -> {
                        backStack.removeLastOrNull()
                    }

                    is PlantDetailEffect.Navigation.ToEditPlant -> {
                        backStack.add(PlantEditScreen(it.plantId))
                    }

                    is PlantDetailEffect.Navigation.ToNewDiary -> {
                        backStack.add(DiaryEditScreen(it.plantId))
                    }

                    is PlantDetailEffect.Navigation.ToDiaryDetail -> {
                        // TODO: Navigate to DiaryDetailScreen
                    }

                    is PlantDetailEffect.Navigation.ToMoreDiaries -> {
                        backStack.add(DiaryListScreen)
                    }
                }
            }
        )
    }

    entry<PlantEditScreen> {
        PlantEditScreen(
            plantId = it.plantId,
            navigate = { backStack.removeLastOrNull() }
        )
    }

    entry<DiaryEditScreen> {
        DiaryEditScreen(
            plantId = it.plantId,
            diaryId = it.diaryId,
            navigate = { nav ->
                when (nav) {
                    is DiaryEditEffect.Navigation.Back,
                    is DiaryEditEffect.Navigation.SaveComplete -> {
                        backStack.removeLastOrNull()
                    }
                }
            }
        )
    }

    entry<DiaryListScreen> {
        DiaryListScreen(
            navigate = { /* TODO */ }
        )
    }

    entry<SettingScreen> {
        SettingScreen()
    }
}