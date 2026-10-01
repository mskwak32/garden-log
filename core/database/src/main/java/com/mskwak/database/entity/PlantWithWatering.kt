package com.mskwak.database.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.time.LocalDate

// 조회 전용 관계 모델. DAO의 트랜잭션으로 식물·사진·이력을 일관되게 조회
data class PlantWithWatering(
    @Embedded val plant: PlantEntity,
    @Relation(parentColumn = "pictureId", entityColumn = "id")
    val picture: PictureEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "plantId",
        entity = WateringLogEntity::class,
        projection = ["date"]
    )
    val wateringDates: List<LocalDate>
)
