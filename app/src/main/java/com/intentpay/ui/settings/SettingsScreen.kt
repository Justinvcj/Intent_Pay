package com.intentpay.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes
import com.intentpay.ui.components.GlassButton
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.components.GlassTextField
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosRed
import com.intentpay.ui.theme.TextSecondary

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

    LaunchedEffect(budget) {
        budget?.let {
            monthlyInput = it.monthlyBudget.toString()
            dailyInput = it.dailyLimit.toString()
            reflectionInput = it.reflectionThreshold.toString()
            sheetIdInput = it.googleDriveFolderId ?: ""
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = IosBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
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
        ) {
            Text("BUDGET LIMITS", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    GlassTextField(
                        value = monthlyInput,
                        onValueChange = { monthlyInput = it },
                        placeholder = "Monthly Budget (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 0.dp
                    )
                    HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                    
                    GlassTextField(
                        value = dailyInput,
                        onValueChange = { dailyInput = it },
                        placeholder = "Daily Soft Limit (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 0.dp
                    )
                    HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                    
                    GlassTextField(
                        value = reflectionInput,
                        onValueChange = { reflectionInput = it },
                        placeholder = "Reflection Threshold (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 0.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("GOOGLE DRIVE SYNC", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    GlassTextField(
                        value = sheetIdInput,
                        onValueChange = { sheetIdInput = it },
                        placeholder = "Google Drive Folder ID",
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 0.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (signedInEmail != null) {
                Text(
                    text = "Signed in as: $signedInEmail",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                GlassButton(
                    text = "Sign Out",
                    onClick = {
                        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener {
                            signedInEmail = null
                        }
                    },
                    isPrimary = false
                )
            } else {
                GlassButton(
                    text = "Sign In with Google",
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        signInLauncher.launch(client.signInIntent)
                    },
                    isPrimary = false
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            GlassButton(
                text = "Save Settings",
                onClick = {
                    viewModel.updateBudget(
                        monthly = monthlyInput.toDoubleOrNull() ?: 10000.0,
                        daily = dailyInput.toDoubleOrNull() ?: 500.0,
                        reflection = reflectionInput.toDoubleOrNull() ?: 200.0
                    )
                    viewModel.updateSheetId(sheetIdInput)
                    onBack()
                },
                isPrimary = true
            )
            
            Spacer(modifier = Modifier.height(100.dp)) // padding for bottom bar
        }
    }
}
