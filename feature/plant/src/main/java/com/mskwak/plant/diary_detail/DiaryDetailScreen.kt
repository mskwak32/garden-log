package com.mskwak.plant.diary_detail

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.ArrowBackBlack
import com.mskwak.common_ui.icon.MoreHorizBlack
import com.mskwak.common_ui.icon.WaterDropBlue
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.AppDropDownMenu
import com.mskwak.common_ui.ui_component.PagerDotIndicator
import com.mskwak.common_ui.ui_component.TextBadge
import com.mskwak.common_ui.util.toDateString
import com.mskwak.plant.R
import com.mskwak.plant.dialog.ImageZoomDialog
import java.time.LocalDate

@Composable
fun DiaryDetailScreen(
    viewModel: DiaryDetailViewModel = hiltViewModel(),
    navigate: (DiaryDetailEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.setEvent(DiaryDetailEvent.DeleteConfirm)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiaryDetailEffect.Navigation -> navigate(effect)
                is DiaryDetailEffect.ShowDeleteConfirmDialog -> showDeleteDialog = true
            }
        }
    }
}

@Composable
private fun Content(
    state: DiaryDetailState,
    onEvent: (DiaryDetailEvent) -> Unit
) {
    var zoomImagePath by remember { mutableStateOf<String?>(null) }

    zoomImagePath?.let {
        ImageZoomDialog(imagePath = it, onDismiss = { zoomImagePath = null })
    }

    Scaffold(
        topBar = { TopBar(state, onEvent) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp)
        ) {
            // 사진 섹션
            if (state.picturePaths.isNotEmpty()) {
                PhotoPagerSection(
                    picturePaths = state.picturePaths,
                    onImageClick = { zoomImagePath = it }
                )
                Spacer(Modifier.height(16.dp))
            }

            // 날짜
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = state.diaryDate.toDateString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.isWatered) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = IconPack.WaterDropBlue,
                        contentDescription = stringResource(R.string.watering_icon_description),
                        modifier = Modifier.size(20.dp),
                        tint = androidx.compose.ui.graphics.Color.Unspecified
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 일기 내용
            if (state.memo.isNotBlank()) {
                Text(
                    text = state.memo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.diary_content_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PhotoPagerSection(
    picturePaths: List<String>,
    onImageClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { picturePaths.size })
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
        ) { page ->
            AsyncImage(
                model = picturePaths[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onImageClick(picturePaths[page]) }
            )
        }
        if (picturePaths.size > 1) {
            Spacer(Modifier.height(8.dp))
            PagerDotIndicator(pagerState = pagerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    state: DiaryDetailState,
    onEvent: (DiaryDetailEvent) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.plantName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (state.isHarvested) {
                    Spacer(Modifier.width(8.dp))
                    TextBadge(text = stringResource(R.string.harvested))
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(DiaryDetailEvent.BackClick) }) {
                Icon(
                    imageVector = IconPack.ArrowBackBlack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = IconPack.MoreHorizBlack,
                        contentDescription = stringResource(R.string.menu)
                    )
                }
                AppDropDownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            menuExpanded = false
                            onEvent(DiaryDetailEvent.EditClick)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            menuExpanded = false
                            onEvent(DiaryDetailEvent.DeleteClick)
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(R.string.message_diary_delete_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDiaryDetail() {
    GardenLogTheme {
        Content(
            state = DiaryDetailState(
                plantName = "바질",
                diaryDate = LocalDate.of(2025, 6, 10),
                memo = "오늘은 바질이 많이 자랐다. 물을 줬더니 잎이 싱싱해 보인다."
            ),
            onEvent = {}
        )
    }
}
