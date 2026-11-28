package com.intentpay.utils

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.intentpay.ui.components.GlassButton
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.theme.IosGreen
import com.intentpay.ui.theme.TextSecondary

/** State checks + intents for the three detection channels. */
object PermissionHelper {

    /** Runtime permissions requested for the SMS channel (+ notification posting on API 33+). */
    fun runtimePermissions(): Array<String> {
        val list = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list += Manifest.permission.POST_NOTIFICATIONS
        }
        return list.toTypedArray()
    }

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun isSmsGranted(context: Context): Boolean =
        granted(context, Manifest.permission.RECEIVE_SMS) && granted(context, Manifest.permission.READ_SMS)

    fun isPostNotificationsGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            granted(context, Manifest.permission.POST_NOTIFICATIONS)

    fun isNotificationListenerEnabled(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    fun isAccessibilityEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any {
            ComponentName.unflattenFromString(it)?.packageName == context.packageName
        }
    }

    fun notificationListenerIntent() =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun accessibilityIntent() =
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

/** Holds live permission state; call [refresh] on resume / after a permission result. */
@Stable
class PermissionState(private val context: Context) {
    var sms by mutableStateOf(PermissionHelper.isSmsGranted(context) && PermissionHelper.isPostNotificationsGranted(context))
        private set
    var notificationListener by mutableStateOf(PermissionHelper.isNotificationListenerEnabled(context))
        private set
    var accessibility by mutableStateOf(PermissionHelper.isAccessibilityEnabled(context))
        private set

    fun refresh() {
        sms = PermissionHelper.isSmsGranted(context) && PermissionHelper.isPostNotificationsGranted(context)
        notificationListener = PermissionHelper.isNotificationListenerEnabled(context)
        accessibility = PermissionHelper.isAccessibilityEnabled(context)
    }
}

@Composable
fun rememberPermissionState(): PermissionState {
    val context = LocalContext.current
    val state = remember { PermissionState(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}

/** The three permission rows (SMS, Notification Access, Accessibility) with status + action button. */
@Composable
fun DetectionPermissionRows(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val state = rememberPermissionState()

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { state.refresh() }

    GlassCard(modifier = modifier.fillMaxWidth(), cornerRadius = 12.dp) {
        Column {
            PermissionRow(
                title = "SMS Access",
                subtitle = "Detect bank & UPI SMS alerts",
                enabled = state.sms,
                onClick = { smsLauncher.launch(PermissionHelper.runtimePermissions()) }
            )
            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            PermissionRow(
                title = "Notification Access",
                subtitle = "Read GPay, PhonePe, Paytm, BHIM alerts",
                enabled = state.notificationListener,
                onClick = { context.startActivity(PermissionHelper.notificationListenerIntent()) }
            )
            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            PermissionRow(
                title = "Accessibility",
                subtitle = "Catch payments as they happen",
                enabled = state.accessibility,
                onClick = { context.startActivity(PermissionHelper.accessibilityIntent()) }
            )
        }
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        if (enabled) {
            Text("✓ Enabled", color = IosGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        } else {
            // GlassButton is fillMaxWidth by default; without a fixed width it consumes the whole
            // row and collapses the weighted label column to zero width.
            GlassButton(text = "Enable", onClick = onClick, isPrimary = false, modifier = Modifier.width(110.dp))
        }
    }
}
