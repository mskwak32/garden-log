package com.mskwak.gardendailylog.ui.bottom_nav

import androidx.compose.ui.graphics.vector.ImageVector
import com.mskwak.design.IconPack
import com.mskwak.design.icon.BookMarkBlack
import com.mskwak.design.icon.HomeBlack
import com.mskwak.design.icon.SettingsBlack
import com.mskwak.gardendailylog.R

/**
 * 하단 탭 메뉴 정의
 */
sealed class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    object DiaryList : BottomNavItem("DiaryList", R.string.bottom_nav_diary, IconPack.BookMarkBlack)
    object PlantList : BottomNavItem("PlantList", R.string.bottom_nav_home, IconPack.HomeBlack)
    object Setting : BottomNavItem("Setting", R.string.bottom_nav_setting, IconPack.SettingsBlack)
}