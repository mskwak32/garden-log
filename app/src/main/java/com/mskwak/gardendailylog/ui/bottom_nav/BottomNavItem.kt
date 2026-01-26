package com.mskwak.gardendailylog.ui.bottom_nav

import androidx.compose.ui.graphics.vector.ImageVector
import com.mskwak.common_ui.Screen
import com.mskwak.design.IconPack
import com.mskwak.design.icon.BookMarkBlack
import com.mskwak.design.icon.HomeBlack
import com.mskwak.design.icon.SettingsBlack
import com.mskwak.gardendailylog.R
import com.mskwak.plant.diary_list.DiaryListScreen
import com.mskwak.plant.plant_list.PlantListScreen
import com.mskwak.plant.setting.SettingScreen

/**
 * 하단 탭 메뉴 정의
 */
sealed interface BottomNavItem {
    val titleRes: Int
    val icon: ImageVector
    val screen: Screen

    data object DiaryList : BottomNavItem {
        override val titleRes: Int = R.string.bottom_nav_diary
        override val icon: ImageVector = IconPack.BookMarkBlack
        override val screen: Screen = DiaryListScreen
    }

    data object PlantList : BottomNavItem {
        override val titleRes: Int = R.string.bottom_nav_home
        override val icon: ImageVector = IconPack.HomeBlack
        override val screen: Screen = PlantListScreen
    }

    data object Setting : BottomNavItem {
        override val titleRes: Int = R.string.bottom_nav_setting
        override val icon: ImageVector = IconPack.SettingsBlack
        override val screen: Screen = SettingScreen
    }

    companion object {
        val items = listOf(DiaryList, PlantList, Setting)
    }
}
