package com.mskwak.plant.plant_detail

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mskwak.design.IconPack
import com.mskwak.design.icon.ArrowBackBlack
import com.mskwak.design.icon.MoreHorizBlack
import com.mskwak.design.icon.WaterDropBlue
import com.mskwak.design.icon.WaterDropRed
import com.mskwak.design.icon.WaterDropWhite
import com.mskwak.design.icon.WaterDropWithBackground
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.design.ui_component.AppDropDownMenu
import com.mskwak.design.ui_component.Switch
import com.mskwak.design.util.clickableWithoutRipple
import com.mskwak.design.util.toDateString
import com.mskwak.design.util.toTimeString
import com.mskwak.domain.Constants
import com.mskwak.plant.R
import com.mskwak.plant.dialog.ExactAlarmPermissionDialog
import com.mskwak.plant.model.DiaryListItemUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
fun PlantDetailScreen(
    viewModel: PlantDetailViewModel = hiltViewModel(),
    navigate: (PlantDetailEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showExactAlarmPermissionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showExactAlarmPermissionDialog) {
        ExactAlarmPermissionDialog(
            onDismiss = { showExactAlarmPermissionDialog = false }
        )
    }

    if (showDeleteDialog) {
        PlantDetailDeleteDialog(
            onConfirm = {
                viewModel.setEvent(PlantDetailEvent.OnDeleteConfirmClicked)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlantDetailEffect.Navigation -> navigate(effect)
                is PlantDetailEffect.ShowExactAlarmPermissionDialog -> {
                    showExactAlarmPermissionDialog = true
                }

                is PlantDetailEffect.ShowDeleteConfirmDialog -> {
                    showDeleteDialog = true
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onEvent: (PlantDetailEvent) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = { onEvent(PlantDetailEvent.OnBackClicked) }
            ) {
                Icon(
                    imageVector = IconPack.ArrowBackBlack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = IconPack.MoreHorizBlack,
                        contentDescription = stringResource(R.string.menu),
                        modifier = Modifier.rotate(90f)
                    )
                }
                AppDropDownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showMenu = false
                            onEvent(PlantDetailEvent.OnEditPlantClicked)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            showMenu = false
                            onEvent(PlantDetailEvent.OnDeletePlantClicked)
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun Content(
    state: PlantDetailState,
    onEvent: (PlantDetailEvent) -> Unit
) {
    var wateringAnimationKey by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopBar(onEvent = onEvent) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 30.dp)
        ) {
            Header(
                imagePath = state.plantImagePath,
                plantName = state.plantName,
                createdAt = state.createdAt
            )

            Spacer(Modifier.height(16.dp))
            WateringInfo(
                dDays = state.dDays,
                hasWateringPeriod = state.hasWateringPeriod,
                wateringStatus = state.wateringStatus,
                lastWateringDate = state.lastWateringDate,
                wateringAlarm = state.wateringAlarmTime,
                isWateringActive = state.isWateringActive,
                wateringAnimationKey = wateringAnimationKey,
                onEvent = onEvent
            )

            Spacer(Modifier.height(16.dp))
            WateringBox(
                memo = state.memo,
                onEvent = { event ->
                    if (event is PlantDetailEvent.OnWateringClicked) {
                        wateringAnimationKey++
                    }
                    onEvent(event)
                }
            )

            Spacer(Modifier.height(16.dp))
            DiaryBox(
                plantName = state.plantName,
                diaryList = state.diaries,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun PlantImage(
    modifier: Modifier = Modifier,
    imagePath: String?
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(15.dp))
    ) {
        if (imagePath == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_flower_pot),
                    contentDescription = null,
                    modifier = modifier.size(130.dp)
                )
            }
        } else {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    imagePath: String?,
    plantName: String,
    createdAt: LocalDate?
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PlantImage(imagePath = imagePath)

        Spacer(Modifier.height(12.dp))
        Column {
            Text(
                text = plantName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    text = "${stringResource(R.string.plant_date)} :",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = createdAt.toDateString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WateringInfo(
    modifier: Modifier = Modifier,
    hasWateringPeriod: Boolean,
    dDays: Int,
    wateringStatus: WateringStatus,
    lastWateringDate: LocalDate?,
    wateringAlarm: LocalTime?,
    isWateringActive: Boolean,
    wateringAnimationKey: Int,
    onEvent: (PlantDetailEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(15.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        val lottieComposition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.lottie_water_loader)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(35.dp)
            ) {
                if (wateringAnimationKey > 0) {
                    key(wateringAnimationKey) {
                        LottieAnimation(
                            composition = lottieComposition,
                            iterations = 1,
                            modifier = Modifier
                                .size(35.dp)
                                .scale(1.8f)
                        )
                        Image(
                            imageVector = IconPack.WaterDropWhite,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    Image(
                        imageVector = when (wateringStatus) {
                            WateringStatus.TODAY,
                            WateringStatus.UPCOMING -> IconPack.WaterDropBlue

                            WateringStatus.TODAY_DONE -> IconPack.WaterDropWithBackground
                            WateringStatus.OVERDUE -> IconPack.WaterDropRed
                        },
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }


            Spacer(Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.watering),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = when {
                            !hasWateringPeriod -> {
                                stringResource(R.string.watering_d_day_plus_format, dDays)
                            }

                            wateringStatus == WateringStatus.OVERDUE -> {
                                stringResource(R.string.watering_d_day_plus_format, dDays)
                            }

                            else -> {
                                stringResource(R.string.watering_d_day_minus_format, dDays)
                            }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (lastWateringDate != null) {
                    val lastWateringText = run {
                        val today = LocalDate.now()
                        when (val daysBetween = ChronoUnit.DAYS.between(lastWateringDate, today)) {
                            0L -> stringResource(R.string.today)
                            in 1..Constants.LAST_WATERING_TEXT_CRITERION -> {
                                stringResource(R.string.days_ago_format, daysBetween)
                            }

                            else -> lastWateringDate.toDateString()
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${stringResource(R.string.watering_last_date)}: $lastWateringText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (wateringAlarm != null) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.watering_alarm) +
                            ": ${wateringAlarm.toTimeString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Switch(
                    checked = isWateringActive,
                    onCheckedChange = { onEvent(PlantDetailEvent.ToggleWateringAlarmActive(it)) },
                    width = 36.dp
                )
            }
        }
    }
}

@Composable
private fun WateringBox(
    modifier: Modifier = Modifier,
    memo: String?,
    onEvent: (PlantDetailEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(15.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(15.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                .clickableWithoutRipple {
                    onEvent(PlantDetailEvent.OnWateringClicked)
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = IconPack.WaterDropWhite,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiary)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.watering),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = memo ?: "${stringResource(R.string.memo)}:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        )
    }
}

@Composable
private fun DiaryBox(
    modifier: Modifier = Modifier,
    plantName: String,
    diaryList: List<DiaryListItemUiModel>,
    onEvent: (PlantDetailEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(15.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(15.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.plant_diary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = { onEvent(PlantDetailEvent.OnNewDiaryClicked) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_diary),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        if (diaryList.isEmpty()) {
            DiaryEmpty(plantName)
        } else {
            diaryList.forEach { diary ->
                PlantDetailDiaryItem(
                    diary = diary,
                    onClick = { onEvent(PlantDetailEvent.OnDiaryClicked(diary.id)) }
                )
            }
            Button(
                onClick = { onEvent(PlantDetailEvent.OnMoreDiaryClicked) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Text(
                    text = stringResource(R.string.view_more),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DiaryEmpty(
    plantName: String
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_notebook))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.diary_list_empty_with_plantName, plantName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewContent() {
    val state = PlantDetailState(
        plantImagePath = null,
        plantName = "바질",
        createdAt = LocalDate.now(),
        dDays = 1,
        hasWateringPeriod = true,
        wateringStatus = WateringStatus.UPCOMING,
        lastWateringDate = LocalDate.now().minusDays(3),
        wateringAlarmTime = LocalTime.of(9, 0),
        isWateringActive = true,
        memo = "물을 줄 때는 흙이 마른 것을 확인하고 화분 받침대에 고인 물은 버려주세요."
    )
    GardenLogTheme {
        Content(
            state = state,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDiaryBox() {
    val now = LocalDate.now()
    val diaryList = listOf(
        DiaryListItemUiModel(
            id = 1,
            date = now,
            content = "오늘은 물을 주었습니다. 새 싹이 나기 시작했어요.",
            imagePath = null
        ),
        DiaryListItemUiModel(
            id = 2,
            date = now.minusDays(4),
            content = "햇빛이 잘 드는 곳으로 옮겨주었습니다.",
            imagePath = null
        )
    )
    GardenLogTheme {
        DiaryBox(
            diaryList = diaryList,
            plantName = "식물이름",
            onEvent = {}
        )
    }
}