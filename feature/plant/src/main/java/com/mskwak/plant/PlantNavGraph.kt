package com.mskwak.plant

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import com.mskwak.common_ui.Screen
import com.mskwak.plant.diary_list.DiaryListScreen
import com.mskwak.plant.plant_detail.PlantDetailEffect
import com.mskwak.plant.plant_detail.PlantDetailScreen
import com.mskwak.plant.plant_edit.PlantEditScreen
import com.mskwak.plant.plant_list.PlantListEffect
import com.mskwak.plant.plant_list.PlantListScreen
import com.mskwak.plant.setting.SettingScreen

fun EntryProviderScope<Screen>.plantNavGraph(
    backStack: SnapshotStateList<Screen>
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
            navigate = { nav ->
                when (nav) {
                    is PlantDetailEffect.Navigation.Back -> {
                        backStack.removeLastOrNull()
                    }

                    is PlantDetailEffect.Navigation.ToEditPlant -> {
                        backStack.add(PlantEditScreen(it.plantId))
                    }

                    else -> { /* TODO */
                    }
                }
            }
        )
    }

    entry<PlantEditScreen> {
        PlantEditScreen(
            navigate = { backStack.removeLastOrNull() }
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