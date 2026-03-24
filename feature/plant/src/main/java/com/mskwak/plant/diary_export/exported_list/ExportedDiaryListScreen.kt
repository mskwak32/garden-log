package com.mskwak.plant.diary_export.exported_list

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.ArrowBackBlack
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.domain.model.ExportedFileInfo
import com.mskwak.plant.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportedDiaryListScreen(
    viewModel: ExportedDiaryListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var deleteTargetUri by remember { mutableStateOf<Uri?>(null) }

    deleteTargetUri?.let { uri ->
        ExportedDiaryDeleteDialog(
            uri = uri,
            onConfirm = { confirmedUri ->
                viewModel.setEvent(ExportedDiaryListEvent.OnDeleteConfirmed(confirmedUri))
                deleteTargetUri = null
            },
            onDismiss = { deleteTargetUri = null }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExportedDiaryListEffect.NavigateBack -> onNavigateBack()

                is ExportedDiaryListEffect.ShareFile -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                }

                is ExportedDiaryListEffect.ShowDeleteConfirmDialog -> {
                    deleteTargetUri = effect.uri
                }
            }
        }
    }

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: ExportedDiaryListState,
    onEvent: (ExportedDiaryListEvent) -> Unit
) {
    Scaffold(
        topBar = { TopBar(onEvent = onEvent) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.files.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.exported_diary_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn {
                        items(state.files, key = { it.contentUri }) { file ->
                            FileItem(
                                file = file,
                                onShare = { onEvent(ExportedDiaryListEvent.OnShareClicked(file.contentUri.toUri())) },
                                onDelete = { onEvent(ExportedDiaryListEvent.OnDeleteClicked(file.contentUri.toUri())) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileItem(
    file: ExportedFileInfo,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(file.createdAt) {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(file.createdAt))
    }
    val sizeStr = remember(file.fileSize) {
        if (file.fileSize >= 1024 * 1024) {
            "%.1f MB".format(file.fileSize / (1024f * 1024f))
        } else {
            "${file.fileSize / 1024} KB"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$dateStr · $sizeStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onShare) {
            Text(stringResource(R.string.exported_diary_share))
        }
        TextButton(onClick = onDelete) {
            Text(
                text = stringResource(R.string.delete),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onEvent: (ExportedDiaryListEvent) -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.exported_diary_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(ExportedDiaryListEvent.OnBackClicked) }) {
                Icon(
                    imageVector = IconPack.ArrowBackBlack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ExportedDiaryListScreenPreview() {
    GardenLogTheme {
        Content(
            state = ExportedDiaryListState(isLoading = false, files = emptyList()),
            onEvent = {}
        )
    }
}
