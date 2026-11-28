package com.intentpay.ui.pending

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intentpay.data.local.TransactionEntity
import com.intentpay.domain.models.Category
import com.intentpay.ui.components.GroupedCard
import com.intentpay.ui.components.formatInr

/** Raw source codes are internal; show how the payment was caught in plain words. */
private fun sourceLabel(source: String): String = when (source) {
    "SMS" -> "Detected from SMS"
    "UPI_INTERCEPT" -> "Caught when you paid"
    "MANUAL" -> "Added by hand"
    else -> "Detected from $source" // notification listener stores the app name, e.g. GPay
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingReviewScreen(
    viewModel: PendingReviewViewModel,
    onBack: () -> Unit
) {
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pending review", style = MaterialTheme.typography.titleMedium) },
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
        if (pendingTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Nothing to review",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Payments that need a category will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingTransactions, key = { it.id }) { tx ->
                    PendingTransactionCard(
                        transaction = tx,
                        onCategorySelected = { category -> viewModel.categorize(tx, category) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingTransactionCard(
    transaction: TransactionEntity,
    onCategorySelected: (Category) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val sign = if (transaction.isIncome) "+" else "−"

    GroupedCard(modifier = Modifier.animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant ?: "Unknown merchant",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                    Text(
                        text = sourceLabel(transaction.source),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Text(
                    text = "$sign${formatInr(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    color = if (transaction.isIncome) colors.primary else colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Category",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Category.values().filter { it != Category.UNCATEGORIZED }) { category ->
                    AssistChip(
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayName, style = MaterialTheme.typography.bodyMedium) },
                        shape = RoundedCornerShape(12.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = colors.onSurface,
                            containerColor = colors.surfaceVariant
                        ),
                        border = null
                    )
                }
            }
        }
    }
}
