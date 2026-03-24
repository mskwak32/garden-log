package com.mskwak.plant.diary_export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.ArrowBackBlack
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.WheelDatePicker
import com.mskwak.plant.R
import java.time.LocalDate

@Composable
fun DiaryExportScreen(
    viewModel: DiaryExportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNoticeDialog by remember { mutableStateOf(true) }

    val successMessage = stringResource(R.string.diary_export_success)
    val failMessage = stringResource(R.string.diary_export_fail)
    val noDiaryMessage = stringResource(R.string.diary_export_no_diary)

    if (showNoticeDialog) {
        DiaryExportNoticeDialog(
            onConfirm = { showNoticeDialog = false },
            onDismiss = { viewModel.setEvent(DiaryExportEvent.OnBackClicked) }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiaryExportEffect.NavigateBack -> onNavigateBack()
                is DiaryExportEffect.ShowExportSuccess -> {
                    snackbarHostState.showSnackbar(successMessage)
                    onNavigateBack()
                }

                is DiaryExportEffect.ShowExportFail -> {
                    snackbarHostState.showSnackbar(failMessage)
                }

                is DiaryExportEffect.ShowNoDiaryInRange -> {
                    snackbarHostState.showSnackbar(noDiaryMessage)
                }
            }
        }
    }

    Content(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::setEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: DiaryExportState,
    snackbarHostState: SnackbarHostState,
    onEvent: (DiaryExportEvent) -> Unit
) {
    Scaffold(
        topBar = { TopBar(onEvent = onEvent) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // 첫 페이지 포함 여부
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = state.includeFirstPage,
                        onCheckedChange = { onEvent(DiaryExportEvent.OnIncludeFirstPageChanged(it)) }
                    )
                    Text(
                        text = stringResource(R.string.diary_export_include_first_page),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(24.dp))

                // 날짜 범위 선택 (일기가 있는 경우에만 활성화)
                if (state.diaryDateRange != null) {
                    DateRangeSection(
                        state = state,
                        onEvent = onEvent
                    )
                }

                Spacer(Modifier.height(32.dp))

                // 내보내기 버튼
                Button(
                    onClick = { onEvent(DiaryExportEvent.OnExportClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isExporting
                ) {
                    Text(stringResource(R.string.diary_export_button))
                }
            }

            // 로딩 오버레이
            if (state.isExporting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun DateRangeSection(
    state: DiaryExportState,
    onEvent: (DiaryExportEvent) -> Unit
) {
    val minDate = state.diaryDateRange!!.first
    val maxDate = state.diaryDateRange.second

    Text(
        text = stringResource(R.string.diary_export_start_date),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    WheelDatePicker(
        initialDate = state.startDate,
        minDate = minDate,
        maxDate = state.endDate, // 시작 날짜는 종료 날짜를 초과할 수 없음
        onDateSelected = { onEvent(DiaryExportEvent.OnStartDateChanged(it)) }
    )

    Spacer(Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.diary_export_end_date),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    WheelDatePicker(
        initialDate = state.endDate,
        minDate = state.startDate, // 종료 날짜는 시작 날짜보다 앞설 수 없음
        maxDate = maxDate,
        onDateSelected = { onEvent(DiaryExportEvent.OnEndDateChanged(it)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onEvent: (DiaryExportEvent) -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.diary_export_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(DiaryExportEvent.OnBackClicked) }) {
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
private fun DiaryExportScreenPreview() {
    GardenLogTheme {
        Content(
            state = DiaryExportState(
                plantName = "토마토",
                diaryDateRange = LocalDate.of(2024, 1, 1) to LocalDate.of(2024, 12, 31),
                startDate = LocalDate.of(2024, 1, 1),
                endDate = LocalDate.of(2024, 12, 31)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
