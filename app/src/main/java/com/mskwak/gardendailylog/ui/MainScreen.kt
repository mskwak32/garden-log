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
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mskwak.design.ui_component.LocalNavBottomBarPadding
import com.mskwak.gardendailylog.R
import com.mskwak.gardendailylog.ui.bottom_nav.BottomNavItem
import com.mskwak.plant.plantNavGraph
import com.mskwak.plant.plant_list.PlantListNavKey
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    openAppSetting: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation 3: 단일 백스택 사용
    val backStack = rememberNavBackStack(BottomNavItem.PlantList.screen)

    // 현재 화면이 탭 메뉴인지 확인
    val currentScreen = backStack.lastOrNull()
    val tabScreens = BottomNavItem.items.map { it.screen }
    val isTabScreen = currentScreen in tabScreens

    NotificationPermission(
        snackbarHostState = snackbarHostState,
        openAppSetting = openAppSetting
    )

    BackPressFinish(currentScreen)

    val density = LocalDensity.current
    var navBarHeight by remember { mutableStateOf(0.dp) }
    val bottomBarPadding = if (isTabScreen) navBarHeight else 0.dp

    CompositionLocalProvider(LocalNavBottomBarPadding provides bottomBarPadding) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider {
                    plantNavGraph(backStack)
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                predictivePopTransitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(animationSpec = tween(700)),
                        initialContentExit = fadeOut(animationSpec = tween(700)),
                    )
                }
            )

            // Snackbar: NavigationBar 위에 표시
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isTabScreen) navBarHeight + 8.dp else 8.dp)
            )

            // 탭 메뉴 화면일 때만 BottomBar 표시
            if (isTabScreen) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    backStack = backStack,
                    tabScreens = tabScreens,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                navBarHeight = coordinates.size.height.toDp()
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentScreen: NavKey?,
    backStack: NavBackStack<NavKey>,
    tabScreens: List<NavKey>,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
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
    currentNavKey: NavKey?
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = currentNavKey == PlantListNavKey) {
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
