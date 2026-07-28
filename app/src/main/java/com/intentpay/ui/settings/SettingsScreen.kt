package com.intentpay.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosRed
import com.intentpay.ui.theme.SurfaceDarkGray
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
            sheetIdInput = it.googleSheetId ?: ""
        }
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = SurfaceDarkGray,
        unfocusedContainerColor = SurfaceDarkGray,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = IosBlue,
        unfocusedLabelColor = TextSecondary
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = IosBlue)
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkGray, RoundedCornerShape(10.dp))
            ) {
                TextField(
                    value = monthlyInput,
                    onValueChange = { monthlyInput = it },
                    label = { Text("Monthly Budget (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                HorizontalDivider(color = Color(0x33FFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                
                TextField(
                    value = dailyInput,
                    onValueChange = { dailyInput = it },
                    label = { Text("Daily Soft Limit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                HorizontalDivider(color = Color(0x33FFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                
                TextField(
                    value = reflectionInput,
                    onValueChange = { reflectionInput = it },
                    label = { Text("Reflection Threshold (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("GOOGLE SHEETS SYNC", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkGray, RoundedCornerShape(10.dp))
            ) {
                TextField(
                    value = sheetIdInput,
                    onValueChange = { sheetIdInput = it },
                    label = { Text("Google Sheet ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (signedInEmail != null) {
                Text(
                    text = "Signed in as: $signedInEmail",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Button(
                    onClick = {
                        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener {
                            signedInEmail = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkGray)
                ) {
                    Text("Sign Out", color = IosRed, fontSize = 17.sp)
                }
            } else {
                Button(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        signInLauncher.launch(client.signInIntent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkGray)
                ) {
                    Text("Sign In with Google", color = IosBlue, fontSize = 17.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

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
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue)
            ) {
                Text("Save Settings", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
