package com.mskwak.plant.diary_detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DiaryDetailNavKey(val diaryId: Int) : NavKey
