package com.mskwak.plant.plant_edit

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.AddPhotoBlack
import com.mskwak.common_ui.icon.ArrowBackBlack
import com.mskwak.common_ui.icon.ArrowForwardIosBlack
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.LabeledClickableField
import com.mskwak.common_ui.ui_component.Switch
import com.mskwak.common_ui.util.clickableWithoutRipple
import com.mskwak.common_ui.util.toDateString
import com.mskwak.common_ui.util.toTimeString
import com.mskwak.plant.R
import com.mskwak.plant.dialog.ExactAlarmPermissionDialog
import com.mskwak.plant.dialog.PhotoAction
import com.mskwak.plant.dialog.SelectPhotoDialog
import com.mskwak.plant.dialog.WateringPeriodDialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantEditScreen(
    viewModel: PlantEditViewModel = hiltViewModel(),
    navigate: (PlantEditEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showCreatedDatePicker by remember { mutableStateOf(false) }
    var showLastWateringDatePicker by remember { mutableStateOf(false) }
    var showWateringPeriodDialog by remember { mutableStateOf(false) }
    var showAlarmTimePicker by remember { mutableStateOf(false) }
    var showExactAlarmPermissionDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
        snackbarHostState = snackbarHostState,
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

    if (showCreatedDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.createdDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showCreatedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.setEvent(PlantEditEvent.OnCreatedDateChanged(date))
                    }
                    showCreatedDatePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatedDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLastWateringDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.lastWateringDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showLastWateringDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.setEvent(PlantEditEvent.OnLastWateringDateChanged(date))
                    }
                    showLastWateringDatePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLastWateringDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showWateringPeriodDialog) {
        WateringPeriodDialog(
            initialPeriod = state.wateringPeriod,
            onDismiss = { showWateringPeriodDialog = false },
            onConfirm = { period ->
                viewModel.setEvent(PlantEditEvent.OnWateringPeriodChanged(period))
                showWateringPeriodDialog = false
            }
        )
    }

    if (showExactAlarmPermissionDialog) {
        ExactAlarmPermissionDialog(
            onDismiss = { showExactAlarmPermissionDialog = false }
        )
    }

    if (showAlarmTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.wateringAlarmTime.hour,
            initialMinute = state.wateringAlarmTime.minute,
            is24Hour = false
        )
        TimePickerDialog(
            onDismissRequest = { showAlarmTimePicker = false },
            title = { Text(stringResource(R.string.watering_alarm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setEvent(
                        PlantEditEvent.OnWateringAlarmTimeChanged(
                            LocalTime.of(timePickerState.hour, timePickerState.minute)
                        )
                    )
                    showAlarmTimePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlarmTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlantEditEffect.Navigation -> navigate(effect)
                is PlantEditEffect.ShowPhotoPickerDialog -> showPhotoDialog = true
                is PlantEditEffect.ShowCreatedDatePicker -> showCreatedDatePicker = true
                is PlantEditEffect.ShowLastWateringDatePicker -> showLastWateringDatePicker = true
                is PlantEditEffect.ShowWateringPeriodDialog -> showWateringPeriodDialog = true
                is PlantEditEffect.ShowWateringAlarmTimePicker -> showAlarmTimePicker = true
                is PlantEditEffect.ShowExactAlarmPermissionDialog -> showExactAlarmPermissionDialog =
                    true

                is PlantEditEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }
            }
        }
    }
}

@Composable
private fun Content(
    state: PlantEditState,
    onEvent: (PlantEditEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopBar(state, onEvent)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                minLines = 2,
                maxLines = 4,
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
                enabled = state.isSaveEnabled,
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