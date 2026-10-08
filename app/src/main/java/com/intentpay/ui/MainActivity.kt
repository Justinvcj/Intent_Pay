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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.intentpay.ui.pending.PendingReviewViewModelFactory
import com.intentpay.ui.analytics.AnalyticsViewModelFactory
import com.intentpay.ui.history.HistoryViewModelFactory
import com.intentpay.ui.onboarding.OnboardingScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as IntentPayApplication
        val repository = app.transactionRepository

        val dashboardFactory = DashboardViewModelFactory(repository)
        val manualPayFactory = ManualPaymentViewModelFactory(repository)
        val settingsFactory = SettingsViewModelFactory(repository)
        val pendingReviewFactory = PendingReviewViewModelFactory(repository, app.merchantMemory)
        val analyticsFactory = AnalyticsViewModelFactory(repository)
        val historyFactory = HistoryViewModelFactory(repository)

        setContent {
            IntentPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var splashCompleted by remember { mutableStateOf(false) }
                    val isOnboardingComplete by app.dataStoreManager.isOnboardingComplete.collectAsState(initial = null)
                    val scope = rememberCoroutineScope()
                    
                    if (!splashCompleted || isOnboardingComplete == null) {
                        AnimatedSplashScreen(
                            onAnimationFinished = {
                                splashCompleted = true
                            }
                        )
                    } else if (isOnboardingComplete == false) {
                        OnboardingScreen(
                            onComplete = {
                                scope.launch {
                                    app.dataStoreManager.setOnboardingComplete(true)
                                }
                            }
                        )
                    } else {
                        MainScreen(
                            dashboardFactory = dashboardFactory,
                            manualPayFactory = manualPayFactory,
                            settingsFactory = settingsFactory,
                            pendingReviewFactory = pendingReviewFactory,
                            analyticsFactory = analyticsFactory,
                            historyFactory = historyFactory
                        )
                    }
                }
            }
        }
    }
}
