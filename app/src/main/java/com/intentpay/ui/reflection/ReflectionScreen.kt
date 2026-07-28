package com.intentpay.ui.reflection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.domain.models.Category
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosRed
import com.intentpay.ui.theme.SurfaceDarkGray
import com.intentpay.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReflectionScreen(
    viewModel: ReflectionViewModel,
    amount: Double,
    onFinish: () -> Unit
) {
    var purpose by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.UNCATEGORIZED) }
    
    val isSaved by viewModel.isSaved.collectAsState()
    
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onFinish()
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Hold on.",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = IosRed,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "What was this ₹$amount for?",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            TextField(
                value = purpose,
                onValueChange = { purpose = it },
                placeholder = { Text("Justification (Required)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDarkGray,
                    unfocusedContainerColor = SurfaceDarkGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedPlaceholderColor = TextSecondary
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "CATEGORY",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            
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
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.saveReflection(purpose, selectedCategory) },
                enabled = purpose.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosBlue,
                    disabledContainerColor = SurfaceDarkGray
                )
            ) {
                Text("Save & Continue", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = if(purpose.isNotBlank()) Color.White else TextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
