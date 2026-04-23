package com.intentpay.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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

    LaunchedEffect(budget) {
        budget?.let {
            monthlyInput = it.monthlyBudget.toString()
            dailyInput = it.dailyLimit.toString()
            reflectionInput = it.reflectionThreshold.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Settings") },
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
            OutlinedTextField(
                value = monthlyInput,
                onValueChange = { monthlyInput = it },
                label = { Text("Monthly Budget") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = dailyInput,
                onValueChange = { dailyInput = it },
                label = { Text("Daily Soft Limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = reflectionInput,
                onValueChange = { reflectionInput = it },
                label = { Text("Reflection Threshold") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.updateBudget(
                        monthly = monthlyInput.toDoubleOrNull() ?: 10000.0,
                        daily = dailyInput.toDoubleOrNull() ?: 500.0,
                        reflection = reflectionInput.toDoubleOrNull() ?: 200.0
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Save Settings")
            }
        }
    }
}
