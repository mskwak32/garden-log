package com.mskwak.plant.plant_detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class PlantDetailNavKey(val plantId: Int) : NavKey
