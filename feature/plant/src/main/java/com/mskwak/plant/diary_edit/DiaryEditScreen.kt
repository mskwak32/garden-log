package com.mskwak.plant.diary_edit

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mskwak.design.IconPack
import com.mskwak.design.icon.AddPhotoBlack
import com.mskwak.design.icon.CloseBlack
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.design.ui_component.LabeledClickableField
import com.mskwak.design.util.toDateString
import com.mskwak.plant.R
import com.mskwak.plant.dialog.PhotoAction
import com.mskwak.plant.dialog.SelectPhotoDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    viewModel: DiaryEditViewModel = hiltViewModel(),
    navigate: (DiaryEditEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.setEvent(DiaryEditEvent.OnPictureAdded(it)) }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.setEvent(DiaryEditEvent.OnPictureAdded(it)) }
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

                    PhotoAction.DELETE -> { /* 삭제 버튼 없음 */ }
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.diaryDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.setEvent(DiaryEditEvent.OnDateChanged(date))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            text = { Text(stringResource(R.string.message_discard_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.confirmDiscard()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiaryEditEffect.Navigation -> navigate(effect)
                is DiaryEditEffect.ShowPhotoPickerDialog -> showPhotoDialog = true
                is DiaryEditEffect.ShowDatePicker -> showDatePicker = true
                is DiaryEditEffect.ShowDiscardConfirmDialog -> showDiscardDialog = true
                is DiaryEditEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }
            }
        }
    }
}

@Composable
private fun Content(
    state: DiaryEditState,
    onEvent: (DiaryEditEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = { TopBar(state, onEvent) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 30.dp)
        ) {
            // 사진 섹션
            PhotoSection(
                picturePaths = state.picturePaths,
                onAddPhoto = { onEvent(DiaryEditEvent.OnAddPhotoClicked) },
                onRemovePhoto = { index -> onEvent(DiaryEditEvent.OnPictureRemoved(index)) }
            )

            // 날짜
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                LabeledClickableField(
                    label = stringResource(R.string.date),
                    value = state.diaryDate.toDateString(),
                    onClick = { onEvent(DiaryEditEvent.OnDateClicked) },
                )
            }

            // 일기 입력
            OutlinedTextField(
                value = state.memo,
                onValueChange = { onEvent(DiaryEditEvent.OnMemoChanged(it)) },
                placeholder = { Text(stringResource(R.string.diary_content_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    state: DiaryEditState,
    onEvent: (DiaryEditEvent) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = state.plantName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(DiaryEditEvent.OnBackClicked) }) {
                Icon(
                    imageVector = IconPack.CloseBlack,
                    contentDescription = stringResource(R.string.close)
                )
            }
        },
        actions = {
            TextButton(
                enabled = state.isSaveEnabled,
                onClick = { onEvent(DiaryEditEvent.OnSaveClicked) }
            ) {
                Text(
                    text = if (state.isEditMode) stringResource(R.string.edit)
                    else stringResource(R.string.save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun PhotoSection(
    picturePaths: List<String>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 사진 추가 버튼
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(onClick = onAddPhoto),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconPack.AddPhotoBlack,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 추가된 사진 목록 (가로 스크롤)
        LazyRow(
            contentPadding = PaddingValues(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(picturePaths) { index, path ->
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = path,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    // X 삭제 버튼
                    IconButton(
                        onClick = { onRemovePhoto(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                    ) {
                        Icon(
                            imageVector = IconPack.CloseBlack,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    CircleShape
                                )
                                .padding(2.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light - New", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark - New", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNew() {
    GardenLogTheme {
        Content(
            state = DiaryEditState(
                plantName = "바질",
                diaryDate = LocalDate.of(2025, 6, 10),
            ),
            onEvent = {},
        )
    }
}
