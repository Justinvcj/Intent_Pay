package com.intentpay.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intentpay.IntentPayApplication
import com.intentpay.ui.dashboard.DashboardScreen
import com.intentpay.ui.dashboard.DashboardViewModel
import com.intentpay.ui.dashboard.DashboardViewModelFactory
import com.intentpay.ui.manualpayment.ManualPaymentScreen
import com.intentpay.ui.manualpayment.ManualPaymentViewModel
import com.intentpay.ui.manualpayment.ManualPaymentViewModelFactory
import com.intentpay.ui.qrscanner.QrScannerScreen
import com.intentpay.ui.settings.SettingsScreen
import com.intentpay.ui.settings.SettingsViewModel
import com.intentpay.ui.settings.SettingsViewModelFactory
import com.intentpay.ui.splash.AnimatedSplashScreen
import com.intentpay.ui.theme.IntentPayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Implement Android 12 SplashScreen API support
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        val app = application as IntentPayApplication
        val repository = app.expenseRepository

        val dashboardFactory = DashboardViewModelFactory(repository)
        val manualPayFactory = ManualPaymentViewModelFactory(repository)
        val settingsFactory = SettingsViewModelFactory(repository)

        setContent {
            IntentPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var splashCompleted by remember { mutableStateOf(false) }
                    
                    if (!splashCompleted) {
                        AnimatedSplashScreen(
                            onAnimationFinished = {
                                splashCompleted = true
                            }
                        )
                    } else {
                        val navController = rememberNavController()
                        
                        NavHost(navController = navController, startDestination = "dashboard") {
                            composable("dashboard") {
                                val viewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToManualPayment = { navController.navigate("manual_payment") },
                                    onNavigateToSettings = { navController.navigate("settings") }
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
                                val viewModel: SettingsViewModel = viewModel(factory = settingsFactory)
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
