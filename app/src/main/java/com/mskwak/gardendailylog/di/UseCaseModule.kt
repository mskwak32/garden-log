package com.mskwak.gardendailylog.di

import com.mskwak.domain.repository.*
import com.mskwak.domain.usecase.config.GetMinimumAppVersionUseCase
import com.mskwak.domain.usecase.diary.*
import com.mskwak.domain.usecase.export.DeleteExportedFileUseCase
import com.mskwak.domain.usecase.export.GenerateExportUseCase
import com.mskwak.domain.usecase.export.GetExportedFilesUseCase
import com.mskwak.domain.usecase.picture.DeletePictureUseCase
import com.mskwak.domain.usecase.picture.SavePictureUseCase
import com.mskwak.domain.usecase.plant.*
import com.mskwak.domain.usecase.watering.*
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
    fun provideAddWateringLogUseCase(
        wateringLogRepository: WateringLogRepository
    ): AddWateringLogUseCase {
        return AddWateringLogUseCase(wateringLogRepository)
    }

    @Provides
    fun provideGetWateringLogExistsUseCase(
        wateringLogRepository: WateringLogRepository
    ): GetWateringLogExistsUseCase {
        return GetWateringLogExistsUseCase(wateringLogRepository)
    }

    @Provides
    fun provideGetWateringDatesForExportUseCase(
        wateringLogRepository: WateringLogRepository
    ): GetWateringDatesForExportUseCase {
        return GetWateringDatesForExportUseCase(wateringLogRepository)
    }

    @Provides
    fun provideWateringNowUseCase(
        plantRepository: PlantRepository,
        setWateringAlarmUseCase: SetWateringAlarmUseCase,
        addWateringLogUseCase: AddWateringLogUseCase
    ): WateringNowUseCase {
        return WateringNowUseCase(plantRepository, setWateringAlarmUseCase, addWateringLogUseCase)
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

    @Provides
    fun provideGetDiaryDateRangeUseCase(
        diaryRepository: DiaryRepository
    ): GetDiaryDateRangeUseCase {
        return GetDiaryDateRangeUseCase(diaryRepository)
    }

    @Provides
    fun provideGetDiariesForExportUseCase(
        diaryRepository: DiaryRepository
    ): GetDiariesForExportUseCase {
        return GetDiariesForExportUseCase(diaryRepository)
    }

    /* Export UseCases */
    @Provides
    fun provideGenerateExportUseCase(exportRepository: ExportRepository): GenerateExportUseCase {
        return GenerateExportUseCase(exportRepository)
    }

    @Provides
    fun provideGetExportedFilesUseCase(exportRepository: ExportRepository): GetExportedFilesUseCase {
        return GetExportedFilesUseCase(exportRepository)
    }

    @Provides
    fun provideDeleteExportedFileUseCase(exportRepository: ExportRepository): DeleteExportedFileUseCase {
        return DeleteExportedFileUseCase(exportRepository)
    }
}