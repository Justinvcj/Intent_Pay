package com.intentpay.ui.manualpayment

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.intentpay.domain.models.Category
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
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
    var expanded by remember { mutableStateOf(false) }
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
                    // Uri.getQueryParameter implicitly handles URL decoding
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pay with Purpose") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Scan QR", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = merchantName,
                onValueChange = { merchantName = it },
                label = { Text("Merchant Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("Merchant UPI ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Purpose (Mandatory)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Category.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                selectedCategory = cat
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                            // If no UPI app is installed
                            onBack()
                        }
                    }
                },
                enabled = purpose.isNotBlank() && amount.isNotBlank() && upiId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Proceed to Pay", fontWeight = FontWeight.Bold)
            }
        }
    }
}
