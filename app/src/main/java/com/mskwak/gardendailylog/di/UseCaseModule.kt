package com.mskwak.gardendailylog.di

import com.mskwak.domain.repository.AppConfigRepository
import com.mskwak.domain.repository.DiaryRepository
import com.mskwak.domain.repository.PictureRepository
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.WateringAlarmRepository
import com.mskwak.domain.useCase.config.GetMinimumAppVersionUseCase
import com.mskwak.domain.useCase.diary.AddDiaryUseCase
import com.mskwak.domain.useCase.diary.DeleteDiaryUseCase
import com.mskwak.domain.useCase.diary.GetDiariesByPlantIdUseCase
import com.mskwak.domain.useCase.diary.GetDiariesUseCase
import com.mskwak.domain.useCase.diary.GetDiaryUseCase
import com.mskwak.domain.useCase.diary.UpdateDiaryUseCase
import com.mskwak.domain.useCase.picture.DeletePictureUseCase
import com.mskwak.domain.useCase.picture.SavePictureUseCase
import com.mskwak.domain.useCase.plant.AddPlantUseCase
import com.mskwak.domain.useCase.plant.DeletePlantUseCase
import com.mskwak.domain.useCase.plant.GetPlantNameUseCase
import com.mskwak.domain.useCase.plant.GetPlantUseCase
import com.mskwak.domain.useCase.plant.GetPlantsWithSortOrderUseCase
import com.mskwak.domain.useCase.plant.HarvestPlantUseCase
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

    /* Watering UseCases */
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
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): UpdateWateringAlarmActivationUseCase {
        return UpdateWateringAlarmActivationUseCase(plantRepository, setWateringAlarmUseCase)
    }

    @Provides
    fun provideWateringNowUseCase(
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): WateringNowUseCase {
        return WateringNowUseCase(plantRepository, setWateringAlarmUseCase)
    }

    /* Plant UseCases */
    @Provides
    fun provideGetPlantsWithSortOrderUseCase(
        plantRepository: PlantRepository,
        getRemainWateringDateUseCase: GetRemainWateringDateUseCase
    ): GetPlantsWithSortOrderUseCase {
        return GetPlantsWithSortOrderUseCase(plantRepository, getRemainWateringDateUseCase)
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

    @Provides
    fun provideHarvestPlantUseCase(
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase
    ): HarvestPlantUseCase {
        return HarvestPlantUseCase(plantRepository, setWateringAlarmUseCase)
    }

    /* Config UseCases */
    @Provides
    fun provideGetMinimumAppVersionUseCase(
        appConfigRepository: AppConfigRepository
    ): GetMinimumAppVersionUseCase {
        return GetMinimumAppVersionUseCase(appConfigRepository)
    }

    /* Picture UseCases */
    @Provides
    fun provideSavePictureUseCase(
        pictureRepository: PictureRepository
    ): SavePictureUseCase {
        return SavePictureUseCase(pictureRepository)
    }

    @Provides
    fun provideDeletePictureUseCase(
        pictureRepository: PictureRepository
    ): DeletePictureUseCase {
        return DeletePictureUseCase(pictureRepository)
    }

    /* Diary UseCases */
    @Provides
    fun provideAddDiaryUseCase(
        diaryRepository: DiaryRepository
    ): AddDiaryUseCase {
        return AddDiaryUseCase(diaryRepository)
    }

    @Provides
    fun provideDeleteDiaryUseCase(
        diaryRepository: DiaryRepository,
        pictureRepository: PictureRepository
    ): DeleteDiaryUseCase {
        return DeleteDiaryUseCase(diaryRepository, pictureRepository)
    }

    @Provides
    fun provideGetDiariesByPlantIdUseCase(
        diaryRepository: DiaryRepository
    ): GetDiariesByPlantIdUseCase {
        return GetDiariesByPlantIdUseCase(diaryRepository)
    }

    @Provides
    fun provideGetDiaryUseCase(
        diaryRepository: DiaryRepository
    ): GetDiaryUseCase {
        return GetDiaryUseCase(diaryRepository)
    }

    @Provides
    fun provideGetDiariesUseCase(
        diaryRepository: DiaryRepository
    ): GetDiariesUseCase {
        return GetDiariesUseCase(diaryRepository)
    }

    @Provides
    fun provideUpdateDiaryUseCase(
        diaryRepository: DiaryRepository
    ): UpdateDiaryUseCase {
        return UpdateDiaryUseCase(diaryRepository)
    }
}