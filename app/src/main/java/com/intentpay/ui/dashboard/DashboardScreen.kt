package com.intentpay.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.data.local.TransactionEntity
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
    val dailyLimit = budget?.dailyLimit ?: 500.0
    val monthlyBudget = budget?.monthlyBudget ?: 10000.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IntentPay", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onNavigateToSettings) {
                        Text("Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToManualPayment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("PAY", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Budget Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BudgetCard(
                    title = "Today",
                    spent = safeToday,
                    limit = dailyLimit,
                    modifier = Modifier.weight(1f)
                )
                BudgetCard(
                    title = "This Month",
                    spent = safeMonth,
                    limit = monthlyBudget,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(transactions) { tx ->
                    TransactionItem(tx)
                }
            }
        }
    }
}

@Composable
fun BudgetCard(title: String, spent: Double, limit: Double, modifier: Modifier = Modifier) {
    val isExceeded = spent > limit
    val color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("₹$spent", style = MaterialTheme.typography.headlineMedium, color = color)
            Text("/ ₹$limit", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f))
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(tx.dateMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = tx.merchant ?: "Unknown Merchant", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = tx.purpose ?: tx.categoryName, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = dateStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f))
            }
            Text(
                text = "-₹${tx.amount}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp
            )
        }
    }
}
