package com.mskwak.gardendailylog.di

import com.mskwak.domain.repository.DiaryRepository
import com.mskwak.domain.repository.PictureRepository
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.WateringAlarmRepository
import com.mskwak.domain.useCase.plant.AddPlantUseCase
import com.mskwak.domain.useCase.plant.DeletePlantUseCase
import com.mskwak.domain.useCase.plant.GetPlantNameUseCase
import com.mskwak.domain.useCase.plant.GetPlantUseCase
import com.mskwak.domain.useCase.plant.GetPlantWithSortOrderUseCase
import com.mskwak.domain.useCase.plant.UpdatePlantUseCase
import com.mskwak.domain.useCase.watering.GetRemainWateringDateUseCase
import com.mskwak.domain.useCase.watering.GetWateringDaysUseCase
import com.mskwak.domain.useCase.watering.SetWateringAlarmUseCase
import com.mskwak.domain.useCase.watering.UpdateWateringAlarmActivationUseCase
import com.mskwak.domain.useCase.watering.WateringNowUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    // Watering UseCases
    @Provides
    fun provideGetRemainWateringDateUseCase(): GetRemainWateringDateUseCase {
        return GetRemainWateringDateUseCase()
    }

    @Provides
    fun provideSetWateringAlarmUseCase(
        plantRepository: PlantRepository,
        wateringAlarmRepository: WateringAlarmRepository
    ): SetWateringAlarmUseCase {
        return SetWateringAlarmUseCase(
            plantRepository,
            wateringAlarmRepository
        )
    }

    @Provides
    fun provideGetWateringDaysUseCase(
        getRemainWateringDateUseCase: GetRemainWateringDateUseCase
    ): GetWateringDaysUseCase {
        return GetWateringDaysUseCase(getRemainWateringDateUseCase)
    }

    @Provides
    fun provideUpdateWateringAlarmActivationUseCase(
        plantRepository: PlantRepository
    ): UpdateWateringAlarmActivationUseCase {
        return UpdateWateringAlarmActivationUseCase(plantRepository)
    }

    @Provides
    fun provideWateringNowUseCase(
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): WateringNowUseCase {
        return WateringNowUseCase(plantRepository, setWateringAlarmUseCase)
    }

    // Plant UseCases
    @Provides
    fun provideGetPlantWithSortOrderUseCase(
        plantRepository: PlantRepository,
        getRemainWateringDateUseCase: GetRemainWateringDateUseCase
    ): GetPlantWithSortOrderUseCase {
        return GetPlantWithSortOrderUseCase(plantRepository, getRemainWateringDateUseCase)
    }

    @Provides
    fun provideAddPlantUseCase(
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): AddPlantUseCase {
        return AddPlantUseCase(plantRepository, setWateringAlarmUseCase)
    }

    @Provides
    fun provideDeletePlantUseCase(
        plantRepository: PlantRepository,
        diaryRepository: DiaryRepository,
        pictureRepository: PictureRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): DeletePlantUseCase {
        return DeletePlantUseCase(
            plantRepository,
            diaryRepository,
            pictureRepository,
            setWateringAlarmUseCase
        )
    }

    @Provides
    fun provideGetPlantUseCase(plantRepository: PlantRepository): GetPlantUseCase {
        return GetPlantUseCase(plantRepository)
    }

    @Provides
    fun provideUpdatePlantUseCase(
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): UpdatePlantUseCase {
        return UpdatePlantUseCase(plantRepository, setWateringAlarmUseCase)
    }

    @Provides
    fun provideGetPlantNameUseCase(plantRepository: PlantRepository): GetPlantNameUseCase {
        return GetPlantNameUseCase(plantRepository)
    }
}