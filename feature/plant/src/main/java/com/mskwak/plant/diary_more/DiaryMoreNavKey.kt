package com.mskwak.plant.diary_more

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DiaryMoreNavKey(val plantId: Int) : NavKey
