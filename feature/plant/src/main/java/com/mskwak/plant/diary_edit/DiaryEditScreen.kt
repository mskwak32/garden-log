package com.mskwak.plant.diary_edit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.mskwak.design.IconPack
import com.mskwak.design.icon.AddPhotoBlack
import com.mskwak.design.icon.ArrowBackBlack
import com.mskwak.design.icon.CloseBlack
import com.mskwak.design.ui_component.LabeledClickableField
import com.mskwak.design.util.toDateString
import com.mskwak.plant.R
import com.mskwak.plant.dialog.PhotoAction
import com.mskwak.plant.dialog.SelectPhotoDialog
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId

@Serializable
data class DiaryEditScreen(val plantId: Int, val diaryId: Int? = null) : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    viewModel: DiaryEditViewModel = hiltViewModel(),
    navigate: (DiaryEditEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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

    BackHandler {
        viewModel.setEvent(DiaryEditEvent.OnBackClicked)
    }

    Scaffold(
        topBar = {
            DiaryEditTopBar(
                plantName = state.plantName,
                isSaveEnabled = state.isSaveEnabled,
                onBackClick = { viewModel.setEvent(DiaryEditEvent.OnBackClicked) },
                onSaveClick = { viewModel.setEvent(DiaryEditEvent.OnSaveClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Photos
            PhotoSection(
                imagePaths = state.imagePaths,
                onAddClick = { viewModel.setEvent(DiaryEditEvent.OnPhotoClicked) },
                onRemoveClick = { index -> viewModel.setEvent(DiaryEditEvent.OnPictureRemoved(index)) }
            )

            Spacer(Modifier.height(16.dp))

            // Date
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                LabeledClickableField(
                    label = stringResource(R.string.date),
                    value = state.date.toDateString(),
                    onClick = { viewModel.setEvent(DiaryEditEvent.OnDateClicked) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Content
            TextField(
                value = state.content,
                onValueChange = { viewModel.setEvent(DiaryEditEvent.OnContentChanged(it)) },
                placeholder = { Text(stringResource(R.string.diary_content_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }

    if (showPhotoDialog) {
        SelectPhotoDialog(
            showDeleteButton = false,
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
                    PhotoAction.DELETE -> {}
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date
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

    if (showBackConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            text = { Text(stringResource(R.string.message_diary_back_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirmDialog = false
                    viewModel.setEvent(DiaryEditEvent.OnConfirmBackClicked)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiaryEditEffect.Navigation -> navigate(effect)
                DiaryEditEffect.ShowPhotoPickerDialog -> showPhotoDialog = true
                DiaryEditEffect.ShowDatePicker -> showDatePicker = true
                DiaryEditEffect.ShowBackConfirmDialog -> showBackConfirmDialog = true
                is DiaryEditEffect.ShowSnackbar -> {
                    val message = effect.messageResId?.let { context.getString(it) } ?: effect.message
                    message?.let { snackbarHostState.showSnackbar(it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryEditTopBar(
    plantName: String,
    isSaveEnabled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = plantName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = IconPack.ArrowBackBlack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            TextButton(
                enabled = isSaveEnabled,
                onClick = onSaveClick
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
private fun PhotoSection(
    imagePaths: List<String>,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        item {
            AddPhotoButton(onClick = onAddClick)
        }
        itemsIndexed(imagePaths) { index, path ->
            PhotoItem(
                path = path,
                onRemoveClick = { onRemoveClick(index) }
            )
        }
    }
}

@Composable
private fun AddPhotoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconPack.AddPhotoBlack,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhotoItem(
    path: String,
    onRemoveClick: () -> Unit
) {
    Box(modifier = Modifier.size(80.dp)) {
        AsyncImage(
            model = path,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onRemoveClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconPack.CloseBlack,
                contentDescription = stringResource(R.string.delete),
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
