package com.mskwak.gardendailylog.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mskwak.common_ui.Screen
import com.mskwak.gardendailylog.R
import com.mskwak.gardendailylog.ui.bottom_nav.BottomNavItem
import com.mskwak.plant.diary_list.DiaryListScreen
import com.mskwak.plant.plant_list.PlantListScreen
import com.mskwak.plant.setting.SettingScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    openAppSetting: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation 3: 단일 백스택 사용
    val backStack = remember { mutableStateListOf(BottomNavItem.PlantList.screen) }

    // 현재 화면이 탭 메뉴인지 확인
    val currentScreen = backStack.lastOrNull()
    val tabScreens = BottomNavItem.items.map { it.screen }
    val isTabScreen = currentScreen in tabScreens

    NotificationPermission(
        snackbarHostState = snackbarHostState,
        openAppSetting = openAppSetting
    )

    BackPressFinish(currentScreen)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // 탭 메뉴 화면일 때만 BottomBar 표시
            if (isTabScreen) {
                NavigationBar {
                    BottomNavItem.items.forEach { item ->
                        NavigationBarItem(
                            item = item,
                            selected = currentScreen == item.screen,
                            onClick = {
                                // 현재 탭과 다른 탭을 선택한 경우에만 네비게이션
                                if (currentScreen != item.screen) {
                                    // 홈탭만 남기고 나머지 탭 제거
                                    while (
                                        backStack.size > 1 &&
                                        backStack.lastOrNull() in tabScreens
                                    ) {
                                        backStack.removeLastOrNull()
                                    }
                                    // 홈탭이 아닌 경우에만 추가 (홈탭은 이미 백스택 맨 아래에 있음)
                                    if (item.screen != BottomNavItem.PlantList.screen) {
                                        backStack.add(item.screen)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            entryProvider = entryProvider {
                entry<DiaryListScreen> {
                    DiaryListScreen(
                        navigate = { /* TODO */ }
                    )
                }
                entry<PlantListScreen> {
                    PlantListScreen(
                        navigate = { /* TODO */ }
                    )
                }
                entry<SettingScreen> {
                    SettingScreen()
                }
            }
        )
    }
}

@Composable
private fun RowScope.NavigationBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val title = stringResource(item.titleRes)
    NavigationBarItem(
        icon = { Icon(item.icon, contentDescription = title) },
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        selected = selected,
        onClick = onClick
    )
}

/**
 * 알림 권한 체크 및 요청
 */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun NotificationPermission(
    snackbarHostState: SnackbarHostState,
    openAppSetting: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.message_notification_permission),
                        actionLabel = context.getString(R.string.permission_setting_action),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        openAppSetting()
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}

/**
 * 백버튼 2번 눌러 종료.
 * 홈 화면(PlantList)에 있을 때에만 앱 종료 로직이 동작하도록 설정.
 * 다른 탭이나 상세화면에서는 기본 뒤로가기 동작
 */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun BackPressFinish(
    currentScreen: Screen?
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = currentScreen == PlantListScreen) {
        if (System.currentTimeMillis() > backPressedTime + 2000) {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(
                context,
                context.getString(R.string.message_finish_app),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            activity?.finish()
        }
    }
}
