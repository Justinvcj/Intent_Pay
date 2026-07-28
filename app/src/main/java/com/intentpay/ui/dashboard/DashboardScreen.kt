package com.intentpay.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.data.local.TransactionEntity
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosGreen
import com.intentpay.ui.theme.SurfaceDarkGray
import com.intentpay.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToManualPayment: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val budget by viewModel.budgetDetails.collectAsState()
    val todaySpending by viewModel.todaySpending.collectAsState()
    val monthSpending by viewModel.monthSpending.collectAsState()

    val safeToday = todaySpending ?: 0.0
    val safeMonth = monthSpending ?: 0.0
    val monthlyBudget = budget?.monthlyBudget ?: 10000.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = IosBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = SurfaceDarkGray.copy(alpha = 0.9f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToManualPayment,
                containerColor = IosBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Summary",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            item {
                // Main Balance / Budget Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkGray)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Monthly Spending",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹$safeMonth",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "of ₹$monthlyBudget budget",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Today's spending row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "₹$safeToday",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Recent Transactions",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions",
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDarkGray)
                    ) {
                        Column {
                            transactions.forEachIndexed { index, tx ->
                                TransactionRow(tx)
                                if (index < transactions.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 64.dp),
                                        color = Color(0x33FFFFFF),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp)) // FAB padding
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(tx.dateMillis))
    
    val isIncome = tx.isIncome
    val amountColor = if (isIncome) IosGreen else Color.White
    val sign = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Future detail view */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isIncome) IosGreen.copy(alpha = 0.2f) else IosBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (tx.merchant ?: "U").take(1).uppercase(),
                color = if (isIncome) IosGreen else IosBlue,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.merchant ?: "Unknown",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${tx.purpose ?: tx.categoryName} • $dateStr",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        
        Text(
            text = "$sign₹${tx.amount}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = amountColor
        )
    }
}
