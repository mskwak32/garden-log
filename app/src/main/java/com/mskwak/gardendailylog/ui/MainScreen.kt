package com.mskwak.gardendailylog.ui

import android.Manifest
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mskwak.gardendailylog.R
import com.mskwak.gardendailylog.ui.bottom_nav.BottomNavItem
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    openAppSetting: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(
        BottomNavItem.DiaryList,
        BottomNavItem.PlantList,
        BottomNavItem.Setting
    )

    NotificationPermission(
        snackbarHostState = snackbarHostState,
        openAppSetting = openAppSetting
    )

    BackPressFinish(currentRoute)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // 현재 경로가 탭 메뉴 중 하나일 때만 BottomBar 표시
            if (items.any { it.route == currentRoute }) {
                NavigationBar(containerColor = Color.White) {
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(screen, currentDestination, navController)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.PlantList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.DiaryList.route) {}
            composable(BottomNavItem.PlantList.route) {}
            composable(BottomNavItem.Setting.route) {}
        }
    }
}

@Composable
private fun RowScope.NavigationBarItem(
    screen: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val title = stringResource(screen.titleRes)
    NavigationBarItem(
        icon = { Icon(screen.icon, contentDescription = title) },
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

/**
 * 알림 권한 체크 및 요청
 */
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
 * 홈 화면에 있을 때에만 앱 종료 로직이 동작하도록 설정.
 * 다른 탭이나 상세화면에서는 기본 뒤로가기 동작
 */
@Composable
private fun BackPressFinish(
    currentRoute: String?
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = currentRoute == BottomNavItem.PlantList.route) {
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