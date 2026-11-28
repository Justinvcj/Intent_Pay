package com.intentpay.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.intentpay.data.local.TransactionEntity
import com.intentpay.domain.models.Category
import com.intentpay.ui.components.DetectedDot
import com.intentpay.ui.components.GroupedCard
import com.intentpay.ui.components.ListDivider
import com.intentpay.ui.components.ScreenTitle
import com.intentpay.ui.components.formatInr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToManualPayment: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPendingReview: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val budget by viewModel.budgetDetails.collectAsState()
    val todaySpending by viewModel.todaySpending.collectAsState()
    val monthSpending by viewModel.monthSpending.collectAsState()
    val pendingCount by viewModel.uncategorizedCount.collectAsState()

    val safeToday = todaySpending ?: 0.0
    val safeMonth = monthSpending ?: 0.0
    val monthlyBudget = budget?.monthlyBudget ?: 10000.0
    val progress = if (monthlyBudget > 0) (safeMonth / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    val overBudget = safeMonth > monthlyBudget
    val monthTitle = SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The host (MainScreen) Scaffold already applies system-bar insets to this
        // destination; applying them again here would double the top/bottom padding.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToManualPayment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add payment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            item {
                ScreenTitle(monthTitle, modifier = Modifier.padding(bottom = 24.dp))
            }

            // The moment on this screen: one large figure, one thin progress line, nothing else competing.
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Spent this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatInr(safeMonth),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (overBudget) {
                            "${formatInr(safeMonth - monthlyBudget)} over ${formatInr(monthlyBudget)} budget"
                        } else {
                            "${formatInr(monthlyBudget - safeMonth)} left of ${formatInr(monthlyBudget)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (pendingCount > 0) {
                item {
                    GroupedCard(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToPendingReview() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Pending review",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    if (pendingCount == 1) "1 payment needs a category" else "$pendingCount payments need a category",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                GroupedCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatInr(safeToday),
                            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Recent payments",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "No payments yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add one with the plus button, or turn on detection in Settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    GroupedCard {
                        transactions.forEachIndexed { index, tx ->
                            TransactionRow(tx)
                            if (index < transactions.size - 1) {
                                // Inset past the 40dp avatar: 16 gutter + 40 + 12 gap.
                                ListDivider(startInset = 68.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Stored category names are enum constants (FOOD); show the display name (Food). */
private fun categoryLabel(name: String): String =
    runCatching { Category.valueOf(name).displayName }
        .getOrElse { name.lowercase().replaceFirstChar { it.uppercase() } }

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val dateStr = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(tx.dateMillis))
    val colors = MaterialTheme.colorScheme
    val isIncome = tx.isIncome
    val amountColor = if (isIncome) colors.primary else colors.onSurface
    val sign = if (isIncome) "+" else "−"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InitialAvatar(initial = (tx.merchant ?: "U").take(1).uppercase())

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tx.merchant ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (tx.autoDetected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    DetectedDot()
                }
            }
            Text(
                text = "${tx.purpose ?: categoryLabel(tx.categoryName)} · $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$sign${formatInr(tx.amount)}",
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            color = amountColor
        )
    }
}

/** Flat initial avatar: a neutral surface step, no color. */
@Composable
private fun InitialAvatar(initial: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
