package com.intentpay.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.intentpay.ui.analytics.AnalyticsScreen
import com.intentpay.ui.analytics.AnalyticsViewModel
import com.intentpay.ui.analytics.AnalyticsViewModelFactory
import com.intentpay.ui.dashboard.DashboardScreen
import com.intentpay.ui.dashboard.DashboardViewModel
import com.intentpay.ui.dashboard.DashboardViewModelFactory
import com.intentpay.ui.history.HistoryScreen
import com.intentpay.ui.history.HistoryViewModel
import com.intentpay.ui.history.HistoryViewModelFactory
import com.intentpay.ui.manualpayment.ManualPaymentScreen
import com.intentpay.ui.manualpayment.ManualPaymentViewModel
import com.intentpay.ui.manualpayment.ManualPaymentViewModelFactory
import com.intentpay.ui.pending.PendingReviewScreen
import com.intentpay.ui.pending.PendingReviewViewModel
import com.intentpay.ui.pending.PendingReviewViewModelFactory
import com.intentpay.ui.qrscanner.QrScannerScreen
import com.intentpay.ui.settings.SettingsScreen
import com.intentpay.ui.settings.SettingsViewModelFactory

private data class Tab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Four top-level sections, single-word labels, filled icon when selected.
private val tabs = listOf(
    Tab("dashboard", "Summary", Icons.Rounded.Home, Icons.Outlined.Home),
    Tab("analytics", "Analytics", Icons.Rounded.PieChart, Icons.Outlined.PieChart),
    Tab("history", "History", Icons.Rounded.History, Icons.Outlined.History),
    Tab("settings", "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
)

@Composable
fun MainScreen(
    dashboardFactory: DashboardViewModelFactory,
    manualPayFactory: ManualPaymentViewModelFactory,
    settingsFactory: SettingsViewModelFactory,
    pendingReviewFactory: PendingReviewViewModelFactory,
    analyticsFactory: AnalyticsViewModelFactory,
    historyFactory: HistoryViewModelFactory
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // The tab bar stays on the four top-level screens only.
            if (currentDestination?.route in tabs.map { it.route }) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding),
            // One short fade for every move. Tabs are peers, so they should not slide.
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(120)) }
        ) {
            composable("dashboard") {
                val viewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToManualPayment = { navController.navigate("manual_payment") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToPendingReview = { navController.navigate("pending_review") }
                )
            }

            composable("manual_payment") { backStackEntry ->
                val viewModel: ManualPaymentViewModel = viewModel(factory = manualPayFactory)
                val qrResult by backStackEntry.savedStateHandle.getStateFlow<String?>("qr_result", null).collectAsState()

                ManualPaymentScreen(
                    viewModel = viewModel,
                    qrResult = qrResult,
                    onClearQrResult = { backStackEntry.savedStateHandle.remove<String>("qr_result") },
                    onNavigateToScanner = { navController.navigate("qr_scanner") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("qr_scanner") {
                QrScannerScreen(
                    onScanSuccess = { result ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("qr_result", result)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                val viewModel: com.intentpay.ui.settings.SettingsViewModel = viewModel(factory = settingsFactory)
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("pending_review") {
                val viewModel: PendingReviewViewModel = viewModel(factory = pendingReviewFactory)
                PendingReviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("analytics") {
                val viewModel: AnalyticsViewModel = viewModel(factory = analyticsFactory)
                AnalyticsScreen(viewModel = viewModel)
            }

            composable("history") {
                val viewModel: HistoryViewModel = viewModel(factory = historyFactory)
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
