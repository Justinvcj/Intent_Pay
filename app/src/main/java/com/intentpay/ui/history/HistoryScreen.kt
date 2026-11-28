package com.intentpay.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intentpay.ui.components.GroupedCard
import com.intentpay.ui.components.ListDivider
import com.intentpay.ui.components.ScreenTitle
import com.intentpay.ui.dashboard.TransactionRow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle("History", modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp))

        if (groupedTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "No payments yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Payments you add or Intent Pay detects will be listed here by month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                groupedTransactions.forEach { (month, transactions) ->
                    stickyHeader {
                        // Opaque so rows never show through while the header sticks.
                        Text(
                            text = month,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    item {
                        GroupedCard {
                            transactions.forEachIndexed { index, tx ->
                                TransactionRow(tx)
                                if (index < transactions.size - 1) {
                                    ListDivider(startInset = 68.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
