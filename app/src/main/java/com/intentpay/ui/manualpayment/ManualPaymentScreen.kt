package com.intentpay.ui.manualpayment

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.domain.models.Category
import com.intentpay.ui.components.AppTextField
import com.intentpay.ui.components.PrimaryButton
import com.intentpay.ui.components.SecondaryButton
import com.intentpay.ui.components.SectionLabel
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
                        snackbarHostState.showSnackbar("QR details added")
                    } else {
                        snackbarHostState.showSnackbar("That is not a UPI QR code")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("That is not a UPI QR code")
                }
            } else {
                snackbarHostState.showSnackbar("That is not a UPI QR code")
            }
            onClearQrResult()
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Camera access needed") },
            text = { Text("Allow camera access in Settings to scan a UPI QR code.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Add payment", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
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
        ) {
            SecondaryButton(
                text = "Scan QR code",
                onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("Payment details")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount (₹)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                AppTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = "Merchant"
                )
                AppTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = "UPI ID",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                AppTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = "What is it for",
                    supportingText = "Required"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Category")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.displayName, style = MaterialTheme.typography.bodyMedium) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Pay with UPI app",
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
                        return@PrimaryButton
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
                enabled = purpose.isNotBlank() && amount.isNotBlank() && upiId.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
