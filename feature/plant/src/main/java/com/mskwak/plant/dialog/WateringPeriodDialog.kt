package com.mskwak.plant.dialog

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.domain.Constants
import com.mskwak.plant.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun WateringPeriodDialog(
    initialPeriod: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedPeriod by remember { mutableIntStateOf(initialPeriod) }
    val noneText = stringResource(R.string.none)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.watering_period),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                NumberPickerWheel(
                    value = selectedPeriod,
                    range = 0..Constants.MAX_WATERING_PERIOD,
                    onValueChange = { selectedPeriod = it },
                    displayFormatter = { value ->
                        if (value == 0) noneText else value.toString()
                    },
                    modifier = Modifier.width(80.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.watering_period_unit_dialog),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedPeriod) }) {
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

@Composable
private fun NumberPickerWheel(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    displayFormatter: (Int) -> String,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = 3
) {
    val itemHeightDp = 40.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.toPx() }
    val halfVisibleCount = visibleItemCount / 2
    val totalHeight = itemHeightDp * visibleItemCount

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = value - range.first
    )

    // 선택값 추적
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .map { centerIndex -> (centerIndex + range.first).coerceIn(range) }
            .collect { newValue -> onValueChange(newValue) }
    }

    Box(
        modifier = modifier.height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = Modifier
                .height(totalHeight)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    // 상단 fade
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = itemHeightPx
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    // 하단 fade
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startY = size.height - itemHeightPx,
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn
                    )
                },
            contentPadding = PaddingValues(vertical = itemHeightDp * halfVisibleCount)
        ) {
            items(range.count()) { index ->
                val itemValue = index + range.first

                Box(
                    modifier = Modifier
                        .height(itemHeightDp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayFormatter(itemValue),
                        fontSize = if (itemValue == value) 20.sp else 16.sp,
                        color = if (itemValue == value) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 선택 영역 divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = itemHeightDp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = itemHeightDp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

@Preview
@Composable
private fun PreviewWateringPeriodDialog() {
    GardenLogTheme {
        WateringPeriodDialog(
            initialPeriod = 3,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
