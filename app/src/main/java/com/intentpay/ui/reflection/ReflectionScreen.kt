package com.intentpay.ui.reflection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.domain.models.Category
import com.intentpay.ui.components.GlassButton
import com.intentpay.ui.components.GlassTextField
import com.intentpay.ui.components.MeshBackground
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosRed
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

    MeshBackground {
        Scaffold(
            containerColor = Color.Transparent
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
                
                GlassTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    placeholder = "Justification (Required)",
                    modifier = Modifier.fillMaxWidth()
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) IosBlue else Color(0x1AFFFFFF))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(32.dp))
                
                GlassButton(
                    text = "Save & Continue",
                    onClick = { viewModel.saveReflection(purpose, selectedCategory) },
                    isPrimary = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { viewModel.skipReflection() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now", color = IosBlue, fontSize = 15.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
