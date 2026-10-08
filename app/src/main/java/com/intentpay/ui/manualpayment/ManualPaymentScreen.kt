package com.intentpay.ui.manualpayment

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.domain.models.Category
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.SurfaceDarkGray
import com.intentpay.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualPaymentScreen(
    viewModel: ManualPaymentViewModel,
    qrResult: String?,
    onClearQrResult: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onBack: () -> Unit
) {
    var upiId by remember { mutableStateOf("") }
    var merchantName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.UNCATEGORIZED) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var scannedRawUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> 
        onBack() 
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNavigateToScanner()
        } else {
            showPermissionDeniedDialog = true
        }
    }

    LaunchedEffect(qrResult) {
        if (qrResult != null) {
            if (qrResult.startsWith("upi://pay", ignoreCase = true)) {
                try {
                    val uri = Uri.parse(qrResult)
                    val pa = uri.getQueryParameter("pa")
                    val pn = uri.getQueryParameter("pn")
                    val am = uri.getQueryParameter("am")
                    
                    if (!pa.isNullOrBlank()) {
                        upiId = pa
                        if (!pn.isNullOrBlank()) merchantName = pn
                        if (!am.isNullOrBlank()) amount = am
                        scannedRawUri = qrResult
                        snackbarHostState.showSnackbar("UPI details imported")
                    } else {
                        snackbarHostState.showSnackbar("Invalid UPI QR Code")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Invalid UPI QR Code")
                }
            } else {
                snackbarHostState.showSnackbar("Invalid UPI QR Code")
            }
            onClearQrResult()
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("We need camera access to scan QR codes for payments.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK")
                }
            }
        )
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Manual Payment", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White) },
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
            Button(
                onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkGray)
            ) {
                Text("Scan QR Code Instead", color = IosBlue, fontSize = 17.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("PAYMENT DETAILS", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

            // iOS Style Grouped List Background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkGray, RoundedCornerShape(10.dp))
            ) {
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                HorizontalDivider(color = Color(0x33FFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                
                TextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Merchant Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                HorizontalDivider(color = Color(0x33FFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                
                TextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Merchant UPI ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                HorizontalDivider(color = Color(0x33FFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                
                TextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose (Mandatory)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("CATEGORY", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) IosBlue else SurfaceDarkGray,
                    ) {
                        Text(
                            text = cat.displayName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val sanitizedAmount = amount
                        .replace("₹", "")
                        .replace(",", "")
                        .replace("\\s+".toRegex(), "")
                        .trim()
                        
                    val decimalCount = sanitizedAmount.count { it == '.' }
                    val amountVal = sanitizedAmount.toDoubleOrNull()

                    if (sanitizedAmount.isEmpty() || decimalCount > 1 || amountVal == null || amountVal <= 0.0) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Invalid amount")
                        }
                        return@Button
                    }
                    
                    if (purpose.isNotBlank() && upiId.isNotBlank()) {
                        viewModel.recordPlannedTransaction(amountVal, merchantName, purpose, selectedCategory)
                        
                        val finalAmountStr = if (amountVal % 1.0 == 0.0) {
                            amountVal.toLong().toString()
                        } else {
                            amountVal.toString()
                        }
                        
                        val uri = if (scannedRawUri != null) {
                            var finalUriString = scannedRawUri!!
                            val paymentUri = Uri.parse(finalUriString)
                            val existingAm = paymentUri.getQueryParameter("am")
                            
                            if (existingAm == null) {
                                finalUriString += if (finalUriString.contains("?")) "&am=$finalAmountStr" else "?am=$finalAmountStr"
                            } else if (existingAm != finalAmountStr) {
                                finalUriString = finalUriString.replace(Regex("([?&])am=[^&]*"), "$1am=$finalAmountStr")
                            }
                            
                            Uri.parse(finalUriString)
                        } else {
                            val timestamp = System.currentTimeMillis()
                            val random = (1000..9999).random()
                            val transactionId = "TID_${timestamp}_$random"
                            val transactionRef = "TR_${timestamp}_$random"
                            
                            Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", upiId)
                                .appendQueryParameter("pn", merchantName)
                                .appendQueryParameter("tid", transactionId)
                                .appendQueryParameter("tr", transactionRef)
                                .appendQueryParameter("am", finalAmountStr)
                                .appendQueryParameter("cu", "INR")
                                .build()
                        }
                            
                        Log.d("IntentPay_UPI", uri.toString())
                        
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            launcher.launch(intent)
                        } catch (e: Exception) {
                            onBack()
                        }
                    }
                },
                enabled = purpose.isNotBlank() && amount.isNotBlank() && upiId.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosBlue,
                    disabledContainerColor = SurfaceDarkGray
                )
            ) {
                Text("Proceed to Pay", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
