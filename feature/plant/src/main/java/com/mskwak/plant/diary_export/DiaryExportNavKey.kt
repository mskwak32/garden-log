package com.mskwak.plant.diary_export

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DiaryExportNavKey(val plantId: Int) : NavKey
