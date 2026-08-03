package com.intentpay.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.data.local.TransactionEntity
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosGreen
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToManualPayment,
                containerColor = IosBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 96.dp, end = 16.dp) // Adjusted padding for floating nav
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
            }
        },
        containerColor = Color.Transparent
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
                GlassCard(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Monthly Spending",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Light,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹$safeMonth",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "of ₹$monthlyBudget budget",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Light,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Today's spending row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Light,
                                color = TextSecondary
                            )
                            Text(
                                text = "₹$safeToday",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
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
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions",
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 24.dp)
                    )
                }
            } else {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        cornerRadius = 20.dp
                    ) {
                        Column {
                            transactions.forEachIndexed { index, tx ->
                                TransactionRow(tx)
                                if (index < transactions.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 72.dp),
                                        color = Color(0x1AFFFFFF),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(140.dp)) // Padding for floating nav
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

    val avatarGradient = if (isIncome) {
        Brush.linearGradient(listOf(Color(0xFF32D74B), Color(0xFF0F5A1F)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF053E7A)))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Future detail view */ }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Placeholder with Gradient
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (tx.merchant ?: "U").take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.merchant ?: "Unknown",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${tx.purpose ?: tx.categoryName} • $dateStr",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
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
