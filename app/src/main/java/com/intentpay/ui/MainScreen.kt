package com.intentpay.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.intentpay.ui.components.MeshBackground
import com.intentpay.ui.dashboard.DashboardScreen
import com.intentpay.ui.dashboard.DashboardViewModel
import com.intentpay.ui.dashboard.DashboardViewModelFactory
import com.intentpay.ui.manualpayment.ManualPaymentScreen
import com.intentpay.ui.manualpayment.ManualPaymentViewModel
import com.intentpay.ui.manualpayment.ManualPaymentViewModelFactory
import com.intentpay.ui.qrscanner.QrScannerScreen
import com.intentpay.ui.settings.SettingsScreen
import com.intentpay.ui.settings.SettingsViewModelFactory
import com.intentpay.ui.pending.PendingReviewScreen
import com.intentpay.ui.pending.PendingReviewViewModel
import com.intentpay.ui.pending.PendingReviewViewModelFactory
import com.intentpay.ui.analytics.AnalyticsScreen
import com.intentpay.ui.analytics.AnalyticsViewModel
import com.intentpay.ui.analytics.AnalyticsViewModelFactory
import com.intentpay.ui.history.HistoryScreen
import com.intentpay.ui.history.HistoryViewModel
import com.intentpay.ui.history.HistoryViewModelFactory
import com.intentpay.ui.theme.GlassSurface
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.TextSecondary

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
    
    MeshBackground {
        Scaffold(
            containerColor = Color.Transparent, // Let MeshBackground show through
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                // Only show bottom bar on main screens
                val showBottomBar = currentDestination?.route in listOf("dashboard", "analytics", "history", "settings")
                
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    ) {
                        NavigationBar(
                            modifier = Modifier.clip(RoundedCornerShape(percent = 50)),
                            containerColor = GlassSurface,
                            contentColor = Color.White
                        ) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.Home, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                selected = currentDestination?.hierarchy?.any { it.route == "dashboard" } == true,
                                onClick = {
                                    navController.navigate("dashboard") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IosBlue,
                                    selectedTextColor = IosBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.PieChart, contentDescription = "Analytics") },
                                label = { Text("Analytics") },
                                selected = currentDestination?.hierarchy?.any { it.route == "analytics" } == true,
                                onClick = {
                                    navController.navigate("analytics") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IosBlue,
                                    selectedTextColor = IosBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.History, contentDescription = "History") },
                                label = { Text("History") },
                                selected = currentDestination?.hierarchy?.any { it.route == "history" } == true,
                                onClick = {
                                    navController.navigate("history") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IosBlue,
                                    selectedTextColor = IosBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                                onClick = {
                                    navController.navigate("settings") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IosBlue,
                                    selectedTextColor = IosBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController, 
                startDestination = "dashboard",
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 1f,
                            stiffness = 400f
                        )
                    )
                },
                exitTransition = {
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { -1000 },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 1f,
                            stiffness = 400f
                        )
                    )
                },
                popEnterTransition = {
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { -1000 },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 1f,
                            stiffness = 400f
                        )
                    )
                },
                popExitTransition = {
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 1f,
                            stiffness = 400f
                        )
                    )
                }
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
}
