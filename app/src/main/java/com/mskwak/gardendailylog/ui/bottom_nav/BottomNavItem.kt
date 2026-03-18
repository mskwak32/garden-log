package com.mskwak.gardendailylog.ui.bottom_nav

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.BookMarkBlack
import com.mskwak.common_ui.icon.HomeBlack
import com.mskwak.common_ui.icon.SettingsBlack
import com.mskwak.gardendailylog.R
import com.mskwak.plant.diary_list.DiaryListNavKey
import com.mskwak.plant.plant_list.PlantListNavKey
import com.mskwak.plant.setting.SettingNavKey

/**
 * 하단 탭 메뉴 정의
 */
sealed interface BottomNavItem {
    val titleRes: Int
    val icon: ImageVector
    val screen: NavKey

    data object DiaryList : BottomNavItem {
        override val titleRes: Int = R.string.bottom_nav_diary
        override val icon: ImageVector = IconPack.BookMarkBlack
        override val screen: NavKey = DiaryListNavKey
    }

    data object PlantList : BottomNavItem {
        override val titleRes: Int = R.string.bottom_nav_home
        override val icon: ImageVector = IconPack.HomeBlack
        override val screen: NavKey = PlantListNavKey
    }

    data object Setting : BottomNavItem {
        override val titleRes: Int = R.string.bottom_nav_setting
        override val icon: ImageVector = IconPack.SettingsBlack
        override val screen: NavKey = SettingNavKey
    }

    companion object {
        val items = listOf(DiaryList, PlantList, Setting)
    }
}
