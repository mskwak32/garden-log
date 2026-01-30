package com.mskwak.plant.plant_edit

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mskwak.common_ui.Screen
import com.mskwak.design.IconPack
import com.mskwak.design.icon.AddPhotoBlack
import com.mskwak.design.icon.ArrowBackBlack
import com.mskwak.design.icon.ArrowForwardIosBlack
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.design.ui_component.Switch
import com.mskwak.design.util.clickableWithoutRipple
import com.mskwak.design.util.toDateString
import com.mskwak.design.util.toTimeString
import com.mskwak.plant.R
import com.mskwak.plant.dialog.PhotoAction
import com.mskwak.plant.dialog.SelectPhotoDialog
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class PlantEditScreen(val plantId: Int? = null) : Screen

@Composable
fun PlantEditScreen(
    viewModel: PlantEditViewModel = hiltViewModel(),
    navigate: (PlantEditEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showPhotoDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.setEvent(PlantEditEvent.OnPictureChanged(it)) }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.setEvent(PlantEditEvent.OnPictureChanged(it)) }
        }
    }

    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createCameraUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                R.string.message_photo_camera_permission,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Content(
        state = state,
        onEvent = viewModel::setEvent,
    )

    if (showPhotoDialog) {
        SelectPhotoDialog(
            showDeleteButton = state.plantImagePath != null,
            onDismiss = { showPhotoDialog = false },
            onAction = { action ->
                showPhotoDialog = false
                when (action) {
                    PhotoAction.GALLERY -> {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }

                    PhotoAction.CAMERA -> {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val uri = viewModel.createCameraUri()
                            cameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    PhotoAction.DELETE -> {
                        viewModel.setEvent(PlantEditEvent.OnPictureRemoved)
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlantEditEffect.Navigation -> navigate(effect)
                is PlantEditEffect.ShowPhotoPickerDialog -> showPhotoDialog = true
                else -> {}
            }
        }
    }
}

@Composable
private fun Content(
    state: PlantEditState,
    onEvent: (PlantEditEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(state, onEvent)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 30.dp)
        ) {
            // 사진
            PlantPhotoSection(
                imagePath = state.plantImagePath,
                onClick = { onEvent(PlantEditEvent.OnPhotoClicked) }
            )

            Spacer(Modifier.height(20.dp))
            // 식물 이름
            OutlinedTextField(
                value = state.plantName,
                onValueChange = { onEvent(PlantEditEvent.OnNameChanged(it)) },
                label = { Text(stringResource(R.string.plant_name)) },
                isError = state.isNameError,
                supportingText = if (state.isNameError) {
                    { Text(stringResource(R.string.message_input_required_field)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))
            // 심은 날짜
            LabeledClickableField(
                label = stringResource(R.string.plant_date),
                value = state.createdDate.toDateString(),
                onClick = { onEvent(PlantEditEvent.OnCreatedDateClicked) }
            )

            Spacer(Modifier.height(12.dp))
            // 메모
            OutlinedTextField(
                value = state.memo,
                onValueChange = { onEvent(PlantEditEvent.OnMemoChanged(it)) },
                label = { Text(stringResource(R.string.memo)) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))
            // 물주기 섹션
            WateringSection(
                lastWateringDate = state.lastWateringDate,
                wateringPeriod = state.wateringPeriod,
                wateringAlarmTime = state.wateringAlarmTime,
                isWateringAlarmActive = state.isWateringAlarmActive,
                hasWateringPeriod = state.wateringPeriod > 0,
                onEvent = onEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    state: PlantEditState,
    onEvent: (PlantEditEvent) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (state.isEditMode) stringResource(R.string.edit)
                else stringResource(R.string.new_plant),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(PlantEditEvent.OnBackClicked) }) {
                Icon(
                    imageVector = IconPack.ArrowBackBlack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            TextButton(
                onClick = { onEvent(PlantEditEvent.OnSaveClicked) }
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun PlantPhotoSection(
    imagePath: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
    ) {
        if (imagePath == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = IconPack.AddPhotoBlack,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.photo_image),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LabeledClickableField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = IconPack.ArrowForwardIosBlack,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WateringSection(
    lastWateringDate: LocalDate,
    wateringPeriod: Int,
    wateringAlarmTime: LocalTime,
    isWateringAlarmActive: Boolean,
    hasWateringPeriod: Boolean,
    onEvent: (PlantEditEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                RoundedCornerShape(15.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(15.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.watering),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(16.dp))

        // 마지막 물준 날짜
        FieldRow(
            label = stringResource(R.string.watering_last_date),
            value = lastWateringDate.toDateString(),
            onClick = { onEvent(PlantEditEvent.OnLastWateringDateClicked) }
        )

        Spacer(Modifier.height(12.dp))

        // 물주기 간격
        FieldRow(
            label = stringResource(R.string.watering_period),
            value = if (wateringPeriod == 0) stringResource(R.string.none)
            else stringResource(R.string.watering_period_unit, wateringPeriod),
            onClick = { onEvent(PlantEditEvent.OnWateringPeriodClicked) }
        )

        if (hasWateringPeriod) {
            Spacer(Modifier.height(12.dp))

            // 물주기 알림 시간
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithoutRipple {
                        onEvent(PlantEditEvent.OnWateringAlarmTimeClicked)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.watering_alarm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = wateringAlarmTime.toTimeString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isWateringAlarmActive,
                    onCheckedChange = {
                        onEvent(PlantEditEvent.OnWateringAlarmToggled(it))
                    },
                    width = 36.dp
                )
            }
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = IconPack.ArrowForwardIosBlack,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Light - Add", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark - Add", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAdd() {
    GardenLogTheme {
        Content(
            state = PlantEditState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Light - Edit", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark - Edit", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEdit() {
    GardenLogTheme {
        Content(
            state = PlantEditState(
                isEditMode = true,
                plantName = "바질",
                createdDate = LocalDate.of(2025, 3, 15),
                memo = "물을 줄 때는 흙이 마른 것을 확인하고 화분 받침대에 고인 물은 버려주세요.",
                lastWateringDate = LocalDate.of(2025, 6, 10),
                wateringPeriod = 3,
                wateringAlarmTime = LocalTime.of(9, 0),
                isWateringAlarmActive = true
            ),
            onEvent = {},
        )
    }
}