package com.intentpay.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes
import com.intentpay.utils.PermissionHelper
import com.intentpay.utils.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val budget by viewModel.budget.collectAsState()

    var monthlyInput by remember { mutableStateOf("") }
    var dailyInput by remember { mutableStateOf("") }
    var reflectionInput by remember { mutableStateOf("") }
    var sheetIdInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    var signedInEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        signedInEmail = account?.email
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            signedInEmail = account?.email
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val scope = rememberCoroutineScope()

    val permissionState = rememberPermissionState()
    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionState.refresh() }

    var pendingExportFormat by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            pendingExportFormat?.let { format ->
                scope.launch {
                    try {
                        val exportData = viewModel.generateExportData(format)
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(exportData.toByteArray())
                            }
                        }
                        Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                    }
                    pendingExportFormat = null
                }
            }
        }
    }

    LaunchedEffect(budget) {
        budget?.let {
            monthlyInput = it.monthlyBudget.toString()
            dailyInput = it.dailyLimit.toString()
            reflectionInput = it.reflectionThreshold.toString()
            sheetIdInput = it.googleDriveFolderId ?: ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsGroup(title = "Budget limits") {
                SettingsTextFieldRow(
                    icon = Icons.Rounded.Payments,
                    label = "Monthly budget (₹)",
                    value = monthlyInput,
                    onValueChange = { monthlyInput = it },
                    keyboardType = KeyboardType.Number
                )
                SettingsDivider()
                SettingsTextFieldRow(
                    icon = Icons.Rounded.Today,
                    label = "Daily soft limit (₹)",
                    value = dailyInput,
                    onValueChange = { dailyInput = it },
                    keyboardType = KeyboardType.Number
                )
                SettingsDivider()
                SettingsTextFieldRow(
                    icon = Icons.Rounded.Psychology,
                    label = "Reflection threshold (₹)",
                    value = reflectionInput,
                    onValueChange = { reflectionInput = it },
                    keyboardType = KeyboardType.Number
                )
            }

            SettingsGroup(title = "Detection") {
                SettingsRow(
                    icon = Icons.Rounded.Sms,
                    label = "SMS access",
                    value = if (permissionState.sms) "Enabled" else "Off",
                    trailing = { SettingsChevron() },
                    onClick = { smsLauncher.launch(PermissionHelper.runtimePermissions()) }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    label = "Notification access",
                    value = if (permissionState.notificationListener) "Enabled" else "Off",
                    trailing = { SettingsChevron() },
                    onClick = { context.startActivity(PermissionHelper.notificationListenerIntent()) }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.AccessibilityNew,
                    label = "Accessibility",
                    value = if (permissionState.accessibility) "Enabled" else "Off",
                    trailing = { SettingsChevron() },
                    onClick = { context.startActivity(PermissionHelper.accessibilityIntent()) }
                )
            }

            SettingsGroup(title = "Google Drive sync") {
                SettingsTextFieldRow(
                    icon = Icons.Rounded.Folder,
                    label = "Google Drive folder ID",
                    value = sheetIdInput,
                    onValueChange = { sheetIdInput = it }
                )
                SettingsDivider()
                if (signedInEmail != null) {
                    SettingsRow(
                        icon = Icons.Rounded.AccountCircle,
                        label = "Signed in",
                        value = signedInEmail
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Logout,
                        label = "Sign out",
                        onClick = {
                            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .signOut()
                                .addOnCompleteListener { signedInEmail = null }
                        }
                    )
                } else {
                    SettingsRow(
                        icon = Icons.Rounded.Login,
                        label = "Sign in with Google",
                        trailing = { SettingsChevron() },
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            signInLauncher.launch(client.signInIntent)
                        }
                    )
                }
            }

            SettingsGroup(title = "Data export") {
                SettingsRow(
                    icon = Icons.Rounded.TableChart,
                    label = "Export to CSV",
                    trailing = { SettingsChevron() },
                    onClick = {
                        pendingExportFormat = "csv"
                        exportLauncher.launch("intent_pay_export.csv")
                    }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.DataObject,
                    label = "Export to JSON",
                    trailing = { SettingsChevron() },
                    onClick = {
                        pendingExportFormat = "json"
                        exportLauncher.launch("intent_pay_export.json")
                    }
                )
            }

            Button(
                onClick = {
                    viewModel.updateBudget(
                        monthly = monthlyInput.toDoubleOrNull() ?: 10000.0,
                        daily = dailyInput.toDoubleOrNull() ?: 500.0,
                        reflection = reflectionInput.toDoubleOrNull() ?: 200.0
                    )
                    viewModel.updateSheetId(sheetIdInput)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save settings", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Grouped, rounded surface with an optional muted header above it (iOS Settings style). */
@Composable
fun SettingsGroup(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}

/** Single 48dp-min row: leading icon, label, optional value and trailing slot. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        if (value != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(4.dp))
            trailing()
        }
    }
}

/** Row with a Material3 Switch and an optional description line. */
@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsTextFieldRow(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text(label, style = MaterialTheme.typography.bodyLarge) },
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingsChevron() {
    Icon(
        imageVector = Icons.Rounded.ChevronRight,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Hairline divider inset 48dp (16 gutter + 24 icon + 8 gap) so it clears the icon column. */
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 48.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
