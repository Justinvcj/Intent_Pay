package com.intentpay.ui.pending

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.data.local.TransactionEntity
import com.intentpay.domain.models.Category
import com.intentpay.ui.components.GlassButton
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.components.MeshBackground
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosGreen
import com.intentpay.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingReviewScreen(
    viewModel: PendingReviewViewModel,
    onBack: () -> Unit
) {
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground(
            modifier = Modifier.fillMaxSize()
        ) {
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Pending Review", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color.White) },
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
            if (pendingTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("All caught up! 🎉", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pendingTransactions, key = { it.id }) { tx ->
                        PendingTransactionCard(
                            transaction = tx,
                            onCategorySelected = { category ->
                                viewModel.categorize(tx, category)
                            }
                        )
                    }
                }
            }
        }
        }
    }
}
@Composable
fun PendingTransactionCard(
    transaction: TransactionEntity,
    onCategorySelected: (Category) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().animateContentSize(), cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            val amountColor = if (transaction.isIncome) IosGreen else Color.White
            val sign = if (transaction.isIncome) "+" else "-"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant ?: "Unknown Merchant",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Via ${transaction.source ?: "App"}",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "$sign₹${transaction.amount}",
                    color = amountColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Categorize as:", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))

            val displayCategories = Category.values().filter { it != Category.UNCATEGORIZED }
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayCategories) { category ->
                    GlassButton(
                        text = "${category.emoji} ${category.name.lowercase().capitalize()}",
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        }
    }
}
